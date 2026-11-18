import { useState } from "react";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import Campo from "../../compartilhado/ui/Campo";
import { criarBarbeiro } from "./api";

const VAZIO = { nome: "", email: "", telefone: "", senha: "", percentualComissao: "" };

export default function FormularioNovoBarbeiro({ aoCriar, aoCancelar }) {
  const [valores, setValores] = useState(VAZIO);
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
      const dados = {
        nome: valores.nome,
        email: valores.email.trim(),
        telefone: valores.telefone.replace(/\D/g, ""),
        senha: valores.senha,
      };
      if (valores.percentualComissao !== "") {
        dados.percentualComissao = Number(valores.percentualComissao);
      }
      const criado = await criarBarbeiro(dados);
      setValores(VAZIO);
      aoCriar(criado);
    } catch (erro) {
      if (erro instanceof ErroDaApi && erro.campos.length > 0) {
        setErros(Object.fromEntries(erro.campos.map((c) => [c.campo, c.mensagem])));
      } else if (erro instanceof ErroDaApi) {
        setErroGeral(erro.message);
      } else {
        setErroGeral("Não foi possível falar com o servidor. Tente novamente em instantes.");
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <h2 className="subtitulo">Novo barbeiro</h2>
      <p className="texto-suave">
        Informe a senha inicial e passe-a ao barbeiro. Ele entra com o e-mail e essa senha.
      </p>

      {erroGeral && <p className="alerta" role="alert">{erroGeral}</p>}

      <form className="formulario" onSubmit={enviar} noValidate>
        <Campo id="nome" rotulo="Nome" placeholder="Nome completo"
          value={valores.nome} onChange={alterar} erro={erros.nome} />
        <Campo id="email" rotulo="E-mail" type="email" placeholder="barbeiro@email.com"
          value={valores.email} onChange={alterar} erro={erros.email} />
        <Campo id="telefone" rotulo="Telefone" type="tel" inputMode="numeric"
          placeholder="92 99999-8888" value={valores.telefone} onChange={alterar} erro={erros.telefone} />
        <Campo id="senha" rotulo="Senha inicial" type="password" autoComplete="new-password"
          placeholder="Mínimo de 8 caracteres" value={valores.senha} onChange={alterar} erro={erros.senha} />
        <Campo id="percentualComissao" rotulo="Comissão (%) — opcional" type="number"
          min="0" max="100" step="0.01" placeholder="0"
          value={valores.percentualComissao} onChange={alterar} erro={erros.percentualComissao} />
        <div className="acoes-formulario">
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? "Criando..." : "Criar barbeiro"}
          </button>
          <button type="button" className="botao botao-claro" onClick={aoCancelar} disabled={enviando}>
            Cancelar
          </button>
        </div>
      </form>
    </>
  );
}