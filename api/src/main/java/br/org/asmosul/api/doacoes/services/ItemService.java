package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.ItemDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.specifications.ItemSpecification;
import java.util.Collection;
import java.util.Collections;
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
public class ItemService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nome", "precoUnitario", "dataInativo");

    private final ItemRepository itemRepository;
    private final CategoriaItemRepository categoriaItemRepository;
    private final EstoqueRepository estoqueRepository;

    public ItemService(
            ItemRepository itemRepository,
            CategoriaItemRepository categoriaItemRepository,
            EstoqueRepository estoqueRepository) {
        this.itemRepository = itemRepository;
        this.categoriaItemRepository = categoriaItemRepository;
        this.estoqueRepository = estoqueRepository;
    }

    @Transactional
    public ItemDTO.Detalhe cadastrar(ItemDTO.Requisicao requisicao) {
        if (itemRepository.existsByNomeIgnoreCase(requisicao.nome())) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe um item cadastrado com este nome.");
        }

        CategoriaItem categoria =
                categoriaItemRepository
                        .findByIdAndDataInativoIsNull(requisicao.idCategoria())
                        .orElseThrow(
                                () ->
                                        ValidationException.of(
                                                "idCategoria",
                                                "Categoria de item informada não existe ou está inativa"));

        Item item = requisicao.paraEntidade(categoria);
        Item itemSalvo = itemRepository.save(item);

        return ItemDTO.Detalhe.deEntidade(itemSalvo, 0, List.of());
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<ItemDTO.Resumo> listar(Pageable paginacao, ItemDTO.Filtro filtro) {
        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Specification<Item> spec = ItemSpecification.comFiltro(filtro);
        Page<Item> pagina = itemRepository.findAll(spec, paginacaoSanitizada);

        List<Long> itemIds = pagina.getContent().stream().map(Item::getId).toList();
        Map<Long, Integer> estoquesPorItem = buscarEstoquesPorItemIds(itemIds);

        Page<ItemDTO.Resumo> paginaDtos =
                pagina.map(
                        item ->
                                ItemDTO.Resumo.deEntidade(
                                        item, estoquesPorItem.getOrDefault(item.getId(), 0)));

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<ItemDTO.Resumo> listarTodos(boolean incluirInativos) {
        List<Item> itens =
                incluirInativos
                        ? itemRepository.findAll()
                        : itemRepository.findAllByDataInativoIsNull();

        List<Long> itemIds = itens.stream().map(Item::getId).toList();
        Map<Long, Integer> estoquesPorItem = buscarEstoquesPorItemIds(itemIds);

        return itens.stream()
                .map(
                        item ->
                                ItemDTO.Resumo.deEntidade(
                                        item, estoquesPorItem.getOrDefault(item.getId(), 0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemDTO.Resumo> listarTodos() {
        return listarTodos(false);
    }

    @Transactional(readOnly = true)
    public ItemDTO.Detalhe buscarPorId(Long id) {
        Item item =
                itemRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Item não encontrado com o ID informado: " + id));

        List<Estoque> estoques =
                estoqueRepository.findAllByItemIdAndQuantidadeGreaterThan(id, 0);
        List<ItemDTO.ItemEstoqueLocalDTO> locais =
                estoques.stream()
                        .map(
                                e ->
                                        new ItemDTO.ItemEstoqueLocalDTO(
                                                e.getArmazenamento().getId(),
                                                e.getArmazenamento().getNome(),
                                                e.getQuantidade()))
                        .toList();

        int estoqueTotal = estoques.stream().mapToInt(Estoque::getQuantidade).sum();

        return ItemDTO.Detalhe.deEntidade(item, estoqueTotal, locais);
    }

    @Transactional
    public ItemDTO.Detalhe atualizar(Long id, ItemDTO.Atualizacao requisicao) {
        Item item =
                itemRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Item ativo não encontrado com o ID informado: "
                                                        + id));

        if (itemRepository.existsByNomeIgnoreCaseAndIdNot(requisicao.nome(), id)) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe um item cadastrado com este nome.");
        }

        CategoriaItem categoria =
                categoriaItemRepository
                        .findByIdAndDataInativoIsNull(requisicao.idCategoria())
                        .orElseThrow(
                                () ->
                                        ValidationException.of(
                                                "idCategoria",
                                                "Categoria de item informada não existe ou está inativa"));

        item.setNome(requisicao.nome());
        item.setCategoria(categoria);
        item.setPrecoUnitario(requisicao.precoUnitario());
        item.setUnidadeMedida(requisicao.unidadeMedida());
        item.setDescricao(requisicao.descricao());

        List<Estoque> estoques =
                estoqueRepository.findAllByItemIdAndQuantidadeGreaterThan(id, 0);
        List<ItemDTO.ItemEstoqueLocalDTO> locais =
                estoques.stream()
                        .map(
                                e ->
                                        new ItemDTO.ItemEstoqueLocalDTO(
                                                e.getArmazenamento().getId(),
                                                e.getArmazenamento().getNome(),
                                                e.getQuantidade()))
                        .toList();

        int estoqueTotal = estoques.stream().mapToInt(Estoque::getQuantidade).sum();

        return ItemDTO.Detalhe.deEntidade(item, estoqueTotal, locais);
    }

    @Transactional
    public void desativar(Long id) {
        Item item =
                itemRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Item ativo não encontrado com o ID informado: "
                                                        + id));

        item.desativar();
    }

    @Transactional
    public void reativar(Long id) {
        Item item =
                itemRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Item não encontrado com o ID informado: " + id));

        item.reativar();
    }

    private Map<Long, Integer> buscarEstoquesPorItemIds(Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> resultados = estoqueRepository.somarQuantidadePorItemIds(itemIds);
        Map<Long, Integer> mapa = new HashMap<>();
        for (Object[] linha : resultados) {
            Long id = (Long) linha[0];
            Number soma = (Number) linha[1];
            mapa.put(id, soma != null ? soma.intValue() : 0);
        }
        return mapa;
    }
}
