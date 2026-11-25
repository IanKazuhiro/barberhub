package br.com.barberhub.barbeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;

class EscalaServicoTest {

    private static final long ADMIN = 1L;
    private static final long RAFAEL = 5L;

    private final BarbeiroRepositorio barbeiros = mock(BarbeiroRepositorio.class);
    private final EscalaBarbeiroRepositorio escalas = mock(EscalaBarbeiroRepositorio.class);
    private final AuditoriaServico auditoria = mock(AuditoriaServico.class);
    private final EscalaServico servico = new EscalaServico(barbeiros, escalas, auditoria);

    @BeforeEach
    void existeOBarbeiro() {
        when(barbeiros.existsById(RAFAEL)).thenReturn(true);
    }

    private static IntervaloEscala intervalo(int dia, String inicio, String fim) {
        return new IntervaloEscala(dia, LocalTime.parse(inicio), LocalTime.parse(fim));
    }

    private static EscalaRequisicao escala(IntervaloEscala... intervalos) {
        return new EscalaRequisicao(List.of(intervalos));
    }

    @Test
    void substituiAEscalaInteiraEmOrdemERegistraAuditoria() {
        List<IntervaloEscala> salvos = servico.substituir(ADMIN, RAFAEL,
                escala(intervalo(1, "14:00", "19:00"), intervalo(1, "09:00", "12:00")));

        assertEquals(LocalTime.of(9, 0), salvos.get(0).inicio());
        InOrder ordem = inOrder(escalas);
        ordem.verify(escalas).apagarDoBarbeiro(RAFAEL);
        ordem.verify(escalas).saveAll(any());
        verify(auditoria).registrar(ADMIN, "ALTERAR_ESCALA", "barbeiro", RAFAEL);
    }

    @Test
    void intervalosEncostadosSaoPermitidos() {
        assertEquals(2, servico.substituir(ADMIN, RAFAEL,
                escala(intervalo(1, "09:00", "12:00"), intervalo(1, "12:00", "18:00"))).size());
    }

    @Test
    void mesmoHorarioEmDiasDiferentesEPermitido() {
        assertEquals(2, servico.substituir(ADMIN, RAFAEL,
                escala(intervalo(1, "09:00", "12:00"), intervalo(2, "09:00", "12:00"))).size());
    }

    @Test
    void recusaIntervalosSobrepostosNoMesmoDia() {
        ExcecaoNegocio erro = assertThrows(ExcecaoNegocio.class, () -> servico.substituir(ADMIN, RAFAEL,
                escala(intervalo(1, "11:00", "15:00"), intervalo(1, "09:00", "12:00"))));

        assertEquals(EscalaServico.INTERVALOS_SOBREPOSTOS, erro.getMessage());
        verify(escalas, never()).apagarDoBarbeiro(any());
    }

    @Test
    void recusaInicioIgualOuDepoisDoFim() {
        assertThrows(ExcecaoNegocio.class,
                () -> servico.substituir(ADMIN, RAFAEL, escala(intervalo(1, "12:00", "09:00"))));
        assertThrows(ExcecaoNegocio.class,
                () -> servico.substituir(ADMIN, RAFAEL, escala(intervalo(1, "09:00", "09:00"))));
        verify(escalas, never()).apagarDoBarbeiro(any());
    }

    @Test
    void listaVaziaLimpaAEscala() {
        assertTrue(servico.substituir(ADMIN, RAFAEL, escala()).isEmpty());
        verify(escalas).apagarDoBarbeiro(RAFAEL);
    }

    @Test
    void barbeiroInexistenteDaNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.consultar(99L));
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.substituir(ADMIN, 99L, escala()));
    }
}
