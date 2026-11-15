-- Liga o RLS em todas as tabelas existentes do schema public (inclui flyway_schema_history).
-- Sem nenhuma policy, a API automática do Supabase (anon/authenticated) não enxerga nada.
-- O back-end conecta com o usuário postgres, que ignora o RLS, então continua funcionando.
DO $$
DECLARE
    t RECORD;
BEGIN
    FOR t IN SELECT tablename FROM pg_tables
             WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history' LOOP
        EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY', t.tablename);
    END LOOP;
END $$;

-- Reforço: tira as permissões dos papéis da API do Supabase.
-- Só roda onde esses papéis existem (no PostgreSQL local eles não existem).
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL ON ALL TABLES IN SCHEMA public FROM anon;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL ON ALL TABLES IN SCHEMA public FROM authenticated;
    END IF;
END $$;

-- Administrador inicial, SEM senha: nenhuma senha fica no repositório.
-- A senha será definida pela própria aplicação na entrega do login (sprint de acesso).
INSERT INTO usuario (nome, email, perfil)
VALUES ('Administrador', 'admin@barberhub.local', 'ADMINISTRADOR');