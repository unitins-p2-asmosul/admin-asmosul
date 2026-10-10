package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.KitRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaRepository;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import org.springframework.stereotype.Service;

@Service
public class SaidaService {

    private final SaidaRepository saidaRepository;
    private final SaidaEstoqueRepository saidaEstoqueRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemRepository itemRepository;
    private final KitRepository kitRepository;
    private final EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;
    private final PessoaRepository pessoaRepository;

    public SaidaService(SaidaRepository saidaRepository, SaidaEstoqueRepository saidaEstoqueRepository, EstoqueRepository estoqueRepository, ItemRepository itemRepository, KitRepository kitRepository, EnderecoArmazenamentoRepository enderecoArmazenamentoRepository, PessoaRepository pessoaRepository) {
        this.saidaRepository = saidaRepository;
        this.saidaEstoqueRepository = saidaEstoqueRepository;
        this.estoqueRepository = estoqueRepository;
        this.itemRepository = itemRepository;
        this.kitRepository = kitRepository;
        this.enderecoArmazenamentoRepository = enderecoArmazenamentoRepository;
        this.pessoaRepository = pessoaRepository;
    }
}
