# Padrão de Arquitetura do Backend

Versão: 1.0

# Pom.xml

## Sprint 1

```xml
<modelVersion>4.0.0</modelVersion>
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.8</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
<groupId>br.org.asmosul</groupId>
<artifactId>api</artifactId>
<version>0.0.1-SNAPSHOT</version>
<name/>
<description/>
<url/>
<licenses>
    <license/>
</licenses>
<developers>
    <developer/>
</developers>
<scm>
    <connection/>
    <developerConnection/>
    <tag/>
    <url/>
</scm>
<properties>
    <java.version>25</java.version>
</properties>
<dependencies>
       <!-- Data JPA para os repositories -->
    <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
       <!-- DTOs -->
    <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
       <!-- APIRest -->
    <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
       <!-- Flyway -->
    <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-mysql</artifactId>
    </dependency>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-flyway</artifactId>
       </dependency>
       <!-- Drive Mysql -->
    <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
       <scope>runtime</scope>
    </dependency>
       <!-- Swagger e documentação API -->
       <dependency>
           <groupId>org.springdoc</groupId>
           <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
           <version>3.1.0</version>

       </dependency>
       <!-- Testes -->

       <!--Suíte padrão-->
    <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa-test</artifactId>
       <scope>test</scope>
    </dependency>
    <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-flyway-test</artifactId>
       <scope>test</scope>
    </dependency>
    <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-validation-test</artifactId>
       <scope>test</scope>
    </dependency>
       <!-- Para os testes de APIRest-->
    <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-webmvc-test</artifactId>
       <scope>test</scope>
    </dependency>
       <!-- Database de testes -->
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-testcontainers</artifactId>
           <scope>test</scope>
       </dependency>
       <dependency>
           <groupId>org.testcontainers</groupId>
           <artifactId>junit-jupiter</artifactId>
           <version>1.21.4</version>
           <scope>test</scope>
       </dependency>
       <dependency>
           <groupId>org.testcontainers</groupId>
           <artifactId>testcontainers-mysql</artifactId>
           <version>2.0.1</version>
           <scope>test</scope>
       </dependency>

</dependencies>

    <build>
       <plugins>
          <plugin>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-maven-plugin</artifactId>
          </plugin>

       </plugins>
    </build>

</project>
```

# Banco de dados e Docker compose

Na raiz do projeto backend (`asmosul-api/docker-compose.yml`), disponibiliza-se a infraestrutura do MySQL de desenvolvimento:

```yaml
version: '3.8'

services:
  mysql-dev:
    image: mysql:8.0
    container_name: mysql-dev
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: asmosul_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_dev_data:/var/lib/mysql

volumes:
  mysql_dev_data:
```

- **Para iniciar o banco:** Execute `docker compose up -d`.

# Camadas

O backend será constituido por camadas:

DTO, Controller → Service → Repository → Model

## DTOs

Para evitar varios arquivos pequenos e manter o contexto de cada entidade centralizado, adotamos o padrão **Classe estática + Records**.

### Diretrizes de implementação

1. Uma classe principal (ex.: `PessoaDTO`) agrupa todos os DTOs que serão criados para uma entidade na aplicação
2. Implemente dentro dessa classe principal records específicos
3. Anotações do Bean Validation (`@NotBlank`, `@NotNull`, `@Size`, etc.) devem ser aplicadas diretamente nos parâmetros do record.
4. Crie os Métodos de fábrica:
    - **`deEntidade(Entidade entity)`:** Método estático dentro do record de resposta para converter a entidade JPA no respectivo DTO de saída.
    - **`paraEntidade()`:** Método de instância nos records de requisição/criação para instanciar a entidade a partir dos dados recebidos.
5. Priorize adoção de nomes para os cenários mais comuns:
    1. `Requisicao`
    2. `Atualizacao`
    3. `Resumo`
    4. `Detalhe`
6. Os DTOs de **Atualizacao e Resumo** só devem ser criados se houver diferença real em relação ao DTO de **Requisicao ou Detalhe.** Se a listagem retornar exatamente os mesmos campos do detalhamento (como no caso de tabelas simples tipo Categoria), utilize diretamente o DTO Detalhe.
7. Nos métodos `paraEntidade`, associações de IDs Externos serão vinculadas na camada service, como preencher a categoria de pessoa.

