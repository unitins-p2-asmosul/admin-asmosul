package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.SaidaDTO;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.Saida;
import br.org.asmosul.api.doacoes.models.SaidaEstoque;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaRepository;
import br.org.asmosul.api.doacoes.repositories.specifications.SaidaSpecification;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaidaService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS = Set.of("id", "dataHora");

    private final SaidaRepository saidaRepository;
    private final SaidaEstoqueRepository saidaEstoqueRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemRepository itemRepository;
    private final EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;
    private final PessoaRepository pessoaRepository;

    public SaidaService(
            SaidaRepository saidaRepository,
            SaidaEstoqueRepository saidaEstoqueRepository,
            EstoqueRepository estoqueRepository,
            ItemRepository itemRepository,
            EnderecoArmazenamentoRepository enderecoArmazenamentoRepository,
            PessoaRepository pessoaRepository) {
        this.saidaRepository = saidaRepository;
        this.saidaEstoqueRepository = saidaEstoqueRepository;
        this.estoqueRepository = estoqueRepository;
        this.itemRepository = itemRepository;
        this.enderecoArmazenamentoRepository = enderecoArmazenamentoRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public SaidaDTO.Detalhe cadastrar(SaidaDTO.Requisicao requisicao) {
        Pessoa recebedor = buscarRecebedorAtivo(requisicao.idRecebedor());

        Saida saida = saidaRepository.save(requisicao.paraEntidade(recebedor));
        List<SaidaEstoque> itensSalvos = new ArrayList<>();

        for (int i = 0; i < requisicao.itens().size(); i++) {
            SaidaDTO.ItemSaidaRequisicao itemRequisicao = requisicao.itens().get(i);
            Item item = buscarItemAtivo(itemRequisicao.idItem());
            EnderecoArmazenamento armazenamento =
                    buscarEnderecoAtivo(itemRequisicao.idArmazenamento());

            baixarEstoque(item, armazenamento, itemRequisicao.quantidade(), i);

            SaidaEstoque saidaEstoque =
                    saidaEstoqueRepository.save(
                            new SaidaEstoque(
                                    item, saida, armazenamento, itemRequisicao.quantidade()));
            itensSalvos.add(saidaEstoque);
        }

        marcarComoBeneficiario(recebedor);

        return montarDetalhe(saida, itensSalvos);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<SaidaDTO.Resumo> listar(
            Pageable paginacao, SaidaDTO.SaidaFiltroDTO filtro) {
        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "dataHora");

        Specification<Saida> spec = SaidaSpecification.comFiltro(filtro);
        Page<Saida> pagina = saidaRepository.findAll(spec, paginacaoSanitizada);

        List<Long> saidaIds = pagina.getContent().stream().map(Saida::getId).toList();
        Map<Long, List<SaidaEstoque>> itensPorSaida = buscarItensAgrupados(saidaIds);

        Page<SaidaDTO.Resumo> paginaDtos =
                pagina.map(
                        saida -> {
                            List<SaidaEstoque> itens =
                                    itensPorSaida.getOrDefault(saida.getId(), List.of());
                            List<String> descricoes =
                                    itens.stream().map(item -> item.getItem().getNome()).toList();
                            int quantidadeTotal =
                                    itens.stream().mapToInt(SaidaEstoque::getQuantidade).sum();
                            return SaidaDTO.Resumo.deEntidade(saida, descricoes, quantidadeTotal);
                        });

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public SaidaDTO.Detalhe buscarPorId(Long id) {
        Saida saida = buscarSaida(id);
        List<SaidaEstoque> itens = saidaEstoqueRepository.findAllBySaidaIdComRelacionamentos(id);
        return montarDetalhe(saida, itens);
    }

    @Transactional
    public SaidaDTO.Detalhe atualizar(Long id, SaidaDTO.Atualizacao atualizacao) {
        Saida saida = buscarSaida(id);

        if (atualizacao.idRecebedor() != null) {
            Pessoa novoRecebedor = buscarRecebedorAtivo(atualizacao.idRecebedor());
            saida.setRecebedor(novoRecebedor);
            marcarComoBeneficiario(novoRecebedor);
        }

        if (atualizacao.descricao() != null) {
            saida.setDescricao(atualizacao.descricao());
        }

        List<SaidaEstoque> itens = saidaEstoqueRepository.findAllBySaidaIdComRelacionamentos(id);
        return montarDetalhe(saida, itens);
    }

    @Transactional
    public void deletar(Long id) {
        Saida saida = buscarSaida(id);
        List<SaidaEstoque> itens = saidaEstoqueRepository.findAllBySaidaIdComRelacionamentos(id);

        for (SaidaEstoque itemSaida : itens) {
            estornarEstoque(itemSaida);
        }

        saidaEstoqueRepository.deleteAll(itens);
        saidaRepository.delete(saida);
    }

    @Transactional
    public void deletarItemSaida(Long idSaida, Long idSaidaEstoque) {
        buscarSaida(idSaida);

        SaidaEstoque saidaEstoque =
                saidaEstoqueRepository
                        .findByIdAndSaidaIdComRelacionamentos(idSaidaEstoque, idSaida)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Registro de item da saída não encontrado com o ID informado: "
                                                        + idSaidaEstoque));

        if (saidaEstoqueRepository.countBySaidaId(idSaida) <= 1) {
            throw ValidationException.of(
                    "idSaidaEstoque",
                    "Não é possível remover o último item da saída. Exclua a saída completa via DELETE /doacoes/saidas/"
                            + idSaida);
        }

        estornarEstoque(saidaEstoque);
        saidaEstoqueRepository.delete(saidaEstoque);
    }

    private Pessoa buscarRecebedorAtivo(Long idRecebedor) {
        return pessoaRepository
                .findByIdAndDataInativoIsNull(idRecebedor)
                .orElseThrow(
                        () ->
                                new EntidadeNaoEncontradaException(
                                        "Recebedor ativo não encontrado com o ID informado: "
                                                + idRecebedor));
    }

    private Item buscarItemAtivo(Long idItem) {
        return itemRepository
                .findByIdAndDataInativoIsNull(idItem)
                .orElseThrow(
                        () ->
                                new EntidadeNaoEncontradaException(
                                        "Item ativo não encontrado com o ID informado: " + idItem));
    }

    private EnderecoArmazenamento buscarEnderecoAtivo(Long idEndereco) {
        return enderecoArmazenamentoRepository
                .findByIdAndDataInativoIsNull(idEndereco)
                .orElseThrow(
                        () ->
                                new EntidadeNaoEncontradaException(
                                        "Endereço de armazenamento ativo não encontrado com o ID informado: "
                                                + idEndereco));
    }

    private Saida buscarSaida(Long id) {
        return saidaRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new EntidadeNaoEncontradaException(
                                        "Saída não encontrada com o ID informado: " + id));
    }

    private void marcarComoBeneficiario(Pessoa pessoa) {
        if (!pessoa.isEhBeneficiario()) {
            pessoa.setEhBeneficiario(true);
            pessoaRepository.save(pessoa);
        }
    }

    /**
     * Abate a quantidade do saldo do item no endereço, impedindo saldo negativo (RN11). Itens
     * repetidos na mesma requisição consomem o mesmo saldo, pois a entidade de estoque já
     * gerenciada é reaproveitada dentro da transação.
     */
    private void baixarEstoque(
            Item item, EnderecoArmazenamento armazenamento, Integer quantidadeSaida, int indice) {
        Estoque estoque =
                estoqueRepository
                        .findByItemIdAndArmazenamentoId(item.getId(), armazenamento.getId())
                        .orElse(null);

        int saldo =
                estoque != null && estoque.getQuantidade() != null ? estoque.getQuantidade() : 0;

        if (estoque == null || saldo < quantidadeSaida) {
            throw ValidationException.of(
                    "itens[" + indice + "].quantidade",
                    "Saldo insuficiente do item '"
                            + item.getNome()
                            + "' no endereço '"
                            + armazenamento.getNome()
                            + "'. Disponível: "
                            + saldo
                            + ", solicitado: "
                            + quantidadeSaida);
        }

        estoque.setQuantidade(saldo - quantidadeSaida);
        estoqueRepository.save(estoque);
    }

    private void estornarEstoque(SaidaEstoque saidaEstoque) {
        Estoque estoque =
                estoqueRepository
                        .findByItemIdAndArmazenamentoId(
                                saidaEstoque.getItem().getId(),
                                saidaEstoque.getArmazenamento().getId())
                        .orElseGet(
                                () ->
                                        new Estoque(
                                                saidaEstoque.getItem(),
                                                saidaEstoque.getArmazenamento(),
                                                0));

        int quantidadeAtual = estoque.getQuantidade() != null ? estoque.getQuantidade() : 0;
        estoque.setQuantidade(quantidadeAtual + saidaEstoque.getQuantidade());
        estoqueRepository.save(estoque);
    }

    private SaidaDTO.Detalhe montarDetalhe(Saida saida, List<SaidaEstoque> itensSaida) {
        List<SaidaDTO.ItemSaidaDetalhe> itens =
                itensSaida.stream().map(SaidaDTO.ItemSaidaDetalhe::deEntidade).toList();
        return SaidaDTO.Detalhe.deEntidade(saida, itens);
    }

    private Map<Long, List<SaidaEstoque>> buscarItensAgrupados(Collection<Long> saidaIds) {
        if (saidaIds == null || saidaIds.isEmpty()) {
            return Map.of();
        }

        List<SaidaEstoque> itens =
                saidaEstoqueRepository.findAllBySaidaIdInComRelacionamentos(saidaIds);
        Map<Long, List<SaidaEstoque>> agrupados = new HashMap<>();

        for (SaidaEstoque item : itens) {
            agrupados
                    .computeIfAbsent(item.getSaida().getId(), ignored -> new ArrayList<>())
                    .add(item);
        }

        return agrupados;
    }
}
