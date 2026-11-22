package br.com.barberhub.catalogo;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "combo_item")
@Getter
@NoArgsConstructor
public class ComboItem {

    @EmbeddedId
    private ComboItemId id;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    public ComboItem(Long comboId, Long servicoId) {
        this.id = new ComboItemId(comboId, servicoId);
    }

    @PrePersist
    void aoCriar() {
        criadoEm = Instant.now();
    }
}