package br.com.barberhub.acesso;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    /** Mesmo id do usuário (tabela 1:1). */
    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "aceita_mensagens", nullable = false)
    private boolean aceitaMensagens = true;

    /** Quando a opção de receber mensagens foi dada ou alterada. */
    @Column(name = "consentimento_em")
    private Instant consentimentoEm;

    @Column(name = "barbeiro_favorito_id")
    private Long barbeiroFavoritoId;

    @Column(name = "tipo_corte", length = 120)
    private String tipoCorte;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    public Cliente(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = Instant.now();
    }
}