-- Move a extensão para fora do schema public (alerta do Advisor).
-- Só roda onde o schema "extensions" existe (Supabase); no PostgreSQL local nada muda.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = 'extensions') THEN
        ALTER EXTENSION btree_gist SET SCHEMA extensions;
    END IF;
END $$;

-- Índices que faltavam nas chaves estrangeiras
CREATE INDEX ix_configuracao_atualizado_por ON configuracao (atualizado_por);
CREATE INDEX ix_fechamento_fechado_por ON fechamento_caixa (fechado_por);