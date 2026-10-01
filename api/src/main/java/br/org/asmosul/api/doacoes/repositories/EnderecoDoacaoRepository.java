package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.EnderecoDoacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EnderecoDoacaoRepository
        extends JpaRepository<EnderecoDoacao, Long>, JpaSpecificationExecutor<EnderecoDoacao> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<EnderecoDoacao> findByIdAndDataInativoIsNull(Long id);

    Page<EnderecoDoacao> findAllByDataInativoIsNull(Pageable pageable);

    List<EnderecoDoacao> findAllByDataInativoIsNull();

    default boolean possuiItensVinculados(Long id) {
        return false;
    }
}
