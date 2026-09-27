package br.org.asmosul.api.acesso.repositories;

import br.org.asmosul.api.acesso.models.Conta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByNomeUsuario(String nomeUsuario);

    Optional<Conta> findByNomeUsuarioAndDataInativoIsNull(String nomeUsuario);
}
