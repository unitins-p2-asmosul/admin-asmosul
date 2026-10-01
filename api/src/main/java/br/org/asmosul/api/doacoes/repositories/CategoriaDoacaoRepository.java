package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.CategoriaDoacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoriaDoacaoRepository
        extends JpaRepository<CategoriaDoacao, Long>, JpaSpecificationExecutor<CategoriaDoacao> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<CategoriaDoacao> findByIdAndDataInativoIsNull(Long id);

    Page<CategoriaDoacao> findAllByDataInativoIsNull(Pageable pageable);

    List<CategoriaDoacao> findAllByDataInativoIsNull();
}
