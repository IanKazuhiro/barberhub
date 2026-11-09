# BarberHub

Sistema modular de gestão e agendamento para barbearias. O cliente agenda online; a barbearia gerencia serviços, clientes, equipe, estoque, financeiro, assinaturas e recuperação de clientes.

> Status: em construção. Projeto em Sprints de duas semanas (Scrum com quadro Kanban). Sprint 1: 11/11/2026 a 25/11/2026.

## Tecnologias

- Back-end: Java com Spring Boot e JPA
- Banco de dados: PostgreSQL
- Front-end: React

## Estrutura do repositório

```
barberhub/
├── backend/     API em Spring Boot (criada no T02)
├── frontend/    Aplicação React (criada no T02)
├── README.md
├── .env.example
└── .gitignore
```

## Como rodar o sistema

> Esta seção será completada no cartão T02 (estrutura base do back-end e do front-end).

1. Clone o repositório:
   ```bash
   git clone https://github.com/IanKazuhiro/barberhub.git
   cd barberhub
   git checkout develop
   ```
2. Crie o arquivo de variáveis de ambiente a partir do modelo e preencha com os seus valores:
   ```bash
   cp .env.example .env
   ```
3. Tenha um PostgreSQL rodando e um banco vazio criado (ver variáveis abaixo).
4. Back-end: passos a definir no T02.
5. Front-end: passos a definir no T02.

## Variáveis de ambiente

Os valores reais ficam apenas no arquivo `.env`, que não é versionado. O modelo está em `.env.example`.

| Variável | Para que serve |
| --- | --- |
| `BANCO_URL` | Endereço de conexão com o PostgreSQL |
| `BANCO_USUARIO` | Usuário do banco |
| `BANCO_SENHA` | Senha do banco |
| `JWT_SEGREDO` | Chave usada para assinar os tokens de login |
| `JWT_EXPIRACAO_MINUTOS` | Tempo de validade do login, em minutos |

Os nomes podem ser ajustados no T02; quem mudar atualiza este quadro e o `.env.example` no mesmo pull request.

## Como trabalhamos

- Branches: `main` guarda a versão estável entregue; `develop` reúne o trabalho da Sprint.
- Uma branch por cartão, criada a partir da `develop`: `feature/UC09-agendar-servico` (ou `fix/` para correção).
- Commits pequenos, no imperativo, com o número do caso de uso: `UC09: impede agendamento duplo`.
- Pull request sempre para a `develop`, com a descrição do que mudou, como testar e o link do cartão. Só mescla com duas aprovações: um revisor de código e um revisor funcional.
- Senhas, chaves e dados de conexão nunca entram no repositório.
- Todo o projeto é escrito em português: código, tabelas, commits e documentação.

Regras completas: documento **Definition of Done** do projeto.

## Documentação

Escopo, Especificação de Requisitos (ERS), Modelagem de Dados, Definition of Done e planejamento das Sprints ficam no quadro Trello do projeto.