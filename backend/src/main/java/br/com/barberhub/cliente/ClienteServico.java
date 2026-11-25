package br.com.barberhub.cliente;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.barberhub.acesso.Cliente;
import br.com.barberhub.acesso.ClienteRepositorio;
import br.com.barberhub.acesso.Perfil;
import br.com.barberhub.acesso.Usuario;
import br.com.barberhub.acesso.UsuarioRepositorio;
import br.com.barberhub.compartilhado.auditoria.AuditoriaServico;
import br.com.barberhub.compartilhado.erro.ExcecaoNegocio;
import br.com.barberhub.compartilhado.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

/** Clientes vistos pela barbearia: busca, dados e preferências (RF-CLI-01, 02 e 04). */
@Service
@RequiredArgsConstructor
public class ClienteServico {

    static final String NAO_ENCONTRADO = "Cliente não encontrado.";
    static final String BARBEIRO_INDISPONIVEL = "O barbeiro favorito precisa ser um barbeiro ativo.";
    static final int LIMITE_DA_BUSCA = 50;

    private final UsuarioRepositorio usuarios;
    private final ClienteRepositorio clientes;
    private final ClienteConsultaRepositorio consulta;
    private final AuditoriaServico auditoria;

    /** Parte do nome (sem diferenciar acento e maiúscula) ou do telefone. Busca vazia lista os primeiros. */
    @Transactional(readOnly = true)
    public List<ClienteResumoResposta> buscar(String busca) {
        String texto = busca == null ? "" : busca.trim();
        return consulta.buscar(padraoDoNome(texto), padraoDoTelefone(texto), Limit.of(LIMITE_DA_BUSCA)).stream()
                .map(ClienteResumoResposta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteDetalheResposta detalhar(Long id) {
        return paraDetalhe(buscarUsuario(id), buscarCliente(id));
    }

    @Transactional
    public ClienteDetalheResposta alterarDados(Long administradorId, Long id, DadosClienteRequisicao requisicao) {
        Cliente cliente = buscarCliente(id);
        Usuario usuario = buscarUsuario(id);
        usuario.setNome(requisicao.nome().trim());
        usuario.setTelefone(requisicao.telefone());
        usuarios.save(usuario);
        auditoria.registrar(administradorId, "ALTERAR_CLIENTE", "cliente", id);
        return paraDetalhe(usuario, cliente);
    }

    @Transactional
    public ClienteDetalheResposta alterarPreferencias(Long autorId, Long id, PreferenciasRequisicao requisicao) {
        Cliente cliente = buscarCliente(id);
        Usuario usuario = buscarUsuario(id);
        Long favorito = requisicao.barbeiroFavoritoId();
        // Quem já era o favorito pode continuar, mesmo que tenha sido inativado depois.
        if (favorito != null && !favorito.equals(cliente.getBarbeiroFavoritoId()) && !ehBarbeiroAtivo(favorito)) {
            throw new ExcecaoNegocio(BARBEIRO_INDISPONIVEL);
        }
        cliente.setBarbeiroFavoritoId(favorito);
        cliente.setTipoCorte(textoOuNulo(requisicao.tipoCorte()));
        cliente.setObservacoes(textoOuNulo(requisicao.observacoes()));
        clientes.save(cliente);
        auditoria.registrar(autorId, "ALTERAR_PREFERENCIAS_CLIENTE", "cliente", id);
        return paraDetalhe(usuario, cliente);
    }

    static String padraoDoNome(String texto) {
        String semAcento = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return "%" + semAcento.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    /** Só vira busca por telefone quando não há letras; o vazio não casa com nenhum telefone. */
    static String padraoDoTelefone(String texto) {
        String digitos = texto.replaceAll("\\D", "");
        boolean temLetras = texto.chars().anyMatch(Character::isLetter);
        return digitos.isEmpty() || temLetras ? "" : "%" + digitos + "%";
    }

    private boolean ehBarbeiroAtivo(Long id) {
        return usuarios.findById(id)
                .filter(u -> u.getPerfil() == Perfil.BARBEIRO && u.isAtivo())
                .isPresent();
    }

    private ClienteDetalheResposta paraDetalhe(Usuario usuario, Cliente cliente) {
        String favorito = cliente.getBarbeiroFavoritoId() == null ? null
                : usuarios.findById(cliente.getBarbeiroFavoritoId()).map(Usuario::getNome).orElse(null);
        return ClienteDetalheResposta.de(usuario, cliente, favorito);
    }

    private Cliente buscarCliente(Long id) {
        return clientes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(NAO_ENCONTRADO));
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
