package br.com.barberhub.compartilhado.saude;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaudeController {

    public record RespostaSaude(String aplicacao, String banco) {
    }

    private final DataSource dataSource;

    public SaudeController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/saude")
    public ResponseEntity<RespostaSaude> verificar() {
        boolean bancoOk;
        try (Connection conexao = dataSource.getConnection()) {
            bancoOk = conexao.isValid(2);
        } catch (SQLException e) {
            bancoOk = false;
        }

        if (bancoOk) {
            return ResponseEntity.ok(new RespostaSaude("ok", "ok"));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new RespostaSaude("ok", "indisponivel"));
    }
}