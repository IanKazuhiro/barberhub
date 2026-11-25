package br.com.barberhub.cliente;

import java.time.Instant;

import br.com.barberhub.acesso.Cliente;
import br.com.barberhub.acesso.Usuario;

public record ClienteDetalheResposta(Long id, String nome, String email, String telefone, boolean ativo,
        boolean aceitaMensagens, Long barbeiroFavoritoId, String barbeiroFavorito, String tipoCorte,
        String observacoes, Instant clienteDesde) {

    static ClienteDetalheResposta de(Usuario u, Cliente c, String barbeiroFavorito) {
        return new ClienteDetalheResposta(u.getId(), u.getNome(), u.getEmail(), u.getTelefone(), u.isAtivo(),
                c.isAceitaMensagens(), c.getBarbeiroFavoritoId(), barbeiroFavorito, c.getTipoCorte(),
                c.getObservacoes(), c.getCriadoEm());
    }
}
