package br.com.barberhub.agendamento;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.barbeiro.BarbeiroRepositorio;
import br.com.barberhub.barbeiro.EscalaBarbeiro;
import br.com.barberhub.barbeiro.EscalaBarbeiroRepositorio;
import br.com.barberhub.barbeiro.FolgaBarbeiroRepositorio;
import br.com.barberhub.catalogo.Servico;
import br.com.barberhub.catalogo.ServicoRepositorio;
import br.com.barberhub.compartilhado.erro.ConflitoException;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;

class DisponibilidadeServicoTest {

    private static final long RAFAEL = 5L;
    private static final long CORTE = 1L;
    private static final ZoneId MANAUS = ZoneId.of("America/Manaus");
    /** Terça, 01/12/2026, 10:07 em Manaus. */
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-12-01T14:07:00Z"), MANAUS);
    private static final LocalDate HOJE = LocalDate.of(2026, 12, 1);
    private static final LocalDate AMANHA = HOJE.plusDays(1);
    private static final short QUARTA = 3;

    private final ServicoRepositorio servicos = mock(ServicoRepositorio.class);
    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final BarbeiroRepositorio barbeiros = mock(BarbeiroRepositorio.class);
    private final EscalaBarbeiroRepositorio escalas = mock(EscalaBarbeiroRepositorio.class);
    private final FolgaBarbeiroRepositorio folgas = mock(FolgaBarbeiroRepositorio.class);
    private final ExpedienteRepositorio expedientes = mock(ExpedienteRepositorio.class);
    private final AgendamentoRepositorio agendamentos = mock(AgendamentoRepositorio.class);
    private final DisponibilidadeServico servico = new DisponibilidadeServico(servicos, usuarios, barbeiros,
            escalas, folgas, expedientes, agendamentos, RELOGIO);

    private final Servico corte = new Servico();
    private final Usuario rafael = new Usuario();

    @BeforeEach
    void preparar() {
        corte.setId(CORTE);
        corte.setDuracaoMinutos((short) 30);
        corte.setPreco(new BigDecimal("45.00"));
        corte.setAtivo(true);
        when(servicos.findById(CORTE)).thenReturn(Optional.of(corte));

        rafael.setId(RAFAEL);
        rafael.setPerfil(Perfil.BARBEIRO);
        rafael.setAtivo(true);
        when(usuarios.findById(RAFAEL)).thenReturn(Optional.of(rafael));
        when(barbeiros.existsById(RAFAEL)).thenReturn(true);

        expediente((short) 2, "09:00", "19:00");
        expediente(QUARTA, "09:00", "19:00");
        escala((short) 2, "09:00", "12:00");
        escala(QUARTA, "09:00", "10:00");
    }

    private void expediente(short dia, String abre, String fecha) {
        when(expedientes.findByDiaSemana(dia)).thenReturn(Optional.of(
                new ExpedienteBarbearia(dia, LocalTime.parse(abre), LocalTime.parse(fecha), false)));
    }

    private void escala(short dia, String... horas) {
        List<EscalaBarbeiro> lista = new java.util.ArrayList<>();
        for (int i = 0; i < horas.length; i += 2) {
            lista.add(new EscalaBarbeiro(RAFAEL, dia, LocalTime.parse(horas[i]), LocalTime.parse(horas[i + 1])));
        }
        when(escalas.findByBarbeiroIdAndDiaSemanaOrderByInicioAsc(RAFAEL, dia)).thenReturn(lista);
    }

    private static Agendamento ocupado(long id, LocalDateTime inicio, int minutos) {
        Agendamento a = new Agendamento();
        a.setId(id);
        a.setInicio(inicio.atZone(MANAUS).toInstant());
        a.setFim(inicio.plusMinutes(minutos).atZone(MANAUS).toInstant());
        return a;
    }

    private void ocupados(Agendamento... lista) {
        when(agendamentos.ocupando(eq(RAFAEL), any(), any())).thenReturn(List.of(lista));
    }

    private List<String> livres(LocalDate data) {
        return servico.horariosLivres(RAFAEL, CORTE, data).stream().map(LocalTime::toString).toList();
    }

    @Test
    void ofereceDe15Em15MinutosSemPassarDoFimDaEscala() {
        // Corte de 30 min em escala das 9h às 10h: o último que cabe é 9h30.
        assertEquals(List.of("09:00", "09:15", "09:30"), livres(AMANHA));
    }

    @Test
    void nenhumHorarioUltrapassaOFimDoExpediente() {
        escala(QUARTA, "18:00", "22:00");

        assertEquals(List.of("18:00", "18:15", "18:30"), livres(AMANHA));
    }

    @Test
    void nenhumHorarioComecaAntesDaAbertura() {
        escala(QUARTA, "07:00", "09:30");

        assertEquals(List.of("09:00"), livres(AMANHA));
    }

    @Test
    void respeitaAPausaDoAlmoco() {
        escala(QUARTA, "11:30", "12:00", "13:00", "13:30");

        assertEquals(List.of("11:30", "13:00"), livres(AMANHA));
    }

    @Test
    void servicoMaisLongoOcupaMaisTempo() {
        corte.setDuracaoMinutos((short) 60);

        assertEquals(List.of("09:00"), livres(AMANHA));
    }

