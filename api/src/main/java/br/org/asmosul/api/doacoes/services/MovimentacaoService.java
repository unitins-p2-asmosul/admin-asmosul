package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.MovimentacaoDTO;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.Movimentacao;
import br.org.asmosul.api.doacoes.models.MovimentacaoEstoque;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.MovimentacaoEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.MovimentacaoRepository;
import br.org.asmosul.api.doacoes.repositories.specifications.MovimentacaoSpecification;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MovimentacaoService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "dataHora", "descricao");

    private final MovimentacaoRepository movimentacaoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemRepository itemRepository;
    private final EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;

    public MovimentacaoService(
            MovimentacaoRepository movimentacaoRepository,
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            EstoqueRepository estoqueRepository,
            ItemRepository itemRepository,
            EnderecoArmazenamentoRepository enderecoArmazenamentoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.estoqueRepository = estoqueRepository;
        this.itemRepository = itemRepository;
        this.enderecoArmazenamentoRepository = enderecoArmazenamentoRepository;
    }

    @Transactional
    public MovimentacaoDTO.Detalhe cadastrar(MovimentacaoDTO.Requisicao requisicao) {
        if (requisicao.itens() == null || requisicao.itens().isEmpty()) {
            throw ValidationException.of("itens", "A movimentação deve possuir ao menos um item.");
        }

        EnderecoArmazenamento novoArmazenamento = enderecoArmazenamentoRepository
                .findById(requisicao.idNovoArmazenamento())
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Endereço de armazenamento de destino não encontrado com o ID informado: "
                                + requisicao.idNovoArmazenamento()));

        for (MovimentacaoDTO.ItemMovimentacaoRequisicao itemReq : requisicao.itens()) {
            if (itemReq.idArmazenamentoAntigo().equals(requisicao.idNovoArmazenamento())) {
                throw ValidationException.of(
                        "itens",
                        "O endereço de origem não pode ser idêntico ao endereço de destino no mesmo item.");
            }
        }

        Map<String, Estoque> estoqueCache = new HashMap<>();
        List<MovimentacaoEstoque> itensParaSalvar = new ArrayList<>();
        Movimentacao movimentacao = requisicao.paraEntidade(novoArmazenamento);
        movimentacao = movimentacaoRepository.save(movimentacao);

        for (MovimentacaoDTO.ItemMovimentacaoRequisicao itemReq : requisicao.itens()) {
            Item item = itemRepository
                    .findById(itemReq.idItem())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException(
                            "Item não encontrado com o ID informado: " + itemReq.idItem()));

            EnderecoArmazenamento armazenamentoAntigo = enderecoArmazenamentoRepository
                    .findById(itemReq.idArmazenamentoAntigo())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException(
                            "Endereço de armazenamento de origem não encontrado com o ID informado: "
                                    + itemReq.idArmazenamentoAntigo()));

            Estoque estoqueOrigem = getOrLoadEstoque(estoqueCache, item, armazenamentoAntigo);
            if (estoqueOrigem.getQuantidade() < itemReq.quantidade()) {
                throw ValidationException.of(
                        "quantidade",
                        "Saldo insuficiente no endereço de origem para o item: " + item.getNome());
            }

            estoqueOrigem.setQuantidade(estoqueOrigem.getQuantidade() - itemReq.quantidade());

            Estoque estoqueDestino = getOrLoadEstoque(estoqueCache, item, novoArmazenamento);
            estoqueDestino.setQuantidade(estoqueDestino.getQuantidade() + itemReq.quantidade());

            itensParaSalvar.add(new MovimentacaoEstoque(
                    item, armazenamentoAntigo, movimentacao, itemReq.quantidade()));
        }

        estoqueRepository.saveAll(estoqueCache.values());
        List<MovimentacaoEstoque> itensSalvos = movimentacaoEstoqueRepository.saveAll(itensParaSalvar);

        List<MovimentacaoDTO.ItemMovimentacaoDetalhe> itensDetalhe = itensSalvos.stream()
                .map(MovimentacaoDTO.ItemMovimentacaoDetalhe::deEntidade)
                .toList();

        return MovimentacaoDTO.Detalhe.deEntidade(movimentacao, itensDetalhe);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<MovimentacaoDTO.Resumo> listar(
            MovimentacaoDTO.MovimentacaoFiltroDTO filtro, Pageable paginacao) {
        if (filtro != null
                && filtro.dataHoraInicio() != null
                && filtro.dataHoraFim() != null
                && filtro.dataHoraInicio().isAfter(filtro.dataHoraFim())) {
            throw ValidationException.of(
                    "dataHoraInicio", "A data/hora inicial deve ser anterior à data/hora final.");
        }
        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "dataHora");

        Specification<Movimentacao> spec = MovimentacaoSpecification.comFiltro(filtro);
        Page<Movimentacao> pagina = movimentacaoRepository.findAll(spec, paginacaoSanitizada);

        List<Long> ids = pagina.getContent().stream().map(Movimentacao::getId).toList();
        Map<Long, List<MovimentacaoEstoque>> itensPorMovimentacao = ids.isEmpty()
                ? Map.of()
                : movimentacaoEstoqueRepository.findAllByMovimentacaoIdIn(ids).stream()
                        .collect(Collectors.groupingBy(me -> me.getMovimentacao().getId()));

        Page<MovimentacaoDTO.Resumo> paginaDtos = pagina.map(mov ->
                MovimentacaoDTO.Resumo.deEntidade(
                        mov, itensPorMovimentacao.getOrDefault(mov.getId(), List.of())));

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public MovimentacaoDTO.Detalhe buscarPorId(Long id) {
        Movimentacao movimentacao = movimentacaoRepository
                .findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Movimentação não encontrada com o ID informado: " + id));

        List<MovimentacaoEstoque> itens = movimentacaoEstoqueRepository.findAllByMovimentacaoId(id);
        List<MovimentacaoDTO.ItemMovimentacaoDetalhe> itensDetalhe = itens.stream()
                .map(MovimentacaoDTO.ItemMovimentacaoDetalhe::deEntidade)
                .toList();

        return MovimentacaoDTO.Detalhe.deEntidade(movimentacao, itensDetalhe);
    }

    @Transactional
    public MovimentacaoDTO.Detalhe atualizar(Long id, MovimentacaoDTO.Atualizacao atualizacao) {
        Movimentacao movimentacao = movimentacaoRepository
                .findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Movimentação não encontrada com o ID informado: " + id));

        movimentacao.setDescricao(atualizacao.descricao());

        List<MovimentacaoEstoque> itens = movimentacaoEstoqueRepository.findAllByMovimentacaoId(id);
        List<MovimentacaoDTO.ItemMovimentacaoDetalhe> itensDetalhe = itens.stream()
                .map(MovimentacaoDTO.ItemMovimentacaoDetalhe::deEntidade)
                .toList();

        return MovimentacaoDTO.Detalhe.deEntidade(movimentacao, itensDetalhe);
    }

    private Estoque getOrLoadEstoque(
            Map<String, Estoque> cache, Item item, EnderecoArmazenamento armazenamento) {
        String chave = item.getId() + "_" + armazenamento.getId();
        return cache.computeIfAbsent(chave, k ->
                estoqueRepository.findByItemIdAndArmazenamentoId(item.getId(), armazenamento.getId())
                        .orElseGet(() -> new Estoque(item, armazenamento, 0)));
    }
}
