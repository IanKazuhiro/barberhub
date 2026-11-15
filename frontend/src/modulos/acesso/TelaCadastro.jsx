import { useState } from "react";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import Campo from "../../compartilhado/ui/Campo";
import { cadastrar } from "./api";

const VAZIO = { nome: "", email: "", telefone: "", senha: "" };

export default function TelaCadastro() {
  const [valores, setValores] = useState(VAZIO);
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [conta, setConta] = useState(null);

  function alterar(evento) {
    setValores({ ...valores, [evento.target.name]: evento.target.value });
  }

  async function enviar(evento) {
    evento.preventDefault();
    setErros({});
    setErroGeral("");
    setEnviando(true);
    try {
      const resposta = await cadastrar({
        ...valores,
        telefone: valores.telefone.replace(/\D/g, ""),
      });
      setConta(resposta);
      setValores(VAZIO);
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
    <section className="vidro" aria-labelledby="titulo-cadastro">
      <div className="marca">
        <span className="marca-icone" aria-hidden="true">B</span>
        BarberHub
      </div>

      <div>
        <h1 id="titulo-cadastro" className="titulo">Criar conta</h1>
        <p className="texto-suave">Cadastre-se para agendar seus atendimentos.</p>
      </div>

      {conta && (
        <p className="alerta alerta-sucesso" role="status">
          Conta criada, {conta.nome}. Em breve você poderá entrar com seu e-mail e senha.
        </p>
      )}
      {erroGeral && (
        <p className="alerta" role="alert">{erroGeral}</p>
      )}

      <form className="formulario" onSubmit={enviar} noValidate>
        <Campo id="nome" rotulo="Nome" autoComplete="name" placeholder="Seu nome completo"
          value={valores.nome} onChange={alterar} erro={erros.nome} />
        <Campo id="email" rotulo="E-mail" type="email" autoComplete="email" placeholder="voce@email.com"
          value={valores.email} onChange={alterar} erro={erros.email} />
        <Campo id="telefone" rotulo="Telefone" type="tel" inputMode="numeric" autoComplete="tel"
          placeholder="92 99999-8888" value={valores.telefone} onChange={alterar} erro={erros.telefone} />
        <Campo id="senha" rotulo="Senha" type="password" autoComplete="new-password"
          placeholder="Mínimo de 8 caracteres" value={valores.senha} onChange={alterar} erro={erros.senha} />
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? "Criando conta..." : "Criar conta"}
        </button>
      </form>

      <div className="divisor" />
      <p className="rodape-cartao">
        Já tem conta? <a href="#entrar">Entrar</a>
      </p>
    </section>
  );
}