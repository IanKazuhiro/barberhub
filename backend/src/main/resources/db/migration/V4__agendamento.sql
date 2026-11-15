CREATE TABLE agendamento (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id    BIGINT        NOT NULL,
    barbeiro_id   BIGINT        NOT NULL,
    servico_id    BIGINT        NOT NULL,
    inicio        TIMESTAMPTZ   NOT NULL,
    fim           TIMESTAMPTZ   NOT NULL,
    estado        VARCHAR(12)   NOT NULL,
    preco_cobrado NUMERIC(10,2) NOT NULL,
    -- A chave estrangeira para assinatura é criada na V5
    assinatura_id BIGINT,
    origem        VARCHAR(12)   NOT NULL,
    criado_por    BIGINT        NOT NULL,
    cancelado_em  TIMESTAMPTZ,
    criado_em     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT fk_agendamento_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_agendamento_barbeiro FOREIGN KEY (barbeiro_id)
        REFERENCES barbeiro (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_agendamento_servico FOREIGN KEY (servico_id)
        REFERENCES servico (id) ON DELETE RESTRICT,
    CONSTRAINT fk_agendamento_criado_por FOREIGN KEY (criado_por)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT ck_agendamento_periodo CHECK (fim > inicio),
    CONSTRAINT ck_agendamento_estado CHECK (
        estado IN ('AGENDADO', 'CONCLUIDO', 'CANCELADO', 'FALTA')
    ),
    CONSTRAINT ck_agendamento_preco CHECK (preco_cobrado >= 0),
    CONSTRAINT ck_agendamento_origem CHECK (origem IN ('APP', 'BALCAO', 'RECUPERACAO')),
    -- RN01: um barbeiro não pode ter dois agendamentos ativos que se sobreponham
    CONSTRAINT ex_agendamento_sem_conflito EXCLUDE USING gist (
        barbeiro_id WITH =,
        tstzrange(inicio, fim) WITH &&
    ) WHERE (estado IN ('AGENDADO', 'CONCLUIDO'))
);

CREATE INDEX ix_agendamento_cliente ON agendamento (cliente_id, inicio);
CREATE INDEX ix_agendamento_servico ON agendamento (servico_id);
CREATE INDEX ix_agendamento_criado_por ON agendamento (criado_por);
CREATE INDEX ix_agendamento_assinatura ON agendamento (assinatura_id);