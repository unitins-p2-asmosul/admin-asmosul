package br.org.asmosul.api.comum.config;

import jakarta.transaction.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
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
}
