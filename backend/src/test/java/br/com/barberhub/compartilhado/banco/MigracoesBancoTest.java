package br.com.barberhub.compartilhado.banco;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Aplica todas as migrações Flyway num PostgreSQL 17 descartável e confere o resultado.
 * Não usa o banco de homologação. Precisa do Docker rodando; sem ele, a classe é ignorada.
 */
@Testcontainers(disabledWithoutDocker = true)
class MigracoesBancoTest {

    private static final Set<String> TABELAS_ESPERADAS = Set.of(
            "usuario", "barbeiro", "cliente", "expediente_barbearia", "escala_barbeiro",
            "folga_barbeiro", "configuracao", "servico", "combo_item", "produto",
            "movimento_estoque", "agendamento", "fechamento_caixa", "lancamento", "venda_produto",
            "comissao", "plano", "assinatura", "assinatura_pagamento", "assinatura_historico",
            "mensagem_recuperacao", "auditoria");

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17");

    @BeforeAll
    static void aplicarMigracoes() {
        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load();
        assertTrue(flyway.migrate().success, "As migrações precisam aplicar sem erro.");
        assertEquals(0, flyway.info().pending().length, "Não pode sobrar migração pendente.");
    }

    private static Connection conectar() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static List<String> consultar(String sql) throws SQLException {
        List<String> linhas = new ArrayList<>();
        try (Connection c = conectar(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                linhas.add(r.getString(1));
            }
        }
        return linhas;
    }

