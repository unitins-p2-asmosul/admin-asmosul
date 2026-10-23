package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.EntradaEstoque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaEstoqueRepository extends JpaRepository<EntradaEstoque, Long> {

    @Query(
            "SELECT ee FROM EntradaEstoque ee "
                    + "JOIN FETCH ee.item "
                    + "JOIN FETCH ee.armazenamento "
                    + "WHERE ee.entrada.id = :entradaId "
                    + "ORDER BY ee.id")
    List<EntradaEstoque> findAllByEntradaIdComRelacionamentos(@Param("entradaId") Long entradaId);

    @Query(
            "SELECT ee FROM EntradaEstoque ee "
                    + "JOIN FETCH ee.item "
                    + "JOIN FETCH ee.armazenamento "
                    + "WHERE ee.entrada.id IN :entradaIds "
                    + "ORDER BY ee.id")
    List<EntradaEstoque> findAllByEntradaIdInComRelacionamentos(
            @Param("entradaIds") Collection<Long> entradaIds);

    @Query(
            "SELECT ee FROM EntradaEstoque ee "
                    + "JOIN FETCH ee.item "
                    + "JOIN FETCH ee.armazenamento "
                    + "WHERE ee.id = :idEntradaEstoque AND ee.entrada.id = :idEntrada")
    Optional<EntradaEstoque> findByIdAndEntradaIdComRelacionamentos(
            @Param("idEntradaEstoque") Long idEntradaEstoque,
            @Param("idEntrada") Long idEntrada);
}
