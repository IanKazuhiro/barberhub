-- Extensão usada pela restrição de horário do agendamento (RN01)
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE usuario (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome          VARCHAR(120) NOT NULL,
    email         VARCHAR(160) NOT NULL,
    senha_hash    VARCHAR(100),
    telefone      VARCHAR(20),
    perfil        VARCHAR(15)  NOT NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('CLIENTE', 'BARBEIRO', 'ADMINISTRADOR'))
);

-- E-mail único sem diferenciar maiúsculas
CREATE UNIQUE INDEX uq_usuario_email ON usuario (lower(email));

CREATE TABLE barbeiro (
    usuario_id          BIGINT PRIMARY KEY,
    percentual_comissao NUMERIC(5,2) NOT NULL,
    criado_em           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_barbeiro_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT ck_barbeiro_comissao CHECK (percentual_comissao BETWEEN 0 AND 100)
);

CREATE TABLE cliente (
    usuario_id           BIGINT PRIMARY KEY,
    aceita_mensagens     BOOLEAN     NOT NULL DEFAULT TRUE,
    consentimento_em     TIMESTAMPTZ,
    barbeiro_favorito_id BIGINT,
    tipo_corte           VARCHAR(120),
    observacoes          TEXT,
    anonimizado_em       TIMESTAMPTZ,
    criado_em            TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT fk_cliente_barbeiro_favorito FOREIGN KEY (barbeiro_favorito_id)
        REFERENCES barbeiro (usuario_id) ON DELETE RESTRICT
);

CREATE INDEX ix_cliente_barbeiro_favorito ON cliente (barbeiro_favorito_id);