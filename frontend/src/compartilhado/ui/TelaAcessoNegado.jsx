import { Link } from "react-router-dom";

export default function TelaAcessoNegado() {
  return (
    <section className="cartao" aria-labelledby="titulo-negado">
      <h1 id="titulo-negado" className="titulo">Acesso negado</h1>
      <p className="texto-suave">Seu perfil não tem permissão para abrir esta tela.</p>
      <Link to="/inicio">Voltar ao início</Link>
    </section>
  );
}