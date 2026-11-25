package br.com.barberhub.agendamento;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Agendamentos do cliente: o cliente é sempre o do token. */
@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
public class AgendamentoClienteController {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final DisponibilidadeServico disponibilidade;
    private final AgendamentoServico agendamentos;
    private final MeusAgendamentosServico meusAgendamentos;

    @GetMapping("/disponibilidade")
    public List<String> horariosLivres(@RequestParam Long barbeiroId, @RequestParam Long servicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return disponibilidade.horariosLivres(barbeiroId, servicoId, data).stream().map(HORA::format).toList();
    }

    @GetMapping("/agendamentos")
    public MeusAgendamentosResposta listar(@AuthenticationPrincipal Jwt token) {
        return meusAgendamentos.listar(Long.valueOf(token.getSubject()));
    }

    @PostMapping("/agendamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResposta agendar(@AuthenticationPrincipal Jwt token,
            @Valid @RequestBody NovoAgendamentoRequisicao requisicao) {
        return agendamentos.agendar(Long.valueOf(token.getSubject()), requisicao);
    }
}
