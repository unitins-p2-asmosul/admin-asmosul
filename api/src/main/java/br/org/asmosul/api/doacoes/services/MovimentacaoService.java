package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.MovimentacaoEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.MovimentacaoRepository;
import org.springframework.stereotype.Service;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemRepository itemRepository;
    private final EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository, MovimentacaoEstoqueRepository movimentacaoEstoqueRepository, EstoqueRepository estoqueRepository, ItemRepository itemRepository, EnderecoArmazenamentoRepository enderecoArmazenamentoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.estoqueRepository = estoqueRepository;
        this.itemRepository = itemRepository;
        this.enderecoArmazenamentoRepository = enderecoArmazenamentoRepository;
    }
}
