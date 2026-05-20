# RTM — Matriz de Rastreabilidade de Requisitos

Mapeamento dos requisitos funcionais aos testes automatizados (sem mocks de persistência ou APIs externas).

| ID | Requisito | Classe de produção | Classe de teste | Método de teste |
|----|-----------|-------------------|-----------------|-----------------|
| REQ-001 | CRUD de livros (controller) | `LivroController` | `LivroControllerTest` | `deveExibirListaDeLivros`, `deveExibirTelaDeNovoLivro`, `deveSalvarNovoLivroERedirecionar`, `deveExibirTelaDeEdicaoSeLivroExistir`, `deveAtualizarLivroERedirecionar`, `deveExcluirLivroERedirecionar` |
| REQ-001 | CRUD de livros (serviço) | `LivroService` | `LivroServiceTest` | `deveSalvarLivroComSucesso`, `deveListarPorUsuario`, `deveListarTodosOsLivros`, `usuariosNaoDevemVerLivrosUnsDosOutros` |
| REQ-002 | Autenticação (login sucesso/falha) | `SecurityConfig`, `UsuarioService` | `LoginSecurityIT` | `deveAutenticarUsuarioComCredenciaisCorretas`, `deveFalharLoginComCredenciaisInvalidas` |
| REQ-002 | Cadastro de usuário | `UsuarioController` | `UsuarioControllerE2EIT` | `deveCadastrarUsuarioViaFormulario` |
| REQ-002 | Cadastro e criptografia (serviço) | `UsuarioService` | `UsuarioServiceIntegrationIT` | Testes de `UsuarioServiceIntegrationIT` |
| REQ-002 | Proteção CSRF (cadastro) | `SecurityConfig` | `LoginSecurityIT` | `deveBloquearPostSemCsrf` |
| REQ-002 | Proteção CSRF (livros) | `SecurityConfig` | `LivroControllerTest` | `deveBloquearPostSemCsrf`, `deveAceitarPostComCsrfValido` |
| REQ-002 | Isolamento de dados entre usuários | `LivroController` | `LoginSecurityIT` | `usuarioBNaoPodeEditarLivroDoUsuarioA`, `usuarioBNaoPodeExcluirLivroDoUsuarioA`, `listagemIsolaDadosPorUsuario` |
| REQ-003 | Integração Google Books | `LivroService` | `LivroServiceTest` | `deveBuscarInformacoesExternasComSucesso_UsandoWireMock`, `deveBuscarInformacoesExternasSemResultado` |

---

## REQ-001: CRUD de Livros

### Diagrama de sequência — Cadastrar livro

```mermaid
sequenceDiagram
    actor U as Browser (Thymeleaf)
    participant C as LivroController
    participant S as LivroService
    participant R as LivroRepository
    participant M as MongoDB (Testcontainers)

    U->>C: POST /livros/salvar (form + CSRF)
    C->>S: salvar(dto, emailSessao)
    S->>R: save(Livro)
    R->>M: insert documento
    M-->>R: Livro persistido
    R-->>S: Livro
    S-->>C: LivroResponseDTO
    C-->>U: 302 redirect /livros
```

### Diagrama de sequência — Isolamento na edição

```mermaid
sequenceDiagram
    actor UB as Usuário B (Browser)
    participant C as LivroController
    participant R as LivroRepository
    participant M as MongoDB (Testcontainers)

    UB->>C: GET /livros/editar/{id}
    C->>R: findById(id)
    R->>M: findById
    M-->>R: Livro (usuarioId = usera@)
    C-->>UB: 403 Forbidden
```

---

## REQ-002: Autenticação e Segurança

### Diagrama de sequência — Login com sucesso

```mermaid
sequenceDiagram
    actor U as Browser (Login.html)
    participant SEC as Spring Security
    participant US as UsuarioService
    participant R as UsuarioRepository
    participant M as MongoDB (Testcontainers)

    U->>SEC: POST /login (username, password, CSRF)
    SEC->>US: loadUserByUsername(email)
    US->>R: findByEmail(email)
    R->>M: query usuarios
    M-->>R: Usuario (senha BCrypt)
    SEC-->>U: 302 redirect /livros
```

### Diagrama de sequência — Bloqueio CSRF

```mermaid
sequenceDiagram
    actor A as Cliente sem token
    participant SEC as Spring Security

    A->>SEC: POST /livros/salvar (sem _csrf)
    SEC-->>A: 403 Forbidden
```

---

## REQ-003: Integração Google Books

### Diagrama de sequência — Busca por ISBN (WireMock)

```mermaid
sequenceDiagram
    participant T as LivroServiceTest
    participant S as LivroService
    participant W as WireMock (mappings/)

    T->>S: buscarInformacoesExternas(isbn)
    S->>W: GET /books/v1/volumes?q=isbn:{isbn}
    W-->>S: 200 JSON (stub)
    S-->>T: sem exceção
```

---

## Estrutura de testes (após limpeza)

```
src/test/java/com/example/biblioteca/
├── AbstractIntegrationTest.java
├── TestcontainersConfiguration.java
├── BibliotecaApplicationTests.java
├── controller/
│   ├── LivroControllerTest.java
│   ├── LoginSecurityIT.java
│   └── UsuarioControllerE2EIT.java
└── service/
    ├── LivroServiceTest.java
    └── UsuarioServiceIntegrationIT.java
```
