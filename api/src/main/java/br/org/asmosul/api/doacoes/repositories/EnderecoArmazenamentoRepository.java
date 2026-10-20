package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EnderecoArmazenamentoRepository
        extends JpaRepository<EnderecoArmazenamento, Long>, JpaSpecificationExecutor<EnderecoArmazenamento> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<EnderecoArmazenamento> findByIdAndDataInativoIsNull(Long id);

    Page<EnderecoArmazenamento> findAllByDataInativoIsNull(Pageable pageable);

    List<EnderecoArmazenamento> findAllByDataInativoIsNull();

    @Query("select (count(e) > 0) from Estoque e where e.armazenamento.id = :id")
    boolean possuiItensVinculados(@Param("id") Long id);
}
