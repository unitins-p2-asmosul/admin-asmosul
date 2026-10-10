package br.org.asmosul.api.doacoes.services;

import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoriaItemRepository categoriaItemRepository;
    private final EstoqueRepository estoqueRepository;

    public ItemService(ItemRepository itemRepository, CategoriaItemRepository categoriaItemRepository, EstoqueRepository estoqueRepository) {
        this.itemRepository = itemRepository;
        this.categoriaItemRepository = categoriaItemRepository;
        this.estoqueRepository = estoqueRepository;
    }
}
