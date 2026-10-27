package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.SaidaEstoque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaidaEstoqueRepository extends JpaRepository<SaidaEstoque, Long> {

    @Query(
            "SELECT se FROM SaidaEstoque se "
                    + "JOIN FETCH se.item "
                    + "JOIN FETCH se.armazenamento "
                    + "WHERE se.saida.id = :saidaId "
                    + "ORDER BY se.id")
    List<SaidaEstoque> findAllBySaidaIdComRelacionamentos(@Param("saidaId") Long saidaId);

    @Query(
            "SELECT se FROM SaidaEstoque se "
                    + "JOIN FETCH se.item "
                    + "JOIN FETCH se.armazenamento "
                    + "WHERE se.saida.id IN :saidaIds "
                    + "ORDER BY se.id")
    List<SaidaEstoque> findAllBySaidaIdInComRelacionamentos(
            @Param("saidaIds") Collection<Long> saidaIds);

    @Query(
            "SELECT se FROM SaidaEstoque se "
                    + "JOIN FETCH se.item "
                    + "JOIN FETCH se.armazenamento "
                    + "WHERE se.id = :idSaidaEstoque AND se.saida.id = :idSaida")
    Optional<SaidaEstoque> findByIdAndSaidaIdComRelacionamentos(
            @Param("idSaidaEstoque") Long idSaidaEstoque, @Param("idSaida") Long idSaida);

    long countBySaidaId(Long saidaId);
}
