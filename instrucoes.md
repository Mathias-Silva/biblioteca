# Toda a baboseira abaixo é gerada por IA, qualquer dúvida, perguntem-me.

# Instruções para Integração com SonarCloud

Este projeto já está preparado para a análise do SonarCloud via Maven e GitHub Actions. Como você não possui permissões de administrador no repositório, estas são as instruções para que o **dono do projeto** finalize a configuração.

## Passo 1: Configuração no Dashboard do SonarCloud

1. O dono do projeto deve logar no [SonarCloud](https://sonarcloud.io/) usando a conta do GitHub.
2. Clicar no ícone **+** (canto superior direito) > **Analyze new project**.
3. Selecionar este repositório (`biblioteca`).
4. Após a criação, o SonarCloud fornecerá duas informações cruciais:
   - **Organization Key** (ex: `minha-faculdade`)
   - **Project Key** (ex: `minha-faculdade_biblioteca`)

## Passo 2: Configuração do Token no GitHub

Para que o CI tenha permissão de enviar dados para o SonarCloud:

1. No SonarCloud, vá em **My Account** > **Security**.
2. Gere um novo token (ex: `sonar-github-token`) e **copie-o**.
3. No GitHub, vá na aba **Settings** do repositório.
4. Vá em **Secrets and variables** > **Actions**.
5. Clique em **New repository secret**.
   - **Name:** `SONAR_TOKEN`
   - **Secret:** (cole o token copiado do SonarCloud)

## Passo 3: Ajuste no Arquivo CI

Após obter as chaves no Passo 1, o arquivo `.github/workflows/ci.yml` deve ser atualizado nas linhas 47 e 48:

```yaml
# De:
-Dsonar.projectKey=minha-org_meu-projeto 
-Dsonar.organization=minha-org 

# Para:
-Dsonar.projectKey=CHAVE_DO_PROJETO_AQUI
-Dsonar.organization=CHAVE_DA_ORG_AQUI
```

---

## O que já foi feito no código:

- **No `pom.xml`**: Adicionamos o plugin do **JaCoCo**, que é responsável por medir a cobertura de testes (Test Coverage) e gerar o relatório que o SonarCloud lê.
- **No `.github/workflows/ci.yml`**: Criamos um pipeline otimizado que:
  1. Instala o Java 21.
  2. Executa todos os testes unitários e de integração.
  3. Gera o relatório de cobertura.
  4. Dispara a análise para o SonarCloud usando o `SONAR_TOKEN`.
  5. Salva o `.jar` do projeto como artefato em caso de sucesso.
