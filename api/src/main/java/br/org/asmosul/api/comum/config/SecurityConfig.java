package br.org.asmosul.api.comum.config;

import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.acesso.repositories.ContaRepository;
import br.org.asmosul.api.comum.security.FiltroAutenticacaoJwt;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private List<String> allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(ContaRepository contaRepository) {
        return nomeUsuario ->
                contaRepository
                        .findByNomeUsuario(nomeUsuario)
                        .map(
                                conta -> {
                                    var authorities =
                                            conta.getPerfis().stream()
                                                    .map(Perfil::name)
                                                    .map(
                                                            nome ->
                                                                    new SimpleGrantedAuthority(
                                                                            "ROLE_" + nome))
                                                    .toList();

                                    return User.withUsername(conta.getNomeUsuario())
                                            .password(conta.getSenhaHash())
                                            .authorities(authorities)
                                            .disabled(!conta.isAtivo())
                                            .build();
                                })
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "Conta não encontrada para o usuário informado"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider authenticationProvider) {
        return new ProviderManager(List.of(authenticationProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            FiltroAutenticacaoJwt filtroAutenticacaoJwt,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(
                    session ->
                            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                // anônimo / token inválido -> AuthenticationException -> 401
                .authenticationEntryPoint(
                    (request, response, authException) ->
                        resolver.resolveException(request, response, null, authException))
                // autenticado sem perfil -> AccessDeniedException -> 403
                .accessDeniedHandler(
                    (request, response, accessDeniedException) ->
                        resolver.resolveException(request, response, null, accessDeniedException)))
            .authorizeHttpRequests(auth -> auth
                // Rotas Públicas
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers(
                    "/v3-docs/**",
                    "/swagger-ui/**"
                ).permitAll()

                //Acessos
                .requestMatchers(HttpMethod.GET, "/contas/eu").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/contas/minha-senha").authenticated()

                // Exceções (para permitir que módulos consultem dados um do outro
                .requestMatchers(HttpMethod.GET, "/pessoas").authenticated()
                .requestMatchers(HttpMethod.GET, "/pessoas/categorias/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/pessoas/comorbidades/**").authenticated()

                //Módulos
                .requestMatchers("/acessos/**").hasAnyRole(Perfil.GERENCIADOR_ACESSO.name())
                .requestMatchers("/doacoes/**").hasAnyRole(Perfil.GERENCIADOR_DOACOES.name())
                .requestMatchers("/pessoas/**").hasAnyRole(Perfil.GERENCIADOR_PESSOAS.name())
                .requestMatchers("/capacitacoes/**").hasAnyRole(Perfil.GERENCIADOR_CAPACITACOES.name())
                .requestMatchers("/relatorios/**").hasAnyRole(Perfil.GERENCIADOR_RELATORIOS.name())

                // Qualquer outro endpoint requer autenticação
                .anyRequest().authenticated()

            )
            .addFilterBefore(
                    filtroAutenticacaoJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
