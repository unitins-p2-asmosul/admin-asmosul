package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.models.Categoria;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByIdAndDataInativoIsNull(Long id);

    Page<Categoria> findAllByDataInativoIsNull(Pageable pageable);

    Page<Categoria> findAllByDataInativoIsNotNull(Pageable pageable);


    List<Categoria> findAllByDataInativoIsNull();

    Page<Categoria> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Categoria> findByNomeContainingIgnoreCaseAndDataInativoIsNull(
            String nome, Pageable pageable);

    Page<Categoria> findByNomeContainingIgnoreCaseAndDataInativoIsNotNull(
        String nome, Pageable pageable);

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);

    @Query(
            "SELECT c.id, COUNT(DISTINCT p.id) "
                    + "FROM Pessoa p JOIN p.categorias c "
                    + "WHERE c.id IN :categoriaIds AND p.dataInativo IS NULL "
                    + "GROUP BY c.id")
    List<Object[]> contarPessoasAtivasPorCategoriaIds(
            @Param("categoriaIds") Collection<Long> categoriaIds);
}