### Exemplo de estrutura de DTO

```java
public final class PessoaDTO {
		// os comentários não são necessários e
		//devem ser evitados em excesso no código real.
		//Eles são apenas para deixar o exemplo claro.

    private PessoaDTO() {
        // Construtor privado para impedir instanciação da classe container
    }

    /**
     * DTO para criação de novos registros (Request Body).
     */
    public record Requisicao(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        String cpf,

        @NotNull(message = "O ID da categoria é obrigatório")
        Long categoriaId
    ) {
        public Pessoa paraEntidade() {
		        return new Pessoa(this.nome, this.cpf);
        }
    }

    /**
     * DTO para atualização parcial ou total de registros existentes.
	   * Criar somente de necessário
     */
    public record Atualizacao(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        Long categoriaId
    ) {}

    /**
     * DTO de listagem / visualização resumida
     */
    public record Resumo(
        Long id,
        String nome,
        String cpf,
        boolean ativo
    ) {
        public static Resumo deEntidade(Pessoa pessoa) {
            return new Resumo(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.isAtivo()
            );
        }
    }

    /**
     * DTO de visualização detalhada com relacionamentos carregados.
     */
    public record Detalhe(
        Long id,
        String nome,
        String cpf,
        boolean ativo,
        CategoriaDTO.Resumo categoria
    ) {
        public static Detalhe deEntidade(Pessoa pessoa) {
            return new Detalhe(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.isAtivo(),
                pessoa.getCategoria() != null ? CategoriaDTO.Resumo.deEntidade(pessoa.getCategoria()) : null
            );
        }
    }
}

```

### Dto de paginação

O `RespostaPaginada` é um `record` genérico padronizado que encapsula uma lista de registros retornada em uma consulta juntamente com os metadados calculados de paginação (página atual, tamanho da página, total de registros e total de páginas), consumindo a estrutura nativa `Page<T>` do Spring Data por meio do método de conversão `dePage`.

```java
public record RespostaPaginada<T>(
    List<T> dados,
    int paginaAtual,
    int tamanhoPagina,
    long totalElementos,
    int totalPaginas
) {
    public static <T> RespostaPaginada<T> dePage(Page<T> page) {
        return new RespostaPaginada<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
```

## Controllers

A camada de Controller é responsável unicamente pela interface HTTP da aplicação. Ela recebe as requisições, aciona a validação dos DTOs, delega a execução para a camada de Service e traduz o resultado no código de status HTTP correspondente.

### Diretrizes de implementação

- Não use `try catch` , pois isto é responsabilidade do tratador global de exceções
- A Injeção de dependência deve ser via construtor
    - Exemplo:

    ```java
    public class PessoaController {

        private final PessoaService pessoaService;

        public PessoaController(PessoaService pessoaService) {
            this.pessoaService = pessoaService;
        }
    }
    ```

- Todos os parâmetros necessários devem possuir notação `@valid`
- Utilize anotações como `@ApiResponse`  e `@Operation` nos controllers e seguir o documento de contrato de API
- Cubra todas as respostas na classe `ResponseEntity<T>` do Spring
- No listar, utilize `Pageable paginacao`  e `boolean incluirInativos`  como parâmetros, ver interface `Pageable` do Spring em: https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Pageable.html

### Exemplo de estrutura de classe Controller

| **Nome do Método** | **Rota** | **Verbo HTTP** | **Código HTTP** | **Retorno (ResponseEntity<T>)** |
| --- | --- | --- | --- | --- |
| `cadastrar` | `/pessoas` | `POST` | `201 Created` | `ResponseEntity<PessoaDTO.Detalhe>`  |
| `listar` | `/pessoas` | `GET` | `200 OK` | `ResponseEntity<RespostaPaginada<PessoaDTO.Resumo>>` |
| `buscarPorId` | `/pessoas/{id}` | `GET` | `200 OK` | `ResponseEntity<PessoaDTO.Detalhe>` |
| `atualizar` | `/pessoas/{id}` | `PUT` | `200 OK` | `ResponseEntity<PessoaDTO.Detalhe>` |
| `desativar` | `/pessoas/{id}/desativar` | `PATCH` | `204 No Content` | `ResponseEntity<Void>` |
| `reativar` | `/pessoas/{id}/reativar` | `PATCH` | `204 No Content` | `ResponseEntity<Void>` |

