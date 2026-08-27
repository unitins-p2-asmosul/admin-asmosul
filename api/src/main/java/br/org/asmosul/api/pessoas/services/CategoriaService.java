package br.org.asmosul.api.pessoas.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.pessoas.dtos.CategoriaDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private static final java.util.Set<String> CAMPOS_ORDENACAO_VALIDOS =
            java.util.Set.of("id", "nome", "descricao", "dataInativo");

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
            Pageable paginacao, boolean incluirInativos) {
        Pageable paginacaoSanitizada =
                br.org.asmosul.api.comum.utils.PaginacaoUtils.sanitizarPaginacao(
                        paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Page<Categoria> pagina =
                incluirInativos
                        ? categoriaRepository.findAll(paginacaoSanitizada)
                        : categoriaRepository.findAllByDataInativoIsNull(paginacaoSanitizada);

        Page<CategoriaDTO.Resumo> paginaDtos = pagina.map(CategoriaDTO.Resumo::deEntidade);
        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public java.util.List<CategoriaDTO.Resumo> listarTodas() {
        return categoriaRepository.findAllByDataInativoIsNull().stream()
                .map(CategoriaDTO.Resumo::deEntidade)
                .toList();
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
}
