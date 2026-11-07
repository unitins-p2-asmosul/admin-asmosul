package br.org.asmosul.api.relatorios.repositories;

import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.relatorios.repositories.projections.AgrupamentoContagemProjection;
import br.org.asmosul.api.relatorios.repositories.projections.PessoaComorbidadeProjection;
import br.org.asmosul.api.relatorios.repositories.projections.TotaisPessoasProjection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatorioPessoaRepository extends JpaRepository<Pessoa, Long> {

    @Query(value = """
        SELECT
            SUM(p.data_inativo IS NULL) AS totalAtivo,
            SUM(p.eh_beneficiario = 1 AND p.data_inativo IS NULL) AS totalBeneficiario,
            SUM(p.eh_doador = 1 AND p.data_inativo IS NULL) AS totalDoador,
            SUM(p.data_inativo IS NOT NULL) AS totalInativo,
            COUNT(p.id) AS totalCadastrosGerais
        FROM pessoa p
    """, nativeQuery = true)
    TotaisPessoasProjection buscarTotaisCards();

    @Query(value = """
        SELECT
            COALESCE(p.sexo, 'NAO_INFORMADO') AS chave,
            COUNT(p.id) AS total
        FROM pessoa p
        WHERE p.data_inativo IS NULL
        GROUP BY p.sexo
        ORDER BY total DESC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> agruparPorSexo();

    @Query(value = """
        SELECT
            COALESCE(p.escolaridade, 'NAO_INFORMADO') AS chave,
            COUNT(p.id) AS total
        FROM pessoa p
        WHERE p.data_inativo IS NULL
        GROUP BY p.escolaridade
        ORDER BY total DESC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> agruparPorEscolaridade();

    @Query(value = """
        SELECT
            COALESCE(p.faixa_renda, 'NAO_INFORMADO') AS chave,
            COUNT(p.id) AS total
        FROM pessoa p
        WHERE p.data_inativo IS NULL
        GROUP BY p.faixa_renda
        ORDER BY total DESC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> agruparPorFaixaRenda();

    // -- Comorbidades -- //

    @Query(value = """
        SELECT
            c.nome AS chave,
            COUNT(pc.id_pessoa) AS total
        FROM comorbidade c
        LEFT JOIN pessoa_comorbidade pc ON pc.id_comorbidade = c.id
        LEFT JOIN pessoa p ON p.id = pc.id_pessoa AND p.data_inativo IS NULL
        WHERE c.data_inativo IS NULL
        GROUP BY c.id, c.nome
        ORDER BY total DESC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarDistribuicaoComorbidades();

    @Query(value = """
    SELECT DISTINCT p
    FROM Pessoa p
    JOIN p.comorbidades c
    WHERE p.dataInativo IS NULL
      AND c.dataInativo IS NULL
      AND (
          :termoBusca IS NULL
          OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          OR p.cpfCnpj LIKE CONCAT('%', :termoBusca, '%')
      )
      AND (
          (:comorbidadeIds) IS NULL
          OR c.id IN (:comorbidadeIds)
      )
    """,
        countQuery = """
    SELECT COUNT(DISTINCT p.id)
    FROM Pessoa p
    JOIN p.comorbidades c
    WHERE p.dataInativo IS NULL
      AND c.dataInativo IS NULL
      AND (
          :termoBusca IS NULL
          OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          OR p.cpfCnpj LIKE CONCAT('%', :termoBusca, '%')
      )
      AND (
          (:comorbidadeIds) IS NULL
          OR c.id IN (:comorbidadeIds)
      )
    """)
    Page<Pessoa> listarPessoasComorbidades(
        @Param("termoBusca") String termoBusca,
        @Param("comorbidadeIds") List<Long> comorbidadeIds,
        Pageable pageable
    );

    // --- Categorias --- //
    @Query(value = """
        SELECT
            c.nome AS chave,
            COUNT(pc.id_pessoa) AS total
        FROM categoria c
        LEFT JOIN pessoa_categoria pc ON pc.id_categoria = c.id
        LEFT JOIN pessoa p ON p.id = pc.id_pessoa AND p.data_inativo IS NULL
        WHERE c.data_inativo IS NULL
        GROUP BY c.id, c.nome
        ORDER BY total DESC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarDistribuicaoCategoriasPessoa();
}
