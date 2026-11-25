package br.com.barberhub.agendamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Regra configurável em chave e valor. Só leitura por enquanto: a tela para editar é o UC16. */
@Entity
@Table(name = "configuracao")
@Getter
@NoArgsConstructor
public class Configuracao {

    @Id
    @Column(length = 60)
    private String chave;

    @Column(nullable = false, length = 200)
    private String valor;

    Configuracao(String chave, String valor) {
        this.chave = chave;
        this.valor = valor;
    }
}
