import { useState } from "react";
import { Link, Navigate, useLocation } from "react-router-dom";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import Campo from "../../compartilhado/ui/Campo";
import { entrarNoSistema } from "./api";
import { useSessao } from "./useSessao";

const VAZIO = { email: "", senha: "" };

export default function TelaLogin() {
  const { usuario, entrar } = useSessao();
  const local = useLocation();
  const [valores, setValores] = useState(VAZIO);
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState("");
  const [enviando, setEnviando] = useState(false);

  if (usuario) {
    return <Navigate to={local.state?.de ?? "/inicio"} replace />;
  }

  function alterar(evento) {
    setValores({ ...valores, [evento.target.name]: evento.target.value });
  }

  async function enviar(evento) {
    evento.preventDefault();
    setErros({});
    setErroGeral("");
    setEnviando(true);
    try {
      const resposta = await entrarNoSistema({
        email: valores.email.trim(),
        senha: valores.senha,
      });
      entrar(resposta);
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
    <section className="vidro" aria-labelledby="titulo-login">
      <div className="marca">
        <span className="marca-icone" aria-hidden="true">B</span>
        BarberHub
      </div>

      <div>
        <h1 id="titulo-login" className="titulo">Entrar</h1>
        <p className="texto-suave">Acesse sua conta para continuar.</p>
      </div>

      {erroGeral && (
        <p className="alerta" role="alert">{erroGeral}</p>
      )}

      <form className="formulario" onSubmit={enviar} noValidate>
        <Campo id="email" rotulo="E-mail" type="email" autoComplete="email" placeholder="voce@email.com"
          value={valores.email} onChange={alterar} erro={erros.email} />
        <Campo id="senha" rotulo="Senha" type="password" autoComplete="current-password"
          placeholder="Sua senha" value={valores.senha} onChange={alterar} erro={erros.senha} />
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? "Entrando..." : "Entrar"}
        </button>
      </form>

      <div className="divisor" />
      <p className="rodape-cartao">
        Ainda não tem conta? <Link to="/cadastro">Criar conta</Link>
      </p>
    </section>
  );
}