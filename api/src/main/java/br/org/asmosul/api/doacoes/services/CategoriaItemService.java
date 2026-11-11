package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.CategoriaItemDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.specifications.CategoriaItemSpecification;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaItemService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nome", "descricao", "dataInativo");

    private final CategoriaItemRepository categoriaItemRepository;
    private final ItemRepository itemRepository;

    public CategoriaItemService(
            CategoriaItemRepository categoriaItemRepository, ItemRepository itemRepository) {
        this.categoriaItemRepository = categoriaItemRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public CategoriaItemDTO.Detalhe cadastrar(CategoriaItemDTO.Requisicao requisicao) {
        if (categoriaItemRepository.existsByNomeIgnoreCase(requisicao.nome())) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe uma categoria de item cadastrada com este nome.");
        }

        CategoriaItem categoria = requisicao.paraEntidade();
        CategoriaItem categoriaSalva = categoriaItemRepository.save(categoria);

        return CategoriaItemDTO.Detalhe.deEntidade(categoriaSalva);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<CategoriaItemDTO.Resumo> listar(
            Pageable paginacao,
            boolean incluirInativos,
            boolean apenasInativos,
            String nome,
            String descricao) {

        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Specification<CategoriaItem> spec =
                CategoriaItemSpecification.comFiltro(
                        nome, descricao, incluirInativos, apenasInativos);

        Page<CategoriaItem> pagina =
                categoriaItemRepository.findAll(spec, paginacaoSanitizada);

        Map<Long, Integer> itensPertencentesPorCategoria =
                buscarItensPertencentesPorCategorias(pagina.getContent());

        Page<CategoriaItemDTO.Resumo> paginaDtos =
                pagina.map(
                        categoria ->
                                CategoriaItemDTO.Resumo.deEntidade(
                                        categoria,
                                        itensPertencentesPorCategoria.getOrDefault(
                                                categoria.getId(), 0)));

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<CategoriaItemDTO.Resumo> listarTodas(boolean incluirInativos) {
        List<CategoriaItem> categorias =
                incluirInativos
                        ? categoriaItemRepository.findAll()
                        : categoriaItemRepository.findAllByDataInativoIsNull();

        Map<Long, Integer> itensPertencentesPorCategoria =
                buscarItensPertencentesPorCategorias(categorias);

        return categorias.stream()
                .map(
                        categoria ->
                                CategoriaItemDTO.Resumo.deEntidade(
                                        categoria,
                                        itensPertencentesPorCategoria.getOrDefault(
                                                categoria.getId(), 0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaItemDTO.Resumo> listarTodas() {
        return listarTodas(false);
    }

    @Transactional(readOnly = true)
    public CategoriaItemDTO.Detalhe buscarPorId(Long id) {
        CategoriaItem categoria =
                categoriaItemRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria de item não encontrada com o ID informado: "
                                                        + id));

        return CategoriaItemDTO.Detalhe.deEntidade(categoria);
    }

    @Transactional
    public CategoriaItemDTO.Detalhe atualizar(Long id, CategoriaItemDTO.Atualizacao requisicao) {
        CategoriaItem categoria =
                categoriaItemRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria de item ativa não encontrada com o ID informado: "
                                                        + id));

        if (categoriaItemRepository.existsByNomeIgnoreCaseAndIdNot(requisicao.nome(), id)) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe uma categoria de item cadastrada com este nome.");
        }

        categoria.setNome(requisicao.nome());
        categoria.setDescricao(requisicao.descricao());

        return CategoriaItemDTO.Detalhe.deEntidade(categoria);
    }

    @Transactional
    public void desativar(Long id) {
        CategoriaItem categoria =
                categoriaItemRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria de item ativa não encontrada com o ID informado: "
                                                        + id));

        if (itemRepository.existsByCategoria_IdAndDataInativoIsNull(id)) {
                throw new ConflitoDadosException(
                        "Não é possível inativar a categoria porque ela possui itens ativos vinculados.");
        }

        categoria.desativar();
    }

    @Transactional
    public void reativar(Long id) {
        CategoriaItem categoria =
                categoriaItemRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria de item não encontrada com o ID informado: "
                                                        + id));

        categoria.reativar();
    }

    private Map<Long, Integer> buscarItensPertencentesPorCategorias(
            List<CategoriaItem> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> ids =
                categorias.stream().map(CategoriaItem::getId).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> resultados = itemRepository.contarItensAtivosPorCategoriaIds(ids);
        Map<Long, Integer> mapa = new HashMap<>();
        if (resultados != null) {
            for (Object[] linha : resultados) {
                Long id = ((Number) linha[0]).longValue();
                Number total = (Number) linha[1];
                mapa.put(id, total != null ? total.intValue() : 0);
            }
        }
        return mapa;
    }
}
