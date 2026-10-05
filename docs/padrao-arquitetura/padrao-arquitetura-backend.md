# Padrão de Arquitetura do Backend

**Versão:** 1.1

## Importante!

Realize adições e modificações no backend mantendo a documentação do OpenAPI/Swagger rigorosamente atualizada via anotações nos Controllers, além de seguir os diagramas de classe e DER na pasta `docs/`. O Swagger oficial do ambiente de homologação pode ser consultado em: [https://h.asmosul.site/api/swagger-ui.html](https://h.asmosul.site/api/swagger-ui.html).

## Banco de dados e Docker Compose

Na raiz do projeto backend (`asmosul-api/docker-compose.yml`), disponibiliza-se a infraestrutura do MySQL de desenvolvimento.

- **Para iniciar o banco:** execute `docker compose up -d`.

## Camadas

O backend é constituído pelas seguintes camadas:

```
DTO, Controller → Service → Repository → Model
```

---

## DTOs

Para evitar múltiplos arquivos pequenos e manter o contexto de cada entidade centralizado, adota-se o padrão **Classe container estática + Records**.

### Diretrizes de Implementação

1. **Classe Container:** uma classe `final` com construtor `private` (ex.: `PessoaDTO`, `CategoriaDTO`) agrupa os records do ciclo de vida da entidade.

2. **Records Específicos de Domínio:**
    - **`Requisicao`:** entrada para criação de registros (`POST`). Deve conter o método de instância `paraEntidade()` para instanciar a entidade a partir dos dados recebidos, aplicando valores padrão quando aplicável (ex.: flags booleanas e contadores zerados). IDs de relacionamentos são recebidos como `Long` ou `List<Long>`.
    - **`Atualizacao`:** entrada para edição total ou parcial (`PUT`).
    - **`Resumo`:** saída leve para listagens e tabelas paginadas (`GET`). Deve conter o método estático de fábrica `deEntidade(Entidade entity)`. Relacionamentos associados são convertidos para suas respectivas versões resumidas (ex.: `List<CategoriaDTO.Resumo>`), nunca retornando apenas IDs soltos.
    - **`Detalhe`:** saída rica para visualização e edição detalhada (`GET /{id}`). Também implementa o método estático `deEntidade(Entidade entity)`.

3. **DTOs Autônomos (Filtros e Integrações):** quando o DTO representar parâmetros de consulta complexos ou objetos de integração externa, declare-o como record autônomo (ex.: `PessoaFiltroDTO`, `CepDTO`).

4. **Validações de Entrada:** aplique validações do Bean Validation (`@NotBlank`, `@Size`, `@Pattern`, `@Email`, etc.) diretamente nos parâmetros do record.

5. **Formatação de Datas:** utilize `@JsonFormat(pattern = "dd-MM-yyyy")` para campos `LocalDate` nos records de transporte JSON, e `@DateTimeFormat(pattern = "dd-MM-yyyy")` em records de parâmetros de consulta/filtro via query params.

### DTO e Utilitários de Paginação

Para padronizar respostas paginadas e evitar falhas de ordenação no banco de dados, utilizam-se os seguintes componentes transversais:

1. **Resposta Paginada (`RespostaPaginada<T>`)**
    - Record genérico padronizado que encapsula a lista de registros (`dados`) e metadados de paginação (`paginaAtual`, `tamanhoPagina`, `totalElementos`, `totalPaginas`).
    - Consome a estrutura nativa `Page<T>` do Spring Data por meio do método de fábrica `dePage(Page<T> page)`.
    - **Arquivo de referência:** `api/comum/dtos/RespostaPaginada.java`

2. **Sanitização de Ordenação (`PaginacaoUtils`)**
    - Utilitário que valida os campos de ordenação recebidos na requisição contra uma *whitelist* de campos permitidos (`Set<String>`) definida no Service.
    - Caso a requisição venha sem ordenação (`unsorted`) ou com campos inválidos não mapeados, aplica automaticamente a ordenação ascendente no campo padrão informado.
    - **Arquivo de referência:** `api/comum/utils/PaginacaoUtils.java`

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| CRUD com relacionamentos e regras ricas | `api/pessoas/dtos/PessoaDTO.java` |
| CRUD padrão e tabelas de domínio | `api/pessoas/dtos/CategoriaDTO.java` e `ComorbidadeDTO.java` |
| Filtros avançados via Query Params | `api/pessoas/dtos/PessoaFiltroDTO.java` |
| Objetos de integração externa | `api/pessoas/dtos/CepDTO.java` |

---

## Controllers

A camada de Controller é responsável unicamente pela interface HTTP da aplicação. Ela recebe as requisições, aciona a validação dos DTOs via Bean Validation, delega a execução para a camada de Service e traduz o resultado no código de status HTTP correspondente.

### Diretrizes de Implementação

- **Tratamento Centralizado de Erros:** não utilize blocos `try-catch` nos controllers. Todas as exceções de negócio e validação são capturadas pelo `GlobalExceptionHandler`.

- **Injeção de Dependências:** utilize estritamente injeção via construtor (proibido o uso de `@Autowired` em atributos).

- **Validação de Payload:** todo corpo de requisição (`@RequestBody`) de criação ou alteração deve conter a anotação `@Valid`.

- **Documentação OpenAPI / Swagger:**
    - Anote a classe com `@Tag(name = "...", description = "...")`.
    - Anote cada método com `@Operation(summary = "...", description = "...")` e `@ApiResponses` mapeando os status esperados (ex.: 200, 201, 400, 404, 409).
    - Em parâmetros de paginação e filtros complexos (records como `PessoaFiltroDTO`), utilize **obrigatoriamente** `@ParameterObject` do SpringDoc junto com `@PageableDefault` para exibir os parâmetros organizados no Swagger.

- **Padrão de Respostas (`ResponseEntity`):**
    - **Criação (`201 Created`):** monte a URI do recurso criado com `UriComponentsBuilder` e retorne via `ResponseEntity.created(uri).body(detalhe)`, com o header `Location` apontando para `/{id}`.
    - **Consultas e Atualizações (`200 OK`):** retorne `ResponseEntity.ok(resultado)`.
    - **Ações sem corpo (`204 No Content`):** operações de desativação e reativação devem retornar `ResponseEntity.noContent().build()`.

- **Listagens:**
    - **Paginada (Padrão):** rota raiz (`GET /recursos`), recebendo paginação sanitizada e o parâmetro `@RequestParam(defaultValue = "false") boolean incluirInativos`.
    - **Completa/Não Paginada (Para Dropdowns):** rota auxiliar (`GET /recursos/todas`), restrita a tabelas de domínio/apoio de baixo volume (ex.: `Categoria`, `Comorbidade`). Entidades volumosas (como `Pessoa`) operam estritamente de forma paginada.

- **Ciclo de Vida Lógico (Soft Delete):** a inativação e reativação são padronizadas via `PATCH /{id}/desativar` e `PATCH /{id}/reativar`. Não se adota exclusão física (`DELETE`).

### Tabela de Padronização de Endpoints

| Nome do Método | Rota | Verbo HTTP | Código HTTP | Retorno (ResponseEntity) |
| --- | --- | --- | --- | --- |
| `cadastrar` | `/{recursos}` | `POST` | `201 Created` (com header Location) | `ResponseEntity<DTO.Detalhe>` |
| `listar` | `/{recursos}` | `GET` | `200 OK` | `ResponseEntity<RespostaPaginada<DTO.Resumo>>` |
| `listarTodas` | `/{recursos}/todas` | `GET` | `200 OK` | `ResponseEntity<List<DTO.Resumo>>` |
| `buscarPorId` | `/{recursos}/{id}` | `GET` | `200 OK` | `ResponseEntity<DTO.Detalhe>` |
| `atualizar` | `/{recursos}/{id}` | `PUT` | `200 OK` | `ResponseEntity<DTO.Detalhe>` |
| `desativar` | `/{recursos}/{id}/desativar` | `PATCH` | `204 No Content` | `ResponseEntity<Void>` |
| `reativar` | `/{recursos}/{id}/reativar` | `PATCH` | `204 No Content` | `ResponseEntity<Void>` |

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| CRUD complexo com filtros avançados e relacionamentos | `api/pessoas/controllers/PessoaController.java` |
| CRUD padrão com listagem paginada e total para selects | `api/pessoas/controllers/CategoriaController.java` e `ComorbidadeController.java` |
| Controller de integração externa | `api/pessoas/controllers/CepController.java` |

---

## Service

A camada de Service encapsula todas as regras de negócio, validações lógicas, controle transacional e a coordenação entre repositórios, DTOs e entidades JPA.

### Diretrizes de Implementação

- **Anotação de Camada:** anote sempre a classe com `@Service`.

- **Injeção de Dependências:** obrigatória via construtor (proibido o uso de `@Autowired` em atributos).

- **Controle Transacional:** utilize `@Transactional` para operações de escrita/modificação de estado no banco, e `@Transactional(readOnly = true)` para operações exclusivas de leitura.

- **Isolamento por DTOs:** métodos de serviços devem receber e retornar DTOs de forma estrita, nunca expondo entidades diretamente para as camadas externas.

- **Tratamento de Exceções de Domínio:** lance diretamente exceções customizadas de negócio (`ValidationException.of(...)`, `ConflitoDadosException`, `EntidadeNaoEncontradaException`) interceptadas pelo tratador global.

- **Ciclo de Vida e Atualização de Entidades:**
    - Utilize o método `.save()` do repositório exclusivamente para cadastros.
    - Para alterações de dados ou estado (desativação/reativação), recupere a entidade ativa e modifique os dados (via setters ou métodos encapsulados na entidade, a critério do desenvolvedor), permitindo o *dirty checking* transacional sem acionar `save()` redundante.

- **Filtros e Consultas:**
    - Para filtros simples (menos de 3 campos), utilize Derived Queries no Spring Data JPA.
    - Para entidades que demandem filtros dinâmicos com **3 ou mais campos**, utilize **JPA Specifications** (ex.: `PessoaSpecification.comFiltro(...)`).

- **Sanitização de Ordenação Obrigatória:** em todo método `listar` que receba `Pageable`, utilize **obrigatoriamente** `PaginacaoUtils.sanitizarPaginacao(...)`:
    - Defina uma constante privada na classe de Service contendo a *whitelist* de campos permitidos para ordenação: `private static final Set<String> CAMPOS_ORDENACAO_VALIDOS = Set.of("id", "nome", ...);`
    - Defina sempre um campo padrão de ordenação para requisições sem ordenação (`unsorted`) ou com campos inválidos.

- **Listagem Completa para Componentes de Seleção (Dropdowns):** para entidades de apoio de baixo volume (como Categorias e Comorbidades), forneça a sobrecarga `listarTodas(boolean incluirInativos)` e `listarTodas()` retornando `List<DTO.Resumo>` sem paginação. Entidades principais volumosas (ex.: `Pessoa`) não devem conter esse método.

### Tabela de Padronização de Métodos

| Método | Parâmetros de Entrada | Retorno | Anotação Transacional |
| --- | --- | --- | --- |
| `cadastrar` | `DTO.Requisicao` | `DTO.Detalhe` | `@Transactional` |
| `listar` | `[DTOFiltro], Pageable, boolean incluirInativos` | `RespostaPaginada<DTO.Resumo>` | `@Transactional(readOnly = true)` |
| `listarTodas` | `boolean incluirInativos` (opcional sem args com default `false`, só tabelas de apoio) | `List<DTO.Resumo>` | `@Transactional(readOnly = true)` |
| `buscarPorId` | `Long id` | `DTO.Detalhe` | `@Transactional(readOnly = true)` |
| `atualizar` | `Long id, DTO.Atualizacao` | `DTO.Detalhe` | `@Transactional` |
| `desativar` | `Long id` | `void` | `@Transactional` |
| `reativar` | `Long id` | `void` | `@Transactional` |

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| Regras de negócio avançadas, JPA Specification (3+ filtros) e validações cruzadas | `api/pessoas/services/PessoaService.java` |
| CRUD padrão com listagem não paginada para seleção | `api/pessoas/services/CategoriaService.java` e `ComorbidadeService.java` |
| Consumo de integrações externas / clients | `api/pessoas/services/CepService.java` |

---

## Repository

A camada de Repository é responsável exclusivamente pela persistência e recuperação de dados, abstraindo o acesso ao banco por meio do Spring Data JPA.

### Diretrizes de Implementação

- **Declaração de Interface:** declare repositórios como uma `interface` que estende `JpaRepository<Entidade, Long>`.

- **Sem Anotação `@Repository`:** não utilize `@Repository`, pois interfaces derivadas de `JpaRepository` já são gerenciadas automaticamente pelo Spring.

- **Suporte a Specifications:** entidades que demandem filtros dinâmicos com 3 ou mais campos devem estender adicionalmente `JpaSpecificationExecutor<Entidade>` na interface do repositório.

- **Convenção de Derived Queries:**
    - Utilize as convenções do Spring Data JPA para montar consultas automáticas por nome do método (`findByX`, `existsByX`, `countByX`).
    - **Validação de Duplicidade:** use `existsByX` para cadastros e `existsByXAndIdNot` para atualizações (ex.: `existsByNome(String nome)`, `existsByNomeAndIdNot(String nome, Long id)`).
    - **Estado Ativo:** buscas unitárias e listagens padrão ativas devem assegurar o filtro de inativação: `findByIdAndDataInativoIsNull(Long id)` e `findAllByDataInativoIsNull(Pageable pageable)`.
    - **Listagens para Dropdowns:** forneça `findAllByDataInativoIsNull()` retornando `List<Entidade>` diretamente para tabelas de apoio consumidas por seletores.

- **Otimização de Carregamento (`@EntityGraph`):** utilize a anotação `@EntityGraph(attributePaths = {"..."})` nas consultas do repositório quando for necessário carregar relacionamentos mapeados sem incorrer em problemas de N+1.

- **Consultas Customizadas com `@Query`:** utilize JPQL apenas quando o nome da Derived Query for excessivamente longo ou envolver junções não suportadas nativamente pelo método de nome. Evite queries nativas (`nativeQuery = true`), salvo casos pontuais de relatórios complexos.

### Especificações Dinâmicas (`Specification`)

Para cenários com 3 ou mais campos de busca opcionais e dinâmicos:

- Crie uma classe utilitária final no pacote `repositories` (ex.: `PessoaSpecification`) com construtor privado.
- Centralize a lógica em métodos estáticos como `comFiltro(DTOFiltro filtro, boolean incluirInativos)` retornando `Specification<Entidade>`.
- Construa a lista de `Predicate` avaliando nulos e strings em branco, utilizando `criteriaBuilder.like`, `lower`, `equal`, e `criteriaBuilder.isNull / isNotNull` para o controle de `dataInativo`.
- Em filtros que realizem `join` com coleções associadas (ex.: categorias ou comorbidades), aplique `query.distinct(true)` para evitar duplicação de linhas na paginação.

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| Repositório com Specification, EntityGraph e validações | `api/pessoas/repositories/PessoaRepository.java` |
| Implementação de Specification com filtros múltiplos e Joins | `api/pessoas/repositories/PessoaSpecification.java` |
| Repositório CRUD padrão com Derived Queries e suporte a dropdowns | `api/pessoas/repositories/CategoriaRepository.java` e `ComorbidadeRepository.java` |

---

## Models

As entidades mapeiam as tabelas do banco de dados relacional e concentram o estado e os comportamentos essenciais do domínio da aplicação.

### Diretrizes de Implementação

- **Gestão de Esquema:** a criação e modificação de tabelas e colunas é de responsabilidade exclusiva dos scripts do Flyway. O Hibernate opera em modo de checagem com `spring.jpa.hibernate.ddl-auto: validate`.

- **Herança de Classes Base:** não redeclare campos padronizados como `id` ou `dataInativo`. Herde sempre de:
    - `EntidadeBase`: fornece `Long id`.
    - `EntidadeInativavel`: estende `EntidadeBase` e adiciona `LocalDateTime dataInativo` e o método utilitário `isAtivo()`.

- **Encapsulamento e Construtores:**
    - Mantenha atributos privados com getters e setters.
    - Forneça um construtor `protected` sem argumentos exigido pela especificação JPA.
    - Forneça construtores públicos para instanciação com parâmetros obrigatórios e valores padrão pré-definidos.
    - Métodos específicos de negócio (como `atualizarDados(...)`, `desativar()` e `reativar()`) podem ser adicionados para encapsular mudanças de estado, caso desejado.

- **Relacionamentos e Performance:**
    - Por padrão, utilize `fetch = FetchType.LAZY` em relacionamentos simples `@ManyToOne` e `@OneToOne`.
    - Inicialize coleções (`@ManyToMany`, `@OneToMany`) diretamente na declaração (ex.: `new HashSet<>()`).
    - Em coleções associativas `@ManyToMany`, utilize a anotação `@BatchSize(size = 25)` para otimizar o carregamento das associações e mitigar o impacto de N+1.

- **Nomenclatura Relacional:**
    - Mapeie explicitamente o nome da tabela no singular com `@Table(name = "...")` acompanhando as migrations (ex.: `pessoa`, `categoria`, `comorbidade`).
    - Mapeie colunas em *snake_case* utilizando `@Column(name = "...")`.
    - Mapeie campos de texto extensos sem limite prévio via `@Column(columnDefinition = "TEXT")`.
    - Enums mapeados no banco devem conter expressamente a anotação `@Enumerated(EnumType.STRING)`.

### Padrão de Enums de Domínio

Enums consumidos pelas APIs (ex.: `Escolaridade`, `Sexo`, `RendaFamiliar`, `TipoPessoa`):

- **Serialização de Saída (GET):** retornam um objeto estruturado contendo `{ "codigo": "...", "descricao": "..." }` utilizando a anotação `@JsonFormat(shape = JsonFormat.Shape.OBJECT)`.
- **Desserialização de Entrada (POST / PUT):** aceitam a String simples do nome ou código através do método anotado com `@JsonCreator` (ex.: `deCodigo(String valor)`). Valores inexistentes lançam `ValidationException.of(...)` gerando status HTTP 400 Bad Request.

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| Entidade com relacionamentos complexos, ManyToMany e `@BatchSize` | `api/pessoas/models/Pessoa.java` |
| Entidades padrão de domínio com soft delete | `api/pessoas/models/Categoria.java` e `Comorbidade.java` |
| Classes base abstratas | `api/comum/models/EntidadeBase.java` e `EntidadeInativavel.java` |
| Enum de domínio com formato duplo | `api/pessoas/models/Escolaridade.java` (e `Sexo.java`) |

---

## Tratamento de Exceções

O tratamento de erros é centralizado e padronizado pelo protocolo **RFC 7807 / RFC 9457 (Problem Details)**, eliminando o uso de blocos `try-catch` em Controllers e Services.

### Diretrizes de Implementação

- Exceções devem ser lançadas livremente com o `throw`, sem uso de `try-catch`, para serem interceptadas pelo tratador global.
- Utiliza-se a classe `ProblemDetail` do Spring Boot 3+ em vez de classes de resposta customizadas manuais.
- Regras de negócio violadas devem ser lançadas via métodos estáticos `ValidationException.of(...)` (400 Bad Request) ou `ValidationException.ofConflito(...)` (409 Conflict).
- Todo payload de erro conterá os campos padrão: `type`, `title`, `status`, `detail`, `instance`, `timestamp` e a lista `erros` (quando aplicável).

### Estrutura de Arquivos e Responsabilidades

1. **`ValidationException`** (`api/comum/exceptions/ValidationException.java`)
   Exceção customizada de negócio (Runtime) para inconsistências e conflitos de regras.
    - **Record Interno:** `CampoErro(String campo, String mensagem)`
    - **Métodos de Fábrica:** `static ValidationException of(String campo, String mensagem)` e `static ValidationException ofConflito(...)`

2. **`EntidadeNaoEncontradaException`** (`api/comum/exceptions/EntidadeNaoEncontradaException.java`)
   Exceção customizada de negócio para consultas sem resultado (gera HTTP 404 Not Found).
    - **Construtor:** recebe a mensagem explicativa do recurso não encontrado.

3. **`ConflitoDadosException`** (`api/comum/exceptions/ConflitoDadosException.java`)
   Exceção lançada quando chaves ou atributos únicos já existem no banco (gera HTTP 409 Conflict).

4. **`GlobalExceptionHandler`** (`api/comum/exceptions/GlobalExceptionHandler.java`)
   Componente anotado com `@RestControllerAdvice` **que estende `ResponseEntityExceptionHandler`**. Traduz exceções em respostas no formato `ProblemDetail`:
    - Trata `ValidationException` (400 ou 409).
    - Sobrescreve `handleMethodArgumentNotValid` para mapear erros de anotações do `@Valid` (400 Bad Request).
    - Trata `EntidadeNaoEncontradaException` (404 Not Found).
    - Trata `ConflitoDadosException` (409 Conflict).
    - Trata `Exception.class` como fallback final (500 Internal Server Error).

---

## Controle de Esquema e Migrations (Flyway)

O gerenciamento do banco de dados relacional é feito exclusivamente via **Flyway**, garantindo versionamento estruturado e paridade de esquema entre os ambientes locais e de produção.

### Diretrizes de Uso

- Todos os scripts devem residir em `src/main/resources/db/migration/`.
- Padrão de nomenclatura obrigatório: `V<Versão>__<descricao_em_snake_case>.sql`
- Scripts que já foram executados em outros ambientes ou branch principal **nunca devem ser editados**. Qualquer alteração ou correção exige a criação de uma nova versão sequencial (`V2`, `V3`, etc.).

---

## Organização dos diretórios

A arquitetura do projeto agrupa componentes por módulo funcional (ex.: `pessoas`, `acesso`) e mantém classes utilitárias, infraestrutura e classes transversais centralizadas no pacote `comum`.

```
.
├── pom.xml
├── docker-compose.yml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── br/org/asmosul/api
│   │   │       ├── ApiApplication.java
│   │   │       ├── comum
│   │   │       │   ├── config/
│   │   │       │   ├── dtos
│   │   │       │   │   └── RespostaPaginada.java
│   │   │       │   ├── exceptions
│   │   │       │   │   ├── ConflitoDadosException.java
│   │   │       │   │   ├── EntidadeNaoEncontradaException.java
│   │   │       │   │   ├── GlobalExceptionHandler.java
│   │   │       │   │   └── ValidationException.java
│   │   │       │   ├── models
│   │   │       │   │   ├── EntidadeBase.java
│   │   │       │   │   └── EntidadeInativavel.java
│   │   │       │   └── utils
│   │   │       │       └── PaginacaoUtils.java
│   │   │       └── pessoas
│   │   │           ├── controllers
│   │   │           │   ├── CategoriaController.java
│   │   │           │   ├── CepController.java
│   │   │           │   ├── ComorbidadeController.java
│   │   │           │   └── PessoaController.java
│   │   │           ├── dtos
│   │   │           │   ├── CategoriaDTO.java
│   │   │           │   ├── CepDTO.java
│   │   │           │   ├── ComorbidadeDTO.java
│   │   │           │   ├── PessoaDTO.java
│   │   │           │   └── PessoaFiltroDTO.java
│   │   │           ├── models
│   │   │           │   ├── Categoria.java
│   │   │           │   ├── Comorbidade.java
│   │   │           │   ├── Escolaridade.java
│   │   │           │   ├── Pessoa.java
│   │   │           │   ├── RendaFamiliar.java
│   │   │           │   ├── Sexo.java
│   │   │           │   ├── TipoPessoa.java
│   │   │           │   └── Uf.java
│   │   │           ├── repositories
│   │   │           │   ├── CategoriaRepository.java
│   │   │           │   ├── ComorbidadeRepository.java
│   │   │           │   ├── PessoaRepository.java
│   │   │           │   └── PessoaSpecification.java
│   │   │           └── services
│   │   │               ├── CategoriaService.java
│   │   │               ├── CepService.java
│   │   │               ├── ComorbidadeService.java
│   │   │               └── PessoaService.java
│   │   └── resources
│   │       ├── application.yaml
│   │       └── db/migration
│   │           ├── V1__criar_tabelas_iniciais.sql
│   │           ├── V2__corrigir_faixa_de_renda.sql
│   │           ├── V3__adicionar_data_inativo_categiria.sql
│   │           ├── V4__adicionar_data_inativo_comorbidade.sql
│   │           ├── V5__criar_tabelas_pessoas_e_relacionamentos.sql
│   │           ├── V6__corrigir_faixa_de_renda_mais_de_tres_mil.sql
│   │           ├── V7__corrigir_enum_sexo.sql
│   │           ├── V8__adicionar_cnpj.sql
│   │           ├── V9__adicionar_endereco_pessoa.sql
│   │           ├── V10__permitir_data_nascimento_nula_pessoa.sql
│   │           └── V11__criar_tabelas_modulo_acesso.sql
│   └── test
│       └── java
│           └── br/org/asmosul/api
│               ├── comum
│               │   ├── config
│               │   │   └── BaseAPITest.java
│               │   └── exceptions
│               │       └── GlobalExceptionHandlerTest.java
│               └── pessoas
│                   ├── controllers
│                   │   ├── CategoriaControllerTest.java
│                   │   ├── CepControllerTest.java
│                   │   ├── ComorbidadeControllerTest.java
│                   │   └── PessoaControllerTest.java
│                   └── services
│                       └── PessoaServiceTest.java
```

---

## Padrão de Testes Automatizados

A cobertura de testes automatizados no backend prioriza **testes de integração ponta a ponta na camada de Controller**.

### Diretrizes e Regras Gerais

- **Padrão Obrigatório (Maior Volume — Integração de Endpoints):**
    - Todo endpoint deve possuir cobertura estendendo `BaseAPITest` através de `MockMvc`.
    - Cenários essenciais a cobrir:
        1. **Sucesso (Caminho Feliz):** requisição com dados válidos (`200 OK` ou `201 Created` com header `Location`).
        2. **Validação de Entrada:** falha por campos inválidos, em branco ou fora do padrão do Bean Validation (`400 Bad Request`).
        3. **Integridade e Conflito:** tentativa de duplicidade de chaves únicas como CPF/CNPJ, nome ou e-mail (`409 Conflict`).
        4. **Recurso Não Encontrado:** consulta ou atualização por ID inexistente ou inativo (`404 Not Found`).
        5. **Transições de Estado:** operações de desativação e reativação lógica (`204 No Content`).

- **Exceção Pontual (Menor Volume — Testes Unitários de Service):**
    - Testes na camada de Service **não são obrigatórios para operações CRUD triviais**.
    - Devem ser criados apenas quando houver regras de negócio ricas, validações cruzadas complexas, controle temporal ou cálculos acumulados.
    - Devem ser testes unitários rápidos utilizando `@ExtendWith(MockitoExtension.class)`, `@Mock` e `@InjectMocks`.

- **Segurança e Perfis de Acesso (401 / 403):**
    - Não é necessário testar segurança em todos os métodos de CRUD.
    - Criar ao menos um teste de autorização (`403 Forbidden`) por módulo funcional com `@WithMockUser(roles = "...")` quando perfis forem restritos.

- **Isolamento de Dados:** em testes que herdam de `BaseAPITest`, limpe os dados criados no `@BeforeEach` com `repository.deleteAll()` para evitar colisões entre execuções.

- **Legibilidade:** agrupe os cenários de teste utilizando anotações `@Nested` e `@DisplayName` para categorizar cada rota/operação.

### Nomenclatura dos Métodos de Teste

Adota-se a estrutura em três partes separadas por *underline*:

```
metodoEmTeste_cenarioOuCondicao_resultadoEsperado
```

Exemplos:

- `cadastrar_comDadosValidos_retornaStatus201ELocationHeader`
- `cadastrar_comNomeEmBranco_retornaStatus400`
- `cadastrar_comCpfDuplicado_retornaStatus409`
- `buscarPorId_comIdInexistenteOuInativo_retornaStatus404`

### Matriz de Cobertura de Testes

| Camada / Contexto | Cenário Avaliado | Status HTTP / Resultado | Quando Criar |
| --- | --- | --- | --- |
| Controller (Integração) | Sucesso no cadastro/atualização/busca | `200 OK` / `201 Created` | Todo endpoint |
| Controller (Integração) | Violação de Bean Validation (`@Valid`) | `400 Bad Request` | Todo endpoint com entrada de dados |
| Controller (Integração) | Busca por ID inexistente ou inativo | `404 Not Found` | Todo endpoint que busca por identificador |
| Controller (Integração) | Duplicidade (CPF, CNPJ, E-mail, Nome) | `409 Conflict` | Operações de cadastro/atualização com unicidade |
| Controller (Segurança) | Acesso sem token ou credencial inválida | `401 Unauthorized` | Endpoints do fluxo de autenticação |
| Controller (Segurança) | Perfil sem permissão (`@WithMockUser`) | `403 Forbidden` | Ao menos um por módulo funcional |
| Service (Unitário) | Validações condicionais e regras ricas | Lançamento de `ValidationException` | Exclusivo para regras de negócio complexas |

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| Configuração Base com Testcontainers | `api/comum/config/BaseAPITest.java` |
| Testes de Integração completos com filtros, validações e soft delete | `api/pessoas/controllers/PessoaControllerTest.java` e `CategoriaControllerTest.java` |
| Testes Unitários com Mockito para regras complexas | `api/pessoas/services/PessoaServiceTest.java` |
