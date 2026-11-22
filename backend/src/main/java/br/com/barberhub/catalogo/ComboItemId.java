package br.com.barberhub.catalogo;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ComboItemId implements Serializable {

    @Column(name = "combo_id")
    private Long comboId;

    @Column(name = "servico_id")
    private Long servicoId;
}