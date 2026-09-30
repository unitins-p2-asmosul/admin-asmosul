package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.EnderecoDoacaoDTO;
import br.org.asmosul.api.doacoes.models.EnderecoDoacao;
import br.org.asmosul.api.doacoes.repositories.EnderecoDoacaoRepository;
import br.org.asmosul.api.doacoes.repositories.EnderecoDoacaoSpecification;
import br.org.asmosul.api.pessoas.models.Uf;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnderecoDoacaoService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nome", "cidade", "bairro", "dataInativo");

    private final EnderecoDoacaoRepository enderecoDoacaoRepository;

    public EnderecoDoacaoService(EnderecoDoacaoRepository enderecoDoacaoRepository) {
        this.enderecoDoacaoRepository = enderecoDoacaoRepository;
    }

    @Transactional
    public EnderecoDoacaoDTO.Detalhe cadastrar(EnderecoDoacaoDTO.Requisicao requisicao) {
        if (enderecoDoacaoRepository.existsByNomeIgnoreCase(requisicao.nome())) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe um endereço de armazenamento cadastrado com este nome.");
        }

        EnderecoDoacao endereco = requisicao.paraEntidade();
        EnderecoDoacao enderecoSalvo = enderecoDoacaoRepository.save(endereco);

        return EnderecoDoacaoDTO.Detalhe.deEntidade(enderecoSalvo);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<EnderecoDoacaoDTO.Resumo> listar(
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

        Specification<EnderecoDoacao> spec =
                EnderecoDoacaoSpecification.comFiltro(
                        nome, cep, uf, cidade, bairro, logradouro, incluirInativos);

        Page<EnderecoDoacao> pagina = enderecoDoacaoRepository.findAll(spec, paginacaoSanitizada);

        Page<EnderecoDoacaoDTO.Resumo> paginaDtos = pagina.map(EnderecoDoacaoDTO.Resumo::deEntidade);

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<EnderecoDoacaoDTO.Resumo> listarTodas(boolean incluirInativos) {
        List<EnderecoDoacao> enderecos =
                incluirInativos
                        ? enderecoDoacaoRepository.findAll()
                        : enderecoDoacaoRepository.findAllByDataInativoIsNull();

        return enderecos.stream().map(EnderecoDoacaoDTO.Resumo::deEntidade).toList();
    }

    @Transactional(readOnly = true)
    public List<EnderecoDoacaoDTO.Resumo> listarTodas() {
        return listarTodas(false);
    }

    @Transactional(readOnly = true)
    public EnderecoDoacaoDTO.Detalhe buscarPorId(Long id) {
        EnderecoDoacao endereco =
                enderecoDoacaoRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Endereço de armazenamento ativo não encontrado com o ID informado: "
                                                        + id));

        return EnderecoDoacaoDTO.Detalhe.deEntidade(endereco);
    }

    @Transactional
    public EnderecoDoacaoDTO.Detalhe atualizar(Long id, EnderecoDoacaoDTO.Atualizacao requisicao) {
        EnderecoDoacao endereco =
                enderecoDoacaoRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Endereço de armazenamento ativo não encontrado com o ID informado: "
                                                        + id));

        if (enderecoDoacaoRepository.existsByNomeIgnoreCaseAndIdNot(requisicao.nome(), id)) {
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

        return EnderecoDoacaoDTO.Detalhe.deEntidade(endereco);
    }

    @Transactional
    public void desativar(Long id) {
        EnderecoDoacao endereco =
                enderecoDoacaoRepository
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
        EnderecoDoacao endereco =
                enderecoDoacaoRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Endereço de armazenamento não encontrado com o ID informado: "
                                                        + id));

        endereco.reativar();
    }

    protected void validarItensVinculados(Long id) {
        if (enderecoDoacaoRepository.possuiItensVinculados(id)) {
            throw ValidationException.of(
                    "endereco",
                    "Não é possível desativar um endereço que possui itens armazenados vinculados (RN012)");
        }
    }
}
