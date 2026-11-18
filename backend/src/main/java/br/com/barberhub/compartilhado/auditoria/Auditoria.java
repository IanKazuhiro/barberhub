package br.com.barberhub.compartilhado.auditoria;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auditoria")
@Getter
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 40)
    private String acao;

    @Column(nullable = false, length = 40)
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    public Auditoria(Long usuarioId, String acao, String entidade, Long entidadeId) {
        this.usuarioId = usuarioId;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.dataHora = Instant.now();
    }
}