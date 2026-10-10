package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import org.springframework.stereotype.Service;

@Service
public class EntradaService {

    private final EntradaRepository entradaRepository;
    private final EntradaEstoqueRepository entradaEstoqueRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemRepository itemRepository;
    private final EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;
    private final PessoaRepository pessoaRepository;

    public EntradaService(EntradaRepository entradaRepository, EntradaEstoqueRepository entradaEstoqueRepository, EstoqueRepository estoqueRepository, ItemRepository itemRepository, EnderecoArmazenamentoRepository enderecoArmazenamentoRepository, PessoaRepository pessoaRepository) {
        this.entradaRepository = entradaRepository;
        this.entradaEstoqueRepository = entradaEstoqueRepository;
        this.estoqueRepository = estoqueRepository;
        this.itemRepository = itemRepository;
        this.enderecoArmazenamentoRepository = enderecoArmazenamentoRepository;
        this.pessoaRepository = pessoaRepository;
    }
}
