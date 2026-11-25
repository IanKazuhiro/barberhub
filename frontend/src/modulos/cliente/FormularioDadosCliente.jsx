import { useState } from "react";
import { errosDoFormulario } from "../../compartilhado/api/mensagem";
import Campo from "../../compartilhado/ui/Campo";
import { alterarDadosDoCliente } from "./api";

export default function FormularioDadosCliente({ cliente, aoSalvar, aoCancelar }) {
  const [valores, setValores] = useState({ nome: cliente.nome, telefone: cliente.telefone ?? "" });
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
      aoSalvar(await alterarDadosDoCliente(cliente.id, {
        nome: valores.nome,
        telefone: valores.telefone.replace(/\D/g, ""),
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
      {erroGeral && <p className="alerta" role="alert">{erroGeral}</p>}
      <div className="grade-campos">
        <Campo id="nome" rotulo="Nome" autoFocus maxLength={120}
          value={valores.nome} onChange={alterar} erro={erros.nome} />
        <Campo id="telefone" rotulo="Telefone" type="tel" inputMode="numeric"
          value={valores.telefone} onChange={alterar} erro={erros.telefone} />
      </div>
      <div className="acoes-formulario">
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? "Salvando..." : "Salvar dados"}
        </button>
        <button type="button" className="botao botao-claro" onClick={aoCancelar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  );
}
