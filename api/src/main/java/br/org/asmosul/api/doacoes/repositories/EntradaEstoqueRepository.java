package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.EntradaEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaEstoqueRepository extends JpaRepository<EntradaEstoque, Long> {
}
