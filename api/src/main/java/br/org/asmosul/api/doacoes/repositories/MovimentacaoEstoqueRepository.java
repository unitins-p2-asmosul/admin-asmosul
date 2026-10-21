package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    @EntityGraph(attributePaths = {"item", "armazenamentoAntigo"})
    List<MovimentacaoEstoque> findAllByMovimentacaoId(Long movimentacaoId);

    @EntityGraph(attributePaths = {"item", "armazenamentoAntigo"})
    List<MovimentacaoEstoque> findAllByMovimentacaoIdIn(List<Long> movimentacaoIds);
}
