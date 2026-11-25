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
- `compartilhado/`: `erro/` (ManipuladorDeErros, ErroResposta, exceções), `seguranca/` (ConfiguracaoSeguranca, ConfiguracaoJwt), `auditoria/` (AuditoriaServico.registrar(usuarioId, acao, entidade, entidadeId)), `tempo/` (`Clock` no fuso da barbearia: use-o no lugar de `Instant.now()` em regra de negócio, para os testes pararem o relógio), `saude/`.
- **Erros** sempre no formato `ErroResposta { status, codigo, mensagem, campos[{campo, mensagem}], momento }`:
  400 DADOS_INVALIDOS (com `campos`), 401 NAO_AUTENTICADO, 403 ACESSO_NEGADO, 404 NAO_ENCONTRADO (`RecursoNaoEncontradoException`), 409 CONFLITO (`ConflitoException`: pedido válido, mas outro chegou antes), 422 REGRA_DE_NEGOCIO (`ExcecaoNegocio`).
- Ações administrativas relevantes registram auditoria (ex.: CRIAR_BARBEIRO, INATIVAR_USUARIO, ATIVAR_USUARIO, TROCAR_PERFIL_<PERFIL>).
- Erros de tipo de parâmetro (`?perfil=XYZ`, id não numérico) e parâmetro ausente dão 400 DADOS_INVALIDOS com `campos`; tipo de conteúdo errado dá 415 TIPO_NAO_SUPORTADO.
- O e-mail do administrador inicial está em `Usuario.EMAIL_ADMINISTRADOR_INICIAL` (no front, ainda repetido em `TelaAcessos.jsx`).

### Front-end: organização

- `src/modulos/<modulo>/`: `api.js` (funções que chamam a API), telas `TelaXxx.jsx`, formulários.
- `src/compartilhado/`: `api/cliente.js` (`requisitar(caminho, { semToken })`, adiciona o Bearer; erros viram `ErroDaApi` com `campos`), `ui/` (Campo, Dialogo, Icone, LayoutPagina, TelaAcessoNegado, TelaNaoEncontrada), `api/mensagem.js` (`mensagemDe` e `errosDoFormulario`, para não repetir o tratamento de erro nas telas), `formato/data.js` (datas no fuso da barbearia), `sessao/armazenamento.js` (sessão no sessionStorage), `navegacao/menu.js`.
- `Campo` aceita `dica` (texto de ajuda) e `classe` (posição na grade, ex.: `campo-largo`). `Dialogo` é a confirmação do sistema: use no lugar do `window.confirm` (`titulo`, `descricao`, `rotuloConfirmar`, `perigo`, `aoConfirmar`, `aoCancelar`). `Icone` desenha traços em SVG pelo nome (`inicio`, `acessos`, `equipe`, `clientes`, `tesoura`, `calendario`, `lista`, `relogio`, `mais`, `lapis`, `energia`, `seta`, `sair`, `menu`, `fechar`); sem biblioteca externa.
- **Menu:** cada tela nova acrescenta um item em `ITENS_MENU` (`menu.js`) com `rotulo`, `caminho`, `icone`, `perfis` e `descricao`; a tela inicial mostra os atalhos sozinha. Rotas em `App.jsx`, dentro de `LayoutAutenticado` **e** de `<RotaProtegida perfis={[...]} />` (fora disso a tela abre sem menu e sem proteção).
- **Estilo:** glassmorphism com minimalismo moderno em tons de cinza. Tudo em `src/index.css`, com variáveis (`--tinta`, `--vidro`, `--campo`, `--sucesso`, `--erro`, `--transicao`...). Reaproveite as classes existentes (`cartao`, `botao`, `botao-pequeno`, `botao-claro`, `botao-perigo`, `formulario`, `grade-campos`, `alerta`, `alerta-sucesso`, `seletor`, `abas`, `etiqueta`, `perfil-etiqueta`, `resumo`, `vazio`, `esqueleto`, `lista-usuarios`, `usuario-linha`, `cartao-servico`...) e acrescente um bloco comentado por cartão no fim do arquivo.
- **Movimento:** animações de entrada (`surgir`, `aparecer`, `ampliar`) e transição entre telas pela `key` da rota em `LayoutAutenticado` (`.pagina-transicao`). Tudo é desligado em `@media (prefers-reduced-motion: reduce)`, no fim do CSS. Elemento que tem `:hover` com `transform` não pode usar `animation ... both`, senão o preenchimento da animação trava o hover.
- **Responsivo, mobile first:** estilo base para celular; `@media (min-width: 900px)` para computador. Teste também em 390 px de largura.

