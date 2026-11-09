package br.org.asmosul.api.relatorios.repositories;

import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.relatorios.repositories.projections.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatorioDoacoesRepository extends JpaRepository<Item, Long> {

    @Query(value = """
        SELECT i.nome AS chave, IFNULL(SUM(se.quantidade), 0) AS total
        FROM item i
        INNER JOIN saida_estoque se ON se.id_item = i.id
        WHERE i.data_inativo IS NULL
        GROUP BY i.id, i.nome
        ORDER BY total DESC
        LIMIT 3
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarTop3ItensMaisSaem();

    @Query(value = """
        SELECT i.nome AS chave, IFNULL(SUM(se.quantidade), 0) AS total
        FROM item i
        LEFT JOIN saida_estoque se ON se.id_item = i.id
        WHERE i.data_inativo IS NULL
        GROUP BY i.id, i.nome
        ORDER BY total ASC
        LIMIT 3
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarTop3ItensMenosSaem();

    @Query(value = """
        SELECT i.nome AS chave, IFNULL(SUM(ee.quantidade), 0) AS total
        FROM item i
        INNER JOIN entrada_estoque ee ON ee.id_item = i.id
        WHERE i.data_inativo IS NULL
        GROUP BY i.id, i.nome
        ORDER BY total DESC
        LIMIT 3
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarTop3ItensMaisEntram();

    @Query(value = """
        SELECT i.nome AS chave, IFNULL(SUM(ee.quantidade), 0) AS total
        FROM item i
        LEFT JOIN entrada_estoque ee ON ee.id_item = i.id
        WHERE i.data_inativo IS NULL
        GROUP BY i.id, i.nome
        ORDER BY total ASC
        LIMIT 3
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarTop3ItensMenosEntram();

    @Query(value = """
        SELECT
            i.id AS itemId,
            i.nome AS itemNome,
            c.nome AS categoriaNome,
            i.unidade_medida AS unidadeMedida,
            IFNULL(e.saldo_estoque, 0) AS saldoEstoque,
            IFNULL(mov.total_entrada, 0) AS totalEntrada,
            IFNULL(mov.total_saida, 0) AS totalSaida
        FROM item i
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        LEFT JOIN (
            SELECT id_item, SUM(quantidade) AS saldo_estoque
            FROM estoque
            GROUP BY id_item
        ) e ON e.id_item = i.id
        LEFT JOIN (
            SELECT
                id_item,
                SUM(entrada) AS total_entrada,
                SUM(saida) AS total_saida
            FROM (
                SELECT id_item, quantidade AS entrada, 0 AS saida FROM entrada_estoque
                UNION ALL
                SELECT id_item, 0 AS entrada, quantidade AS saida FROM saida_estoque
            ) u
            GROUP BY id_item
        ) mov ON mov.id_item = i.id
        WHERE (:incluirInativos = true OR i.data_inativo IS NULL)
          AND (:nomeItem IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :nomeItem, '%')))
          AND (:categoriaId IS NULL OR c.id = :categoriaId)
          AND (:unidadeMedida IS NULL OR i.unidade_medida = :unidadeMedida)
          AND (:saldoMin IS NULL OR IFNULL(e.saldo_estoque, 0) >= :saldoMin)
          AND (:saldoMax IS NULL OR IFNULL(e.saldo_estoque, 0) <= :saldoMax)
          AND (:entradasMin IS NULL OR IFNULL(mov.total_entrada, 0) >= :entradasMin)
          AND (:entradasMax IS NULL OR IFNULL(mov.total_entrada, 0) <= :entradasMax)
          AND (:saidasMin IS NULL OR IFNULL(mov.total_saida, 0) >= :saidasMin)
          AND (:saidasMax IS NULL OR IFNULL(mov.total_saida, 0) <= :saidasMax)
        ORDER BY
          CASE WHEN :sortField = 'nomeItem' AND :sortDirection = 'ASC' THEN i.nome END ASC,
          CASE WHEN :sortField = 'nomeItem' AND :sortDirection = 'DESC' THEN i.nome END DESC,
          CASE WHEN :sortField = 'nomeCategoria' AND :sortDirection = 'ASC' THEN c.nome END ASC,
          CASE WHEN :sortField = 'nomeCategoria' AND :sortDirection = 'DESC' THEN c.nome END DESC,
          CASE WHEN :sortField = 'unidadeMedida' AND :sortDirection = 'ASC' THEN i.unidade_medida END ASC,
          CASE WHEN :sortField = 'unidadeMedida' AND :sortDirection = 'DESC' THEN i.unidade_medida END DESC,
          CASE WHEN :sortField = 'saldoEstoque' AND :sortDirection = 'ASC' THEN IFNULL(e.saldo_estoque, 0) END ASC,
          CASE WHEN :sortField = 'saldoEstoque' AND :sortDirection = 'DESC' THEN IFNULL(e.saldo_estoque, 0) END DESC,
          CASE WHEN :sortField = 'totalEntradas' AND :sortDirection = 'ASC' THEN IFNULL(mov.total_entrada, 0) END ASC,
          CASE WHEN :sortField = 'totalEntradas' AND :sortDirection = 'DESC' THEN IFNULL(mov.total_entrada, 0) END DESC,
          CASE WHEN :sortField = 'totalSaidas' AND :sortDirection = 'ASC' THEN IFNULL(mov.total_saida, 0) END ASC,
          CASE WHEN :sortField = 'totalSaidas' AND :sortDirection = 'DESC' THEN IFNULL(mov.total_saida, 0) END DESC,
          i.nome ASC, i.id ASC
    """,
        countQuery = """
        SELECT COUNT(i.id)
        FROM item i
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        LEFT JOIN (
            SELECT id_item, SUM(quantidade) AS saldo_estoque
            FROM estoque
            GROUP BY id_item
        ) e ON e.id_item = i.id
        LEFT JOIN (
            SELECT
                id_item,
                SUM(entrada) AS total_entrada,
                SUM(saida) AS total_saida
            FROM (
                SELECT id_item, quantidade AS entrada, 0 AS saida FROM entrada_estoque
                UNION ALL
                SELECT id_item, 0 AS entrada, quantidade AS saida FROM saida_estoque
            ) u
            GROUP BY id_item
        ) mov ON mov.id_item = i.id
        WHERE (:incluirInativos = true OR i.data_inativo IS NULL)
          AND (:nomeItem IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :nomeItem, '%')))
          AND (:categoriaId IS NULL OR c.id = :categoriaId)
          AND (:unidadeMedida IS NULL OR i.unidade_medida = :unidadeMedida)
          AND (:saldoMin IS NULL OR IFNULL(e.saldo_estoque, 0) >= :saldoMin)
          AND (:saldoMax IS NULL OR IFNULL(e.saldo_estoque, 0) <= :saldoMax)
          AND (:entradasMin IS NULL OR IFNULL(mov.total_entrada, 0) >= :entradasMin)
          AND (:entradasMax IS NULL OR IFNULL(mov.total_entrada, 0) <= :entradasMax)
          AND (:saidasMin IS NULL OR IFNULL(mov.total_saida, 0) >= :saidasMin)
          AND (:saidasMax IS NULL OR IFNULL(mov.total_saida, 0) <= :saidasMax)
    """, nativeQuery = true)
    Page<GiroEstoqueProjection> buscarGiroEstoque(
        @Param("nomeItem") String nomeItem,
        @Param("categoriaId") Long categoriaId,
        @Param("unidadeMedida") String unidadeMedida,
        @Param("saldoMin") Integer saldoMin,
        @Param("saldoMax") Integer saldoMax,
        @Param("entradasMin") Integer entradasMin,
        @Param("entradasMax") Integer entradasMax,
        @Param("saidasMin") Integer saidasMin,
        @Param("saidasMax") Integer saidasMax,
        @Param("incluirInativos") boolean incluirInativos,
        @Param("sortField") String sortField,
        @Param("sortDirection") String sortDirection,
        Pageable pageable
    );

    // --- Locais de Armazenamento ---

    @Query(value = """
        SELECT
            COUNT(DISTINCT ea.id) AS totalLocais,
            IFNULL(SUM(e.quantidade), 0) AS volumeFisicoTotal,
            COUNT(DISTINCT CASE WHEN e.quantidade > 0 THEN e.id_armazenamento END) AS locaisEmUso
        FROM endereco_armazenamento ea
        LEFT JOIN estoque e ON e.id_armazenamento = ea.id
        WHERE ea.data_inativo IS NULL
    """, nativeQuery = true)
    LocaisArmazenamentoProjection buscarTotaisLocaisArmazenamento();

    @Query(value = """
        SELECT
            ea.nome AS chave,
            IFNULL(SUM(e.quantidade), 0) AS total
        FROM endereco_armazenamento ea
        LEFT JOIN estoque e ON e.id_armazenamento = ea.id
        WHERE ea.data_inativo IS NULL
        GROUP BY ea.id, ea.nome
        HAVING total > 0
        ORDER BY total DESC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarDistribuicaoVolumePorLocal();

    // --- Inventário Consolidado de Estoque ---

    @Query(value = """
        SELECT
            IFNULL(SUM(e.quantidade), 0) AS saldoFisicoGeral,
            IFNULL(SUM(e.quantidade * i.preco_unitario), 0.00) AS valorTotalEstimado
        FROM estoque e
        INNER JOIN item i ON i.id = e.id_item
        WHERE i.data_inativo IS NULL
    """, nativeQuery = true)
    TotalEstoqueGeralProjection buscarTotaisEstoqueGeral();

    @Query(value = """
        SELECT
            e.id AS idEstoque,
            i.id AS idItem,
            i.nome AS itemNome,
            c.nome AS categoriaNome,
            ea.nome AS localArmazenamentoNome,
            e.quantidade AS quantidade,
            i.unidade_medida AS unidadeMedida,
            i.preco_unitario AS valorUnitario,
            (e.quantidade * i.preco_unitario) AS valorTotal
        FROM estoque e
        INNER JOIN item i ON i.id = e.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        INNER JOIN endereco_armazenamento ea ON ea.id = e.id_armazenamento
        WHERE i.data_inativo IS NULL
          AND (:nomeItem IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :nomeItem, '%')))
          AND (:filtrarCategorias = false OR c.id IN (:categoriasIds))
          AND (:filtrarLocais = false OR ea.id IN (:locaisIds))
          AND (:quantidadeMinima IS NULL OR e.quantidade >= :quantidadeMinima)
          AND (:valorMinimo IS NULL OR (e.quantidade * i.preco_unitario) >= :valorMinimo)
        ORDER BY
          CASE WHEN :sortField = 'nomeItem' AND :sortDirection = 'ASC' THEN i.nome END ASC,
          CASE WHEN :sortField = 'nomeItem' AND :sortDirection = 'DESC' THEN i.nome END DESC,
          CASE WHEN :sortField = 'nomeCategoria' AND :sortDirection = 'ASC' THEN c.nome END ASC,
          CASE WHEN :sortField = 'nomeCategoria' AND :sortDirection = 'DESC' THEN c.nome END DESC,
          CASE WHEN :sortField = 'localArmazenamento' AND :sortDirection = 'ASC' THEN ea.nome END ASC,
          CASE WHEN :sortField = 'localArmazenamento' AND :sortDirection = 'DESC' THEN ea.nome END DESC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'ASC' THEN e.quantidade END ASC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'DESC' THEN e.quantidade END DESC,
          CASE WHEN :sortField = 'valorUnitarioEstimado' AND :sortDirection = 'ASC' THEN i.preco_unitario END ASC,
          CASE WHEN :sortField = 'valorUnitarioEstimado' AND :sortDirection = 'DESC' THEN i.preco_unitario END DESC,
          CASE WHEN :sortField = 'valorTotalEstimado' AND :sortDirection = 'ASC' THEN (e.quantidade * i.preco_unitario) END ASC,
          CASE WHEN :sortField = 'valorTotalEstimado' AND :sortDirection = 'DESC' THEN (e.quantidade * i.preco_unitario) END DESC,
          i.nome ASC, ea.nome ASC, e.id ASC
    """,
        countQuery = """
        SELECT COUNT(e.id)
        FROM estoque e
        INNER JOIN item i ON i.id = e.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        INNER JOIN endereco_armazenamento ea ON ea.id = e.id_armazenamento
        WHERE i.data_inativo IS NULL
          AND (:nomeItem IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :nomeItem, '%')))
          AND (:filtrarCategorias = false OR c.id IN (:categoriasIds))
          AND (:filtrarLocais = false OR ea.id IN (:locaisIds))
          AND (:quantidadeMinima IS NULL OR e.quantidade >= :quantidadeMinima)
          AND (:valorMinimo IS NULL OR (e.quantidade * i.preco_unitario) >= :valorMinimo)
    """, nativeQuery = true)
    Page<InventarioEstoqueProjection> listarInventarioEstoque(
        @Param("nomeItem") String nomeItem,
        @Param("categoriasIds") List<Long> categoriasIds,
        @Param("filtrarCategorias") boolean filtrarCategorias,
        @Param("locaisIds") List<Long> locaisIds,
        @Param("filtrarLocais") boolean filtrarLocais,
        @Param("quantidadeMinima") Integer quantidadeMinima,
        @Param("valorMinimo") BigDecimal valorMinimo,
        @Param("sortField") String sortField,
        @Param("sortDirection") String sortDirection,
        Pageable pageable
    );

    // --- Categorias de Itens ---

    @Query(value = """
        SELECT
            c.nome AS chave,
            COUNT(i.id) AS total
        FROM categoria_item c
        LEFT JOIN item i ON i.id_categoria = c.id AND i.data_inativo IS NULL
        WHERE c.data_inativo IS NULL
        GROUP BY c.id, c.nome
        ORDER BY total DESC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarDistribuicaoCategoriasItens();

    @Query(value = """
        SELECT
            c.id AS idCategoria,
            c.nome AS nome,
            c.descricao AS descricao,
            COUNT(DISTINCT i.id) AS itensDistintos,
            IFNULL(SUM(e.quantidade), 0) AS saldoFisico
        FROM categoria_item c
        LEFT JOIN item i ON i.id_categoria = c.id AND i.data_inativo IS NULL
        LEFT JOIN estoque e ON e.id_item = i.id
        WHERE c.data_inativo IS NULL
          AND (
            :termoBusca IS NULL
            OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(c.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          )
        GROUP BY c.id, c.nome, c.descricao
        ORDER BY
          CASE WHEN :sortField = 'nomeCategoria' AND :sortDirection = 'ASC' THEN c.nome END ASC,
          CASE WHEN :sortField = 'nomeCategoria' AND :sortDirection = 'DESC' THEN c.nome END DESC,
          CASE WHEN :sortField = 'itensDistintos' AND :sortDirection = 'ASC' THEN COUNT(DISTINCT i.id) END ASC,
          CASE WHEN :sortField = 'itensDistintos' AND :sortDirection = 'DESC' THEN COUNT(DISTINCT i.id) END DESC,
          CASE WHEN :sortField = 'saldoFisico' AND :sortDirection = 'ASC' THEN IFNULL(SUM(e.quantidade), 0) END ASC,
          CASE WHEN :sortField = 'saldoFisico' AND :sortDirection = 'DESC' THEN IFNULL(SUM(e.quantidade), 0) END DESC,
          c.nome ASC, c.id ASC
    """,
        countQuery = """
        SELECT COUNT(c.id)
        FROM categoria_item c
        WHERE c.data_inativo IS NULL
          AND (
            :termoBusca IS NULL
            OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(c.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          )
    """, nativeQuery = true)
    Page<CategoriaItemResumoProjection> listarTabelaCategorias(
        @Param("termoBusca") String termoBusca,
        @Param("sortField") String sortField,
        @Param("sortDirection") String sortDirection,
        Pageable pageable
    );

    // --- Financeiro ---

    @Query(value = """
        SELECT
            IFNULL((
                SELECT SUM(se.quantidade * i.preco_unitario)
                FROM saida_estoque se
                INNER JOIN saida s ON s.id = se.id_saida
                INNER JOIN item i ON i.id = se.id_item
                WHERE (:dataInicio IS NULL OR s.data_hora >= :dataInicio)
                  AND (:dataFim IS NULL OR s.data_hora <= :dataFim)
            ), 0.00) AS totalSaidas,
            IFNULL((
                SELECT SUM(ee.quantidade * i.preco_unitario)
                FROM entrada_estoque ee
                INNER JOIN entrada e ON e.id = ee.id_entrada
                INNER JOIN item i ON i.id = ee.id_item
                WHERE (:dataInicio IS NULL OR e.data_hora >= :dataInicio)
                  AND (:dataFim IS NULL OR e.data_hora <= :dataFim)
            ), 0.00) AS totalEntradas
    """, nativeQuery = true)
    BalancoFinanceiroProjection buscarBalancoFinanceiro(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    @Query(value = """
        SELECT
            i.id AS itemId,
            i.nome AS itemNome,
            c.nome AS categoriaNome,
            i.unidade_medida AS unidadeMedida,
            SUM(se.quantidade) AS quantidadeTotal,
            AVG(i.preco_unitario) AS precoUnitarioMedio,
            SUM(se.quantidade * i.preco_unitario) AS valorTotalFinanceiro
        FROM saida_estoque se
        INNER JOIN saida s ON s.id = se.id_saida
        INNER JOIN item i ON i.id = se.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        WHERE (:dataInicio IS NULL OR s.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR s.data_hora <= :dataFim)
          AND (:termoItem IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoItem, '%')))
          AND (:termoCategoria IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termoCategoria, '%')))
        GROUP BY i.id, i.nome, c.nome, i.unidade_medida
        ORDER BY
          CASE WHEN :sortField = 'itemNome' AND :sortDirection = 'ASC' THEN i.nome END ASC,
          CASE WHEN :sortField = 'itemNome' AND :sortDirection = 'DESC' THEN i.nome END DESC,
          CASE WHEN :sortField = 'categoriaNome' AND :sortDirection = 'ASC' THEN c.nome END ASC,
          CASE WHEN :sortField = 'categoriaNome' AND :sortDirection = 'DESC' THEN c.nome END DESC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'ASC' THEN SUM(se.quantidade) END ASC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'DESC' THEN SUM(se.quantidade) END DESC,
          CASE WHEN :sortField = 'precoUnitarioMedio' AND :sortDirection = 'ASC' THEN AVG(i.preco_unitario) END ASC,
          CASE WHEN :sortField = 'precoUnitarioMedio' AND :sortDirection = 'DESC' THEN AVG(i.preco_unitario) END DESC,
          CASE WHEN :sortField = 'valorTotalFinanceiro' AND :sortDirection = 'ASC' THEN SUM(se.quantidade * i.preco_unitario) END ASC,
          CASE WHEN :sortField = 'valorTotalFinanceiro' AND :sortDirection = 'DESC' THEN SUM(se.quantidade * i.preco_unitario) END DESC,
          i.nome ASC, i.id ASC
    """,
        countQuery = """
        SELECT COUNT(DISTINCT se.id_item)
        FROM saida_estoque se
        INNER JOIN saida s ON s.id = se.id_saida
        INNER JOIN item i ON i.id = se.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        WHERE (:dataInicio IS NULL OR s.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR s.data_hora <= :dataFim)
          AND (:termoItem IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoItem, '%')))
          AND (:termoCategoria IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termoCategoria, '%')))
    """, nativeQuery = true)
    Page<ItemMovimentacaoFinanceiraProjection> listarFinanceiroSaidas(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("termoItem") String termoItem,
        @Param("termoCategoria") String termoCategoria,
        @Param("sortField") String sortField,
        @Param("sortDirection") String sortDirection,
        Pageable pageable
    );

    @Query(value = """
        SELECT
            i.id AS itemId,
            i.nome AS itemNome,
            c.nome AS categoriaNome,
            i.unidade_medida AS unidadeMedida,
            SUM(ee.quantidade) AS quantidadeTotal,
            AVG(i.preco_unitario) AS precoUnitarioMedio,
            SUM(ee.quantidade * i.preco_unitario) AS valorTotalFinanceiro
        FROM entrada_estoque ee
        INNER JOIN entrada e ON e.id = ee.id_entrada
        INNER JOIN item i ON i.id = ee.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        WHERE (:dataInicio IS NULL OR e.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR e.data_hora <= :dataFim)
          AND (:termoItem IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoItem, '%')))
          AND (:termoCategoria IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termoCategoria, '%')))
        GROUP BY i.id, i.nome, c.nome, i.unidade_medida
        ORDER BY
          CASE WHEN :sortField = 'itemNome' AND :sortDirection = 'ASC' THEN i.nome END ASC,
          CASE WHEN :sortField = 'itemNome' AND :sortDirection = 'DESC' THEN i.nome END DESC,
          CASE WHEN :sortField = 'categoriaNome' AND :sortDirection = 'ASC' THEN c.nome END ASC,
          CASE WHEN :sortField = 'categoriaNome' AND :sortDirection = 'DESC' THEN c.nome END DESC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'ASC' THEN SUM(ee.quantidade) END ASC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'DESC' THEN SUM(ee.quantidade) END DESC,
          CASE WHEN :sortField = 'precoUnitarioMedio' AND :sortDirection = 'ASC' THEN AVG(i.preco_unitario) END ASC,
          CASE WHEN :sortField = 'precoUnitarioMedio' AND :sortDirection = 'DESC' THEN AVG(i.preco_unitario) END DESC,
          CASE WHEN :sortField = 'valorTotalFinanceiro' AND :sortDirection = 'ASC' THEN SUM(ee.quantidade * i.preco_unitario) END ASC,
          CASE WHEN :sortField = 'valorTotalFinanceiro' AND :sortDirection = 'DESC' THEN SUM(ee.quantidade * i.preco_unitario) END DESC,
          i.nome ASC, i.id ASC
    """,
        countQuery = """
        SELECT COUNT(DISTINCT ee.id_item)
        FROM entrada_estoque ee
        INNER JOIN entrada e ON e.id = ee.id_entrada
        INNER JOIN item i ON i.id = ee.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        WHERE (:dataInicio IS NULL OR e.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR e.data_hora <= :dataFim)
          AND (:termoItem IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoItem, '%')))
          AND (:termoCategoria IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termoCategoria, '%')))
    """, nativeQuery = true)
    Page<ItemMovimentacaoFinanceiraProjection> listarFinanceiroEntradas(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("termoItem") String termoItem,
        @Param("termoCategoria") String termoCategoria,
        @Param("sortField") String sortField,
        @Param("sortDirection") String sortDirection,
        Pageable pageable
    );

    // --- Relatório de Saídas ---

    @Query(value = """
        SELECT
            IFNULL(SUM(se.quantidade), 0) AS saldoFisicoGeral,
            IFNULL(SUM(se.quantidade * i.preco_unitario), 0.00) AS valorTotalEstimado
        FROM saida_estoque se
        INNER JOIN saida s ON s.id = se.id_saida
        INNER JOIN item i ON i.id = se.id_item
        WHERE (:dataInicio IS NULL OR s.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR s.data_hora <= :dataFim)
          AND (:recebedorId IS NULL OR s.id_recebedor = :recebedorId)
          AND (:localArmazenamentoId IS NULL OR se.id_armazenamento = :localArmazenamentoId)
          AND (:categoriaItemId IS NULL OR i.id_categoria = :categoriaItemId)
          AND (
            :termoBusca IS NULL
            OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(s.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          )
    """, nativeQuery = true)
    TotalEstoqueGeralProjection buscarTotaisSaidas(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("recebedorId") Long recebedorId,
        @Param("localArmazenamentoId") Long localArmazenamentoId,
        @Param("categoriaItemId") Long categoriaItemId,
        @Param("termoBusca") String termoBusca
    );

    @Query(value = """
        SELECT
            LPAD(MONTH(s.data_hora), 2, '0') AS chave,
            IFNULL(SUM(se.quantidade), 0) AS total
        FROM saida_estoque se
        INNER JOIN saida s ON s.id = se.id_saida
        WHERE YEAR(s.data_hora) = :ano
        GROUP BY MONTH(s.data_hora)
        ORDER BY MONTH(s.data_hora) ASC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarSaidasPorMes(@Param("ano") int ano);

    @Query(value = """
        SELECT
            CAST(YEAR(s.data_hora) AS CHAR) AS chave,
            IFNULL(SUM(se.quantidade), 0) AS total
        FROM saida_estoque se
        INNER JOIN saida s ON s.id = se.id_saida
        GROUP BY YEAR(s.data_hora)
        ORDER BY YEAR(s.data_hora) ASC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarSaidasPorAno();

    @Query(value = """
        SELECT
            p.id AS idRecebedor,
            p.nome AS nomeRecebedor,
            IFNULL(p.quantidade_coabitante, 1) AS quantidadeCoabitantes,
            COUNT(DISTINCT s.id) AS totalEntregasRecebidas,
            IFNULL(SUM(se.quantidade), 0) AS totalItensRecebidos
        FROM pessoa p
        INNER JOIN saida s ON s.id_recebedor = p.id
        INNER JOIN saida_estoque se ON se.id_saida = s.id
        WHERE p.data_inativo IS NULL
          AND (:dataInicio IS NULL OR s.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR s.data_hora <= :dataFim)
        GROUP BY p.id, p.nome, p.quantidade_coabitante
        ORDER BY totalItensRecebidos DESC
        LIMIT 10
    """, nativeQuery = true)
    List<RankingRecebedorProjection> buscarTopQuemMaisRecebe(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    @Query(value = """
        SELECT
            p.id AS idRecebedor,
            p.nome AS nomeRecebedor,
            IFNULL(p.quantidade_coabitante, 1) AS quantidadeCoabitantes,
            COUNT(DISTINCT s.id) AS totalEntregasRecebidas,
            IFNULL(SUM(se.quantidade), 0) AS totalItensRecebidos
        FROM pessoa p
        LEFT JOIN saida s ON s.id_recebedor = p.id
          AND (:dataInicio IS NULL OR s.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR s.data_hora <= :dataFim)
        LEFT JOIN saida_estoque se ON se.id_saida = s.id
        WHERE p.eh_beneficiario = 1
          AND p.data_inativo IS NULL
        GROUP BY p.id, p.nome, p.quantidade_coabitante
        ORDER BY totalItensRecebidos ASC, totalEntregasRecebidas ASC, p.nome ASC
        LIMIT 10
    """, nativeQuery = true)
    List<RankingRecebedorProjection> buscarTopQuemMenosRecebe(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    @Query(value = """
        SELECT
            i.nome AS itemNome,
            c.nome AS categoriaNome,
            se.quantidade AS quantidade,
            i.preco_unitario AS precoUnitario,
            (se.quantidade * i.preco_unitario) AS valorEstimado,
            ea.nome AS enderecoArmazenamentoNome,
            p.nome AS recebedorNome,
            p.cpf_cnpj AS cpfCnpj,
            s.data_hora AS dataHora,
            s.descricao AS descricao
        FROM saida_estoque se
        INNER JOIN saida s ON s.id = se.id_saida
        INNER JOIN item i ON i.id = se.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        INNER JOIN endereco_armazenamento ea ON ea.id = se.id_armazenamento
        INNER JOIN pessoa p ON p.id = s.id_recebedor
        WHERE (:dataInicio IS NULL OR s.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR s.data_hora <= :dataFim)
          AND (:recebedorId IS NULL OR s.id_recebedor = :recebedorId)
          AND (:localArmazenamentoId IS NULL OR se.id_armazenamento = :localArmazenamentoId)
          AND (:categoriaItemId IS NULL OR i.id_categoria = :categoriaItemId)
          AND (
            :termoBusca IS NULL
            OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(s.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          )
        ORDER BY
          CASE WHEN :sortField = 'dataHora' AND :sortDirection = 'ASC' THEN s.data_hora END ASC,
          CASE WHEN :sortField = 'dataHora' AND :sortDirection = 'DESC' THEN s.data_hora END DESC,
          CASE WHEN :sortField = 'itemNome' AND :sortDirection = 'ASC' THEN i.nome END ASC,
          CASE WHEN :sortField = 'itemNome' AND :sortDirection = 'DESC' THEN i.nome END DESC,
          CASE WHEN :sortField = 'categoriaNome' AND :sortDirection = 'ASC' THEN c.nome END ASC,
          CASE WHEN :sortField = 'categoriaNome' AND :sortDirection = 'DESC' THEN c.nome END DESC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'ASC' THEN se.quantidade END ASC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'DESC' THEN se.quantidade END DESC,
          CASE WHEN :sortField = 'precoUnitario' AND :sortDirection = 'ASC' THEN i.preco_unitario END ASC,
          CASE WHEN :sortField = 'precoUnitario' AND :sortDirection = 'DESC' THEN i.preco_unitario END DESC,
          CASE WHEN :sortField = 'valorEstimado' AND :sortDirection = 'ASC' THEN (se.quantidade * i.preco_unitario) END ASC,
          CASE WHEN :sortField = 'valorEstimado' AND :sortDirection = 'DESC' THEN (se.quantidade * i.preco_unitario) END DESC,
          CASE WHEN :sortField = 'localArmazenamento' AND :sortDirection = 'ASC' THEN ea.nome END ASC,
          CASE WHEN :sortField = 'localArmazenamento' AND :sortDirection = 'DESC' THEN ea.nome END DESC,
          CASE WHEN :sortField = 'recebedorNome' AND :sortDirection = 'ASC' THEN p.nome END ASC,
          CASE WHEN :sortField = 'recebedorNome' AND :sortDirection = 'DESC' THEN p.nome END DESC,
          s.data_hora DESC, se.id DESC
    """,
        countQuery = """
        SELECT COUNT(se.id)
        FROM saida_estoque se
        INNER JOIN saida s ON s.id = se.id_saida
        INNER JOIN item i ON i.id = se.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        INNER JOIN endereco_armazenamento ea ON ea.id = se.id_armazenamento
        INNER JOIN pessoa p ON p.id = s.id_recebedor
        WHERE (:dataInicio IS NULL OR s.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR s.data_hora <= :dataFim)
          AND (:recebedorId IS NULL OR s.id_recebedor = :recebedorId)
          AND (:localArmazenamentoId IS NULL OR se.id_armazenamento = :localArmazenamentoId)
          AND (:categoriaItemId IS NULL OR i.id_categoria = :categoriaItemId)
          AND (
            :termoBusca IS NULL
            OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(s.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          )
    """, nativeQuery = true)
    Page<SaidaDiscriminadaTabelaProjection> listarSaidasDiscriminadas(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("recebedorId") Long recebedorId,
        @Param("localArmazenamentoId") Long localArmazenamentoId,
        @Param("categoriaItemId") Long categoriaItemId,
        @Param("termoBusca") String termoBusca,
        @Param("sortField") String sortField,
        @Param("sortDirection") String sortDirection,
        Pageable pageable
    );

    // --- Relatório de Entradas ---

    @Query(value = """
        SELECT
            IFNULL(SUM(ee.quantidade), 0) AS saldoFisicoGeral,
            IFNULL(SUM(ee.quantidade * i.preco_unitario), 0.00) AS valorTotalEstimado
        FROM entrada_estoque ee
        INNER JOIN entrada e ON e.id = ee.id_entrada
        INNER JOIN item i ON i.id = ee.id_item
        WHERE (:dataInicio IS NULL OR e.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR e.data_hora <= :dataFim)
          AND (:doadorId IS NULL OR e.id_doador = :doadorId)
          AND (:localArmazenamentoId IS NULL OR ee.id_armazenamento = :localArmazenamentoId)
          AND (:categoriaItemId IS NULL OR i.id_categoria = :categoriaItemId)
          AND (
            :termoBusca IS NULL
            OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(e.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          )
    """, nativeQuery = true)
    TotalEstoqueGeralProjection buscarTotaisEntradas(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("doadorId") Long doadorId,
        @Param("localArmazenamentoId") Long localArmazenamentoId,
        @Param("categoriaItemId") Long categoriaItemId,
        @Param("termoBusca") String termoBusca
    );

    @Query(value = """
        SELECT
            LPAD(MONTH(e.data_hora), 2, '0') AS chave,
            IFNULL(SUM(ee.quantidade), 0) AS total
        FROM entrada_estoque ee
        INNER JOIN entrada e ON e.id = ee.id_entrada
        WHERE YEAR(e.data_hora) = :ano
        GROUP BY MONTH(e.data_hora)
        ORDER BY MONTH(e.data_hora) ASC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarEntradasPorMes(@Param("ano") int ano);

    @Query(value = """
        SELECT
            CAST(YEAR(e.data_hora) AS CHAR) AS chave,
            IFNULL(SUM(ee.quantidade), 0) AS total
        FROM entrada_estoque ee
        INNER JOIN entrada e ON e.id = ee.id_entrada
        GROUP BY YEAR(e.data_hora)
        ORDER BY YEAR(e.data_hora) ASC
    """, nativeQuery = true)
    List<AgrupamentoContagemProjection> buscarEntradasPorAno();

    @Query(value = """
        SELECT
            p.id AS idDoador,
            p.nome AS nomeDoador,
            COUNT(DISTINCT e.id) AS totalDoacoesRealizadas,
            IFNULL(SUM(ee.quantidade), 0) AS totalItensDoados
        FROM pessoa p
        INNER JOIN entrada e ON e.id_doador = p.id
        INNER JOIN entrada_estoque ee ON ee.id_entrada = e.id
        WHERE p.data_inativo IS NULL
          AND (:dataInicio IS NULL OR e.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR e.data_hora <= :dataFim)
        GROUP BY p.id, p.nome
        ORDER BY totalItensDoados DESC
        LIMIT 10
    """, nativeQuery = true)
    List<RankingDoadorProjection> buscarTopQuemMaisDoa(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    @Query(value = """
        SELECT
            i.nome AS itemNome,
            c.nome AS categoriaNome,
            ee.quantidade AS quantidade,
            i.preco_unitario AS precoUnitario,
            (ee.quantidade * i.preco_unitario) AS valorEstimado,
            ea.nome AS enderecoArmazenamentoNome,
            p.nome AS doadorNome,
            p.cpf_cnpj AS cpfCnpj,
            e.data_hora AS dataHora,
            e.descricao AS descricao
        FROM entrada_estoque ee
        INNER JOIN entrada e ON e.id = ee.id_entrada
        INNER JOIN item i ON i.id = ee.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        INNER JOIN endereco_armazenamento ea ON ea.id = ee.id_armazenamento
        INNER JOIN pessoa p ON p.id = e.id_doador
        WHERE (:dataInicio IS NULL OR e.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR e.data_hora <= :dataFim)
          AND (:doadorId IS NULL OR e.id_doador = :doadorId)
          AND (:localArmazenamentoId IS NULL OR ee.id_armazenamento = :localArmazenamentoId)
          AND (:categoriaItemId IS NULL OR i.id_categoria = :categoriaItemId)
          AND (
            :termoBusca IS NULL
            OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(e.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          )
        ORDER BY
          CASE WHEN :sortField = 'dataHora' AND :sortDirection = 'ASC' THEN e.data_hora END ASC,
          CASE WHEN :sortField = 'dataHora' AND :sortDirection = 'DESC' THEN e.data_hora END DESC,
          CASE WHEN :sortField = 'itemNome' AND :sortDirection = 'ASC' THEN i.nome END ASC,
          CASE WHEN :sortField = 'itemNome' AND :sortDirection = 'DESC' THEN i.nome END DESC,
          CASE WHEN :sortField = 'categoriaNome' AND :sortDirection = 'ASC' THEN c.nome END ASC,
          CASE WHEN :sortField = 'categoriaNome' AND :sortDirection = 'DESC' THEN c.nome END DESC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'ASC' THEN ee.quantidade END ASC,
          CASE WHEN :sortField = 'quantidade' AND :sortDirection = 'DESC' THEN ee.quantidade END DESC,
          CASE WHEN :sortField = 'precoUnitario' AND :sortDirection = 'ASC' THEN i.preco_unitario END ASC,
          CASE WHEN :sortField = 'precoUnitario' AND :sortDirection = 'DESC' THEN i.preco_unitario END DESC,
          CASE WHEN :sortField = 'valorEstimado' AND :sortDirection = 'ASC' THEN (ee.quantidade * i.preco_unitario) END ASC,
          CASE WHEN :sortField = 'valorEstimado' AND :sortDirection = 'DESC' THEN (ee.quantidade * i.preco_unitario) END DESC,
          CASE WHEN :sortField = 'localArmazenamento' AND :sortDirection = 'ASC' THEN ea.nome END ASC,
          CASE WHEN :sortField = 'localArmazenamento' AND :sortDirection = 'DESC' THEN ea.nome END DESC,
          CASE WHEN :sortField = 'doadorNome' AND :sortDirection = 'ASC' THEN p.nome END ASC,
          CASE WHEN :sortField = 'doadorNome' AND :sortDirection = 'DESC' THEN p.nome END DESC,
          e.data_hora DESC, ee.id DESC
    """,
        countQuery = """
        SELECT COUNT(ee.id)
        FROM entrada_estoque ee
        INNER JOIN entrada e ON e.id = ee.id_entrada
        INNER JOIN item i ON i.id = ee.id_item
        INNER JOIN categoria_item c ON c.id = i.id_categoria
        INNER JOIN endereco_armazenamento ea ON ea.id = ee.id_armazenamento
        INNER JOIN pessoa p ON p.id = e.id_doador
        WHERE (:dataInicio IS NULL OR e.data_hora >= :dataInicio)
          AND (:dataFim IS NULL OR e.data_hora <= :dataFim)
          AND (:doadorId IS NULL OR e.id_doador = :doadorId)
          AND (:localArmazenamentoId IS NULL OR ee.id_armazenamento = :localArmazenamentoId)
          AND (:categoriaItemId IS NULL OR i.id_categoria = :categoriaItemId)
          AND (
            :termoBusca IS NULL
            OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
            OR LOWER(e.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%'))
          )
    """, nativeQuery = true)
    Page<EntradaDiscriminadaProjection> listarEntradasDiscriminadas(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("doadorId") Long doadorId,
        @Param("localArmazenamentoId") Long localArmazenamentoId,
        @Param("categoriaItemId") Long categoriaItemId,
        @Param("termoBusca") String termoBusca,
        @Param("sortField") String sortField,
        @Param("sortDirection") String sortDirection,
        Pageable pageable
    );

}