    private static long inserirUsuario(String perfil) throws SQLException {
        String sql = "INSERT INTO usuario (nome, email, perfil) VALUES (?, ?, ?) RETURNING id";
        try (Connection c = conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "Pessoa de teste");
            p.setString(2, UUID.randomUUID() + "@teste.com");
            p.setString(3, perfil);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getLong(1);
            }
        }
    }

    private static void executar(String sql, Object... parametros) throws SQLException {
        try (Connection c = conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                p.setObject(i + 1, parametros[i]);
            }
            p.executeUpdate();
        }
    }

    private static long inserirServico() throws SQLException {
        String sql = "INSERT INTO servico (nome, duracao_minutos, preco) VALUES (?, 30, 40.00) RETURNING id";
        try (Connection c = conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "Corte " + UUID.randomUUID());
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getLong(1);
            }
        }
    }

    private static OffsetDateTime horario(int hora, int minuto) {
        return OffsetDateTime.of(2030, 1, 15, hora, minuto, 0, 0, ZoneOffset.UTC);
    }

    private static void agendar(long cliente, long barbeiro, long servico,
            OffsetDateTime inicio, OffsetDateTime fim, String estado) throws SQLException {
        executar("INSERT INTO agendamento (cliente_id, barbeiro_id, servico_id, inicio, fim, estado,"
                + " preco_cobrado, origem, criado_por) VALUES (?, ?, ?, ?, ?, ?, 40.00, 'APP', ?)",
                cliente, barbeiro, servico, inicio, fim, estado, cliente);
    }

    @Test
    void criaTodasAsTabelasDoModelo() throws SQLException {
        List<String> existentes = consultar(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'");

        assertTrue(existentes.containsAll(TABELAS_ESPERADAS),
                "Faltam tabelas: " + TABELAS_ESPERADAS.stream().filter(t -> !existentes.contains(t)).toList());
    }

    @Test
    void todaTabelaTemRlsLigado() throws SQLException {
        // Regra do projeto: tabela nova precisa de ENABLE ROW LEVEL SECURITY na própria migração.
        List<String> semRls = consultar("""
                SELECT c.relname FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = 'public' AND c.relkind = 'r'
                  AND c.relname <> 'flyway_schema_history' AND NOT c.relrowsecurity
                """);

        assertEquals(List.of(), semRls, "Tabelas sem RLS ligado");
    }

    @Test
    void administradorInicialExisteSemSenha() throws SQLException {
        try (Connection c = conectar(); Statement s = c.createStatement();
                ResultSet r = s.executeQuery(
                        "SELECT perfil, senha_hash, ativo FROM usuario WHERE email = 'admin@barberhub.local'")) {
            assertTrue(r.next(), "O administrador inicial precisa existir.");
            assertEquals("ADMINISTRADOR", r.getString("perfil"));
            assertNull(r.getString("senha_hash"), "Nenhuma senha pode vir da migração.");
            assertTrue(r.getBoolean("ativo"));
        }
    }

    @Test
    void configuracoesPadraoForamCriadas() throws SQLException {
        List<String> chaves = consultar("SELECT chave FROM configuracao");

        assertTrue(chaves.containsAll(
                List.of("antecedencia_minima_horas", "dias_inatividade", "limite_mensagens")));
    }

    @Test
    void expedienteInicialTemOsSeteDiasEDomingoFechado() throws SQLException {
        assertEquals(List.of("7"), consultar("SELECT count(*) FROM expediente_barbearia"));
        assertEquals(List.of("0"), consultar("SELECT dia_semana FROM expediente_barbearia WHERE fechado"));
    }

    @Test
    void emailUnicoSemDiferenciarMaiusculas() throws SQLException {
        String email = UUID.randomUUID() + "@teste.com";
        executar("INSERT INTO usuario (nome, email, perfil) VALUES ('A', ?, 'CLIENTE')", email.toLowerCase());

        SQLException erro = assertThrows(SQLException.class, () ->
                executar("INSERT INTO usuario (nome, email, perfil) VALUES ('B', ?, 'CLIENTE')",
                        email.toUpperCase()));

        assertEquals("23505", erro.getSQLState());
    }

    @Test
    void perfilForaDaListaEhRecusado() {
        SQLException erro = assertThrows(SQLException.class, () ->
                executar("INSERT INTO usuario (nome, email, perfil) VALUES ('A', ?, 'DONO')",
                        UUID.randomUUID() + "@teste.com"));

        assertEquals("23514", erro.getSQLState());
    }

    @Test
    void barbeiroNaoPodeTerDoisAgendamentosSobrepostos() throws SQLException {
        long cliente = inserirUsuario("CLIENTE");
        long barbeiro = inserirUsuario("BARBEIRO");
        executar("INSERT INTO cliente (usuario_id) VALUES (?)", cliente);
        executar("INSERT INTO barbeiro (usuario_id, percentual_comissao) VALUES (?, 10)", barbeiro);
        long servico = inserirServico();

        agendar(cliente, barbeiro, servico, horario(10, 0), horario(10, 30), "AGENDADO");

        // Sobrepõe 10:15 a 10:45: o banco recusa (RN01).
        SQLException conflito = assertThrows(SQLException.class, () ->
                agendar(cliente, barbeiro, servico, horario(10, 15), horario(10, 45), "AGENDADO"));
        assertEquals("23P01", conflito.getSQLState());

        // Começar exatamente quando o outro termina é permitido.
        assertDoesNotThrow(() ->
                agendar(cliente, barbeiro, servico, horario(10, 30), horario(11, 0), "AGENDADO"));
        // Horário cancelado não ocupa a agenda.
        assertDoesNotThrow(() ->
                agendar(cliente, barbeiro, servico, horario(10, 0), horario(10, 30), "CANCELADO"));
    }

    @Test
    void saldoDeEstoqueNaoFicaNegativo() {
        SQLException erro = assertThrows(SQLException.class, () ->
                executar("INSERT INTO produto (nome, preco_venda, saldo_estoque) VALUES (?, 20.00, -1)",
                        "Pomada " + UUID.randomUUID()));

        assertEquals("23514", erro.getSQLState());
    }

    @Test
    void naoApagaUsuarioQueTemBarbeiro() throws SQLException {
        long barbeiro = inserirUsuario("BARBEIRO");
        executar("INSERT INTO barbeiro (usuario_id, percentual_comissao) VALUES (?, 10)", barbeiro);

        SQLException erro = assertThrows(SQLException.class, () ->
                executar("DELETE FROM usuario WHERE id = ?", barbeiro));

        assertEquals("23503", erro.getSQLState());
    }
}