## Banco de dados

- **Tabelas só por migração Flyway** (`backend/src/main/resources/db/migration`). Existem V1 a V10 (V10: expediente inicial); a próxima é **V11**. Nunca edite uma migração já aplicada: corrigir é criar outra. `spring.jpa.hibernate.ddl-auto=validate` (o JPA só confere).
- As 22 tabelas do modelo completo já existem (inclusive de módulos futuros: `servico`, `combo_item`, `agendamento`, `lancamento`...). Antes de criar algo, veja se a tabela já está nas migrações.
- **RLS ligado em todas as tabelas** (V8), sem policies: a API automática do Supabase fica fechada e só o back-end acessa. Tabela nova precisa de `ALTER TABLE ... ENABLE ROW LEVEL SECURITY` na migração.
- Chaves estrangeiras com `ON DELETE RESTRICT`: para apagar usuário, apague antes `auditoria`, `cliente`/`barbeiro` e só depois `usuario`.
- **Um único banco Supabase, compartilhado entre o ambiente local e o Render.** O que se apaga localmente some da homologação. Nunca apague nem altere o `admin@barberhub.local`.
- Trocar perfil de usuário: `PATCH /api/admin/usuarios/{id}/perfil` (tela Acessos). O serviço `TrocaPerfilServico` remove a linha do perfil antigo (`cliente` ou `barbeiro`) e cria a do novo; se o banco recusar por histórico (agendamentos, barbeiro favorito), responde 422. Não troca o próprio perfil nem o do administrador inicial. Vale na hora: o back-end usa o perfil do banco, não o do token.

## Segurança

