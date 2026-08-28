package br.org.asmosul.api.pessoas.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.pessoas.dtos.ComorbidadeDTO;
import br.org.asmosul.api.pessoas.models.Comorbidade;
import br.org.asmosul.api.pessoas.repositories.ComorbidadeRepository;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComorbidadeService {

    private static final java.util.Set<String> CAMPOS_ORDENACAO_VALIDOS =
            java.util.Set.of("id", "nome", "descricao", "dataInativo");

    private final ComorbidadeRepository comorbidadeRepository;

    public ComorbidadeService(ComorbidadeRepository comorbidadeRepository) {
        this.comorbidadeRepository = comorbidadeRepository;
    }

    @Transactional
    public ComorbidadeDTO.Detalhe cadastrar(ComorbidadeDTO.Requisicao requisicao) {
        if (comorbidadeRepository.existsByNome(requisicao.nome())) {
            throw new ConflitoDadosException("Já existe uma comorbidade cadastrada com este nome.");
        }

        Comorbidade comorbidade = requisicao.paraEntidade();
        Comorbidade comorbidadeSalva = comorbidadeRepository.save(comorbidade);

        return ComorbidadeDTO.Detalhe.deEntidade(comorbidadeSalva);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<ComorbidadeDTO.Resumo> listar(
            Pageable paginacao, boolean incluirInativos) {
        Pageable paginacaoSanitizada =
                br.org.asmosul.api.comum.utils.PaginacaoUtils.sanitizarPaginacao(
                        paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        Page<Comorbidade> pagina =
                incluirInativos
                        ? comorbidadeRepository.findAll(paginacaoSanitizada)
                        : comorbidadeRepository.findAllByDataInativoIsNull(paginacaoSanitizada);

        Page<ComorbidadeDTO.Resumo> paginaDtos = pagina.map(ComorbidadeDTO.Resumo::deEntidade);
        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public java.util.List<ComorbidadeDTO.Resumo> listarTodas(boolean incluirInativos) {
        java.util.List<Comorbidade> comorbidades =
                incluirInativos
                        ? comorbidadeRepository.findAll()
                        : comorbidadeRepository.findAllByDataInativoIsNull();

        return comorbidades.stream()
                .map(ComorbidadeDTO.Resumo::deEntidade)
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<ComorbidadeDTO.Resumo> listarTodas() {
        return listarTodas(false);
    }

    @Transactional(readOnly = true)
    public ComorbidadeDTO.Detalhe buscarPorId(Long id) {
        Comorbidade comorbidade =
                comorbidadeRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Comorbidade ativa não encontrada com o ID informado: "
                                                        + id));

        return ComorbidadeDTO.Detalhe.deEntidade(comorbidade);
    }

    @Transactional
    public ComorbidadeDTO.Detalhe atualizar(Long id, ComorbidadeDTO.Atualizacao requisicao) {
        Comorbidade comorbidade =
                comorbidadeRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Comorbidade ativa não encontrada com o ID informado: "
                                                        + id));

        if (comorbidadeRepository.existsByNomeAndIdNot(requisicao.nome(), id)) {
            throw new ConflitoDadosException("Já existe uma comorbidade cadastrada com este nome.");
        }

        comorbidade.setNome(requisicao.nome());
        comorbidade.setDescricao(requisicao.descricao());

        return ComorbidadeDTO.Detalhe.deEntidade(comorbidade);
    }

    @Transactional
    public void desativar(Long id) {
        Comorbidade comorbidade =
                comorbidadeRepository
                        .findByIdAndDataInativoIsNull(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Comorbidade ativa não encontrada com o ID informado: "
                                                        + id));

        comorbidade.setDataInativo(LocalDateTime.now());
    }

    @Transactional
    public void reativar(Long id) {
        Comorbidade comorbidade =
                comorbidadeRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Comorbidade não encontrada com o ID informado: "
                                                        + id));

        comorbidade.setDataInativo(null);
    }

    @Transactional
    public void excluir(Long id) {
        Comorbidade comorbidade =
                comorbidadeRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Comorbidade não encontrada com o ID informado: "
                                                        + id));

        comorbidadeRepository.delete(comorbidade);
    }
}
