# RTM — Matriz de Rastreabilidade de Requisitos

Mapeamento dos requisitos funcionais aos testes automatizados (sem mocks de persistência ou APIs externas).

| ID | Requisito | Classe de produção | Classe de teste | Método de teste |
|----|-----------|-------------------|-----------------|-----------------|
| REQ-001 | CRUD de livros (controller) | `LivroController` | `LivroControllerTest` | `deveExibirListaDeLivros`, `deveExibirTelaDeNovoLivro`, `deveSalvarNovoLivroERedirecionar`, `deveExcluirLivroERedirecionar`, `deveBuscarIsbnViaApi` |
| REQ-001 | CRUD de livros (serviço) | `LivroService` | `LivroServiceTest` | `deveSalvarLivroComSucesso`, `deveListarPorUsuario`, `deveListarTodosOsLivros`, `deveBuscarPorIsbnComSucesso_UsandoWireMock`, `deveBuscarPorIsbnSemResultado` |
| REQ-002 | Autenticação (login sucesso/falha) | `SecurityConfig`, `UsuarioService` | `LoginSecurityIT` | `deveAutenticarUsuarioComCredenciaisCorretas`, `deveFalharLoginComCredenciaisInvalidas` |
| REQ-002 | Cadastro de usuário (front + controller) | `UsuarioController` | `UsuarioControllerE2EIT` | `deveCadastrarUsuarioViaFormulario` |
| REQ-002 | Cadastro e criptografia (serviço) | `UsuarioService` | `UsuarioServiceIntegrationIT` | Testes de `UsuarioServiceIntegrationIT` |
| REQ-002 | Proteção CSRF (cadastro e livros) | `SecurityConfig` | `LoginSecurityIT`, `LivroControllerTest` | `deveBloquearPostSemCsrf` (Login/usuarios), `deveBloquearPostSemCsrf`, `deveAceitarPostComCsrfValido` (livros) |
| REQ-002 | Isolamento de dados entre usuários | `LivroController`, `LivroService` | `LoginSecurityIT`, `LivroServiceTest` | `usuarioBNaoPodeEditarLivroDoUsuarioA`, `usuarioBNaoPodeExcluirLivroDoUsuarioA`, `listagemIsolaDadosPorUsuario` |
| REQ-003 | Integração Google Books (busca por ISBN) | `LivroService` | `LivroServiceTest` | `deveBuscarPorIsbnComSucesso_UsandoWireMock`, `deveBuscarPorIsbnSemResultado` |
| REQ-004 | Integração ViaCEP / Busca de CEP (preenchimento de endereço no cadastro) | `CepService`, `UsuarioController` | `CepServiceTest` | `deveBuscarCepComSucesso_UsandoWireMock`, `deveRetornarVazioParaCepInvalido` |

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

## REQ-004: Integração ViaCEP (Busca de CEP)

### Descrição
Recurso para obter dados de endereço a partir do CEP durante o fluxo de cadastro de usuário. Implementado com `CepService` que consome a API ViaCEP; em testes de integração usamos WireMock (configurado em AbstractIntegrationTest).

### Diagrama de sequência — Busca de CEP (WireMock)

```mermaid
sequenceDiagram
    actor U as Browser (cadastro.html)
    participant C as UsuarioController
    participant S as CepService
    participant W as WireMock (mappings/ ViaCEP)

    U->>C: GET /usuarios/buscar-cep?cep=01001-000
    C->>S: buscar(cep)
    S->>W: GET /ws/01001000/json
    W-->>S: 200 JSON (stub)
    S-->>C: CepLookupDTO
    C-->>U: 200 JSON (endereço)
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
    ├── CepServiceTest.java
    ├── LivroServiceTest.java
    └── UsuarioServiceIntegrationIT.java
```
