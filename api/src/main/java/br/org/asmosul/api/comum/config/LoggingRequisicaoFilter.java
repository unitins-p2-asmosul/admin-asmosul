package br.org.asmosul.api.comum.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoggingRequisicaoFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingRequisicaoFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/swagger-ui") || uri.contains("/api-docs") || uri.contains("/v3-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long inicio = System.nanoTime();
        String metodo = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String caminho = query == null || query.isBlank() ? uri : uri + "?" + query;

        log.info("---> [INÍCIO] {} {}", metodo, caminho);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;
            if (response.getStatus() >= 400 && response.getStatus() < 500) {
                log.warn(
                        "Requisição finalizada com erro cliente: {} {} status={}",
                        metodo,
                        caminho,
                        response.getStatus());
            }
            log.info(
                    "---> [FIM] {} {} duração={}ms status={}",
                    metodo,
                    caminho,
                    duracaoMs,
                    response.getStatus());
        }
    }
}
