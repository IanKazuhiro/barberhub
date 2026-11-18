import { Link } from "react-router-dom";

export default function TelaNaoEncontrada() {
  return (
    <section className="cartao" aria-labelledby="titulo-404">
      <h1 id="titulo-404" className="titulo">Página não encontrada</h1>
      <p className="texto-suave">O endereço que você abriu não existe.</p>
      <Link to="/inicio">Voltar ao início</Link>
    </section>
  );
}