CREATE TABLE expediente_barbearia (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dia_semana    SMALLINT    NOT NULL,
    abertura      TIME,
    fechamento    TIME,
    fechado       BOOLEAN     NOT NULL DEFAULT FALSE,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_expediente_dia UNIQUE (dia_semana),
    CONSTRAINT ck_expediente_dia CHECK (dia_semana BETWEEN 0 AND 6),
    CONSTRAINT ck_expediente_horario CHECK (
        fechado OR (abertura IS NOT NULL AND fechamento IS NOT NULL AND abertura < fechamento)
    )
);

CREATE TABLE escala_barbeiro (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    barbeiro_id   BIGINT      NOT NULL,
    dia_semana    SMALLINT    NOT NULL,
    inicio        TIME        NOT NULL,
    fim           TIME        NOT NULL,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_escala_barbeiro FOREIGN KEY (barbeiro_id)
        REFERENCES barbeiro (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT ck_escala_dia CHECK (dia_semana BETWEEN 0 AND 6),
    CONSTRAINT ck_escala_horario CHECK (inicio < fim)
);

CREATE INDEX ix_escala_barbeiro ON escala_barbeiro (barbeiro_id);

CREATE TABLE folga_barbeiro (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    barbeiro_id BIGINT      NOT NULL,
    data        DATE        NOT NULL,
    motivo      VARCHAR(200),
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_folga_barbeiro FOREIGN KEY (barbeiro_id)
        REFERENCES barbeiro (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT uq_folga_barbeiro_data UNIQUE (barbeiro_id, data)
);

CREATE TABLE configuracao (
    chave          VARCHAR(60)  PRIMARY KEY,
    valor          VARCHAR(200) NOT NULL,
    descricao      VARCHAR(200),
    atualizado_por BIGINT,
    criado_em      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_configuracao_usuario FOREIGN KEY (atualizado_por)
        REFERENCES usuario (id) ON DELETE RESTRICT
);

INSERT INTO configuracao (chave, valor, descricao) VALUES
    ('antecedencia_minima_horas', '2',  'Antecedência mínima para cancelar ou remarcar'),
    ('dias_inatividade',          '30', 'Dias sem atendimento para o cliente entrar na recuperação'),
    ('limite_mensagens',          '2',  'Máximo de mensagens de recuperação por cliente');