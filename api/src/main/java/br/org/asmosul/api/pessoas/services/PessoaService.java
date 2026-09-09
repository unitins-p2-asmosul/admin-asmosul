package br.org.asmosul.api.pessoas.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.pessoas.dtos.PessoaDTO;
import br.org.asmosul.api.pessoas.dtos.PessoaFiltroDTO;
import br.org.asmosul.api.pessoas.models.*;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import br.org.asmosul.api.pessoas.repositories.ComorbidadeRepository;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import br.org.asmosul.api.pessoas.repositories.PessoaSpecification;
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
            Set.of(
                    "id",
                    "nome",
                    "cpfCnpj",
                    "telefone",
                    "email",
                    "dataNascimento",
                    "profissao",
                    "cidade",
                    "bairro",
                    "tipoPessoa");

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
        TipoPessoa tipo =
                requisicao.tipoPessoa() != null ? requisicao.tipoPessoa() : TipoPessoa.FISICA;
        validarRegrasNegocio(
                tipo,
                requisicao.cpfCnpj(),
                requisicao.email(),
                requisicao.dataNascimento(),
                requisicao.sexo(),
                requisicao.escolaridade(),
                requisicao.rendaFamiliar(),
                requisicao.comorbidades(),
                requisicao.categorias(),
                requisicao.cep(),
                requisicao.uf(),
                requisicao.cidade(),
                requisicao.bairro(),
                requisicao.logradouro(),
                requisicao.quantidadeCoabitantes(),
                requisicao.ehBeneficiario(),
                requisicao.ehDoador(),
                null);

        validarUnicidade(requisicao.cpfCnpj(), requisicao.email(), null);

        Pessoa pessoa = requisicao.paraEntidade();

        atribuirComorbidadesECategorias(pessoa, requisicao.comorbidades(), requisicao.categorias());

        Pessoa pessoaSalva = pessoaRepository.save(pessoa);

        return PessoaDTO.Detalhe.deEntidade(pessoaSalva);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<PessoaDTO.Resumo> listar(
            PessoaFiltroDTO filtro, Pageable paginacao, boolean incluirInativos) {
        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Page<Pessoa> pagina =
                pessoaRepository.findAll(
                        PessoaSpecification.comFiltro(filtro, incluirInativos),
                        paginacaoSanitizada);

        Page<PessoaDTO.Resumo> paginaDtos = pagina.map(PessoaDTO.Resumo::deEntidade);
        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<PessoaDTO.Resumo> listar(Pageable paginacao, boolean incluirInativos) {
        return listar(null, paginacao, incluirInativos);
    }

    @Transactional(readOnly = true)
    public List<PessoaDTO.Resumo> listarTodas(boolean incluirInativos) {
        List<Pessoa> pessoas =
                incluirInativos
                        ? pessoaRepository.findAll()
                        : pessoaRepository.findAllByDataInativoIsNull();

        return pessoas.stream().map(PessoaDTO.Resumo::deEntidade).toList();
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

        // RN08 - Alteração de tipo de pessoa é estritamente proibida
        if (requisicao.tipoPessoa() != null && requisicao.tipoPessoa() != pessoa.getTipoPessoa()) {
            throw ValidationException.of("tipoPessoa", "O tipo de pessoa não pode ser alterado");
        }

        TipoPessoa tipo = pessoa.getTipoPessoa();
        validarRegrasNegocio(
                tipo,
                requisicao.cpfCnpj(),
                requisicao.email(),
                requisicao.dataNascimento(),
                requisicao.sexo(),
                requisicao.escolaridade(),
                requisicao.rendaFamiliar(),
                requisicao.comorbidades(),
                requisicao.categorias(),
                requisicao.cep(),
                requisicao.uf(),
                requisicao.cidade(),
                requisicao.bairro(),
                requisicao.logradouro(),
                requisicao.quantidadeCoabitantes(),
                requisicao.ehBeneficiario(),
                requisicao.ehDoador(),
                id);

        validarUnicidade(requisicao.cpfCnpj(), requisicao.email(), id);

        pessoa.atualizarDados(
                requisicao.nome(),
                requisicao.cpfCnpj(),
                requisicao.dataNascimento(),
                requisicao.sexo(),
                requisicao.telefone(),
                requisicao.email(),
                requisicao.escolaridade(),
                requisicao.profissao(),
                requisicao.rendaFamiliar(),
                requisicao.descricao(),
                requisicao.cep(),
                requisicao.uf(),
                requisicao.cidade(),
                requisicao.bairro(),
                requisicao.logradouro(),
                requisicao.complementoEndereco(),
                requisicao.quantidadeCoabitantes(),
                Boolean.TRUE.equals(requisicao.ehBeneficiario()),
                Boolean.TRUE.equals(requisicao.ehDoador()));

        atribuirComorbidadesECategorias(pessoa, requisicao.comorbidades(), requisicao.categorias());

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

        pessoa.desativar();
    }

    @Transactional
    public void reativar(Long id) {
        Pessoa pessoa =
                pessoaRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Pessoa não encontrada com o ID informado: " + id));

        pessoa.reativar();
    }

    @Transactional
    public void excluir(Long id) {
        Pessoa pessoa =
                pessoaRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Pessoa não encontrada com o ID informado: " + id));

        pessoaRepository.delete(pessoa);
    }

    private void validarRegrasNegocio(
            TipoPessoa tipo,
            String cpfCnpj,
            String email,
            java.time.LocalDate dataNascimento,
            Sexo sexo,
            Escolaridade escolaridade,
            RendaFamiliar rendaFamiliar,
            List<Long> comorbidades,
            List<Long> categorias,
            String cep,
            Uf uf,
            String cidade,
            String bairro,
            String logradouro,
            Integer quantidadeCoabitantes,
            Boolean ehBeneficiario,
            Boolean ehDoador,
            Long idAtual) {

        if (tipo == TipoPessoa.JURIDICA) {
            // RN05 - Restrição de Papel para Pessoa Jurídica (proibido ser beneficiária)
            if (Boolean.TRUE.equals(ehBeneficiario)) {
                throw ValidationException.of(
                        "ehBeneficiario",
                        "Pessoa Jurídica não pode ser cadastrada como beneficiária");
            }

            // RN06 - Dados Obrigatórios de Pessoa Jurídica
            if (cpfCnpj == null || cpfCnpj.length() != 14) {
                throw ValidationException.of(
                        "cpfCnpj", "CNPJ deve conter exatamente 14 dígitos numéricos");
            }

            if (email == null || email.isBlank()) {
                throw ValidationException.of(
                        "email", "O e-mail é obrigatório para Pessoa Jurídica");
            }

            if (cep == null || cep.isBlank()) {
                throw ValidationException.of("cep", "O CEP é obrigatório para Pessoa Jurídica");
            }

            if (uf == null) {
                throw ValidationException.of(
                        "uf", "O estado (UF) é obrigatório para Pessoa Jurídica");
            }

            if (cidade == null || cidade.isBlank()) {
                throw ValidationException.of(
                        "cidade", "A cidade é obrigatória para Pessoa Jurídica");
            }

            if (bairro == null || bairro.isBlank()) {
                throw ValidationException.of(
                        "bairro", "O bairro é obrigatório para Pessoa Jurídica");
            }

            if (logradouro == null || logradouro.isBlank()) {
                throw ValidationException.of(
                        "logradouro", "O logradouro é obrigatório para Pessoa Jurídica");
            }

            if (categorias == null || categorias.isEmpty()) {
                throw ValidationException.of(
                        "categorias",
                        "Pessoa Jurídica deve possuir ao menos uma categoria vinculada");
            }

            // PJ não pode possuir atributos específicos de PF
            if (comorbidades != null && !comorbidades.isEmpty()) {
                throw ValidationException.of(
                        "comorbidades", "Pessoa Jurídica não pode possuir comorbidades");
            }

            if (dataNascimento != null) {
                throw ValidationException.of(
                        "dataNascimento", "Pessoa Jurídica não deve possuir data de nascimento");
            }

            if (sexo != null) {
                throw ValidationException.of("sexo", "Pessoa Jurídica não deve possuir sexo");
            }

            if (escolaridade != null) {
                throw ValidationException.of(
                        "escolaridade", "Pessoa Jurídica não deve possuir escolaridade");
            }

            if (rendaFamiliar != null) {
                throw ValidationException.of(
                        "rendaFamiliar", "Pessoa Jurídica não deve possuir renda familiar");
            }

            if (quantidadeCoabitantes != null && quantidadeCoabitantes > 0) {
                throw ValidationException.of(
                        "quantidadeCoabitantes",
                        "Pessoa Jurídica não deve possuir quantidade de coabitantes");
            }
        } else {
            // RN07 - Dados Obrigatórios de Pessoa Física
            if (cpfCnpj == null || cpfCnpj.length() != 11) {
                throw ValidationException.of(
                        "cpfCnpj", "CPF deve conter exatamente 11 dígitos numéricos");
            }

            if (dataNascimento == null) {
                throw ValidationException.of(
                        "dataNascimento", "A data de nascimento é obrigatória para Pessoa Física");
            }

            if (categorias == null || categorias.isEmpty()) {
                throw ValidationException.of(
                        "categorias",
                        "Pessoa Física deve possuir ao menos uma categoria vinculada");
            }
        }
    }

    private void validarUnicidade(String cpfCnpj, String email, Long idAtual) {
        if (idAtual == null) {
            if (pessoaRepository.existsByCpfCnpj(cpfCnpj)) {
                throw new ConflitoDadosException(
                        "Já existe uma pessoa cadastrada com este CPF/CNPJ.");
            }
            if (email != null && !email.isBlank() && pessoaRepository.existsByEmail(email)) {
                throw new ConflitoDadosException(
                        "Já existe uma pessoa cadastrada com este e-mail.");
            }
        } else {
            if (pessoaRepository.existsByCpfCnpjAndIdNot(cpfCnpj, idAtual)) {
                throw new ConflitoDadosException(
                        "Já existe uma pessoa cadastrada com este CPF/CNPJ.");
            }
            if (email != null
                    && !email.isBlank()
                    && pessoaRepository.existsByEmailAndIdNot(email, idAtual)) {
                throw new ConflitoDadosException(
                        "Já existe uma pessoa cadastrada com este e-mail.");
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
            pessoa.setComorbidades(new HashSet<>());
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
            pessoa.setCategorias(new HashSet<>());
        }
    }
}
