package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EnderecoArmazenamentoRepository
        extends JpaRepository<EnderecoArmazenamento, Long>, JpaSpecificationExecutor<EnderecoArmazenamento> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<EnderecoArmazenamento> findByIdAndDataInativoIsNull(Long id);

    Page<EnderecoArmazenamento> findAllByDataInativoIsNull(Pageable pageable);

    List<EnderecoArmazenamento> findAllByDataInativoIsNull();

    default boolean possuiItensVinculados(Long id) {
        return false;
    }
}