    @Test
    void servicoQueNaoCabeNaEscalaNaoTemHorario() {
        corte.setDuracaoMinutos((short) 75);

        assertTrue(livres(AMANHA).isEmpty());
    }

    @Test
    void escalaForaDaGradeComecaNoProximoQuartoDeHora() {
        escala(QUARTA, "09:10", "10:00");

        assertEquals(List.of("09:15", "09:30"), livres(AMANHA));
    }

    @Test
    void naoOfereceHorarioQueBateComOutroAgendamento() {
        escala(QUARTA, "09:00", "11:00");
        ocupados(ocupado(1, AMANHA.atTime(9, 30), 30));

        // 9h15 invadiria o das 9h30; 10h encosta no fim dele e pode.
        assertEquals(List.of("09:00", "10:00", "10:15", "10:30"), livres(AMANHA));
    }

    @Test
    void naFolgaNaoHaHorario() {
        when(folgas.existsByBarbeiroIdAndData(RAFAEL, AMANHA)).thenReturn(true);

        assertTrue(livres(AMANHA).isEmpty());
    }

    @Test
    void comABarbeariaFechadaNaoHaHorario() {
        when(expedientes.findByDiaSemana(QUARTA))
                .thenReturn(Optional.of(new ExpedienteBarbearia(QUARTA, null, null, true)));
        assertTrue(livres(AMANHA).isEmpty());

        when(expedientes.findByDiaSemana(QUARTA)).thenReturn(Optional.empty());
        assertTrue(livres(AMANHA).isEmpty());
    }

    @Test
    void semEscalaNoDiaNaoHaHorario() {
        escala(QUARTA);

        assertTrue(livres(AMANHA).isEmpty());
    }

    @Test
    void domingoEhODiaZero() {
        LocalDate domingo = LocalDate.of(2026, 12, 6);
        expediente((short) 0, "09:00", "19:00");
        escala((short) 0, "09:00", "09:30");

        assertEquals(List.of("09:00"), livres(domingo));
    }

    @Test
    void hojeSoOfereceOQueAindaNaoPassou() {
        // São 10h07 em Manaus.
        assertEquals("10:15", livres(HOJE).get(0));
        assertFalse(livres(HOJE).contains("10:00"));
    }

    @Test
    void ontemEAlemDe30DiasNaoTemHorario() {
        assertTrue(livres(HOJE.minusDays(1)).isEmpty());
        LocalDate limite = HOJE.plusDays(30);
        short diaDoLimite = (short) (limite.getDayOfWeek().getValue() % 7);
        short diaSeguinte = (short) (limite.plusDays(1).getDayOfWeek().getValue() % 7);
        expediente(diaDoLimite, "09:00", "19:00");
        expediente(diaSeguinte, "09:00", "19:00");
        escala(diaDoLimite, "09:00", "10:00");
        escala(diaSeguinte, "09:00", "10:00");

        assertFalse(livres(limite).isEmpty());
        assertTrue(livres(limite.plusDays(1)).isEmpty());
    }

    @Test
    void recusaServicoInativoEBarbeiroInativoOuQueNaoEhBarbeiro() {
        corte.setAtivo(false);
        assertThrows(ExcecaoNegocio.class, () -> livres(AMANHA));

        corte.setAtivo(true);
        rafael.setAtivo(false);
        assertThrows(ExcecaoNegocio.class, () -> livres(AMANHA));

        rafael.setAtivo(true);
        rafael.setPerfil(Perfil.CLIENTE);
        assertThrows(ExcecaoNegocio.class, () -> livres(AMANHA));
    }

    @Test
    void aoGravarRecusaHorarioQueNuncaSeriaOferecido() {
        for (LocalDateTime invalido : List.of(
                AMANHA.atTime(9, 10),
                AMANHA.atTime(9, 15, 30),
                AMANHA.atTime(9, 45),
                AMANHA.atTime(8, 0),
                HOJE.atTime(10, 0),
                HOJE.plusDays(36).atTime(9, 0))) {
            assertThrows(ExcecaoNegocio.class, () -> servico.exigirLivre(RAFAEL, 30, invalido, null),
                    () -> "Deveria recusar " + invalido);
        }
        assertDoesNotThrow(() -> servico.exigirLivre(RAFAEL, 30, AMANHA.atTime(9, 30), null));
    }

    @Test
    void aoGravarHorarioJaOcupadoDaConflito() {
        ocupados(ocupado(1, AMANHA.atTime(9, 0), 30));

        assertThrows(ConflitoException.class, () -> servico.exigirLivre(RAFAEL, 30, AMANHA.atTime(9, 0), null));
    }

    @Test
    void oProprioAgendamentoNaoAtrapalhaAoRemarcar() {
        ocupados(ocupado(7, AMANHA.atTime(9, 0), 30));

        assertDoesNotThrow(() -> servico.exigirLivre(RAFAEL, 30, AMANHA.atTime(9, 15), 7L));
        assertTrue(servico.horariosLivres(RAFAEL, 30, AMANHA, 7L).contains(LocalTime.of(9, 15)));
    }
}
