package br.com.barberhub.acesso;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.barbeiro.Barbeiro;
import br.com.barberhub.barbeiro.BarbeiroRepositorio;
import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

/**
 * Troca o perfil de um usuário e acerta as tabelas ligadas ao perfil (cliente e barbeiro).
 * Cada perfil tem a sua linha: CLIENTE em "cliente", BARBEIRO em "barbeiro", ADMINISTRADOR em nenhuma.
 */
@Service
@RequiredArgsConstructor
public class TrocaPerfilServico {

    static final String NAO_ENCONTRADO = "Usuário não encontrado.";
    static final String NAO_TROCA_O_PROPRIO = "Você não pode trocar o seu próprio perfil.";
    static final String NAO_TROCA_ADMIN_INICIAL = "O perfil do administrador inicial não pode ser trocado.";
    static final String COM_HISTORICO = "Não foi possível trocar o perfil: este usuário já tem dados "
            + "ligados ao perfil atual (como agendamentos ou barbeiro favorito).";

    private final UsuarioRepositorio usuarios;
    private final ClienteRepositorio clientes;
    private final BarbeiroRepositorio barbeiros;
    private final AuditoriaServico auditoria;

    @Transactional
    public UsuarioAdminResposta trocar(Long administradorId, Long id, Perfil novoPerfil) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));

        if (usuario.getId().equals(administradorId)) {
            throw new ExcecaoNegocio(NAO_TROCA_O_PROPRIO);
        }
        if (Usuario.EMAIL_ADMINISTRADOR_INICIAL.equalsIgnoreCase(usuario.getEmail())) {
            throw new ExcecaoNegocio(NAO_TROCA_ADMIN_INICIAL);
        }
        if (usuario.getPerfil() == novoPerfil) {
            return UsuarioAdminResposta.de(usuario);
        }

        try {
            // Primeiro tira a linha do perfil antigo (o banco recusa se houver histórico ligado a ela)...
            if (novoPerfil != Perfil.CLIENTE && clientes.existsById(id)) {
                clientes.deleteById(id);
                clientes.flush();
            }
            if (novoPerfil != Perfil.BARBEIRO && barbeiros.existsById(id)) {
                barbeiros.deleteById(id);
                barbeiros.flush();
            }
            // ...depois garante a linha do perfil novo.
            if (novoPerfil == Perfil.CLIENTE && !clientes.existsById(id)) {
                clientes.save(new Cliente(id));
            }
            if (novoPerfil == Perfil.BARBEIRO && !barbeiros.existsById(id)) {
                barbeiros.save(new Barbeiro(id, BigDecimal.ZERO));
            }
        } catch (DataIntegrityViolationException e) {
            throw new ExcecaoNegocio(COM_HISTORICO);
        }

        usuario.setPerfil(novoPerfil);
        usuarios.save(usuario);
        auditoria.registrar(administradorId, "TROCAR_PERFIL_" + novoPerfil.name(), "usuario", usuario.getId());
        return UsuarioAdminResposta.de(usuario);
    }
}
