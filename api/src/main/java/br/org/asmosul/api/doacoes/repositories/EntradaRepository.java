package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Long>, JpaSpecificationExecutor<Entrada> {
}