- Senhas com BCrypt (`BCryptPasswordEncoder`, custo 10); senha de 8 a 72 caracteres; nunca devolver senha nem hash.
- Login devolve JWT (claims `sub` = id, `perfil`, `iss` barberhub), válido por 60 min por padrão. O front guarda na sessão da aba e envia `Authorization: Bearer`.
- **A cada requisição** o back-end busca o usuário do token no banco (`ConfiguracaoSeguranca.conversorDeToken`): conta inexistente ou inativa dá 401, e o perfil usado é o do banco, não o claim `perfil` do token. Inativar conta e trocar perfil valem na hora.
- Autoridade `ROLE_<PERFIL>`. `/api/admin/**` só ADMINISTRADOR, `/api/barbeiro/**` só BARBEIRO, `/api/cliente/**` só CLIENTE, `/api/clientes/**` ADMINISTRADOR e BARBEIRO. Sem token: 401; perfil errado: 403.
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
| PATCH | /api/admin/usuarios/{id}/perfil | admin | troca o perfil (corpo `{"perfil":"BARBEIRO"}`) |
| GET, POST | /api/admin/servicos | admin | lista (filtro `ativo`) e cria serviço (201) |
| PUT | /api/admin/servicos/{id} | admin | altera serviço simples |
| PATCH | /api/admin/servicos/{id}/inativar e /ativar | admin | situação do serviço ou combo |
| GET, POST | /api/admin/combos | admin | lista (filtro `ativo`) e cria combo (201) |
| PUT | /api/admin/combos/{id} | admin | altera combo |
| GET | /api/cliente/catalogo | cliente | serviços e combos ativos, com preço e duração |
| GET | /api/admin/barbeiros | admin | lista a equipe, com comissão e se tem escala |
| PUT | /api/admin/barbeiros/{id} | admin | altera nome, telefone e comissão |
| GET, PUT | /api/admin/barbeiros/{id}/escala | admin | consulta e substitui a escala da semana |
| GET, POST | /api/admin/barbeiros/{id}/folgas | admin | folgas de hoje em diante; cria (201) |
| DELETE | /api/admin/barbeiros/{id}/folgas/{folgaId} | admin | remove folga (204) |
| GET | /api/admin/barbeiros/{id}/agendamentos-futuros | admin | o que ficou marcado com o barbeiro |
| GET | /api/barbeiros | logado | barbeiros ativos (só id e nome) |
| GET | /api/clientes?busca= | admin, barbeiro | busca por parte do nome ou telefone (até 50) |
| GET | /api/clientes/{id} | admin, barbeiro | dados e preferências |
| PUT | /api/clientes/{id}/preferencias | admin, barbeiro | barbeiro favorito, tipo de corte, observações |
| GET | /api/clientes/{id}/historico | admin, barbeiro | atendimentos concluídos |
| PUT | /api/admin/clientes/{id} | admin | altera nome e telefone do cliente |
| GET, PUT | /api/cliente/dados | cliente | os próprios dados e a opção de mensagens |
| GET | /api/cliente/disponibilidade | cliente | horários livres (`barbeiroId`, `servicoId`, `data`) |
| GET, POST | /api/cliente/agendamentos | cliente | próximos e anteriores; agenda (201, ou 409 se ocupado) |
| GET | /api/cliente/agendamentos/{id}/horarios | cliente | horários livres para remarcar (`data`) |
| PATCH | /api/cliente/agendamentos/{id}/cancelar e /remarcar | cliente | dentro da antecedência mínima (204) |
| GET | /api/barbeiro/agenda | barbeiro | agenda do próprio barbeiro no dia (`data` opcional) |

## Variáveis de ambiente (back-end, lidas do sistema, não de `.env`)

`BANCO_URL`, `BANCO_USUARIO`, `BANCO_SENHA`, **`JWT_SEGREDO`** (obrigatória, mínimo 32 caracteres; o nome não é JWT_SECRET), `JWT_EXPIRACAO_MINUTOS` (opcional; se existir, não pode estar vazia), `ADMIN_SENHA_INICIAL` (opcional), `CORS_ORIGENS` (opcional), `BARBEARIA_FUSO` (opcional; padrão `America/Manaus`). No Windows o Ian as definiu como variáveis de usuário (reabrir o VS Code após mudar). Front-end: `frontend/.env` com `VITE_API_URL`.

## Como rodar e testar

