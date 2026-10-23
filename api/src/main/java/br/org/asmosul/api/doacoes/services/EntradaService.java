package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.utils.PaginacaoUtils;
import br.org.asmosul.api.doacoes.dtos.EntradaDTO;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Entrada;
import br.org.asmosul.api.doacoes.models.EntradaEstoque;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.specifications.EntradaSpecification;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.util.ArrayList;
import java.util.Collection;
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
public class EntradaService {

    private static final Set<String> CAMPOS_ORDENACAO_VALIDOS = Set.of("id", "dataHora");

    private final EntradaRepository entradaRepository;
    private final EntradaEstoqueRepository entradaEstoqueRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemRepository itemRepository;
    private final EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;
    private final PessoaRepository pessoaRepository;

    public EntradaService(
            EntradaRepository entradaRepository,
            EntradaEstoqueRepository entradaEstoqueRepository,
            EstoqueRepository estoqueRepository,
            ItemRepository itemRepository,
            EnderecoArmazenamentoRepository enderecoArmazenamentoRepository,
            PessoaRepository pessoaRepository) {
        this.entradaRepository = entradaRepository;
        this.entradaEstoqueRepository = entradaEstoqueRepository;
        this.estoqueRepository = estoqueRepository;
        this.itemRepository = itemRepository;
        this.enderecoArmazenamentoRepository = enderecoArmazenamentoRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public EntradaDTO.Detalhe cadastrar(EntradaDTO.Requisicao requisicao) {
        Pessoa doador = buscarDoadorAtivo(requisicao.idDoador());

        Entrada entrada = entradaRepository.save(requisicao.paraEntidade(doador));
        List<EntradaEstoque> itensSalvos = new ArrayList<>();

        for (EntradaDTO.ItemEntradaRequisicao itemRequisicao : requisicao.itens()) {
            Item item = buscarItemAtivo(itemRequisicao.idItem());
            EnderecoArmazenamento armazenamento =
                    buscarEnderecoAtivo(itemRequisicao.idArmazenamento());

            EntradaEstoque entradaEstoque =
                    entradaEstoqueRepository.save(
                            new EntradaEstoque(
                                    item,
                                    armazenamento,
                                    entrada,
                                    itemRequisicao.quantidade()));
            itensSalvos.add(entradaEstoque);

            incrementarEstoque(item, armazenamento, itemRequisicao.quantidade());
        }

        if (!doador.isEhDoador()) {
            doador.setEhDoador(true);
            pessoaRepository.save(doador);
        }

        return montarDetalhe(entrada, itensSalvos);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<EntradaDTO.Resumo> listar(
            Pageable paginacao, EntradaDTO.EntradaFiltroDTO filtro) {
        Pageable paginacaoSanitizada =
                PaginacaoUtils.sanitizarPaginacao(
                        paginacao, CAMPOS_ORDENACAO_VALIDOS, "dataHora");

        Specification<Entrada> spec = EntradaSpecification.comFiltro(filtro);
        Page<Entrada> pagina = entradaRepository.findAll(spec, paginacaoSanitizada);

        List<Long> entradaIds = pagina.getContent().stream().map(Entrada::getId).toList();
        Map<Long, List<EntradaEstoque>> itensPorEntrada = buscarItensAgrupados(entradaIds);

        Page<EntradaDTO.Resumo> paginaDtos =
                pagina.map(
                        entrada ->
                                EntradaDTO.Resumo.deEntidade(
                                        entrada,
                                        itensPorEntrada.getOrDefault(entrada.getId(), List.of())));

        return RespostaPaginada.dePage(paginaDtos);
    }

    @Transactional(readOnly = true)
    public EntradaDTO.Detalhe buscarPorId(Long id) {
        Entrada entrada = buscarEntrada(id);
        List<EntradaEstoque> itens =
                entradaEstoqueRepository.findAllByEntradaIdComRelacionamentos(id);
        return montarDetalhe(entrada, itens);
    }

    @Transactional
    public EntradaDTO.Detalhe atualizar(Long id, EntradaDTO.Atualizacao atualizacao) {
        Entrada entrada = buscarEntrada(id);

        if (atualizacao.idDoador() != null) {
            Pessoa novoDoador = buscarDoadorAtivo(atualizacao.idDoador());
            entrada.setDoador(novoDoador);

            if (!novoDoador.isEhDoador()) {
                novoDoador.setEhDoador(true);
                pessoaRepository.save(novoDoador);
            }
        }

        if (atualizacao.descricao() != null) {
            entrada.setDescricao(atualizacao.descricao());
        }

        List<EntradaEstoque> itens =
                entradaEstoqueRepository.findAllByEntradaIdComRelacionamentos(id);
        return montarDetalhe(entrada, itens);
    }

    @Transactional
    public void deletar(Long id) {
        Entrada entrada = buscarEntrada(id);
        List<EntradaEstoque> itens =
                entradaEstoqueRepository.findAllByEntradaIdComRelacionamentos(id);

        for (EntradaEstoque itemEntrada : itens) {
            estornarEstoque(itemEntrada);
        }

        entradaEstoqueRepository.deleteAll(itens);
        entradaRepository.delete(entrada);
    }

    @Transactional
    public void deletarItemEntrada(Long idEntrada, Long idEntradaEstoque) {
        buscarEntrada(idEntrada);

        EntradaEstoque entradaEstoque =
                entradaEstoqueRepository
                        .findByIdAndEntradaIdComRelacionamentos(idEntradaEstoque, idEntrada)
                        .orElseThrow(
                                () ->
                                        new EntidadeNaoEncontradaException(
                                                "Registro de item da entrada não encontrado com o ID informado: "
                                                        + idEntradaEstoque));

        estornarEstoque(entradaEstoque);
        entradaEstoqueRepository.delete(entradaEstoque);
    }

    private Pessoa buscarDoadorAtivo(Long idDoador) {
        return pessoaRepository
                .findByIdAndDataInativoIsNull(idDoador)
                .orElseThrow(
                        () ->
                                new EntidadeNaoEncontradaException(
                                        "Doador ativo não encontrado com o ID informado: "
                                                + idDoador));
    }

    private Item buscarItemAtivo(Long idItem) {
        return itemRepository
                .findByIdAndDataInativoIsNull(idItem)
                .orElseThrow(
                        () ->
                                new EntidadeNaoEncontradaException(
                                        "Item ativo não encontrado com o ID informado: " + idItem));
    }

    private EnderecoArmazenamento buscarEnderecoAtivo(Long idEndereco) {
        return enderecoArmazenamentoRepository
                .findByIdAndDataInativoIsNull(idEndereco)
                .orElseThrow(
                        () ->
                                new EntidadeNaoEncontradaException(
                                        "Endereço de armazenamento ativo não encontrado com o ID informado: "
                                                + idEndereco));
    }

    private Entrada buscarEntrada(Long id) {
        return entradaRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new EntidadeNaoEncontradaException(
                                        "Entrada não encontrada com o ID informado: " + id));
    }

    private void incrementarEstoque(
            Item item, EnderecoArmazenamento armazenamento, Integer quantidadeEntrada) {
        Estoque estoque =
                estoqueRepository
                        .findByItemIdAndArmazenamentoId(item.getId(), armazenamento.getId())
                        .orElseGet(() -> new Estoque(item, armazenamento, 0));

        int quantidadeAtual = estoque.getQuantidade() != null ? estoque.getQuantidade() : 0;
        estoque.setQuantidade(quantidadeAtual + quantidadeEntrada);
        estoqueRepository.save(estoque);
    }

    private void estornarEstoque(EntradaEstoque entradaEstoque) {
        estoqueRepository
                .findByItemIdAndArmazenamentoId(
                        entradaEstoque.getItem().getId(),
                        entradaEstoque.getArmazenamento().getId())
                .ifPresent(
                        estoque -> {
                            int quantidadeAtual =
                                    estoque.getQuantidade() != null ? estoque.getQuantidade() : 0;
                            int quantidadeEntrada =
                                    entradaEstoque.getQuantidade() != null
                                            ? entradaEstoque.getQuantidade()
                                            : 0;
                            estoque.setQuantidade(Math.max(0, quantidadeAtual - quantidadeEntrada));
                            estoqueRepository.save(estoque);
                        });
    }

    private EntradaDTO.Detalhe montarDetalhe(
            Entrada entrada, List<EntradaEstoque> itensEntrada) {
        List<EntradaDTO.ItemEntradaDetalhe> itens =
                itensEntrada.stream().map(EntradaDTO.ItemEntradaDetalhe::deEntidade).toList();
        return EntradaDTO.Detalhe.deEntidade(entrada, itens);
    }

    private Map<Long, List<EntradaEstoque>> buscarItensAgrupados(Collection<Long> entradaIds) {
        if (entradaIds == null || entradaIds.isEmpty()) {
            return Map.of();
        }

        List<EntradaEstoque> itens =
                entradaEstoqueRepository.findAllByEntradaIdInComRelacionamentos(entradaIds);
        Map<Long, List<EntradaEstoque>> agrupados = new HashMap<>();

        for (EntradaEstoque item : itens) {
            agrupados.computeIfAbsent(item.getEntrada().getId(), ignored -> new ArrayList<>())
                    .add(item);
        }

        return agrupados;
    }
}
