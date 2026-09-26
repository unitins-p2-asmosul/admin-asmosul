package br.org.asmosul.api.comum.config;

import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.acesso.repositories.ContaRepository;
import br.org.asmosul.api.comum.security.FiltroAutenticacaoJwt;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

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
            HttpSecurity http, FiltroAutenticacaoJwt filtroAutenticacaoJwt) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(
                        filtroAutenticacaoJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
