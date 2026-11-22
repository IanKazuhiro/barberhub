package br.com.barberhub.catalogo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepositorio extends JpaRepository<Servico, Long> {

    /** Nome repetido ao criar (o banco só barra o nome idêntico, sem ignorar maiúsculas). */
    boolean existsByNomeIgnoreCase(String nome);

    /** Nome repetido ao alterar: ignora o próprio serviço, para poder salvar sem mudar o nome. */
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<Servico> findAllByOrderByNomeAsc();

    List<Servico> findByAtivoOrderByNomeAsc(boolean ativo);
}