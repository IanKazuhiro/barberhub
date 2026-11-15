CREATE TABLE fechamento_caixa (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data           DATE          NOT NULL,
    total_receitas NUMERIC(12,2) NOT NULL,
    total_despesas NUMERIC(12,2) NOT NULL,
    saldo          NUMERIC(12,2) NOT NULL,
    total_pix      NUMERIC(12,2) NOT NULL,
    total_cartao   NUMERIC(12,2) NOT NULL,
    total_dinheiro NUMERIC(12,2) NOT NULL,
    fechado_por    BIGINT        NOT NULL,
    fechado_em     TIMESTAMPTZ   NOT NULL,
    criado_em      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_fechamento_data UNIQUE (data),
    CONSTRAINT fk_fechamento_usuario FOREIGN KEY (fechado_por)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT ck_fechamento_saldo CHECK (saldo = total_receitas - total_despesas)
);

CREATE TABLE lancamento (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo            VARCHAR(10)   NOT NULL,
    categoria       VARCHAR(60)   NOT NULL,
    valor           NUMERIC(10,2) NOT NULL,
    forma_pagamento VARCHAR(10),
    data_hora       TIMESTAMPTZ   NOT NULL,
    descricao       VARCHAR(200),
    origem          VARCHAR(15)   NOT NULL,
    agendamento_id  BIGINT,
    registrado_por  BIGINT        NOT NULL,
    fechamento_id   BIGINT,
    criado_em       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    -- Um atendimento só pode gerar uma receita (impede cobrar duas vezes)
    CONSTRAINT uq_lancamento_agendamento UNIQUE (agendamento_id),
    CONSTRAINT fk_lancamento_agendamento FOREIGN KEY (agendamento_id)
        REFERENCES agendamento (id) ON DELETE RESTRICT,
    CONSTRAINT fk_lancamento_fechamento FOREIGN KEY (fechamento_id)
        REFERENCES fechamento_caixa (id) ON DELETE RESTRICT,
    CONSTRAINT fk_lancamento_usuario FOREIGN KEY (registrado_por)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT ck_lancamento_tipo CHECK (tipo IN ('RECEITA', 'DESPESA')),
    CONSTRAINT ck_lancamento_valor CHECK (valor > 0),
    CONSTRAINT ck_lancamento_forma CHECK (
        forma_pagamento IS NULL OR forma_pagamento IN ('PIX', 'CARTAO', 'DINHEIRO')
    ),
    CONSTRAINT ck_lancamento_forma_receita CHECK (
        tipo = 'DESPESA' OR forma_pagamento IS NOT NULL
    ),
    CONSTRAINT ck_lancamento_origem CHECK (
        origem IN ('ATENDIMENTO', 'VENDA_PRODUTO', 'ASSINATURA', 'MANUAL')
    )
);

CREATE INDEX ix_lancamento_data ON lancamento (data_hora);
CREATE INDEX ix_lancamento_fechamento ON lancamento (fechamento_id);
CREATE INDEX ix_lancamento_registrado_por ON lancamento (registrado_por);

CREATE TABLE venda_produto (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    produto_id          BIGINT        NOT NULL,
    quantidade          INTEGER       NOT NULL,
    preco_unitario      NUMERIC(10,2) NOT NULL,
    vendedor_id         BIGINT        NOT NULL,
    percentual_comissao NUMERIC(5,2)  NOT NULL,
    lancamento_id       BIGINT        NOT NULL,
    criado_em           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_venda_lancamento UNIQUE (lancamento_id),
    CONSTRAINT fk_venda_produto FOREIGN KEY (produto_id)
        REFERENCES produto (id) ON DELETE RESTRICT,
    CONSTRAINT fk_venda_vendedor FOREIGN KEY (vendedor_id)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT fk_venda_lancamento FOREIGN KEY (lancamento_id)
        REFERENCES lancamento (id) ON DELETE RESTRICT,
    CONSTRAINT ck_venda_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_venda_preco CHECK (preco_unitario >= 0),
    CONSTRAINT ck_venda_comissao CHECK (percentual_comissao BETWEEN 0 AND 100)
);

CREATE INDEX ix_venda_produto ON venda_produto (produto_id);
CREATE INDEX ix_venda_vendedor ON venda_produto (vendedor_id);

CREATE TABLE comissao (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    barbeiro_id       BIGINT        NOT NULL,
    origem            VARCHAR(12)   NOT NULL,
    agendamento_id    BIGINT,
    venda_produto_id  BIGINT,
    base_valor        NUMERIC(10,2) NOT NULL,
    percentual        NUMERIC(5,2)  NOT NULL,
    valor             NUMERIC(10,2) NOT NULL,
    gerada_em         TIMESTAMPTZ   NOT NULL,
    criado_em         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_comissao_agendamento UNIQUE (agendamento_id),
    CONSTRAINT uq_comissao_venda UNIQUE (venda_produto_id),
    CONSTRAINT fk_comissao_barbeiro FOREIGN KEY (barbeiro_id)
        REFERENCES barbeiro (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_comissao_agendamento FOREIGN KEY (agendamento_id)
        REFERENCES agendamento (id) ON DELETE RESTRICT,
    CONSTRAINT fk_comissao_venda FOREIGN KEY (venda_produto_id)
        REFERENCES venda_produto (id) ON DELETE RESTRICT,
    CONSTRAINT ck_comissao_origem CHECK (origem IN ('ATENDIMENTO', 'VENDA')),
    -- Exatamente uma origem preenchida, coerente com o tipo
    CONSTRAINT ck_comissao_uma_origem CHECK (
        (origem = 'ATENDIMENTO' AND agendamento_id IS NOT NULL AND venda_produto_id IS NULL)
        OR (origem = 'VENDA' AND venda_produto_id IS NOT NULL AND agendamento_id IS NULL)
    ),
    CONSTRAINT ck_comissao_base CHECK (base_valor >= 0),
    CONSTRAINT ck_comissao_percentual CHECK (percentual BETWEEN 0 AND 100),
    CONSTRAINT ck_comissao_valor CHECK (valor >= 0)
);

CREATE INDEX ix_comissao_barbeiro ON comissao (barbeiro_id, gerada_em);

-- Fecha a ligação deixada na V3
ALTER TABLE movimento_estoque
    ADD CONSTRAINT fk_movimento_lancamento FOREIGN KEY (lancamento_id)
        REFERENCES lancamento (id) ON DELETE RESTRICT;

CREATE INDEX ix_movimento_lancamento ON movimento_estoque (lancamento_id);