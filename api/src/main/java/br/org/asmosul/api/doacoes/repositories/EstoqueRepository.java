package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.Estoque;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    @Query("SELECT COALESCE(SUM(e.quantidade), 0) FROM Estoque e WHERE e.item.id = :itemId")
    Integer somarQuantidadePorItemId(@Param("itemId") Long itemId);

    @Query("SELECT e FROM Estoque e JOIN FETCH e.armazenamento WHERE e.item.id = :itemId")
    List<Estoque> findByItemIdComArmazenamento(@Param("itemId") Long itemId);

    @Query(
            "SELECT e.item.id, COALESCE(SUM(e.quantidade), 0) FROM Estoque e WHERE e.item.id IN :itemIds GROUP BY e.item.id")
    List<Object[]> somarQuantidadePorItemIds(@Param("itemIds") Collection<Long> itemIds);
    Optional<Estoque> findByItemIdAndArmazenamentoId(Long itemId, Long armazenamentoId);
}
