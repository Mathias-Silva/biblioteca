# Biblioteca Pessoal

Sistema web de gestão de biblioteca pessoal com **Spring Boot 3.3.4**, **Java 21**, **MongoDB**, **Spring Security** (sessão + Thymeleaf) e integração com a **Google Books API** (simulada em testes via WireMock).

## Visão geral

- Cadastro e autenticação de usuários
- CRUD de livros por usuário (cada usuário vê apenas os próprios livros)
- Busca de metadados de livros por ISBN na Google Books API
- Frontend responsivo com Bootstrap 5 e Thymeleaf

## Pré-requisitos

| Ferramenta | Versão mínima |
|------------|---------------|
| Java JDK   | 21            |
| Maven      | 3.9+ (ou use `./mvnw`) |
| Docker     | Para MongoDB local e Testcontainers nos testes |
| MongoDB    | 6.x (local ou container) |

## Execução local

### 1. Subir o MongoDB

```bash
docker run -d --name biblioteca-mongo -p 27017:27017 mongo:6.0
```

### 2. Configurar a aplicação

Edite `src/main/resources/application.properties` se necessário:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/biblioteca_pessoal
api.google-books.url=https://www.googleapis.com/books/v1
app.admin.default-password=AdminSenha123
```

### 3. Executar a aplicação

```bash
./mvnw spring-boot:run
```

Acesse: [http://localhost:8080/login](http://localhost:8080/login)

Na primeira execução é criado o usuário administrador `admin@email.com` com a senha definida em `app.admin.default-password`.

## Testes

Os testes **não utilizam mocks** de repositório ou serviço:

- **MongoDB**: Testcontainers (`mongo:6.0`)
- **Google Books**: WireMock com mapeamentos em `src/test/resources/mappings/`

```bash
./mvnw clean verify
```

### Relatórios JaCoCo

Após `verify`, os relatórios ficam em:

| Artefato | Caminho |
|----------|---------|
| Relatório HTML | `target/site/jacoco/index.html` |
| Relatório XML (SonarCloud) | `target/site/jacoco/jacoco.xml` |

O build **falha** se a cobertura de linhas for inferior a **80%** (regra configurada no `jacoco-maven-plugin`).

## CI/CD

O workflow `.github/workflows/ci.yml` executa:

1. `mvn clean verify` (testes + JaCoCo check)
2. Análise SonarCloud com envio do `jacoco.xml`
3. Validação do Quality Gate (`sonar.qualitygate.wait=true`)

Configure o secret `SONAR_TOKEN` no repositório GitHub (Settings → Secrets → Actions):

1. Gere um token em [SonarCloud → My Account → Security](https://sonarcloud.io/account/security).
2. O usuário do token precisa ter permissão de **Execute Analysis** no projeto `Mathias-Silva_biblioteca` da organização `mathias-silva`.
3. O job `sonarcloud` só roda se `SONAR_TOKEN` estiver definido; o job `build-and-test` (testes + JaCoCo 80%) é o gate obrigatório do CI.

## Estrutura principal

```
src/main/java/com/example/biblioteca/
├── controller/     # LivroController, UsuarioController
├── service/        # LivroService, UsuarioService
├── repository/     # Spring Data MongoDB
├── config/         # Security, exceções
└── BibliotecaApplication.java

src/test/java/.../
├── AbstractIntegrationTest.java
├── service/LivroServiceTest.java
├── service/UsuarioServiceIntegrationIT.java
└── controller/LivroControllerTest.java, LoginSecurityIT.java, UsuarioControllerE2EIT.java
```

## Rastreabilidade

Consulte [RTM.md](RTM.md) para o mapeamento completo requisito → teste automatizado.