### Exemplo de estrutura de classe Controller

```java
@Tag(name = "Pessoas", description = "Endpoints para gerenciamento de associados")
@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @Operation(summary = "Cadastrar uma nova pessoa", description = "Cria um novo registro de associado no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    @PostMapping
    public ResponseEntity<PessoaDTO.Detalhe> cadastrar(
        @RequestBody @Valid PessoaDTO.Requisicao requisicao,
        UriComponentsBuilder uriBuilder
    ) {
        return null;
    }

    @Operation(summary = "Listar pessoas", description = "Retorna uma listagem paginada de associados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<RespostaPaginada<PessoaDTO.Resumo>> listar(
        @PageableDefault(size = 10, sort = "nome") Pageable paginacao,
        @RequestParam(defaultValue = "false") boolean incluirInativos
    ) {
        return null;
    }

    @Operation(summary = "Buscar pessoa por ID", description = "Retorna os detalhes completos de uma pessoa ativa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada ou inativa")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PessoaDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        return null;
    }

    @Operation(summary = "Atualizar dados da pessoa", description = "Atualiza as informações de uma pessoa cadastrada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PessoaDTO.Detalhe> atualizar(
        @PathVariable Long id,
        @RequestBody @Valid PessoaDTO.Atualizacao requisicao
    ) {
        return null;
    }

    @Operation(summary = "Desativar pessoa", description = "Realiza a desativação lógica (soft delete) da pessoa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pessoa desativada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        return null;
    }

    @Operation(summary = "Reativar pessoa", description = "Reativa o registro de uma pessoa previamente desativada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pessoa reativada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        return null;
    }
}
```

## Service

A camada de Service encapsula todas as regras de negócio, validações lógicas, controle transacional e a coordenação entre repositórios, DTOs e entidades JPA.

### Diretrizes de implementação

- A injeção de dependência deve ser via construtor
    - Exemplo:

    ```java
    Public class PessoaService {
      private final PessoaRepository pessoaRepository;
      private final CategoriaRepository categoriaRepository;

      public PessoaService(PessoaRepository pessoaRepository, CategoriaRepository categoriaRepository) {
          this.pessoaRepository = pessoaRepository;
          this.categoriaRepository = categoriaRepository;
    }
    ```

- Anote a classe de service com`@Service`
- Use `@Transactional()` para operações de escrita no banco e `@Transactional(readOnly = true)`  para operações exclusivas de leitura
- Lance exceções específicas com o `throw` , detalhadas no mapeador de exceções
- Services devem receber e retornar DTOs
- Utilize os métodos de “para Entidade” em DTOs de cadastro/atualização, com IDs sendo recuperados pelos repositores com os devidos tratamentos
- Utilize o método `.save()` do repositório para cadastros, **não utilize para operações exclusivamente de atualização!**

### Tabela de padronização dos métodos

| **Método** | **Parâmetros de Entrada** | **Retorno** | **Anotação Transacional** |
| --- | --- | --- | --- |
| `cadastrar` | `DTO.Requisicao` | `DTO.Detalhe` | `@Transactional` |
| `listar` | `Pageable`, `boolean incluirInativos`  | `RespostaPaginada<DTO.Resumo>` | `@Transactional(readOnly = true)` |
| `buscarPorId` | `Long id` | `DTO.Detalhe` | `@Transactional(readOnly = true)` |
| `atualizar` | `Long id`, `DTO.Atualizacao` | `DTO.Detalhe` | `@Transactional` |
| `desativar` | `Long id` | `void` | `@Transactional` |
| `reativar` | `Long id` | `void` | `@Transactional` |

### Exemplo de estrutura de classe Service

```java

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ComorbidadeRepository comorbidadeRepository;

    // Injeção de dependência via construtor
    public PessoaService(
        PessoaRepository pessoaRepository,
        CategoriaRepository categoriaRepository,
        ComorbidadeRepository comorbidadeRepository
    ) {
        this.pessoaRepository = pessoaRepository;
        this.categoriaRepository = categoriaRepository;
        this.comorbidadeRepository = comorbidadeRepository;
    }

    @Transactional
    public PessoaDTO.Detalhe cadastrar(PessoaDTO.Requisicao requisicao) {
        return null;
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<PessoaDTO.Resumo> listar(Pageable paginacao, boolean incluirInativos) {
        return null;
    }

    @Transactional(readOnly = true)
    public PessoaDTO.Detalhe buscarPorId(Long id) {
        return null;
    }

    @Transactional
    public PessoaDTO.Detalhe atualizar(Long id, PessoaDTO.Atualizacao requisicao) {
        return null;
    }

    @Transactional
    public void desativar(Long id) {
    }

    @Transactional
    public void reativar(Long id) {
    }
}
```

