package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
}
