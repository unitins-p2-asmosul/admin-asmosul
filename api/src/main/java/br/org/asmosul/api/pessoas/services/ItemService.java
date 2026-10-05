package br.org.asmosul.api.pessoas.services;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.pessoas.dtos.ItemDTO;
import br.org.asmosul.api.pessoas.dtos.ItemFiltroDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.models.Item;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import br.org.asmosul.api.pessoas.repositories.ItemRepository;
import br.org.asmosul.api.pessoas.repositories.ItemSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoriaRepository categoriaRepository;

    public ItemService(ItemRepository itemRepository, CategoriaRepository categoriaRepository) {
        this.itemRepository = itemRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public ItemDTO.Detalhe cadastrar(ItemDTO.Requisicao dto) {
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Item item = dto.paraEntidade(categoria);
        Item itemSalvo = itemRepository.save(item);

        Double estoque = calcularEstoque(itemSalvo.getId());
        return ItemDTO.Detalhe.deEntidade(itemSalvo, estoque);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<ItemDTO.Resumo> listar(ItemFiltroDTO filtro, Pageable pageable, boolean incluirInativos) {
        Specification<Item> spec = ItemSpecification.comFiltro(filtro, incluirInativos);

        Page<ItemDTO.Resumo> pagina = itemRepository.findAll(spec, pageable)
                .map(item -> ItemDTO.Resumo.deEntidade(item, calcularEstoque(item.getId())));

        return RespostaPaginada.dePage(pagina);
    }

    @Transactional(readOnly = true)
    public ItemDTO.Detalhe buscarPorId(Long id) {
        Item item = itemRepository.findByIdAndDataInativoIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado"));

        Double estoque = calcularEstoque(item.getId());
        return ItemDTO.Detalhe.deEntidade(item, estoque);
    }

    @Transactional
    public ItemDTO.Detalhe atualizar(Long id, ItemDTO.Atualizacao dto) {
        Item item = itemRepository.findByIdAndDataInativoIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        item.setNome(dto.nome());
        item.setCategoria(categoria);
        item.setPrecoUnitario(dto.precoUnitario());
        item.setUnidadeMedida(dto.unidadeMedida());
        item.setDescricao(dto.descricao());

        Item itemAtualizado = itemRepository.save(item);
        Double estoque = calcularEstoque(itemAtualizado.getId());
        return ItemDTO.Detalhe.deEntidade(itemAtualizado, estoque);
    }

    @Transactional
    public void inativar(Long id) {
        Item item = itemRepository.findByIdAndDataInativoIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado"));

        item.setDataInativo(java.time.LocalDateTime.now());
        itemRepository.save(item);
    }

    @Transactional
    public void reativar(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado"));

        item.setDataInativo(null);
        itemRepository.save(item);
    }

    private Double calcularEstoque(Long itemId) {
        return itemRepository.calcularEstoqueAtual(itemId);
    }
}