## Repository

A camada de Repository é responsável exclusivamente pela persistência e recuperação de dados, abstraindo o acesso ao banco por meio do Spring Data JPA.

### Diretrizes de Implementação

- Declare repositórios como uma `interface` que estende `JpaRepository<Entidade, Long>`.
- Não utilize a anotação `@Repository`. Interfaces que herdam de `JpaRepository` já são registradas e gerenciadas automaticamente como componentes do Spring Data.
- Tente utilizar as facilidades do Spring Data JPA:
    - Infelizmente a documentação oficial só está em inglês, mas você pode traduzir para português com IA ou tradutor:
        - https://spring.io/guides/gs/accessing-data-jpa
        - https://medium.com/@pratik.941/how-spring-jpa-works-and-writing-custom-queries-a3d9e67663bc
    - Você pode criar métodos com nomes padronizados, o Spring Data JPA criará queries automaticamente com base unicamente nos nomes deles. Elas são chamadas **Derived Queries**. Para isso, você tem que seguir algumas convenções de nome**:**
        - `findByX`: procura uma entidade pelo atributo “X” com algum valor
        - `existsByX` : valida se uma entidade com atributo X de algum valor existe
        - `countByX` : retorna quantas entidades com algum valor no atributo X existem
        - Exemplos padronizados: `existsByCpf(String cpf)`, `existsByNomeAndIdNot(String nome, Long id)`.
    - Utilize a anotação `@Query` com JPQL apenas quando o nome da Derived Query ficar excessivamente longo ou para consultas que demandem junções complexas
    - Evite o uso de queries nativas (`nativeQuery = true`), a não ser que seja mais complicada como para uso em relatórios.
- Toda busca individual para regras de negócio ativas deve garantir o estado: use `findByIdAndDataInativoIsNull(Long id)` em vez de `findById(Long id)`.
- Toda listagem padrão sem inativos deve usar `findAllByDataInativoIsNull(Pageable pageable)`.
- Para consultas que exijam apenas os registros desativados, utilize `findAllByDataInativoIsNotNull(Pageable pageable)`.
- Utilize o método `.save()`  do repositório para cadastros.

### Exemplo de estrutura e queries

```java
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    // ==========================================
    // Derived Queries
    // ==========================================

    // Busca por ID apenas se estiver ativo (dataInativo IS NULL)
    Optional<Pessoa> findByIdAndDataInativoIsNull(Long id);

    // Listagem paginada de pessoas ativas
    Page<Pessoa> findAllByDataInativoIsNull(Pageable pageable);

    // Validação de duplicidade de CPF (para cadastro)
    boolean existsByCpf(String cpf);

    // Validação de duplicidade ignorando o próprio registro (para atualização)
    boolean existsByCpfAndIdNot(String cpf, Long id);

    // Busca por nome parcial ignorando maiúsculas/minúsculas (apenas ativos)
    Page<Pessoa> findByNomeContainingIgnoreCaseAndDataInativoIsNull(String nome, Pageable pageable);

    // ==========================================
    // Consultas Customizadas com @Query
    // ==========================================

    @Query("""
        SELECT p FROM Pessoa p
        LEFT JOIN FETCH p.categoria
        LEFT JOIN FETCH p.comorbidades
        WHERE p.id = :id AND p.dataInativo IS NULL
    """)
    Optional<Pessoa> buscarPorIdComRelacionamentos(@Param("id") Long id);

    // Update em lote diretamente via banco de dados
    @Modifying
    @Query("UPDATE Pessoa p SET p.dataInativo = CURRENT_TIMESTAMP WHERE p.categoria.id = :categoriaId")
    int desativarTodasPorCategoria(@Param("categoriaId") Long categoriaId);
}
```

## Models

