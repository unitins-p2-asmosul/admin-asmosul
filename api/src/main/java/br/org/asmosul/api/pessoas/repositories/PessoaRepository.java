package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.models.Pessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    // Validação de duplicidade por CPF
    boolean existsByCpf(String cpf);

    // Validação de duplicidade de CPF ignorando o próprio ID (utilizado na edição)
    boolean existsByCpfAndIdNot(String cpf, Long id);

    // Buscar por ID apenas se estiver ativo (Soft Delete)
    Optional<Pessoa> findByIdAndDataInativoIsNull(Long id);

    // Buscar por CPF apenas se estiver ativo
    Optional<Pessoa> findByCpfAndDataInativoIsNull(String cpf);

    // Buscar por nome parcial ignorando maiúsculas/minúsculas (somente ativos)
    Page<Pessoa> findByNomeContainingIgnoreCaseAndDataInativoIsNull(String nome, Pageable pageable);

    // Buscar todos os registros ativos com paginação
    Page<Pessoa> findAllByDataInativoIsNull(Pageable pageable);

    // Buscar todos os registros (ativos e inativos) com paginação
    Page<Pessoa> findAll(Pageable pageable);
}