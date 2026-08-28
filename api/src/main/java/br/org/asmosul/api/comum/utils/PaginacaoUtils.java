package br.org.asmosul.api.comum.utils;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginacaoUtils {

    private PaginacaoUtils() {}

    public static Pageable sanitizarPaginacao(
            Pageable paginacao, Set<String> camposPermitidos, String campoPadrao) {
        if (paginacao == null) {
            return PageRequest.of(0, 10, Sort.by(campoPadrao).ascending());
        }

        if (paginacao.getSort().isUnsorted()) {
            return PageRequest.of(
                    paginacao.getPageNumber(),
                    paginacao.getPageSize(),
                    Sort.by(campoPadrao).ascending());
        }

        List<Sort.Order> ordensValidas =
                paginacao.getSort().stream()
                        .filter(order -> camposPermitidos.contains(order.getProperty()))
                        .toList();

        if (ordensValidas.isEmpty()) {
            return PageRequest.of(
                    paginacao.getPageNumber(),
                    paginacao.getPageSize(),
                    Sort.by(campoPadrao).ascending());
        }

        return PageRequest.of(
                paginacao.getPageNumber(), paginacao.getPageSize(), Sort.by(ordensValidas));
    }
}
