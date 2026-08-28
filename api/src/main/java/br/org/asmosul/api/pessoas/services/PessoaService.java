package br.org.asmosul.api.pessoas.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.pessoas.dtos.PessoaDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.models.Comorbidade;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import br.org.asmosul.api.pessoas.repositories.ComorbidadeRepository;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessoaService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nome", "cpf", "email", "telefone", "dataNascimento", "dataInativo");

    private final PessoaRepository pessoaRepository;
    private final ComorbidadeRepository comorbidadeRepository;
    private final CategoriaRepository categoriaRepository;

    public PessoaService(
            PessoaRepository pessoaRepository,
            ComorbidadeRepository comorbidadeRepository,
            CategoriaRepository categoriaRepository) {
        this.pessoaRepository = pessoaRepository;
        this.comorbidadeRepository = comorbidadeRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public PessoaDTO.Detalhe cadastrar(PessoaDTO.Requisicao requisicao) {
        validarUnicidade(requisicao.cpf(), requisicao.email(), null);

        Pessoa pessoa = requisicao.paraEntidade();

        atribuirComorbidadesECategorias(
                pessoa, requisicao.comorbidades(), requisicao.categorias());

        Pessoa pessoaSalva = pessoaRepository.save(pessoa);

        return PessoaDTO.Detalhe.deEntidade(pessoaSalva);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<PessoaDTO.Resumo> listar(
            Pageable paginacao, boolean incluirInativos) {
        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(
                        paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Page<Pessoa> pagina =
                incluirInativos
                        ? pessoaRepository.findAll(paginacaoSanitizada)
                        : pessoaRepository.findAllByDataInativoIsNull(paginacaoSanitizada);

        Page<PessoaDTO.Resumo> paginaDtos = pagina.map(PessoaDTO.Resumo::deEntidade);
        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<PessoaDTO.Resumo> listarTodas(boolean incluirInativos) {
        List<Pessoa> pessoas =
                incluirInativos
                        ? pessoaRepository.findAll()
                        : pessoaRepository.findAllByDataInativoIsNull();

        return pessoas.stream()
                .map(PessoaDTO.Resumo::deEntidade)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PessoaDTO.Resumo> listarTodas() {
        return listarTodas(false);
    }

    @Transactional(readOnly = true)
    public PessoaDTO.Detalhe buscarPorId(Long id) {
        Pessoa pessoa =
                pessoaRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Pessoa ativa não encontrada com o ID informado: "
                                                        + id));

        return PessoaDTO.Detalhe.deEntidade(pessoa);
    }

    @Transactional
    public PessoaDTO.Detalhe atualizar(Long id, PessoaDTO.Atualizacao requisicao) {
        Pessoa pessoa =
                pessoaRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Pessoa ativa não encontrada com o ID informado: "
                                                        + id));

        validarUnicidade(requisicao.cpf(), requisicao.email(), id);

        pessoa.setNome(requisicao.nome());
        pessoa.setCpf(requisicao.cpf());
        pessoa.setDataNascimento(requisicao.dataNascimento());
        pessoa.setSexo(requisicao.sexo());
        pessoa.setTelefone(requisicao.telefone());
        pessoa.setEmail(requisicao.email());
        pessoa.setEscolaridade(requisicao.escolaridade());
        pessoa.setProfissao(requisicao.profissao());
        pessoa.setRendaFamiliar(requisicao.rendaFamiliar());
        pessoa.setDescricao(requisicao.descricao());

        atribuirComorbidadesECategorias(
                pessoa, requisicao.comorbidades(), requisicao.categorias());

        return PessoaDTO.Detalhe.deEntidade(pessoa);
    }

    @Transactional
    public void desativar(Long id) {
        Pessoa pessoa =
                pessoaRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Pessoa ativa não encontrada com o ID informado: "
                                                        + id));

        pessoa.setDataInativo(LocalDateTime.now());
    }

    @Transactional
    public void reativar(Long id) {
        Pessoa pessoa =
                pessoaRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Pessoa não encontrada com o ID informado: "
                                                        + id));

        pessoa.setDataInativo(null);
    }

    @Transactional
    public void excluir(Long id) {
        Pessoa pessoa =
                pessoaRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Pessoa não encontrada com o ID informado: "
                                                        + id));

        pessoaRepository.delete(pessoa);
    }

    private void validarUnicidade(String cpf, String email, Long idAtual) {
        if (idAtual == null) {
            if (pessoaRepository.existsByCpf(cpf)) {
                throw new ConflitoDadosException("Já existe uma pessoa cadastrada com este CPF.");
            }
            if (email != null && !email.isBlank() && pessoaRepository.existsByEmail(email)) {
                throw new ConflitoDadosException("Já existe uma pessoa cadastrada com este e-mail.");
            }
        } else {
            if (pessoaRepository.existsByCpfAndIdNot(cpf, idAtual)) {
                throw new ConflitoDadosException("Já existe uma pessoa cadastrada com este CPF.");
            }
            if (email != null
                    && !email.isBlank()
                    && pessoaRepository.existsByEmailAndIdNot(email, idAtual)) {
                throw new ConflitoDadosException("Já existe uma pessoa cadastrada com este e-mail.");
            }
        }
    }

    private void atribuirComorbidadesECategorias(
            Pessoa pessoa, List<Long> idsComorbidades, List<Long> idsCategorias) {
            
        // Tratamento para Comorbidades
        if (idsComorbidades != null && !idsComorbidades.isEmpty()) {
            List<Comorbidade> comorbidades = comorbidadeRepository.findAllById(idsComorbidades);
            
            if (comorbidades.size() != idsComorbidades.size()) {
                throw new EntidadeNaoEncontradaException(
                        "Uma ou mais comorbidades informadas não foram encontradas.");
            }
            
            pessoa.setComorbidades(new HashSet<>(comorbidades));
        } else {
            pessoa.setComorbidades(new HashSet<>()); // Inicializa com Set vazio em vez de repassar null
        }

        // Tratamento para Categorias
        if (idsCategorias != null && !idsCategorias.isEmpty()) {
            List<Categoria> categorias = categoriaRepository.findAllById(idsCategorias);
        
            if (categorias.size() != idsCategorias.size()) {
                throw new EntidadeNaoEncontradaException(
                        "Uma ou mais categorias informadas não foram encontradas.");
            }
        
            pessoa.setCategorias(new HashSet<>(categorias));
        } else {
            pessoa.setCategorias(new HashSet<>()); // Inicializa com Set vazio em vez de repassar null
        }
    }
}