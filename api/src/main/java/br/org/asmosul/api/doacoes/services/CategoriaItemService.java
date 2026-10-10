package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.CategoriaItemDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemSpecification;
import java.util.List;
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

    public CategoriaItemService(CategoriaItemRepository categoriaItemRepository) {
        this.categoriaItemRepository = categoriaItemRepository;
    }

    @Transactional
    public CategoriaItemDTO.Detalhe cadastrar(CategoriaItemDTO.Requisicao requisicao) {
        if (categoriaItemRepository.existsByNomeIgnoreCase(requisicao.nome())) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe uma categoria de doação cadastrada com este nome.");
        }

        CategoriaItem categoria = requisicao.paraEntidade();
        CategoriaItem categoriaSalva = categoriaItemRepository.save(categoria);

        return CategoriaItemDTO.Detalhe.deEntidade(categoriaSalva);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<CategoriaItemDTO.Resumo> listar(
            Pageable paginacao, boolean incluirInativos, String nome, String descricao) {

        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Specification<CategoriaItem> spec =
                CategoriaItemSpecification.comFiltro(nome, descricao, incluirInativos);

        Page<CategoriaItem> pagina =
                categoriaItemRepository.findAll(spec, paginacaoSanitizada);

        Page<CategoriaItemDTO.Resumo> paginaDtos =
                pagina.map(CategoriaItemDTO.Resumo::deEntidade);

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<CategoriaItemDTO.Resumo> listarTodas(boolean incluirInativos) {
        List<CategoriaItem> categorias =
                incluirInativos
                        ? categoriaItemRepository.findAll()
                        : categoriaItemRepository.findAllByDataInativoIsNull();

        return categorias.stream().map(CategoriaItemDTO.Resumo::deEntidade).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaItemDTO.Resumo> listarTodas() {
        return listarTodas(false);
    }

    @Transactional(readOnly = true)
    public CategoriaItemDTO.Detalhe buscarPorId(Long id) {
        CategoriaItem categoria =
                categoriaItemRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria de doação ativa não encontrada com o ID informado: "
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
                                                "Categoria de doação ativa não encontrada com o ID informado: "
                                                        + id));

        if (categoriaItemRepository.existsByNomeIgnoreCaseAndIdNot(requisicao.nome(), id)) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe uma categoria de doação cadastrada com este nome.");
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
                                                "Categoria de doação ativa não encontrada com o ID informado: "
                                                        + id));

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
                                                "Categoria de doação não encontrada com o ID informado: "
                                                        + id));

        categoria.reativar();
    }
}
