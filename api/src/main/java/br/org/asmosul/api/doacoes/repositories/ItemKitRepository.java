package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.ItemKit;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemKitRepository extends JpaRepository<ItemKit, Long> {

    @Query("SELECT ik FROM ItemKit ik JOIN FETCH ik.item WHERE ik.kit.id = :kitId")
    List<ItemKit> findAllByKitIdComItem(@Param("kitId") Long kitId);

    @Query(
            "SELECT ik FROM ItemKit ik JOIN FETCH ik.item JOIN FETCH ik.kit WHERE ik.kit.id IN :kitIds")
    List<ItemKit> findAllByKitIdInComItem(@Param("kitIds") Collection<Long> kitIds);

    @Modifying
    @Query("DELETE FROM ItemKit ik WHERE ik.kit.id = :kitId")
    void deleteAllByKitId(@Param("kitId") Long kitId);
}
