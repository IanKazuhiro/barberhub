package br.com.barberhub.catalogo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComboItemRepositorio extends JpaRepository<ComboItem, ComboItemId> {

    List<ComboItem> findById_ComboId(Long comboId);

    @Modifying
    @Query("delete from ComboItem i where i.id.comboId = :comboId")
    void deleteByComboId(@Param("comboId") Long comboId);

    @Query("""
            select count(i) > 0
            from ComboItem i
            join Servico combo on combo.id = i.id.comboId
            where i.id.servicoId = :servicoId and combo.ativo = true
            """)
    boolean existeComboAtivoComServico(@Param("servicoId") Long servicoId);
}