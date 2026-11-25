import { useState } from "react";
import { errosDoFormulario } from "../../compartilhado/api/mensagem";
import Campo from "../../compartilhado/ui/Campo";
import { alterarPreferencias } from "./api";

export default function FormularioPreferencias({ cliente, barbeiros, aoSalvar }) {
  const [valores, setValores] = useState({
    barbeiroFavoritoId: cliente.barbeiroFavoritoId ? String(cliente.barbeiroFavoritoId) : "",
    tipoCorte: cliente.tipoCorte ?? "",
    observacoes: cliente.observacoes ?? "",
  });
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState("");
  const [enviando, setEnviando] = useState(false);

  // O favorito atual entra na lista mesmo que tenha sido inativado depois.
  const favoritoForaDaLista = cliente.barbeiroFavoritoId
    && !barbeiros.some((b) => b.id === cliente.barbeiroFavoritoId);

  function alterar(evento) {
    setValores({ ...valores, [evento.target.name]: evento.target.value });
  }

  async function enviar(evento) {
    evento.preventDefault();
    setErros({});
    setErroGeral("");
    setEnviando(true);
    try {
      aoSalvar(await alterarPreferencias(cliente.id, {
        barbeiroFavoritoId: valores.barbeiroFavoritoId === "" ? null : Number(valores.barbeiroFavoritoId),
        tipoCorte: valores.tipoCorte,
        observacoes: valores.observacoes,
      }));
    } catch (erro) {
      const { campos, geral } = errosDoFormulario(erro);
      setErros(campos);
      setErroGeral(geral);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="formulario" onSubmit={enviar} noValidate>
      <h3 className="subtitulo">Preferências e observações</h3>
      {erroGeral && <p className="alerta" role="alert">{erroGeral}</p>}
      <div className="grade-campos">
        <div className="campo">
          <label htmlFor="barbeiroFavoritoId">Barbeiro favorito</label>
          <select id="barbeiroFavoritoId" name="barbeiroFavoritoId" className="seletor seletor-alto"
            value={valores.barbeiroFavoritoId} onChange={alterar}>
            <option value="">Sem preferência</option>
            {favoritoForaDaLista && (
              <option value={cliente.barbeiroFavoritoId}>{cliente.barbeiroFavorito} (inativo)</option>
            )}
            {barbeiros.map((b) => <option key={b.id} value={b.id}>{b.nome}</option>)}
          </select>
        </div>
        <Campo id="tipoCorte" rotulo="Tipo de corte" maxLength={120} placeholder="Degradê baixo"
          value={valores.tipoCorte} onChange={alterar} erro={erros.tipoCorte} />
        <div className="campo campo-largo">
          <label htmlFor="observacoes">Observações</label>
          <textarea id="observacoes" name="observacoes" rows={4} maxLength={2000}
            placeholder="O que a equipe precisa lembrar no próximo atendimento"
            aria-describedby="observacoes-dica" value={valores.observacoes} onChange={alterar} />
          {erros.observacoes && <p className="campo-erro">{erros.observacoes}</p>}
          <p id="observacoes-dica" className="campo-dica">
            Visível para a equipe. O cliente não vê este texto.
          </p>
        </div>
      </div>
      <div className="acoes-formulario">
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? "Salvando..." : "Salvar preferências"}
        </button>
      </div>
    </form>
  );
}