As entidades mapeiam as tabelas do banco de dados relacional e concentram o estado e os comportamentos essenciais do domínio da aplicação.

### Diretrizes de Implementação

- A criação e alteração de tabelas e colunas é de responsabilidade exclusiva dos scripts de migration (Flyway/Liquibase). Se já não estiver configurado, configure `spring.jpa.hibernate.ddl-auto: validate` para impedir que o Hibernate crie ou altere o banco automaticamente.
- Não repita campos como `id` ou controle de inativação nas entidades concretas. Herde diretamente das classes base padronizadas (`EntidadeBase` ou `EntidadeInativavel`).
    - As entidades filhas dessas classes base não devem implementar `equals` e `hashCode`. Essa lógica fica centralizada e encapsulada na classe base (`EntidadeBase`).
- Mantenha atributos privados com getters e setters
- Toda entidade JPA exige um construtor sem argumentos, crie sempre um construtor `protected` sem argumentos
- Crie construtores públicos nas entidades ao invés de usar setters ao instanciá-las, ou seja…
    - Em Conformidade:

        ```java
        public class Pessoa {
            private String nome;
            private String cpf;

            protected Pessoa() {}

            public Pessoa(String nome, String cpf) {
                this.nome = nome;
                this.cpf = cpf;
            }
            //getters e setters...
        }
        ```

    - Fora do padrão:

        ```java
        public class Pessoa {

        		public Pessoa() {}

            private String nome;
            private String cpf;
            //getters e setters...
        }
        // em outro arquivo...
        Pessoa pessoa = new Pessoa();

        pessoa.setNome("Carlos Silva");
        pessoa.setCpf("12345678900");

        ```

- Por padrão, utilize `fetch = FetchType.LAZY` em relacionamentos `@ManyToOne` e `@OneToOne` para evitar carregar muitos dados na memória.
- Em coleções (`@ManyToMany`, `@OneToMany`), inicialize o atributo diretamente na declaração com `new ArrayList<>()`
- Mapeie explicitamente nomes de tabelas e colunas conforme as migrações do banco: em `snake_case` em plural, com as anotações `@Table(name = "...")` e `@Column(name = "...")`.
- Enums
  - Os Enums de domínio (como Sexo, Escolaridade, RendaFamiliar) devem fornecer formato duplo para a camada de visualização e transporte
  - Serialização de Saída (GET): Retorna um objeto estruturado contendo `{ "codigo": "...", "descricao": "..." }` utilizando a anotação `@JsonFormat(shape = JsonFormat.Shape.OBJECT)`.
  - Desserialização de Entrada (`POST / PUT`): Aceita diretamente a String simples do código (ex.: "FEMININO") por meio do método estático anotado com @JsonCreator. Se o valor recebido for nulo ou inválido, lança uma ValidationException (HTTP 400).

### Exemplo de Implementação de Enum (Sexo.java)
```java
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Sexo {
FEMININO("FEMININO", "Feminino"),
MASCULINO("MASCULINO", "Masculino"),
PREFIRO_NAO_INFORMAR("PREFIRO_NAO_INFORMAR", "Prefiro não informar");

    private final String codigo;
    private final String descricao;

    Sexo(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    @JsonProperty("codigo")
    public String getCodigo() {
        return codigo;
    }

    @JsonProperty("descricao")
    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static Sexo deCodigo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return Arrays.stream(Sexo.values())
                .filter(s -> s.getCodigo().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> ValidationException.of("sexo", "Opção de sexo informada é inválida"));
    }
}
```

### Estrutura das Classes Base

```java

@MappedSuperclass
public abstract class EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
```

```java

@MappedSuperclass
public abstract class EntidadeInativavel extends EntidadeBase {

    @Column(name = "data_inativo")
    private LocalDateTime dataInativo;

    public LocalDateTime getDataInativo() {
        return dataInativo;
    }

    public void setDataInativo(LocalDateTime dataInativo) {
        this.dataInativo = dataInativo;
    }

    public boolean isAtivo() {
        return this.dataInativo == null;
    }
}
```

### Exemplo de Entidade

```java
@Entity
@Table(name = "categorias")
public class Categoria extends EntidadeInativavel {

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    protected Categoria() {}

    public Categoria(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
```

# Tratamento de Exceções

