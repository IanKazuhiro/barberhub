CREATE TABLE plano (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome                   VARCHAR(80)   NOT NULL,
    valor                  NUMERIC(10,2) NOT NULL,
    periodicidade          VARCHAR(10)   NOT NULL,
    creditos_por_ciclo     SMALLINT      NOT NULL,
    validade_creditos_dias SMALLINT,
    acumula_creditos       BOOLEAN       NOT NULL DEFAULT FALSE,
    carencia_dias          SMALLINT      NOT NULL DEFAULT 0,
    regra_troca_plano      VARCHAR(15)   NOT NULL,
    regra_cancelamento     VARCHAR(200),
    ativo                  BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em              TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_plano_nome UNIQUE (nome),
    CONSTRAINT ck_plano_valor CHECK (valor > 0),
    CONSTRAINT ck_plano_periodicidade CHECK (periodicidade IN ('MENSAL', 'TRIMESTRAL')),
    CONSTRAINT ck_plano_creditos CHECK (creditos_por_ciclo > 0),
    CONSTRAINT ck_plano_validade CHECK (validade_creditos_dias > 0),
    CONSTRAINT ck_plano_carencia CHECK (carencia_dias >= 0),
    CONSTRAINT ck_plano_troca CHECK (regra_troca_plano IN ('IMEDIATA', 'PROXIMO_CICLO'))
);

CREATE TABLE assinatura (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id       BIGINT      NOT NULL,
    plano_id         BIGINT      NOT NULL,
    plano_proximo_id BIGINT,
    estado           VARCHAR(20) NOT NULL,
    data_inicio      DATE        NOT NULL,
    ciclo_inicio     DATE        NOT NULL,
    ciclo_fim        DATE        NOT NULL,
    saldo_creditos   SMALLINT    NOT NULL,
    criado_em        TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_assinatura_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_assinatura_plano FOREIGN KEY (plano_id)
        REFERENCES plano (id) ON DELETE RESTRICT,
    CONSTRAINT fk_assinatura_plano_proximo FOREIGN KEY (plano_proximo_id)
        REFERENCES plano (id) ON DELETE RESTRICT,
    CONSTRAINT ck_assinatura_estado CHECK (
        estado IN ('ATIVA', 'PENDENTE_PAGAMENTO', 'SUSPENSA', 'CANCELADA')
    ),
    CONSTRAINT ck_assinatura_ciclo CHECK (ciclo_fim > ciclo_inicio),
    -- O saldo de créditos nunca fica negativo
    CONSTRAINT ck_assinatura_saldo CHECK (saldo_creditos >= 0)
);

CREATE INDEX ix_assinatura_cliente ON assinatura (cliente_id);
CREATE INDEX ix_assinatura_plano ON assinatura (plano_id);
CREATE INDEX ix_assinatura_plano_proximo ON assinatura (plano_proximo_id);

CREATE TABLE assinatura_pagamento (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assinatura_id   BIGINT        NOT NULL,
    valor           NUMERIC(10,2) NOT NULL,
    forma_pagamento VARCHAR(10)   NOT NULL,
    pago_em         TIMESTAMPTZ   NOT NULL,
    ciclo_inicio    DATE          NOT NULL,
    ciclo_fim       DATE          NOT NULL,
    lancamento_id   BIGINT        NOT NULL,
    criado_em       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    -- Impede cobrar o mesmo ciclo duas vezes
    CONSTRAINT uq_pagamento_ciclo UNIQUE (assinatura_id, ciclo_inicio),
    CONSTRAINT uq_pagamento_lancamento UNIQUE (lancamento_id),
    CONSTRAINT fk_pagamento_assinatura FOREIGN KEY (assinatura_id)
        REFERENCES assinatura (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pagamento_lancamento FOREIGN KEY (lancamento_id)
        REFERENCES lancamento (id) ON DELETE RESTRICT,
    CONSTRAINT ck_pagamento_valor CHECK (valor > 0),
    CONSTRAINT ck_pagamento_forma CHECK (forma_pagamento IN ('PIX', 'CARTAO', 'DINHEIRO'))
);

CREATE TABLE assinatura_historico (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assinatura_id BIGINT      NOT NULL,
    evento        VARCHAR(30) NOT NULL,
    de_valor      VARCHAR(60),
    para_valor    VARCHAR(60),
    data          TIMESTAMPTZ NOT NULL,
    usuario_id    BIGINT      NOT NULL,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_historico_assinatura FOREIGN KEY (assinatura_id)
        REFERENCES assinatura (id) ON DELETE RESTRICT,
    CONSTRAINT fk_historico_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE RESTRICT
);

CREATE INDEX ix_historico_assinatura ON assinatura_historico (assinatura_id, data);
CREATE INDEX ix_historico_usuario ON assinatura_historico (usuario_id);

-- Fecha a ligação deixada na V4
ALTER TABLE agendamento
    ADD CONSTRAINT fk_agendamento_assinatura FOREIGN KEY (assinatura_id)
        REFERENCES assinatura (id) ON DELETE RESTRICT;