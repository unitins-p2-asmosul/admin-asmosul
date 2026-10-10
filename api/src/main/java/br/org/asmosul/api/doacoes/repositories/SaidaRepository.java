package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.Saida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SaidaRepository extends JpaRepository<Saida, Long>, JpaSpecificationExecutor<Saida> {
}
