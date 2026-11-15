# BarberHub

Sistema modular de gestão e agendamento para barbearias. O cliente agenda online; a barbearia gerencia serviços, clientes, equipe, estoque, financeiro, assinaturas e recuperação de clientes.

> Status: em construção. Projeto em Sprints de duas semanas (Scrum com quadro Kanban). Sprint 1: 11/11/2026 a 25/11/2026.

## Tecnologias

- Back-end: Java 25 com Spring Boot 4, JPA, Spring Security e Flyway (Maven)
- Banco de dados: PostgreSQL 17 (Supabase em homologação)
- Front-end: React com Vite (JavaScript)

## Estrutura do repositório

```
barberhub/
├── backend/     API em Spring Boot, organizada por módulos
├── frontend/    Aplicação React, organizada por módulos
├── README.md
├── .env.example
└── .gitignore
```

## Pré-requisitos

- Java 25 (JDK). Confira com `java -version`.
- Node.js 20.19 ou superior. Confira com `node -v`.
- Git.
- Um banco PostgreSQL 17: o de homologação no Supabase (peça o acesso ao responsável pelo ambiente) ou um local, como descrito abaixo.

O Maven não precisa ser instalado: o projeto usa o `mvnw` da pasta `backend`.

## Como rodar o sistema

1. Clone o repositório:
   ```bash
   git clone https://github.com/IanKazuhiro/barberhub.git
   cd barberhub
   git checkout develop
   ```
2. Defina as variáveis de ambiente do back-end (seção abaixo).
3. Suba o back-end, em um terminal:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   No Windows (PowerShell), use `.\mvnw spring-boot:run`. Espere a linha `Started BarberhubBackendApplication`. A API fica em `http://localhost:8080`.
4. Suba o front-end, em outro terminal:
   ```bash
   cd frontend
   cp .env.example .env
   npm install
   npm run dev
   ```
   No PowerShell, use `Copy-Item .env.example .env`. A aplicação abre em `http://localhost:5173`.
5. Confira: a página inicial mostra "Servidor: ok. Banco: ok.". A rota de teste do back-end é `GET /api/saude`.

## Variáveis de ambiente

### Back-end

O back-end lê as variáveis do **sistema**, e não de um arquivo `.env`. O modelo com todos os nomes está em `.env.example`, na raiz.

| Variável | Para que serve |
| --- | --- |
| `BANCO_URL` | Endereço de conexão JDBC com o PostgreSQL |
| `BANCO_USUARIO` | Usuário do banco |
| `BANCO_SENHA` | Senha do banco |
| `CORS_ORIGENS` | Endereços do front-end autorizados a chamar a API (opcional; padrão `http://localhost:5173`) |
| `JWT_SEGREDO` e `JWT_EXPIRACAO_MINUTOS` | Login por token (UC02); ainda não são usadas |

Defina-as de uma destas formas:

- **Windows, de forma permanente:** `Win + R`, digite `rundll32 sysdm.cpl,EditEnvironmentVariables`, crie as variáveis em "Variáveis de usuário" e reabra o terminal e o editor.
- **PowerShell, só na janela atual:** `$env:BANCO_URL="..."` (uma linha por variável).
- **Linux e macOS:** `export BANCO_URL="..."` no terminal ou no arquivo de perfil do shell.

### Front-end

O front-end lê o arquivo `frontend/.env`, que não é versionado. Crie-o a partir de `frontend/.env.example`.

| Variável | Para que serve |
| --- | --- |
| `VITE_API_URL` | Endereço do back-end (padrão `http://localhost:8080`) |

## Banco de dados

- **Homologação:** projeto Supabase do grupo. Use a conexão **Session pooler** do painel, no formato JDBC: `jdbc:postgresql://HOST:5432/postgres?sslmode=require`, com o usuário no formato `postgres.CODIGO_DO_PROJETO`. A senha fica só com o responsável pelo ambiente.
- **Local (opcional), com Docker:**
  ```bash
  docker run --name barberhub-banco -e POSTGRES_USER=barberhub -e POSTGRES_PASSWORD=troque_esta_senha -e POSTGRES_DB=barberhub -p 5432:5432 -d postgres:17
  ```
  Nesse caso: `BANCO_URL=jdbc:postgresql://localhost:5432/barberhub`, `BANCO_USUARIO=barberhub` e `BANCO_SENHA=troque_esta_senha`.
- As tabelas são criadas só pelas migrações do Flyway, em `backend/src/main/resources/db/migration`. Ninguém cria nem altera tabelas à mão.
- O teste automático do back-end (`./mvnw test`) sobe a aplicação inteira e por isso precisa das mesmas variáveis do banco.

## Ambiente de homologação

O sistema está publicado para testes e demonstração no Render (plano gratuito), com o banco no Supabase.

| Parte | Endereço |
|---|---|
| Front-end | https://barberhub-frontend.onrender.com |
| Back-end (verificação) | https://barberhub-backend-r9zp.onrender.com/api/saude |

**Atenção: o back-end hiberna após 15 minutos sem uso.** O primeiro acesso depois disso pode levar de 1 a 2 minutos. Antes de uma demonstração, abra o endereço do back-end alguns minutos antes e espere responder `{"aplicacao":"ok","banco":"ok"}`.

O banco do Supabase pausa depois de cerca de 1 semana sem atividade. Se isso acontecer, é só abrir o projeto no painel do Supabase e clicar em **Restore project**.

Como o ambiente é publicado:
- O back-end roda em Docker a partir da pasta `backend`, e as variáveis `BANCO_URL`, `BANCO_USUARIO`, `BANCO_SENHA` e `CORS_ORIGENS` ficam configuradas no painel do Render (nunca no repositório).
- O front-end é um site estático gerado da pasta `frontend`, com `VITE_API_URL` apontando para o back-end.
- Cada push na branch publicada gera um novo deploy automático.

## Como trabalhamos

- Branches: `main` guarda a versão estável entregue; `develop` reúne o trabalho da Sprint.
- Uma branch por cartão, criada a partir da `develop`: `feature/UC09-agendar-servico` (ou `fix/` para correção).
- Commits pequenos, no imperativo, com o número do cartão: `UC09: impede agendamento duplo`.
- Pull request sempre para a `develop`, com a descrição do que mudou, como testar e o link do cartão. Só mescla com duas aprovações: um revisor de código e um revisor funcional.
- Senhas, chaves e dados de conexão nunca entram no repositório.
- Todo o projeto é escrito em português: código, tabelas, commits e documentação.

Regras completas: documento **Definition of Done** do projeto.

## Documentação

Escopo, Especificação de Requisitos (ERS), Modelagem de Dados, Definition of Done e planejamento das Sprints ficam no quadro Trello do projeto.