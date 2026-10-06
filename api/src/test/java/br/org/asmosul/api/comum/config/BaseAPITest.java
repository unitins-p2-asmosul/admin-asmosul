package br.org.asmosul.api.comum.config;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.mysql.MySQLContainer;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
public abstract class BaseAPITest {

    @ServiceConnection static final MySQLContainer mysql;

    static {
        mysql =
            new MySQLContainer("mysql:8.0")
                .withDatabaseName("asmosul_db_test")
                .withUsername("test")
                .withPassword("test");
        mysql.start();
    }

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;
    protected MockMvc mockMvcSemAutenticacao;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .defaultRequest(MockMvcRequestBuilders.get("/")
                    .with(user("asmosul_teste")
                        .roles("GERENCIADOR_PESSOAS", "GERENCIADOR_ACESSO", "GERENCIADOR_DOACOES"))
                    .with(csrf()))
            .build();

        this.mockMvcSemAutenticacao =         MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .defaultRequest(
                get("/")
                    .with(csrf()))
            .build();

    }
}
