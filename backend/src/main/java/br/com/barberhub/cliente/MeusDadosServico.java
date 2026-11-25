package br.com.barberhub.cliente;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.acesso.Cliente;
import br.com.barberhub.acesso.ClienteRepositorio;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

/** O cliente cuidando do próprio cadastro (RF-CLI-02 e RF-CLI-05). */
@Service
@RequiredArgsConstructor
public class MeusDadosServico {

    private final UsuarioRepositorio usuarios;
    private final ClienteRepositorio clientes;
    private final Clock relogio;

    @Transactional(readOnly = true)
    public MeusDadosResposta consultar(Long id) {
        return MeusDadosResposta.de(buscarUsuario(id), buscarCliente(id));
    }

    @Transactional
    public MeusDadosResposta alterar(Long id, MeusDadosRequisicao requisicao) {
        Usuario usuario = buscarUsuario(id);
        Cliente cliente = buscarCliente(id);

        usuario.setNome(requisicao.nome().trim());
        usuario.setTelefone(requisicao.telefone());
        usuarios.save(usuario);
        if (cliente.isAceitaMensagens() != requisicao.aceitaMensagens()) {
            cliente.setAceitaMensagens(requisicao.aceitaMensagens());
            cliente.setConsentimentoEm(relogio.instant());
            clientes.save(cliente);
        }
        return MeusDadosResposta.de(usuario, cliente);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ClienteServico.NAO_ENCONTRADO));
    }

    private Cliente buscarCliente(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ClienteServico.NAO_ENCONTRADO));
    }
}
