package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemKitRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.KitRepository;
import org.springframework.stereotype.Service;

@Service
public class KitService {

    private final KitRepository kitRepository;
    private final ItemKitRepository itemKitRepository;
    private final ItemRepository itemRepository;
    private final EstoqueRepository estoqueRepository;

    public KitService(KitRepository kitRepository, ItemKitRepository itemKitRepository, ItemRepository itemRepository, EstoqueRepository estoqueRepository) {
        this.kitRepository = kitRepository;
        this.itemKitRepository = itemKitRepository;
        this.itemRepository = itemRepository;
        this.estoqueRepository = estoqueRepository;
    }
}
