package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.models.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByIdAndDataInativoIsNull(Long id);

    Page<Categoria> findAllByDataInativoIsNull(Pageable pageable);

    List<Categoria> findAllByDataInativoIsNull();

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
