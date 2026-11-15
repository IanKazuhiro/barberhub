package br.com.barberhub.compartilhado.erro;

public class ExcecaoNegocio extends RuntimeException {
    public ExcecaoNegocio(String mensagem) {
        super(mensagem);
    }
}