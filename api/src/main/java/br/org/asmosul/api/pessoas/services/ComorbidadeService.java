package br.org.asmosul.api.pessoas.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.pessoas.dtos.ComorbidadeDTO;
import br.org.asmosul.api.pessoas.models.Comorbidade;
import br.org.asmosul.api.pessoas.repositories.ComorbidadeRepository;
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
public class ComorbidadeService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS =
            Set.of("id", "nome", "descricao", "dataInativo");

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
            Pageable paginacao, boolean incluirInativos, String nome) {

        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(paginacao, CAMPOS_ORDENACAO_VALIDOS, "nome");

        String nomeNormalizado = nome == null ? "" : nome.trim();

        Page<Comorbidade> pagina;

        if (!nomeNormalizado.isBlank()) {
            pagina =
                    incluirInativos
                            ? comorbidadeRepository.findByNomeContainingIgnoreCase(
                                    nomeNormalizado, paginacaoSanitizada)
                            : comorbidadeRepository
                                    .findByNomeContainingIgnoreCaseAndDataInativoIsNull(
                                            nomeNormalizado, paginacaoSanitizada);
        } else {
            pagina =
                    incluirInativos
                            ? comorbidadeRepository.findAll(paginacaoSanitizada)
                            : comorbidadeRepository.findAllByDataInativoIsNull(paginacaoSanitizada);
        }

        Map<Long, Integer> pessoasAfetadasPorComorbidade =
                buscarPessoasAfetadasPorComorbidades(pagina.getContent());

        Page<ComorbidadeDTO.Resumo> paginaDtos =
                pagina.map(
                        comorbidade ->
                                ComorbidadeDTO.Resumo.deEntidade(
                                        comorbidade,
                                        pessoasAfetadasPorComorbidade.getOrDefault(
                                                comorbidade.getId(), 0)));

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public List<ComorbidadeDTO.Resumo> listarTodas(boolean incluirInativos) {
        List<Comorbidade> comorbidades =
                incluirInativos
                        ? comorbidadeRepository.findAll()
                        : comorbidadeRepository.findAllByDataInativoIsNull();

        Map<Long, Integer> pessoasAfetadasPorComorbidade =
                buscarPessoasAfetadasPorComorbidades(comorbidades);

        return comorbidades.stream()
                .map(
                        comorbidade ->
                                ComorbidadeDTO.Resumo.deEntidade(
                                        comorbidade,
                                        pessoasAfetadasPorComorbidade.getOrDefault(
                                                comorbidade.getId(), 0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ComorbidadeDTO.Resumo> listarTodas() {
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

    private Map<Long, Integer> buscarPessoasAfetadasPorComorbidades(List<Comorbidade> comorbidades) {
        if (comorbidades == null || comorbidades.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> ids =
                comorbidades.stream()
                        .map(Comorbidade::getId)
                        .filter(Objects::nonNull)
                        .toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> resultados =
                comorbidadeRepository.contarPessoasAtivasPorComorbidadeIds(ids);
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