O tratamento de erros é centralizado e padronizado pelo protocolo **RFC 7807 / RFC 9457 (Problem Details)**, eliminando o uso de blocos `try-catch` em Controllers e Services.

### Diretrizes de Implementação

- Exceções devem ser lançadas livremente com o `throw`, sem uso do `try-catch`, para serem interceptadas pelo tratador global.
- Utiliza-se a classe `ProblemDetail` do Spring Boot 3+ em vez de classes de resposta customizadas manuais.
- Regras de negócio violadas devem ser lançadas via métodos estáticos `ValidationException.of(...)` (400 Bad Request) ou `ValidationException.ofConflito(...)` (409 Conflict).
- Todo payload de erro conterá os campos padrão: `type`, `title`, `status`, `detail`, `instance`, `timestamp` e a lista `erros` (quando aplicável).

### Estrutura de Arquivos e Responsabilidades

#### 1. `ValidationException`

Exceção customizada de negócio (Runtime) para inconsistências e conflitos de regras.

- **Record Interno:** `CampoErro(String campo, String mensagem)`
- **Método de Fábrica:** `static ValidationException of(String campo, String mensagem)`

#### 2. `br.org.asmosul.comum.exception.EntidadeNaoEncontradaException`

Exceção customizada de negócio para consultas sem resultado (Gera HTTP 404 Not Found).

- **Construtor:** Recebe a mensagem explicativa do recurso não encontrado (ex.: *"Pessoa ativa não encontrada com o ID informado"*).

#### 3. `br.org.asmosul.comum.exception.GlobalExceptionHandler`

Componente anotado com `@RestControllerAdvice` **que estende ResponseEntityExceptionHandler** (para proteger e manter o comportamento correto de erros do Spring, como o 405 Method Not Allowed). É encarregado de traduzir exceções em respostas no padrão `ProblemDetail`.

- **Método tratarValidationException:** Trata `ValidationException` mapeando para `400` ou `409` com lista de campos afetados.
- **Método handleMethodArgumentNotValid (Sobrescrito):** Trata `MethodArgumentNotValidException` (gerado por `@Valid`) mapeando para `400 Bad Request` com detalhamento dos campos.
- **Método tratarEntidadeNaoEncontrada:** Trata `EntidadeNaoEncontradaException` mapeando para `404 Not Found`.
- **Método tratarErroInesperado:** Trata `Exception.class` (fallback genérico final) mapeando para `500 Internal Server Error`, atuando de forma segura apenas após o Spring tratar suas próprias exceções.

### Exemplos de Payloads de Resposta (JSON)

**1. Erro de Validação de Negócio ou Bean Validation (`400 Bad Request`)**
Disparado por `ValidationException.of("campo", "msg")` ou por anotações `@NotBlank`/`@NotNull` nos DTOs:

JSON

```
{
  "type": "https://api.asmosul.org/errors/dados-invalidos",
  "title": "Erro de validação",
  "status": 400,
  "detail": "Um ou mais campos não passaram na validação.",
  "instance": "/pessoas",
  "timestamp": "2026-08-18T12:10:00-03:00",
  "erros": [
    {
      "campo": "nome",
      "mensagem": "O nome é obrigatório."
    },
    {
      "campo": "dataNascimento",
      "mensagem": "A data de nascimento não pode ser futura."
    }
  ]
}
```

**2. Conflito de Regra de Negócio (`409 Conflict`)**
Disparado por `ValidationException.ofConflito("campo", "msg")` (ex.: CPF ou Nome já cadastrados):

JSON

```
{
  "type": "https://api.asmosul.org/errors/conflito",
  "title": "Conflito de dados",
  "status": 409,
  "detail": "Conflito de dados",
  "instance": "/pessoas",
  "timestamp": "2026-08-18T12:10:00-03:00",
  "erros": [
    {
      "campo": "cpf",
      "mensagem": "Já existe uma pessoa cadastrada com este CPF."
    }
  ]
}
```

**3. Recurso Não Encontrado (`404 Not Found`)**
Disparado por `throw new EntidadeNaoEncontradaException("...")`:

JSON

```
{
  "type": "https://api.asmosul.org/errors/nao-encontrado",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Pessoa ativa não encontrada com o ID informado: 42",
  "instance": "/pessoas/42",
  "timestamp": "2026-08-18T12:10:00-03:00"
}
```

