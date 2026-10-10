package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.SaidaEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaidaEstoqueRepository extends JpaRepository<SaidaEstoque, Long> {
}
