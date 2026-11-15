package br.com.barberhub.compartilhado.erro;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManipuladorDeErros {

    private static final Logger log = LoggerFactory.getLogger(ManipuladorDeErros.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> dadosInvalidos(MethodArgumentNotValidException e) {
        List<ErroResposta.CampoInvalido> campos = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErroResposta.CampoInvalido(f.getField(), f.getDefaultMessage()))
                .toList();
        var corpo = new ErroResposta(400, "DADOS_INVALIDOS", "Há campos inválidos.", campos, Instant.now());
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoIlegivel(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "CORPO_INVALIDO", "O corpo da requisição é inválido.");
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(ExcecaoNegocio.class)
    public ResponseEntity<ErroResposta> regraDeNegocio(ExcecaoNegocio e) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, "REGRA_DE_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> conflito(DataIntegrityViolationException e) {
        log.warn("Violação de integridade no banco", e);
        return resposta(HttpStatus.CONFLICT, "CONFLITO", "A operação conflita com dados já existentes.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> inesperado(Exception e) {
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Ocorreu um erro inesperado.");
    }

    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(ErroResposta.de(status.value(), codigo, mensagem));
    }
}