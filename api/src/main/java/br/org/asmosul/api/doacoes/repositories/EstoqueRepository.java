package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByItemIdAndArmazenamentoId(Long itemId, Long armazenamentoId);
}
