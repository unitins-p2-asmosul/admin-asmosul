package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.KitDTO;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.ItemKit;
import br.org.asmosul.api.doacoes.models.Kit;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemKitRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.KitRepository;
import br.org.asmosul.api.doacoes.repositories.specifications.KitSpecification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KitService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nome", "descricao", "dataInativo");

    private final KitRepository kitRepository;
    private final ItemKitRepository itemKitRepository;
    private final ItemRepository itemRepository;
    private final EstoqueRepository estoqueRepository;
    private final EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;

    @Autowired
    public KitService(
            KitRepository kitRepository,
            ItemKitRepository itemKitRepository,
            ItemRepository itemRepository,
            EstoqueRepository estoqueRepository,
            EnderecoArmazenamentoRepository enderecoArmazenamentoRepository) {
        this.kitRepository = kitRepository;
        this.itemKitRepository = itemKitRepository;
        this.itemRepository = itemRepository;
        this.estoqueRepository = estoqueRepository;
        this.enderecoArmazenamentoRepository = enderecoArmazenamentoRepository;
    }

    public KitService(
            KitRepository kitRepository,
            ItemKitRepository itemKitRepository,
            ItemRepository itemRepository,
            EstoqueRepository estoqueRepository) {
        this(kitRepository, itemKitRepository, itemRepository, estoqueRepository, null);
    }

    @Transactional
    public KitDTO.Detalhe cadastrar(KitDTO.Requisicao requisicao) {
        String nomeNormalizado = KitDTO.normalizarNome(requisicao.nome());
        if (kitRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe um kit cadastrado com este nome.");
        }

        validarItensSemDuplicidade(requisicao.itens());

        Kit kit = new Kit(nomeNormalizado, requisicao.descricao());
        Kit kitSalvo = kitRepository.save(kit);

        List<ItemKit> itensSalvos = salvarItensDoKit(kitSalvo, requisicao.itens());

        return montarDetalhe(kitSalvo, itensSalvos);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<KitDTO.Resumo> listar(Pageable paginacao, KitDTO.KitFiltroDTO filtro) {
        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Specification<Kit> spec = KitSpecification.comFiltro(filtro);
        Page<Kit> pagina = kitRepository.findAll(spec, paginacaoSanitizada);

        List<Long> kitIds = pagina.getContent().stream().map(Kit::getId).toList();
        Map<Long, List<ItemKit>> itensPorKit = buscarItensPorKitIds(kitIds);
        Map<Long, Integer> estoquesPorItem = buscarEstoquesGlobaisDeItens(itensPorKit.values());

        Page<KitDTO.Resumo> paginaDtos =
                pagina.map(
                        kit -> {
                            List<ItemKit> itensDoKit =
                                    itensPorKit.getOrDefault(kit.getId(), List.of());
                            List<String> nomesItens =
                                    itensDoKit.stream().map(ik -> ik.getItem().getNome()).toList();
                            int distribuivel =
                                    calcularQuantidadeDistribuivel(itensDoKit, estoquesPorItem);
                            return KitDTO.Resumo.deEntidade(kit, nomesItens, distribuivel);
                        });

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<KitDTO.Resumo> listarTodos(boolean incluirInativos) {
        List<Kit> kits =
                incluirInativos
                        ? kitRepository.findAll()
                        : kitRepository.findAllByDataInativoIsNull();

        List<Long> kitIds = kits.stream().map(Kit::getId).toList();
        Map<Long, List<ItemKit>> itensPorKit = buscarItensPorKitIds(kitIds);
        Map<Long, Integer> estoquesPorItem = buscarEstoquesGlobaisDeItens(itensPorKit.values());

        return kits.stream()
                .map(
                        kit -> {
                            List<ItemKit> itensDoKit =
                                    itensPorKit.getOrDefault(kit.getId(), List.of());
                            List<String> nomesItens =
                                    itensDoKit.stream().map(ik -> ik.getItem().getNome()).toList();
                            int distribuivel =
                                    calcularQuantidadeDistribuivel(itensDoKit, estoquesPorItem);
                            return KitDTO.Resumo.deEntidade(kit, nomesItens, distribuivel);
                        })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KitDTO.Resumo> listarTodos() {
        return listarTodos(false);
    }

    @Transactional(readOnly = true)
    public KitDTO.Detalhe buscarPorId(Long id) {
        Kit kit =
                kitRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Kit não encontrado com o ID informado: " + id));

        List<ItemKit> itensDoKit = itemKitRepository.findAllByKitIdComItem(id);
        return montarDetalhe(kit, itensDoKit);
    }

    @Transactional
    public KitDTO.Detalhe atualizar(Long id, KitDTO.Atualizacao requisicao) {
        Kit kit =
                kitRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Kit ativo não encontrado com o ID informado: "
                                                        + id));

        String nomeNormalizado = KitDTO.normalizarNome(requisicao.nome());
        if (kitRepository.existsByNomeIgnoreCaseAndIdNot(nomeNormalizado, id)) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe um kit cadastrado com este nome.");
        }

        validarItensSemDuplicidade(requisicao.itens());

        kit.setNome(nomeNormalizado);
        kit.setDescricao(requisicao.descricao());

        itemKitRepository.deleteAllByKitId(id);
        List<ItemKit> itensSalvos = salvarItensDoKit(kit, requisicao.itens());

        return montarDetalhe(kit, itensSalvos);
    }

    @Transactional
    public void desativar(Long id) {
        Kit kit =
                kitRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Kit ativo não encontrado com o ID informado: "
                                                        + id));

        kit.desativar();
    }

    @Transactional
    public void reativar(Long id) {
        Kit kit =
                kitRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Kit não encontrado com o ID informado: " + id));

        kit.reativar();
    }

    @Transactional(readOnly = true)
    public KitDTO.Disponibilidade consultarDisponibilidade(Long idKit, Long localArmazenamentoId) {
        Kit kit =
                kitRepository
                        .findByIdAndDataInativoIsNull(idKit)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Kit ativo não encontrado com o ID informado: "
                                                        + idKit));

        EnderecoArmazenamento local = null;
        if (enderecoArmazenamentoRepository != null) {
            local =
                    enderecoArmazenamentoRepository
                            .findByIdAndDataInativoIsNull(localArmazenamentoId)
                            .orElseThrow(
                                    () ->
                                            new EntidadeNaoEncontradaException(
                                                    "Endereço de armazenamento ativo não encontrado com o ID informado: "
                                                            + localArmazenamentoId));
        }

        List<ItemKit> itensDoKit = itemKitRepository.findAllByKitIdComItem(idKit);
        List<Long> itemIds = itensDoKit.stream().map(ik -> ik.getItem().getId()).toList();

        Map<Long, Integer> saldoNoLocalPorItem = new HashMap<>();
        if (!itemIds.isEmpty()) {
            List<Estoque> estoquesNoLocal =
                    estoqueRepository.findAllByArmazenamentoIdAndItemIdIn(
                            localArmazenamentoId, itemIds);
            for (Estoque estoque : estoquesNoLocal) {
                saldoNoLocalPorItem.put(
                        estoque.getItem().getId(),
                        estoque.getQuantidade() != null ? estoque.getQuantidade() : 0);
            }
        }

        List<KitDTO.ItemDisponibilidade> itensDisponibilidade = new ArrayList<>();
        int minKitsPossiveis = itensDoKit.isEmpty() ? 0 : Integer.MAX_VALUE;

        for (ItemKit itemKit : itensDoKit) {
            Item item = itemKit.getItem();
            int quantidadePorKit = itemKit.getQuantidade();
            int saldoEstoque = saldoNoLocalPorItem.getOrDefault(item.getId(), 0);
            int kitsPossiveisItem =
                    quantidadePorKit > 0 ? Math.max(0, saldoEstoque / quantidadePorKit) : 0;

            minKitsPossiveis = Math.min(minKitsPossiveis, kitsPossiveisItem);

            itensDisponibilidade.add(
                    new KitDTO.ItemDisponibilidade(
                            item.getId(),
                            item.getNome(),
                            item.getUnidadeMedida(),
                            quantidadePorKit,
                            saldoEstoque,
                            kitsPossiveisItem));
        }

        int quantidadeDisponivel = minKitsPossiveis == Integer.MAX_VALUE ? 0 : minKitsPossiveis;

        return new KitDTO.Disponibilidade(
                kit.getId(),
                kit.getNome(),
                localArmazenamentoId,
                local != null ? local.getNome() : null,
                quantidadeDisponivel,
                itensDisponibilidade);
    }

    private void validarItensSemDuplicidade(List<KitDTO.ItemKitRequisicao> itens) {
        if (itens == null || itens.isEmpty()) {
            throw ValidationException.of("itens", "O kit deve conter pelo menos um item.");
        }

        Set<Long> idsVistos = new HashSet<>();
        for (int i = 0; i < itens.size(); i++) {
            KitDTO.ItemKitRequisicao itemReq = itens.get(i);
            if (!idsVistos.add(itemReq.idItem())) {
                throw ValidationException.of(
                        "itens[" + i + "].idItem",
                        "Não é permitido adicionar o mesmo item mais de uma vez no kit.");
            }
        }
    }

    private List<ItemKit> salvarItensDoKit(
            Kit kit, List<KitDTO.ItemKitRequisicao> itensRequisicao) {
        List<ItemKit> itensSalvos = new ArrayList<>();
        for (KitDTO.ItemKitRequisicao itemReq : itensRequisicao) {
            Item item =
                    itemRepository
                            .findByIdAndDataInativoIsNull(itemReq.idItem())
                            .orElseThrow(
                                    () ->
                                            new EntidadeNaoEncontradaException(
                                                    "Item ativo não encontrado com o ID informado: "
                                                            + itemReq.idItem()));

            ItemKit itemKit = new ItemKit(item, kit, itemReq.quantidade());
            itensSalvos.add(itemKitRepository.save(itemKit));
        }
        return itensSalvos;
    }

    private KitDTO.Detalhe montarDetalhe(Kit kit, List<ItemKit> itensDoKit) {
        List<Long> itemIds = itensDoKit.stream().map(ik -> ik.getItem().getId()).toList();
        Map<Long, Integer> estoquesPorItem = buscarEstoquesPorItemIds(itemIds);

        int quantidadeDistribuivel = calcularQuantidadeDistribuivel(itensDoKit, estoquesPorItem);

        List<KitDTO.ItemKitDetalhe> itensDetalhe =
                itensDoKit.stream()
                        .map(
                                ik -> {
                                    Item item = ik.getItem();
                                    int estoqueTotal =
                                            estoquesPorItem.getOrDefault(item.getId(), 0);
                                    int sobra =
                                            Math.max(
                                                    0,
                                                    estoqueTotal
                                                            - (quantidadeDistribuivel
                                                                    * ik.getQuantidade()));
                                    return new KitDTO.ItemKitDetalhe(
                                            item.getId(),
                                            item.getNome(),
                                            item.getUnidadeMedida(),
                                            ik.getQuantidade(),
                                            sobra);
                                })
                        .toList();

        return KitDTO.Detalhe.deEntidade(kit, quantidadeDistribuivel, itensDetalhe);
    }

    private int calcularQuantidadeDistribuivel(
            List<ItemKit> itensDoKit, Map<Long, Integer> estoquesPorItem) {
        if (itensDoKit == null || itensDoKit.isEmpty()) {
            return 0;
        }

        int minKits = Integer.MAX_VALUE;
        for (ItemKit ik : itensDoKit) {
            int qtdNecessaria = ik.getQuantidade() != null ? ik.getQuantidade() : 0;
            if (qtdNecessaria <= 0) {
                return 0;
            }
            int saldo = estoquesPorItem.getOrDefault(ik.getItem().getId(), 0);
            int kitsPossiveis = Math.max(0, saldo / qtdNecessaria);
            minKits = Math.min(minKits, kitsPossiveis);
        }

        return minKits == Integer.MAX_VALUE ? 0 : minKits;
    }

    private Map<Long, List<ItemKit>> buscarItensPorKitIds(Collection<Long> kitIds) {
        if (kitIds == null || kitIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ItemKit> todos = itemKitRepository.findAllByKitIdInComItem(kitIds);
        Map<Long, List<ItemKit>> mapa = new HashMap<>();
        for (ItemKit ik : todos) {
            mapa.computeIfAbsent(ik.getKit().getId(), ignored -> new ArrayList<>()).add(ik);
        }
        return mapa;
    }

    private Map<Long, Integer> buscarEstoquesGlobaisDeItens(
            Collection<List<ItemKit>> colecoesItensKit) {
        Set<Long> itemIds = new HashSet<>();
        for (List<ItemKit> lista : colecoesItensKit) {
            for (ItemKit ik : lista) {
                itemIds.add(ik.getItem().getId());
            }
        }
        return buscarEstoquesPorItemIds(itemIds);
    }

    private Map<Long, Integer> buscarEstoquesPorItemIds(Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> resultados = estoqueRepository.somarQuantidadePorItemIds(itemIds);
        Map<Long, Integer> mapa = new HashMap<>();
        if (resultados != null) {
            for (Object[] linha : resultados) {
                Long id = ((Number) linha[0]).longValue();
                Number soma = (Number) linha[1];
                mapa.put(id, soma != null ? soma.intValue() : 0);
            }
        }
        return mapa;
    }
}
