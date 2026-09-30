package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.CategoriaDoacaoDTO;
import br.org.asmosul.api.doacoes.models.CategoriaDoacao;
import br.org.asmosul.api.doacoes.repositories.CategoriaDoacaoRepository;
import br.org.asmosul.api.doacoes.repositories.CategoriaDoacaoSpecification;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaDoacaoService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nome", "descricao", "dataInativo");

    private final CategoriaDoacaoRepository categoriaDoacaoRepository;

    public CategoriaDoacaoService(CategoriaDoacaoRepository categoriaDoacaoRepository) {
        this.categoriaDoacaoRepository = categoriaDoacaoRepository;
    }

    @Transactional
    public CategoriaDoacaoDTO.Detalhe cadastrar(CategoriaDoacaoDTO.Requisicao requisicao) {
        if (categoriaDoacaoRepository.existsByNomeIgnoreCase(requisicao.nome())) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe uma categoria de doação cadastrada com este nome.");
        }

        CategoriaDoacao categoria = requisicao.paraEntidade();
        CategoriaDoacao categoriaSalva = categoriaDoacaoRepository.save(categoria);

        return CategoriaDoacaoDTO.Detalhe.deEntidade(categoriaSalva);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<CategoriaDoacaoDTO.Resumo> listar(
            Pageable paginacao, boolean incluirInativos, String nome, String descricao) {

        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Specification<CategoriaDoacao> spec =
                CategoriaDoacaoSpecification.comFiltro(nome, descricao, incluirInativos);

        Page<CategoriaDoacao> pagina =
                categoriaDoacaoRepository.findAll(spec, paginacaoSanitizada);

        Page<CategoriaDoacaoDTO.Resumo> paginaDtos =
                pagina.map(CategoriaDoacaoDTO.Resumo::deEntidade);

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<CategoriaDoacaoDTO.Resumo> listarTodas(boolean incluirInativos) {
        List<CategoriaDoacao> categorias =
                incluirInativos
                        ? categoriaDoacaoRepository.findAll()
                        : categoriaDoacaoRepository.findAllByDataInativoIsNull();

        return categorias.stream().map(CategoriaDoacaoDTO.Resumo::deEntidade).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaDoacaoDTO.Resumo> listarTodas() {
        return listarTodas(false);
    }

    @Transactional(readOnly = true)
    public CategoriaDoacaoDTO.Detalhe buscarPorId(Long id) {
        CategoriaDoacao categoria =
                categoriaDoacaoRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria de doação ativa não encontrada com o ID informado: "
                                                        + id));

        return CategoriaDoacaoDTO.Detalhe.deEntidade(categoria);
    }

    @Transactional
    public CategoriaDoacaoDTO.Detalhe atualizar(Long id, CategoriaDoacaoDTO.Atualizacao requisicao) {
        CategoriaDoacao categoria =
                categoriaDoacaoRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria de doação ativa não encontrada com o ID informado: "
                                                        + id));

        if (categoriaDoacaoRepository.existsByNomeIgnoreCaseAndIdNot(requisicao.nome(), id)) {
            throw ValidationException.ofConflito(
                    "nome", "Já existe uma categoria de doação cadastrada com este nome.");
        }

        categoria.setNome(requisicao.nome());
        categoria.setDescricao(requisicao.descricao());

        return CategoriaDoacaoDTO.Detalhe.deEntidade(categoria);
    }

    @Transactional
    public void desativar(Long id) {
        CategoriaDoacao categoria =
                categoriaDoacaoRepository
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
        CategoriaDoacao categoria =
                categoriaDoacaoRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Categoria de doação não encontrada com o ID informado: "
                                                        + id));

        categoria.reativar();
    }
}
