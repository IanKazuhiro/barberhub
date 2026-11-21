# BarberHub — instruções para o Claude

Este arquivo é lido automaticamente pelo Claude Code ao abrir o repositório. Ele guarda o contexto do projeto e a forma de trabalhar combinada. **Atualize a seção "Estado atual" ao fim de cada cartão.**

## Como trabalhar com o Ian

- Ian é o responsável técnico e o **único que valida e mescla pull requests** (decisão tomada por causa do prazo). Ele estuda Engenharia de Software: explique o porquê das decisões em português simples, sem enrolar.
- Fale sempre em **português**. Código, tabelas, commits e documentação também ficam em português.
- Ao começar uma tarefa, diga em uma frase o que vai fazer. Ao terminar, diga o que mudou e como testar.
- Pergunte antes de qualquer coisa difícil de desfazer ou ambígua (mudança de banco, apagar dados, mudar arquitetura). Para o resto, siga em frente e avise as decisões tomadas.
- **Git:** pode criar branch e editar arquivos. **Commit, push, abrir PR, merge e alterações no banco de homologação só quando o Ian pedir.** Quando ele for fazer, entregue prontos: comandos (PowerShell, Windows, VS Code), mensagem do commit, título e descrição do PR e texto do merge.
- Nunca use `git branch -D`, `git reset --hard`, `git push --force` ou `git clean` sem pedido explícito. Para apagar branch, use `git branch -d`.
- Para o que o Ian executa, dê passo a passo numerado e diga o resultado esperado. Se ele mandar print de erro, diagnostique antes de propor outra coisa.
- Use dados fictícios em exemplos e testes (ex.: Rafael Souza, Carla Mendes). Nunca coloque senha, chave, token ou dados de conexão no repositório, no Trello, em exports do Postman ou em mensagens.

## O produto

Sistema web responsivo de gestão e agendamento para barbearias. O cliente agenda online; a barbearia gerencia serviços, clientes, equipe, estoque, financeiro, assinaturas e recuperação de clientes.

- **Perfis:** CLIENTE, BARBEIRO, ADMINISTRADOR (um perfil por usuário). O cadastro público cria só CLIENTE; barbeiros são criados pelo administrador (UC03).
- **Escopo:** 10 módulos e 27 casos de uso (UC01 a UC27), cada um é um cartão no Trello, priorizados em Must, Should e Could. Os 12 Must formam o MVP.
- Prefixos dos módulos: ACE acesso, CLI clientes, CAT catálogo, AGE agendamento, FUN equipe, EST estoque, FIN financeiro, ASS assinaturas, REC recuperação, DAS dashboard.
- Documentos (Escopo, ERS, Modelagem de Dados, Definition of Done) ficam na coluna Docs do Trello. Os critérios de aceite de cada caso de uso estão na descrição do cartão: **leia o cartão antes de implementar**.

## Stack e estrutura

- **Back-end** (`backend/`): Java 25, Spring Boot 4.1.2, Spring Security (JWT HS256 com Nimbus), JPA/Hibernate, Flyway, Lombok, Maven (`mvnw`, não precisa instalar Maven). Pacote base `br.com.barberhub`.
- **Front-end** (`frontend/`): React 19 + Vite, JavaScript (não TypeScript), react-router-dom 7.
- **Banco:** PostgreSQL 17 no Supabase (homologação).
- **Hospedagem:** Render, plano gratuito (back-end em Docker, front-end como site estático).

### Back-end: organização

- Um pacote por módulo, com os nomes da ERS: `acesso`, `barbeiro`, `catalogo`, `agendamento`... **Tudo que é do barbeiro fica no pacote `barbeiro`** (regra do Ian).
- Padrão por funcionalidade: `XxxController` → `XxxServico` → `XxxRepositorio` (JpaRepository), com `XxxRequisicao` e `XxxResposta` como `record` e validação por Bean Validation, mensagens em português.
- `compartilhado/`: `erro/` (ManipuladorDeErros, ErroResposta, exceções), `seguranca/` (ConfiguracaoSeguranca, ConfiguracaoJwt), `auditoria/` (AuditoriaServico.registrar(usuarioId, acao, entidade, entidadeId)), `saude/`.
- **Erros** sempre no formato `ErroResposta { status, codigo, mensagem, campos[{campo, mensagem}], momento }`:
  400 DADOS_INVALIDOS (com `campos`), 401 NAO_AUTENTICADO, 403 ACESSO_NEGADO, 404 NAO_ENCONTRADO (`RecursoNaoEncontradoException`), 409 CONFLITO, 422 REGRA_DE_NEGOCIO (`ExcecaoNegocio`).
- Ações administrativas relevantes registram auditoria (ex.: CRIAR_BARBEIRO, INATIVAR_USUARIO, ATIVAR_USUARIO).

### Front-end: organização