**4. Erro Interno Inesperado (`500 Internal Server Error`)**
Captura de falhas não tratadas via `Exception.class`:

JSON

```
{
  "type": "https://api.asmosul.org/errors/erro-interno",
  "title": "Erro inesperado",
  "status": 500,
  "detail": "Ocorreu um erro inesperado. Tente novamente mais tarde.",
  "instance": "/pessoas",
  "timestamp": "2026-08-18T12:10:00-03:00"
}
```

# Controle de Esquema e Migrations (Flyway)

O gerenciamento do banco de dados relacional é feito exclusivamente via **Flyway**, garantindo versionamento estruturado e paridade de esquema entre os ambientes locais e de produção.

### O que é o Flyway?

O Flyway é uma ferramenta de migração de banco de dados baseada em scripts SQL puros. Ao iniciar a aplicação Spring Boot, ele verifica uma tabela interna de histórico (`flyway_schema_history`) e executa automaticamente apenas os scripts novos em ordem sequencial.

### Diferença entre Flyway e Models (JPA)

| **Aspecto** | **Flyway (Scripts SQL)** | **Models (Entidades JPA)** |
| --- | --- | --- |
| **Papel Principal** | **Dono do esquema:** Cria, altera e versiona tabelas, colunas, chaves estrangeiras e índices. | **Espelho em memória:** Mapeia a estrutura existente para objetos Java para uso pela aplicação. |
| **Execução** | Roda antes do Hibernate subir via arquivos SQL versionados. | Opera durante a execução do sistema manipulando dados. |
| **Configuração** | Define o estado real do banco no MySQL. | Opera com `spring.jpa.hibernate.ddl-auto: validate` (apenas valida se a classe bate com o banco). |

### Diretrizes de Uso

- Todos os scripts devem residir em `src/main/resources/db/migration/`.
- Existem padrões obrigatórios de nomenclatura dos sqls:
    - Formato: `V<Versão>__<descricao_em_snake_case>.sql`
    - Exemplos: `V1__criar_tabelas_iniciais.sql`, `V2__adicionar_coluna_telefone_em_pessoas.sql`.
- Scripts que já foram executados em outros ambientes ou branch principal **nunca devem ser editados**. Qualquer alteração ou correção exige a criação de uma nova versão sequencial (`V2`, `V3`, etc.).

# Organização dos diretório

A arquitetura do projeto agrupa componentes por módulo e mantem componentes utilitários e transversais centralizados no pacote `comum`.

Dessa forma, devemos apenas trabalhar nos diretórios necessários para a sprint (na sprint 1 por exemplo é somente em pessoas)

```
src/main/java/br/org/asmosul/asmosul-api
├── comum/
│   ├── dtos/
│   │   └── RespostaPaginada.java
│   ├── exceptions/
│   │   ├── EntidadeNaoEncontradaException.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ValidationException.java
│   └── models/
│       ├── EntidadeBase.java
│       └── EntidadeInativavel.java
│
├── autenticacao/
│   ├── controllers/
│   ├── dtos/
│   ├── models/
│   ├── repositorys/
│   └── services/
│
├── pessoas/
│   ├── controllers/
│   ├── dtos/
│   ├── models/
│   ├── repositories/
│   └── services/
│
├── capacitacoes/
│   ├── controllers/
│   ├── dtos/
│   ├── models/
│   ├── repositories/
│   └── services/
│
├── doacoes/
│   ├── controllers/
│   ├── dtos/
│   ├── models/
│   ├── repositories/
│   └── services/
│
└── relatorios/
    ├── controllers/
    ├── dtos/
    └── services/

src/main/resources/
├── db/
│   └── migration/
├── application.yml
└── application-prod.yml
```

# Padrão de Testes Automatizados

A cobertura de testes automatizados é **obrigatória** para todos os desenvolvedores que implementarem funcionalidades no backend.

### Diretrizes e Regras Gerais

- Todo endpoint criado ou alterado em um Controller deve possuir testes cobrindo:
    1. **Caminho Feliz:** Sucesso da operação com payload válido (`200 OK` ou `201 Created`).
    2. **Validação de Entrada:** Falha por campos inválidos/nulos (`400 Bad Request`).
    3. **Regra de Negócio / Integridade:** Cenário de registro não encontrado (`404 Not Found`) ou duplicidade/conflito (`409 Conflict`).
