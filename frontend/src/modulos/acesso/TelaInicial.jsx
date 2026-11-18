import { Link } from "react-router-dom";
import { ROTULO_PERFIL, itensDoPerfil } from "../../compartilhado/navegacao/menu";
import { useSessao } from "./useSessao";

export default function TelaInicial() {
  const { usuario } = useSessao();
  const primeiroNome = usuario.nome.split(" ")[0];
  const atalhos = itensDoPerfil(usuario.perfil).filter((item) => item.caminho !== "/inicio");

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-inicio">
        <span className="perfil-etiqueta">{ROTULO_PERFIL[usuario.perfil]}</span>
        <h1 id="titulo-inicio" className="titulo">Olá, {primeiroNome}</h1>
        <p className="texto-suave">Bem-vindo ao BarberHub.</p>
      </section>

      <section className="cartao" aria-labelledby="titulo-conta">
        <h2 id="titulo-conta" className="subtitulo">Sua conta</h2>
        <dl className="dados">
          <div><dt>Nome</dt><dd>{usuario.nome}</dd></div>
          <div><dt>E-mail</dt><dd>{usuario.email}</dd></div>
          <div><dt>Telefone</dt><dd>{usuario.telefone || "Não informado"}</dd></div>
          <div><dt>Perfil</dt><dd>{ROTULO_PERFIL[usuario.perfil]}</dd></div>
        </dl>
      </section>

      <section aria-labelledby="titulo-atalhos">
        <h2 id="titulo-atalhos" className="subtitulo">Atalhos</h2>
        {atalhos.length === 0 ? (
          <p className="texto-suave">
            As funções do seu perfil aparecerão aqui conforme forem liberadas.
          </p>
        ) : (
          <div className="atalhos">
            {atalhos.map((item) => (
              <Link key={item.caminho} to={item.caminho} className="atalho">
                <strong>{item.rotulo}</strong>
                <span>{item.descricao}</span>
              </Link>
            ))}
          </div>
        )}
      </section>
    </>
  );
}