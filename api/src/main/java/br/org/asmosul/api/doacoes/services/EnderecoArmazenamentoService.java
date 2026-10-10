package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.EnderecoArmazenamentoDTO;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.specifications.EnderecoArmazenamentoSpecification;
import br.org.asmosul.api.pessoas.models.Uf;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnderecoArmazenamentoService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nome", "cidade", "bairro", "dataInativo");

    private final EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;

    public EnderecoArmazenamentoService(EnderecoArmazenamentoRepository enderecoArmazenamentoRepository) {
        this.enderecoArmazenamentoRepository = enderecoArmazenamentoRepository;
    }

    @Transactional
    public EnderecoArmazenamentoDTO.Detalhe cadastrar(EnderecoArmazenamentoDTO.Requisicao requisicao) {
        if (enderecoArmazenamentoRepository.existsByNomeIgnoreCase(requisicao.nome())) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe um endereço de armazenamento cadastrado com este nome.");
        }

        EnderecoArmazenamento endereco = requisicao.paraEntidade();
        EnderecoArmazenamento enderecoSalvo = enderecoArmazenamentoRepository.save(endereco);

        return EnderecoArmazenamentoDTO.Detalhe.deEntidade(enderecoSalvo);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<EnderecoArmazenamentoDTO.Resumo> listar(
            Pageable paginacao,
            boolean incluirInativos,
            String nome,
            String cep,
            Uf uf,
            String cidade,
            String bairro,
            String logradouro) {

        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Specification<EnderecoArmazenamento> spec =
                EnderecoArmazenamentoSpecification.comFiltro(
                        nome, cep, uf, cidade, bairro, logradouro, incluirInativos);

        Page<EnderecoArmazenamento> pagina = enderecoArmazenamentoRepository.findAll(spec, paginacaoSanitizada);

        Page<EnderecoArmazenamentoDTO.Resumo> paginaDtos = pagina.map(EnderecoArmazenamentoDTO.Resumo::deEntidade);

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<EnderecoArmazenamentoDTO.Resumo> listarTodas(boolean incluirInativos) {
        List<EnderecoArmazenamento> enderecos =
                incluirInativos
                        ? enderecoArmazenamentoRepository.findAll()
                        : enderecoArmazenamentoRepository.findAllByDataInativoIsNull();

        return enderecos.stream().map(EnderecoArmazenamentoDTO.Resumo::deEntidade).toList();
    }

    @Transactional(readOnly = true)
    public List<EnderecoArmazenamentoDTO.Resumo> listarTodas() {
        return listarTodas(false);
    }

    @Transactional(readOnly = true)
    public EnderecoArmazenamentoDTO.Detalhe buscarPorId(Long id) {
        EnderecoArmazenamento endereco =
                enderecoArmazenamentoRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Endereço de armazenamento ativo não encontrado com o ID informado: "
                                                        + id));

        return EnderecoArmazenamentoDTO.Detalhe.deEntidade(endereco);
    }

    @Transactional
    public EnderecoArmazenamentoDTO.Detalhe atualizar(Long id, EnderecoArmazenamentoDTO.Atualizacao requisicao) {
        EnderecoArmazenamento endereco =
                enderecoArmazenamentoRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Endereço de armazenamento ativo não encontrado com o ID informado: "
                                                        + id));

        if (enderecoArmazenamentoRepository.existsByNomeIgnoreCaseAndIdNot(requisicao.nome(), id)) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe um endereço de armazenamento cadastrado com este nome.");
        }

        endereco.setNome(requisicao.nome());
        endereco.setCep(requisicao.cep());
        endereco.setUf(requisicao.uf());
        endereco.setCidade(requisicao.cidade());
        endereco.setBairro(requisicao.bairro());
        endereco.setLogradouro(requisicao.logradouro());
        endereco.setNumero(requisicao.numero());
        endereco.setComplemento(requisicao.complemento());
        endereco.setInformacoesAdicionais(requisicao.informacoesAdicionais());

        return EnderecoArmazenamentoDTO.Detalhe.deEntidade(endereco);
    }

    @Transactional
    public void desativar(Long id) {
        EnderecoArmazenamento endereco =
                enderecoArmazenamentoRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Endereço de armazenamento ativo não encontrado com o ID informado: "
                                                        + id));

        validarItensVinculados(id);

        endereco.desativar();
    }

    @Transactional
    public void reativar(Long id) {
        EnderecoArmazenamento endereco =
                enderecoArmazenamentoRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Endereço de armazenamento não encontrado com o ID informado: "
                                                        + id));

        endereco.reativar();
    }

    protected void validarItensVinculados(Long id) {
        if (enderecoArmazenamentoRepository.possuiItensVinculados(id)) {
            throw ValidationException.of(
                    "endereco",
                    "Não é possível desativar um endereço que possui itens armazenados vinculados (RN012)");
        }
    }
}