- `src/modulos/<modulo>/`: `api.js` (funções que chamam a API), telas `TelaXxx.jsx`, formulários.
- `src/compartilhado/`: `api/cliente.js` (`requisitar(caminho, { semToken })`, adiciona o Bearer; erros viram `ErroDaApi` com `campos`), `ui/` (Campo, LayoutPagina, TelaAcessoNegado, TelaNaoEncontrada), `sessao/armazenamento.js` (sessão no sessionStorage), `navegacao/menu.js`.
- **Menu:** cada tela nova acrescenta um item em `ITENS_MENU` (`menu.js`) com `rotulo`, `caminho`, `perfis` e `descricao`; a tela inicial mostra os atalhos sozinha. Rotas em `App.jsx`, dentro de `LayoutAutenticado`, protegidas com `<RotaProtegida perfis={[...]} />`.
- **Estilo:** glassmorphism com minimalismo moderno em tons de cinza. Tudo em `src/index.css`, com variáveis (`--tinta`, `--vidro`, `--campo`, `--sucesso`, `--erro`...). Reaproveite as classes existentes (`cartao`, `botao`, `botao-pequeno`, `botao-claro`, `formulario`, `alerta`, `alerta-sucesso`, `seletor`, `perfil-etiqueta`, `lista-usuarios`, `usuario-linha`...) e acrescente um bloco comentado por cartão no fim do arquivo.
- **Responsivo, mobile first:** estilo base para celular; `@media (min-width: 900px)` para computador. Teste também em 390 px de largura.

## Banco de dados

- **Tabelas só por migração Flyway** (`backend/src/main/resources/db/migration`). Existem V1 a V9; a próxima é **V10**. Nunca edite uma migração já aplicada: corrigir é criar outra. `spring.jpa.hibernate.ddl-auto=validate` (o JPA só confere).
- As 22 tabelas do modelo completo já existem (inclusive de módulos futuros: `servico`, `combo_item`, `agendamento`, `lancamento`...). Antes de criar algo, veja se a tabela já está nas migrações.
- **RLS ligado em todas as tabelas** (V8), sem policies: a API automática do Supabase fica fechada e só o back-end acessa. Tabela nova precisa de `ALTER TABLE ... ENABLE ROW LEVEL SECURITY` na migração.
- Chaves estrangeiras com `ON DELETE RESTRICT`: para apagar usuário, apague antes `auditoria`, `cliente`/`barbeiro` e só depois `usuario`.
- **Um único banco Supabase, compartilhado entre o ambiente local e o Render.** O que se apaga localmente some da homologação. Nunca apague nem altere o `admin@barberhub.local`.
- Trocar perfil de usuário ainda não tem endpoint: é feito por SQL, ajustando junto as tabelas `cliente` e `barbeiro`. O usuário precisa entrar de novo, porque o perfil fica dentro do token.

## Segurança

- Senhas com BCrypt (`BCryptPasswordEncoder`, custo 10); senha de 8 a 72 caracteres; nunca devolver senha nem hash.
- Login devolve JWT (claims `sub` = id, `perfil`, `iss` barberhub), válido por 60 min por padrão. O front guarda na sessão da aba e envia `Authorization: Bearer`.
- Autoridade `ROLE_<PERFIL>`. `/api/admin/**` só ADMINISTRADOR, `/api/barbeiro/**` só BARBEIRO, `/api/cliente/**` só CLIENTE. Sem token: 401; perfil errado: 403.
- Login com e-mail inexistente ou senha errada dá a mesma mensagem (e compara com hash falso). Conta inativa com senha correta: 403 "Esta conta está inativa. Procure a barbearia."
- Endpoints públicos: `GET /api/saude`, `POST /api/acesso/cadastro`, `POST /api/acesso/login`.

## Endpoints existentes

| Método | Endpoint | Quem | Para que serve |
|---|---|---|---|
| GET | /api/saude | público | API e banco respondem |
| POST | /api/acesso/cadastro | público | cria conta de cliente (201) |
| POST | /api/acesso/login | público | devolve token e usuário |
| GET | /api/acesso/eu | logado | dados da própria conta |
| POST | /api/admin/barbeiros | admin | cria barbeiro (201) |
| GET | /api/admin/usuarios | admin | lista usuários (filtros `perfil`, `ativo`) |
| PATCH | /api/admin/usuarios/{id}/inativar | admin | inativa conta |
| PATCH | /api/admin/usuarios/{id}/ativar | admin | reativa conta |

## Variáveis de ambiente (back-end, lidas do sistema, não de `.env`)

`BANCO_URL`, `BANCO_USUARIO`, `BANCO_SENHA`, **`JWT_SEGREDO`** (obrigatória, mínimo 32 caracteres; o nome não é JWT_SECRET), `JWT_EXPIRACAO_MINUTOS` (opcional; se existir, não pode estar vazia), `ADMIN_SENHA_INICIAL` (opcional), `CORS_ORIGENS` (opcional). No Windows o Ian as definiu como variáveis de usuário (reabrir o VS Code após mudar). Front-end: `frontend/.env` com `VITE_API_URL`.

## Como rodar e testar

