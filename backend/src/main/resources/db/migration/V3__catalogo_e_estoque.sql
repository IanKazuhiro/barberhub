CREATE TABLE servico (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome            VARCHAR(100)  NOT NULL,
    duracao_minutos SMALLINT      NOT NULL,
    preco           NUMERIC(10,2) NOT NULL,
    eh_combo        BOOLEAN       NOT NULL DEFAULT FALSE,
    ativo           BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_servico_nome UNIQUE (nome),
    CONSTRAINT ck_servico_duracao CHECK (duracao_minutos > 0),
    CONSTRAINT ck_servico_preco CHECK (preco >= 0)
);

CREATE TABLE combo_item (
    combo_id   BIGINT      NOT NULL,
    servico_id BIGINT      NOT NULL,
    criado_em  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (combo_id, servico_id),
    CONSTRAINT fk_combo_item_combo FOREIGN KEY (combo_id)
        REFERENCES servico (id) ON DELETE RESTRICT,
    CONSTRAINT fk_combo_item_servico FOREIGN KEY (servico_id)
        REFERENCES servico (id) ON DELETE RESTRICT,
    CONSTRAINT ck_combo_item_diferente CHECK (servico_id <> combo_id)
);

CREATE INDEX ix_combo_item_servico ON combo_item (servico_id);

CREATE TABLE produto (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome                VARCHAR(100)  NOT NULL,
    categoria           VARCHAR(60),
    preco_venda         NUMERIC(10,2) NOT NULL,
    percentual_comissao NUMERIC(5,2)  NOT NULL DEFAULT 0,
    estoque_minimo      INTEGER       NOT NULL DEFAULT 0,
    saldo_estoque       INTEGER       NOT NULL DEFAULT 0,
    ativo               BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_produto_nome UNIQUE (nome),
    CONSTRAINT ck_produto_preco CHECK (preco_venda >= 0),
    CONSTRAINT ck_produto_comissao CHECK (percentual_comissao BETWEEN 0 AND 100),
    CONSTRAINT ck_produto_estoque_minimo CHECK (estoque_minimo >= 0),
    -- RN09: o saldo nunca fica negativo
    CONSTRAINT ck_produto_saldo CHECK (saldo_estoque >= 0)
);

CREATE TABLE movimento_estoque (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    produto_id    BIGINT       NOT NULL,
    tipo          VARCHAR(20)  NOT NULL,
    quantidade    INTEGER      NOT NULL,
    motivo        VARCHAR(200),
    data_hora     TIMESTAMPTZ  NOT NULL,
    usuario_id    BIGINT       NOT NULL,
    -- A chave estrangeira para lancamento é criada na V5
    lancamento_id BIGINT,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_movimento_produto FOREIGN KEY (produto_id)
        REFERENCES produto (id) ON DELETE RESTRICT,
    CONSTRAINT fk_movimento_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT ck_movimento_tipo CHECK (
        tipo IN ('ENTRADA', 'SAIDA_VENDA', 'SAIDA_PERDA', 'SAIDA_ATENDIMENTO')
    ),
    CONSTRAINT ck_movimento_quantidade CHECK (quantidade > 0)
);

CREATE INDEX ix_movimento_produto ON movimento_estoque (produto_id, data_hora);
CREATE INDEX ix_movimento_usuario ON movimento_estoque (usuario_id);