import { useState } from "react";
import { errosDoFormulario } from "../../compartilhado/api/mensagem";
import Campo from "../../compartilhado/ui/Campo";
import { alterarBarbeiro } from "./api";

export default function FormularioBarbeiro({ barbeiro, aoSalvar, aoCancelar }) {
  const [valores, setValores] = useState({
    nome: barbeiro.nome,
    telefone: barbeiro.telefone ?? "",
    percentualComissao: String(barbeiro.percentualComissao),
  });
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState("");
  const [enviando, setEnviando] = useState(false);

  function alterar(evento) {
    setValores({ ...valores, [evento.target.name]: evento.target.value });
  }

  async function enviar(evento) {
    evento.preventDefault();
    setErros({});
    setErroGeral("");
    setEnviando(true);
    try {
      aoSalvar(await alterarBarbeiro(barbeiro.id, {
        nome: valores.nome,
        telefone: valores.telefone.replace(/\D/g, ""),
        percentualComissao: valores.percentualComissao === "" ? null : Number(valores.percentualComissao),
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
    <>
      <div className="cabecalho-texto">
        <h2 className="subtitulo">Editar {barbeiro.nome}</h2>
        <p className="texto-suave">O e-mail de acesso ({barbeiro.email}) não muda por aqui.</p>
      </div>

      {erroGeral && <p className="alerta" role="alert">{erroGeral}</p>}

      <form className="formulario" onSubmit={enviar} noValidate>
        <div className="grade-campos">
          <Campo id="nome" rotulo="Nome" classe="campo-largo" autoFocus maxLength={120}
            value={valores.nome} onChange={alterar} erro={erros.nome} />
          <Campo id="telefone" rotulo="Telefone" type="tel" inputMode="numeric"
            placeholder="92 99999-8888" value={valores.telefone} onChange={alterar} erro={erros.telefone} />
          <Campo id="percentualComissao" rotulo="Comissão (%)" type="number" inputMode="decimal"
            min="0" max="100" step="0.01" dica="Vale para os próximos atendimentos concluídos."
            value={valores.percentualComissao} onChange={alterar} erro={erros.percentualComissao} />
        </div>
        <div className="acoes-formulario">
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? "Salvando..." : "Salvar alterações"}
          </button>
          <button type="button" className="botao botao-claro" onClick={aoCancelar} disabled={enviando}>
            Cancelar
          </button>
        </div>
      </form>
    </>
  );
}
