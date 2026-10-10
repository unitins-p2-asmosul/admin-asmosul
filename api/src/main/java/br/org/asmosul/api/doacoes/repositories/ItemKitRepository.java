package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.ItemKit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemKitRepository extends JpaRepository<ItemKit, Long> {
}
