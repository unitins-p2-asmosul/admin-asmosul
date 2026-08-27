package br.org.asmosul.api.pessoas.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.pessoas.dtos.PessoaDTO;
import br.org.asmosul.api.pessoas.models.*;
import br.org.asmosul.api.pessoas.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final ComorbidadeRepository comorbidadeRepository;
    private final CategoriaRepository categoriaRepository;

    public PessoaService(
        PessoaRepository pessoaRepository,
        ComorbidadeRepository comorbidadeRepository,
        CategoriaRepository categoriaRepository
    ) {
        this.pessoaRepository = pessoaRepository;
        this.comorbidadeRepository = comorbidadeRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public PessoaDTO.Detalhe cadastrar(PessoaDTO.Requisicao requisicao) {
        if (pessoaRepository.existsByCpf(requisicao.cpf())) {
            throw ValidationException.of("cpf", "Já existe uma pessoa cadastrada com este CPF");
        }

        Pessoa pessoa = requisicao.paraEntidade();
        vincularRelacionamentos(pessoa, requisicao.comorbidades(), requisicao.categorias());

        Pessoa salva = pessoaRepository.save(pessoa);
        return PessoaDTO.Detalhe.deEntidade(salva);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<PessoaDTO.Resumo> listar(Pageable paginacao, boolean incluirInativos) {
        Page<Pessoa> pagina = incluirInativos 
            ? pessoaRepository.findAll(paginacao)
            : pessoaRepository.findAllByDataInativoIsNull(paginacao);

        Page<PessoaDTO.Resumo> paginaDto = pagina.map(PessoaDTO.Resumo::deEntidade);
        return RespostaPaginada.dePage(paginaDto);
    }

    @Transactional(readOnly = true)
    public PessoaDTO.Detalhe buscarPorId(Long id) {
        Pessoa pessoa = pessoaRepository.findByIdAndDataInativoIsNull(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Pessoa ativa não encontrada com o ID informado: " + id));
        return PessoaDTO.Detalhe.deEntidade(pessoa);
    }

    @Transactional(readOnly = true)
    public PessoaDTO.Detalhe buscarPorCpf(String cpf) {
        Pessoa pessoa = pessoaRepository.findByCpfAndDataInativoIsNull(cpf)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Pessoa ativa não encontrada com o CPF informado: " + cpf));
        return PessoaDTO.Detalhe.deEntidade(pessoa);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<PessoaDTO.Resumo> buscarPorNome(String nome, Pageable paginacao) {
        Page<Pessoa> pagina = pessoaRepository.findByNomeContainingIgnoreCaseAndDataInativoIsNull(nome, paginacao);
        Page<PessoaDTO.Resumo> paginaDto = pagina.map(PessoaDTO.Resumo::deEntidade);
        return RespostaPaginada.dePage(paginaDto);
    }

    @Transactional
    public PessoaDTO.Detalhe atualizar(Long id, PessoaDTO.Atualizacao requisicao) {
        Pessoa pessoa = pessoaRepository.findByIdAndDataInativoIsNull(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Pessoa ativa não encontrada para atualização."));

        if (pessoaRepository.existsByCpfAndIdNot(requisicao.cpf(), id)) {
            throw ValidationException.of("cpf", "Já existe outra pessoa cadastrada com este CPF");
        }

        pessoa.atualizarDados(
            requisicao.nome(),
            requisicao.cpf(),
            requisicao.dataNascimento(),
            requisicao.sexo(),
            requisicao.telefone(),
            requisicao.email(),
            requisicao.escolaridade(),
            requisicao.profissao(),
            requisicao.rendaFamiliar(),
            requisicao.descricao()
        );

        vincularRelacionamentos(pessoa, requisicao.comorbidades(), requisicao.categorias());

        return PessoaDTO.Detalhe.deEntidade(pessoa);
    }

    @Transactional
    public void desativar(Long id) {
        Pessoa pessoa = pessoaRepository.findByIdAndDataInativoIsNull(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Pessoa ativa não encontrada com o ID informado: " + id));
        pessoa.desativar();
    }

    @Transactional
    public void reativar(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Pessoa não encontrada com o ID informado: " + id));
        
        if (pessoa.isAtivo()) {
            throw ValidationException.of("id", "A pessoa informada já está ativa.");
        }
        
        pessoa.reativar();
    }

    private void vincularRelacionamentos(Pessoa pessoa, List<Long> idsComorbidades, List<Long> idsCategorias) {
        if (idsComorbidades != null) {
            List<Comorbidade> comorbidades = idsComorbidades.stream()
                .map(id -> comorbidadeRepository.findByIdAndDataInativoIsNull(id)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Comorbidade não encontrada.")))
                .toList();
            pessoa.setComorbidades(comorbidades);
        }

        if (idsCategorias != null) {
            List<Categoria> categorias = idsCategorias.stream()
                .map(id -> categoriaRepository.findByIdAndDataInativoIsNull(id)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada.")))
                .toList();
            pessoa.setCategorias(categorias);
        }
    }
}