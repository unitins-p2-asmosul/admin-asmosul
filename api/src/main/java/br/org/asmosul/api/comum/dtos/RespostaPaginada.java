package br.org.asmosul.api.comum.dtos;

import java.util.List;
import org.springframework.data.domain.Page;

public record RespostaPaginada<T>(
    List<T> dados, int paginaAtual, int tamanhoPagina, long totalElementos, int totalPaginas) {
  public static <T> RespostaPaginada<T> dePage(Page<T> page) {
    return new RespostaPaginada<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
