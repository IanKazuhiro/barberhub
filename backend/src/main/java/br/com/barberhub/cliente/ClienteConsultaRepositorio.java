package br.com.barberhub.cliente;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import br.com.barberhub.acesso.Usuario;

public interface ClienteConsultaRepositorio extends Repository<Usuario, Long> {

    /** Os padrões já chegam prontos (com %, sem acento e com os curingas escapados). */
    @Query("""
            select u from Usuario u
            where u.perfil = br.com.barberhub.acesso.Perfil.CLIENTE
              and (cast(function('translate', lower(u.nome), 'áàâãäéèêëíìîïóòôõöúùûüç', 'aaaaaeeeeiiiiooooouuuuc')
                       as string) like :nome escape '!'
                   or u.telefone like :telefone)
            order by lower(u.nome), u.id
            """)
    List<Usuario> buscar(@Param("nome") String nome, @Param("telefone") String telefone, Limit limite);
}
