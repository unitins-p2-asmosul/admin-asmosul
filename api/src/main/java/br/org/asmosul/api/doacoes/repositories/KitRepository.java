package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.Kit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface KitRepository extends JpaRepository<Kit, Long>, JpaSpecificationExecutor<Kit> {
}