- **Segurança e Perfis de Acesso (401 / 403):**
    - Não é necessário testar segurança em todos os métodos de CRUD para não sobrecarregar o desenvolvimento.
    - **Criar pelo menos um teste de autorização (`403 Forbidden`) por módulo funcional** garantindo que perfis sem permissão sejam bloqueados (quando for implementada autenticação).
- Métodos com validações temporais, controle de saldo/estoque ou desativações em cascata devem conter testes de integração dedicados na camada de Service.
- Dados de testes não devem interferir em outros, se interferir, fazer a limpeza adequada. O @Transactional talvez não funcione nos testes de controllers
- Utilize a anotação `@WithMockUser(roles = "...")` do `spring-security-test` para simular requisições autenticadas sem precisar gerar tokens JWT manualmente.

### Padrão de Nomenclatura dos Métodos de Teste

Adota-se o padrão estruturado em três partes separadas por *underline*:

`metodoEmTeste_cenarioOuCondicao_resultadoEsperado`

**Exemplos:**

- `cadastrarPessoa_comDadosValidos_retornaJsonEStatus201asmosul-api`
- `cadastrarPessoa_comCpfDuplicado_retornaJsonProblemDetailEStatus409`
- `cadastrarPessoa_comPerfilSemPermissao_retornaStatus403`
- `buscarPessoaPorId_quandoIdNaoExistente_retornaStatus404`
- `registrarEntrega_comEstoqueInsuficiente_lancaValidationException`

### Testes de Endpoints / API (Controllers)

Utilizam `@SpringBootTest` e `@AutoConfigureMockMvc` para testar as rotas HTTP, os códigos de status, a validação de perfil e o formato do payload retornado. **A maior parte dos testes serão estes**

#### Classe Base de Testes de API (`BaseAPITest.java`)

Todos os testes de Controller (`@SpringBootTest`) e Service devem herdar obrigatoriamente da classe abstrata `BaseAPITest`.

Ela inicializa um container descartável do MySQL 8.0 via **Testcontainers** e conecta as credenciais dinâmicas do Spring Boot automaticamente por meio da anotação `@ServiceConnection`:

```java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public abstract class BaseAPITest{

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("asmosul_db_test")
            .withUsername("test")
            .withPassword("test");
}
```

#### Exemplo de Teste

```java

class PessoaControllerTest extends BaseAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cadastrarPessoa_comDadosValidos_retornaJsonEStatus201() throws Exception {
        String jsonPayload = """
            {
                "nome": "Carlos Silva",
                "cpf": "12345678901",
                "categoriaId": 1
            }
        """;

        mockMvc.perform(post("/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated());
    }
}
```

### Testes de Regra de Negócio (Services)

Focados na execução direta de métodos Java da camada de `Service` para validação de fluxos intermediários, integridade cruzada e exceções de negócio. **Serão criados em menor quantidade.**

### Matriz de Cobertura

| **Camada / Contexto** | **Cenário Avaliado** | **Status HTTP / Exceção Esperada** | Quando criar |
| --- | --- | --- | --- |
| **Controller** | Sucesso no cadastro/atualização/busca | `200 OK` / `201 Created` | Todo endpoint |
| **Controller** | Campos `@NotBlank`, `@NotNull`, etc.   | `400 Bad Request` (`ProblemDetail` com `erros`)   | Todo endpoint de cadastro ou atualização |
| **Controller** | Busca por ID inexistente ou inativo   | `404 Not Found`
| Todo endpoint de cadastro, atualização ou inativação que conter a exceção |
| **Controller** | Tentativa de duplicidade (CPF, Nome único)   | `409 Conflict`
| Todo endpoint de cadastro, atualização ou reativação que conter a exceção |
| **Controller (Segurança)** | Acesso sem token ou token inválido   | `401 Unauthorized`
| Endpoints do módulo de autenticação |
| **Controller (Segurança)** | Acesso com perfil não autorizado (`@WithMockUser`)   | `403 Forbidden`
| Somente um ou dois endpoints pra cada módulo |
| **Service** | Regras de negócio mais avançadas | Lançamento de `ValidationException` no service | Para regras de negócio complexa |
