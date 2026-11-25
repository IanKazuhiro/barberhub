package br.com.barberhub.agendamento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** O barbeiro é sempre o do token: não há como pedir a agenda de outro. */
@RestController
@RequestMapping("/api/barbeiro/agenda")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaServico agenda;

    @GetMapping
    public List<ItemAgendaResposta> doDia(@AuthenticationPrincipal Jwt token,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return agenda.doDia(Long.valueOf(token.getSubject()), data);
    }
}
