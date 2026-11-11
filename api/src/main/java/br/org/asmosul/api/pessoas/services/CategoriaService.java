package br.org.asmosul.api.pessoas.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.pessoas.dtos.CategoriaDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nome", "descricao", "dataInativo");

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public CategoriaDTO.Detalhe cadastrar(CategoriaDTO.Requisicao requisicao) {
        if (categoriaRepository.existsByNome(requisicao.nome())) {
            throw new ConflitoDadosException("Já existe uma categoria cadastrada com este nome.");
        }

        Categoria categoria = requisicao.paraEntidade();
        Categoria categoriaSalva = categoriaRepository.save(categoria);

        return CategoriaDTO.Detalhe.deEntidade(categoriaSalva);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<CategoriaDTO.Resumo> listar(
            Pageable paginacao, boolean apenasInativos, String nome) {

        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        String nomeNormalizado = nome == null ? "" : nome.trim();

        Page<Categoria> pagina;

        if (!nomeNormalizado.isBlank()) {
            pagina =
                    apenasInativos
                            ? categoriaRepository.findByNomeContainingIgnoreCaseAndDataInativoIsNotNull(
                                    nomeNormalizado, paginacaoSanitizada)
                            : categoriaRepository
                                    .findByNomeContainingIgnoreCaseAndDataInativoIsNull(
                                            nomeNormalizado, paginacaoSanitizada);
        } else {
            pagina =
                    apenasInativos
                            ? categoriaRepository.findAllByDataInativoIsNotNull(paginacaoSanitizada)
                            : categoriaRepository.findAllByDataInativoIsNull(paginacaoSanitizada);
        }

        Map<Long, Integer> pessoasPertencentesPorCategoria =
                buscarPessoasPertencentesPorCategorias(pagina.getContent());

        Page<CategoriaDTO.Resumo> paginaDtos =
                pagina.map(
                        categoria ->
                                CategoriaDTO.Resumo.deEntidade(
                                        categoria,
                                        pessoasPertencentesPorCategoria.getOrDefault(
                                                categoria.getId(), 0)));

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO.Resumo> listarTodas(boolean incluirInativos) {
        List<Categoria> categorias =
                incluirInativos
                        ? categoriaRepository.findAll()
                        : categoriaRepository.findAllByDataInativoIsNull();

        Map<Long, Integer> pessoasPertencentesPorCategoria =
                buscarPessoasPertencentesPorCategorias(categorias);

        return categorias.stream()
                .map(
                        categoria ->
                                CategoriaDTO.Resumo.deEntidade(
                                        categoria,
                                        pessoasPertencentesPorCategoria.getOrDefault(
                                                categoria.getId(), 0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO.Resumo> listarTodas() {
        return listarTodas(false);
    }

    @Transactional(readOnly = true)
    public CategoriaDTO.Detalhe buscarPorId(Long id) {
        Categoria categoria =
                categoriaRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria ativa não encontrada com o ID informado: "
                                                        + id));

        return CategoriaDTO.Detalhe.deEntidade(categoria);
    }

    @Transactional
    public CategoriaDTO.Detalhe atualizar(Long id, CategoriaDTO.Atualizacao requisicao) {
        Categoria categoria =
                categoriaRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria ativa não encontrada com o ID informado: "
                                                        + id));

        if (categoriaRepository.existsByNomeAndIdNot(requisicao.nome(), id)) {
            throw new ConflitoDadosException("Já existe uma categoria cadastrada com este nome.");
        }

        categoria.setNome(requisicao.nome());
        categoria.setDescricao(requisicao.descricao());

        return CategoriaDTO.Detalhe.deEntidade(categoria);
    }

    @Transactional
    public void desativar(Long id) {
        Categoria categoria =
                categoriaRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria ativa não encontrada com o ID informado: "
                                                        + id));

        categoria.setDataInativo(LocalDateTime.now());
    }

    @Transactional
    public void reativar(Long id) {
        Categoria categoria =
                categoriaRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria não encontrada com o ID informado: "
                                                        + id));

        categoria.setDataInativo(null);
    }

    private Map<Long, Integer> buscarPessoasPertencentesPorCategorias(List<Categoria> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> ids =
                categorias.stream().map(Categoria::getId).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> resultados = categoriaRepository.contarPessoasAtivasPorCategoriaIds(ids);
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
