CREATE TABLE mensagem_recuperacao (
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id                 BIGINT      NOT NULL,
    enviada_em                 TIMESTAMPTZ NOT NULL,
    canal                      VARCHAR(10) NOT NULL,
    inicio_periodo_inatividade DATE        NOT NULL,
    sugestao_inicio            TIMESTAMPTZ,
    sugestao_barbeiro_id       BIGINT,
    sugestao_servico_id        BIGINT,
    token_link                 VARCHAR(64) NOT NULL,
    recuperada                 BOOLEAN     NOT NULL DEFAULT FALSE,
    agendamento_id             BIGINT,
    criado_em                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_mensagem_token UNIQUE (token_link),
    CONSTRAINT fk_mensagem_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_mensagem_barbeiro FOREIGN KEY (sugestao_barbeiro_id)
        REFERENCES barbeiro (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_mensagem_servico FOREIGN KEY (sugestao_servico_id)
        REFERENCES servico (id) ON DELETE RESTRICT,
    CONSTRAINT fk_mensagem_agendamento FOREIGN KEY (agendamento_id)
        REFERENCES agendamento (id) ON DELETE RESTRICT,
    CONSTRAINT ck_mensagem_canal CHECK (canal IN ('EMAIL', 'LINK'))
);

CREATE INDEX ix_mensagem_cliente ON mensagem_recuperacao (cliente_id, inicio_periodo_inatividade);
CREATE INDEX ix_mensagem_barbeiro ON mensagem_recuperacao (sugestao_barbeiro_id);
CREATE INDEX ix_mensagem_servico ON mensagem_recuperacao (sugestao_servico_id);
CREATE INDEX ix_mensagem_agendamento ON mensagem_recuperacao (agendamento_id);

CREATE TABLE auditoria (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL,
    acao        VARCHAR(40) NOT NULL,
    entidade    VARCHAR(40) NOT NULL,
    entidade_id BIGINT,
    data_hora   TIMESTAMPTZ NOT NULL,
    detalhe     JSONB,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE RESTRICT
);

CREATE INDEX ix_auditoria_usuario ON auditoria (usuario_id, data_hora);
CREATE INDEX ix_auditoria_entidade ON auditoria (entidade, entidade_id);