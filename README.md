# BarberHub

Sistema modular de gestão e agendamento para barbearias. O cliente agenda online; a barbearia gerencia serviços, clientes, equipe, estoque, financeiro, assinaturas e recuperação de clientes.

> Status: em construção. Projeto em Sprints de duas semanas (Scrum com quadro Kanban). Sprint 1: 11/11/2026 a 25/11/2026. Já funcionam o cadastro de clientes e o login com perfis (cliente, barbeiro e administrador).

## Tecnologias

- Back-end: Java 25 com Spring Boot 4, JPA, Spring Security (login por token JWT) e Flyway (Maven)
- Banco de dados: PostgreSQL 17 (Supabase em homologação)
- Front-end: React com Vite (JavaScript)
- Estilo visual: glassmorphism com minimalismo moderno em tons de cinza (estilo base em `frontend/src/index.css`)

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
2. Defina as variáveis de ambiente do back-end (seção abaixo). **Sem `JWT_SEGREDO` o back-end não sobe.**
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
5. Confira: a aplicação abre na tela de entrada (login) e, embaixo dela, aparece "Servidor: ok. Banco: ok.". A rota de teste do back-end é `GET /api/saude`.
6. Para testar, crie uma conta pela tela de cadastro (ela sempre será de **cliente**) e entre com ela. Ao entrar, você chega à tela inicial do seu perfil.

## Acesso e perfis

- **Cadastro público:** toda conta criada pela tela de cadastro tem o perfil Cliente. Não existe cadastro público de barbeiro nem de administrador.
- **Administrador inicial:** a migração do banco cria o usuário `admin@barberhub.local`, sem senha. Na primeira subida do back-end com `ADMIN_SENHA_INICIAL` definida, essa senha é gravada (apenas se ainda não existir uma). Depois disso a variável não é mais necessária.
- **Barbeiros:** serão criados pelo administrador (UC03, ainda em desenvolvimento).
- **Sessão:** o login devolve um token com validade (60 minutos por padrão). Ao sair, ou quando o token expira, é preciso entrar novamente.
- **Rotas por perfil:** `/api/admin/**` é só do administrador, `/api/barbeiro/**` só do barbeiro e `/api/cliente/**` só do cliente. Sem login a API responde 401; com perfil errado, 403.

## Variáveis de ambiente

### Back-end

O back-end lê as variáveis do **sistema**, e não de um arquivo `.env`. O modelo com todos os nomes está em `.env.example`, na raiz.

| Variável | Para que serve |
| --- | --- |
| `BANCO_URL` | Endereço de conexão JDBC com o PostgreSQL |
| `BANCO_USUARIO` | Usuário do banco |
| `BANCO_SENHA` | Senha do banco |
| `JWT_SEGREDO` | Chave que assina os tokens de login. **Obrigatória**, com no mínimo 32 caracteres. Gere uma própria e nunca a versione |
| `JWT_EXPIRACAO_MINUTOS` | Validade do token em minutos (opcional; padrão 60). Se for definida, precisa ter um número; vazia, impede o back-end de subir |
| `ADMIN_SENHA_INICIAL` | Senha inicial do administrador (opcional; usada só quando o administrador ainda não tem senha) |
| `CORS_ORIGENS` | Endereços do front-end autorizados a chamar a API (opcional; padrão `http://localhost:5173`) |

Defina-as de uma destas formas:

- **Windows, de forma permanente:** `Win + R`, digite `rundll32 sysdm.cpl,EditEnvironmentVariables`, crie as variáveis em "Variáveis de usuário" e reabra o terminal e o editor.
- **PowerShell, só na janela atual:** `$env:BANCO_URL="..."` (uma linha por variável). Vale só para aquele terminal.
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
- O banco de homologação é único e compartilhado entre quem roda localmente e o ambiente publicado. **Apague as contas de teste que criar** (primeiro o registro em `cliente`, depois o em `usuario`) e nunca apague o administrador inicial.

## Testes automáticos

```bash
cd backend
./mvnw test
```

- Os testes de cadastro, login, segurança por perfil e token não usam o banco.
- Um teste sobe a aplicação inteira e por isso precisa das variáveis do banco e de `JWT_SEGREDO` definidas no mesmo terminal.
- Testes das migrações com banco descartável (Testcontainers) ainda não existem; estão registrados como pendência.

## Ambiente de homologação

O sistema está publicado para testes e demonstração no Render (plano gratuito), com o banco no Supabase.

| Parte | Endereço |
|---|---|
| Front-end | https://barberhub-frontend.onrender.com |
| Back-end (verificação) | https://barberhub-backend-r9zp.onrender.com/api/saude |

**Atenção: o back-end hiberna após 15 minutos sem uso.** O primeiro acesso depois disso pode levar de 1 a 2 minutos. Antes de uma demonstração, abra o endereço do back-end alguns minutos antes e espere responder `{"aplicacao":"ok","banco":"ok"}`.

O banco do Supabase pausa depois de cerca de 1 semana sem atividade. Se isso acontecer, é só abrir o projeto no painel do Supabase e clicar em **Restore project**.

Como o ambiente é publicado:
- O back-end roda em Docker a partir da pasta `backend`. As variáveis `BANCO_URL`, `BANCO_USUARIO`, `BANCO_SENHA`, `JWT_SEGREDO`, `CORS_ORIGENS` e, se necessário, `JWT_EXPIRACAO_MINUTOS` e `ADMIN_SENHA_INICIAL` ficam configuradas no painel do Render (nunca no repositório). Uma variável opcional não deve ficar criada com valor vazio.
- O front-end é um site estático gerado da pasta `frontend`, com `VITE_API_URL` apontando para o back-end e uma regra de reescrita de `/*` para `/index.html`, necessária para as rotas do React.
- Cada push na branch `develop` gera um novo deploy automático dos dois serviços. O back-end leva cerca de 3 minutos e o front-end, segundos.

## Como trabalhamos

- Branches: `main` guarda a versão estável entregue; `develop` reúne o trabalho da Sprint.
- Uma branch por cartão, criada a partir da `develop`: `feature/UC09-agendar-servico` (ou `fix/` para correção).
- Commits pequenos, no imperativo, no formato `tipo(módulo): descrição (UCnn)`, por exemplo `feat(agendamento): impede agendamento duplo (UC09)`.
- Pull request sempre para a `develop`, com a descrição do que muda, regras aplicadas, decisões, como testar, verificação já feita e pendências, além do link do cartão. Só mescla com duas aprovações: um revisor de código e um revisor funcional. Nesta Sprint, por causa do prazo, o responsável técnico valida os pull requests.
- Senhas, chaves, tokens e dados de conexão nunca entram no repositório, no Postman exportado, no Trello nem em conversas.
- Todo o projeto é escrito em português: código, tabelas, commits e documentação.

Regras completas: documento **Definition of Done** do projeto.

## Documentação

Escopo, Especificação de Requisitos (ERS), Modelagem de Dados, Definition of Done e planejamento das Sprints ficam no quadro Trello do projeto.