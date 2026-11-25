package br.com.barberhub.cliente;

import br.com.barberhub.acesso.Cliente;
import br.com.barberhub.acesso.Usuario;

/** O que o próprio cliente vê. As anotações internas da barbearia não entram aqui. */
public record MeusDadosResposta(String nome, String email, String telefone, boolean aceitaMensagens) {

    static MeusDadosResposta de(Usuario u, Cliente c) {
        return new MeusDadosResposta(u.getNome(), u.getEmail(), u.getTelefone(), c.isAceitaMensagens());
    }
}