- Back-end: `cd backend` e `.\mvnw spring-boot:run` (API em http://localhost:8080). Front-end: `cd frontend` e `npm run dev` (http://localhost:5173).
- **Testes automáticos:** `.\mvnw test` (39 testes hoje). JUnit 5 + Mockito, `mock()` simples nos testes de serviço; `@WebMvcTest` + MockMvc para rotas. No Spring Boot 4 os pacotes são `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` e `MockitoBean`. O teste que sobe a aplicação inteira precisa das variáveis do banco e do `JWT_SEGREDO`.
- Front-end: `npm run lint` e `npm run build` sem erros antes do PR.
- Cada cartão é verificado em quatro camadas: testes automáticos, Postman (caso certo e casos de erro, conferindo o banco), tela em homologação (um perfil por janela; janela anônima para o segundo) e lint/build.
- Testcontainers (migrações com banco descartável) ainda não existe: pendência registrada.

## Fluxo de trabalho de cada cartão

1. Ler o cartão no Trello (critérios de aceite) e mover para In Progress (o Ian move).
2. Branch a partir da `develop`: `feature/UCnn-nome-curto` (ou `fix/...`). Sem acentos no nome.
3. Planejar em poucas linhas (regras, endpoints, telas) e implementar back-end com testes, depois o front-end.
4. Commits pequenos: `tipo(módulo): descrição (UCnn)`, ex.: `feat(catalogo): cadastro de serviços (UC06)`. Back-end e front-end podem ir em commits separados.
5. PR para a `develop` com as seções: **O que muda · Regras aplicadas · Decisões · Como testar · Verificação já feita · Pendências**, mais o link do cartão.
6. Depois do merge, o Render publica sozinho (back-end ~3 min, front-end segundos). Conferir `https://barberhub-backend-r9zp.onrender.com/api/saude` e testar em `https://barberhub-frontend.onrender.com` (Ctrl+Shift+R ou janela anônima por causa do cache).
7. Fechar: comentário no cartão (o que entregou, regras, verificação, pendências), mover para Done e marcar como concluído; atualizar a lista "Andamento" do documento da Sprint; atualizar o README se mudou algo de uso; atualizar "Estado atual" aqui.

`main` guarda a versão entregue; `develop` reúne a Sprint. A promoção para `main` acontece na entrega da Sprint.

## Ambientes e ferramentas

- Repositório: github.com/IanKazuhiro/barberhub (privado).
- Render (workspace `tea-d0gqmd24d50c738455og`): back-end `srv-dbt1v8mc6qcc73agiih0`, front-end `srv-dbt237ff3r2c73bn4kag` (regra de reescrita `/*` → `/index.html`). O back-end hiberna após 15 min sem uso (1 a 2 min para acordar). Use o Render só para consultar deploys e logs.
- Supabase: projeto "BarberHub - Homolog" (`esazzqwcaussndixxejf`). O plano gratuito pausa após ~1 semana sem uso (restaurar pelo painel).
- Trello: quadro BarberHub (https://trello.com/b/YLLygKlv/barberhub), colunas Backlog, To-Do, In Progress, Review/Tests, Done, Planejamento de Sprints, Docs.
- Documento "BarberHub — Sprint 1" (Claude Docs): https://claude.ai/artifact/L3roNjPNpJB6nr8noqQM56
- Apresentação do projeto (Slides): https://claude.ai/artifact/MUX4AASp7rpDwagG3k9X8G

## Estado atual (atualizar ao fim de cada cartão)

**Sprint 1:** 11/11 a 25/11/2026, demonstração e retrospectiva em 25/11. Meta: qualquer pessoa se cadastra e entra, o sistema reconhece o perfil e o administrador cadastra os serviços que o cliente consulta.

- Concluídos (Done, mesclados na `develop` e em homologação): T01 a T04 (base técnica), T05 (navegação e telas iniciais por perfil, PR #13), UC01 Cadastrar-se (PR #9), UC02 Entrar e sair (PR #11), UC03 Gerenciar acessos (PR #14).
- **Próximo cartão: UC06 Gerenciar serviços e combos** (RF-CAT-01, 03 e 04): administrador cadastra, altera e desativa serviços e combos, com preço e duração. Tabelas já existem: `servico` (nome único, `duracao_minutos` > 0, `preco` ≥ 0, `eh_combo`, `ativo`) e `combo_item` (combo_id, servico_id). Combo tem preço próprio (decisão da Modelagem). Se faltar tempo, o combo (RF-CAT-04) pode ser cortado.
- Depois: UC07 Consultar catálogo (cliente vê serviços, combos e preços).
- Sprint 2 (previsão): UC17 Escala dos barbeiros, UC04 Clientes, UC09 Agendar serviço (sem horário duplo). Sprint 3: UC10, UC11, UC12, UC05.

**Pendências registradas**
- Token de conta inativada vale até expirar: checar a situação da conta a cada requisição (depois da demonstração).
- Recuperação de senha (RF-ACE-07, Could): fora do UC02, ainda sem cartão.
- Testcontainers para testar migrações.
- Possível endpoint `PATCH /api/admin/usuarios/{id}/perfil` (troca de perfil), ainda não decidido.
- README ainda diz que o UC03 está "em desenvolvimento": atualizar.
- Banco de homologação hoje sem barbeiro ativo: criar um para a demonstração.
- PRs da Sprint 1 validados só pelo Ian; voltar às duas revisões da Definition of Done quando o prazo permitir.