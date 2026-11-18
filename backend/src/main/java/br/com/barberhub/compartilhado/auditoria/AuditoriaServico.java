package br.com.barberhub.compartilhado.auditoria;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Registra quem fez o quê. Roda na transação de quem chama. */
@Service
@RequiredArgsConstructor
public class AuditoriaServico {

    private final AuditoriaRepositorio auditorias;

    public void registrar(Long usuarioId, String acao, String entidade, Long entidadeId) {
        auditorias.save(new Auditoria(usuarioId, acao, entidade, entidadeId));
    }
}