package br.com.barberhub.compartilhado.erro;

/** O pedido era válido, mas outro chegou antes (ex.: horário que acabou de ser ocupado). Responde 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