- Back-end: `cd backend` e `.\mvnw spring-boot:run` (API em http://localhost:8080). Front-end: `cd frontend` e `npm run dev` (http://localhost:5173).
- **Testes automáticos:** `.\mvnw test` (230 testes sem Docker, mais 10 do `MigracoesBancoTest`, que só rodam com Docker ligado e senão são ignorados). JUnit 5 + Mockito, `mock()` simples nos testes de serviço; `@WebMvcTest` + MockMvc para rotas (contas fictícias em `ContasDeTeste`). No Spring Boot 4 os pacotes são `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` e `MockitoBean`. O teste que sobe a aplicação inteira precisa das variáveis do banco e do `JWT_SEGREDO` e **aplica as migrações no banco apontado por `BANCO_URL`**: com migração nova, aponte para um PostgreSQL local antes de rodar, senão ela vai para a homologação antes do merge.
- Front-end: `npm run lint` e `npm run build` sem erros antes do PR.
- Cada cartão é verificado em quatro camadas: testes automáticos, Postman (caso certo e casos de erro, conferindo o banco), tela em homologação (um perfil por janela; janela anônima para o segundo) e lint/build.
- `MigracoesBancoTest` (Testcontainers, PostgreSQL 17 descartável) aplica todas as migrações e confere tabelas, RLS ligado em todas, administrador inicial e regras do banco (horário duplo, saldo negativo, e-mail único). **Tabela nova sem RLS faz esse teste falhar.** Ele ainda não foi executado de verdade: a máquina do Ian não tem Docker instalado.

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

- Concluídos (Done, mesclados na `develop` e em homologação): T01 a T04 (base técnica), T05 (navegação e telas iniciais por perfil, PR #13), UC01 Cadastrar-se (PR #9), UC02 Entrar e sair (PR #11), UC03 Gerenciar acessos (PR #14), UC06 Gerenciar serviços e combos (PR #17).
- **Em Review/Tests (mesclados na `develop` e em homologação, faltando o teste de tela do Ian):** UC07 Catálogo (PR #18), UC17 Equipe, escala e folgas (PRs #19 e #26), UC04 Clientes (PR #20), UC09 Agendar (PR #21), UC11 Meus agendamentos (PR #22), UC10 Cancelar ou remarcar (PR #23), UC12 Agenda do dia (PR #24), UC05 Histórico (PR #25). Foram implementados e mesclados pelo Claude a pedido do Ian, sem revisão de outra pessoa.
- **Os 12 casos de uso Must (MVP) estão implementados.** Falta validar em homologação, mover para Done e promover `develop` para `main` (o Ian faz).
- Próximo passo sugerido: **UC13 Concluir atendimento** (Should), porque sem ele nenhum atendimento vira CONCLUÍDO e o histórico do UC05 fica vazio. Depois: UC15 (agendar, remarcar e cancelar pelo administrador), UC16 (tela de expediente e antecedência) e UC14.

**Decisões do agendamento (25/11)**
- Fuso da barbearia: `America/Manaus`. A API manda e recebe data e hora já nesse fuso (`2026-11-25T10:00`); no banco é `TIMESTAMPTZ`. O front repete o fuso em `formato/data.js`.
- Grade de 15 minutos, de hoje até 30 dias à frente. Horário livre = expediente ∩ escala, menos folgas e agendamentos, com o serviço inteiro cabendo.
- Expediente inicial (V10): segunda a sábado das 9h às 19h, domingo fechado.
- Antecedência para cancelar e remarcar: chave `antecedencia_minima_horas` (2), lida a cada uso.
- Remarcar altera a mesma linha (mantém serviço, barbeiro, duração e preço). Horário duplo é barrado pela restrição do banco (RN01); perder a disputa dá 409.
- Dia da semana: 0 = domingo ... 6 = sábado.

**Pendências registradas**
- Recuperação de senha (RF-ACE-07, Could): fora do UC02, ainda sem cartão e sem decisão de como entregar o link (não há envio de e-mail no projeto).
- Instalar o Docker e rodar `MigracoesBancoTest` pela primeira vez (hoje os 10 testes são ignorados). Se algum falhar, ajustar o teste ou a migração.
- Banco de homologação hoje sem barbeiro ativo: criar um para a demonstração na tela **Equipe** e definir a escala dele (sem escala não há horário livre).
- Fora do escopo por decisão do Ian: RF-CLI-07 (anonimizar cliente) e RF-CLI-06 (frequência de visitas), ambos Could.
- Combo guarda a duração somada ao ser salvo: mudar a duração de um serviço não atualiza os combos que o usam até o combo ser salvo de novo.
- Barbeiro inativo: o administrador vê os agendamentos em aberto, mas remarcar ou cancelar por ele depende do UC15.
- Front-end: o menu de quem teve o perfil trocado só atualiza ao recarregar a página (o back-end já barra na hora).
- PRs da Sprint 1 validados só pelo Ian; voltar às duas revisões da Definition of Done quando o prazo permitir.