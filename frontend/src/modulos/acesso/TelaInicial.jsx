import { useNavigate } from "react-router-dom";
import { useSessao } from "./useSessao";

const CONTEUDO = {
  CLIENTE: {
    rotulo: "Cliente",
    texto: "Em breve você poderá agendar seus atendimentos por aqui.",
  },
  BARBEIRO: {
    rotulo: "Barbeiro",
    texto: "Em breve você verá sua agenda do dia por aqui.",
  },
  ADMINISTRADOR: {
    rotulo: "Administrador",
    texto: "Em breve você gerenciará serviços, equipe e acessos por aqui.",
  },
};

export default function TelaInicial() {
  const { usuario, sair } = useSessao();
  const navigate = useNavigate();
  const conteudo = CONTEUDO[usuario.perfil] ?? CONTEUDO.CLIENTE;
  const primeiroNome = usuario.nome.split(" ")[0];

  function aoSair() {
    sair();
    navigate("/entrar", { replace: true });
  }

  return (
    <section className="vidro" aria-labelledby="titulo-inicio">
      <div className="marca">
        <span className="marca-icone" aria-hidden="true">B</span>
        BarberHub
      </div>

      <span className="perfil-etiqueta">{conteudo.rotulo}</span>

      <div>
        <h1 id="titulo-inicio" className="titulo">Olá, {primeiroNome}</h1>
        <p className="texto-suave">{conteudo.texto}</p>
      </div>

      <div className="divisor" />
      <button type="button" className="botao" onClick={aoSair}>Sair</button>
    </section>
  );
}