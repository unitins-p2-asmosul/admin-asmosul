package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.Item;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    boolean existsByCategoria_Id(Long categoriaId);

    Optional<Item> findByIdAndDataInativoIsNull(Long id);

    List<Item> findAllByDataInativoIsNull();

    @Query(
            "SELECT i.categoria.id, COUNT(i.id) "
                    + "FROM Item i "
                    + "WHERE i.categoria.id IN :categoriaIds AND i.dataInativo IS NULL "
                    + "GROUP BY i.categoria.id")
    List<Object[]> contarItensAtivosPorCategoriaIds(
            @Param("categoriaIds") Collection<Long> categoriaIds);
}
