import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import { diaPorExtenso } from "../../compartilhado/formato/data";
import Icone from "../../compartilhado/ui/Icone";
import { MOEDA } from "../catalogo/formato";
import { listarMeusAgendamentos } from "./api";
import { ESTADO } from "./estado";

const ABAS = [
  { valor: "proximos", rotulo: "Próximos" },
  { valor: "anteriores", rotulo: "Anteriores" },
];

export default function TelaMeusAgendamentos() {
  const [agendamentos, setAgendamentos] = useState(null);
  const [aba, setAba] = useState("proximos");
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;
    listarMeusAgendamentos()
      .then((dados) => { if (ativo) setAgendamentos(dados); })
      .catch((e) => { if (ativo) setErro(mensagemDe(e)); });
    return () => { ativo = false; };
  }, []);

  const lista = agendamentos?.[aba] ?? [];

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-meus">
        <div className="cabecalho-pagina">
          <div className="cabecalho-texto">
            <span className="etiqueta">Agendamento</span>
            <h1 id="titulo-meus" className="titulo">Meus agendamentos</h1>
            <p className="texto-suave">Acompanhe o que você marcou.</p>
          </div>
          <Link className="botao-pequeno botao-link" to="/agendar">
            <Icone nome="mais" tamanho={16} />
            Agendar
          </Link>
        </div>
      </section>

      {erro && <p className="alerta" role="alert">{erro}</p>}

      <section aria-labelledby="titulo-lista-meus">
        <div className="barra-filtro">
          <h2 id="titulo-lista-meus" className="subtitulo">Lista</h2>
          <div className="abas" role="group" aria-label="Próximos ou anteriores">
            {ABAS.map((a) => (
              <button key={a.valor} type="button" className={`aba${aba === a.valor ? " ativa" : ""}`}
                aria-pressed={aba === a.valor} onClick={() => setAba(a.valor)}>
                {a.rotulo}
                {agendamentos && <span className="aba-contagem">{agendamentos[a.valor].length}</span>}
              </button>
            ))}
          </div>
        </div>

        {agendamentos === null ? (
          !erro && (
            <div className="esqueleto-grade" aria-hidden="true">
              <div className="esqueleto" />
              <div className="esqueleto" />
            </div>
          )
        ) : lista.length === 0 ? (
          <div className="vazio">
            <span className="vazio-icone"><Icone nome="calendario" tamanho={24} /></span>
            <strong>{aba === "proximos" ? "Nenhum horário marcado" : "Nada por aqui ainda"}</strong>
            <p className="texto-suave">
              {aba === "proximos"
                ? "Reserve um horário para ele aparecer aqui."
                : "Seus atendimentos passados e cancelamentos aparecem aqui."}
            </p>
          </div>
        ) : (
          <ul className="grade-servicos">
            {lista.map((a) => (
              <li key={a.id} className="cartao-servico">
                <div className="servico-topo">
                  <h3 className="servico-nome">{a.servico}</h3>
                  <span className={`situacao ${ESTADO[a.estado].classe}`}>{ESTADO[a.estado].rotulo}</span>
                </div>
                <strong className="inicial-maiuscula">{diaPorExtenso(a.inicio.slice(0, 10))}</strong>
                <div className="servico-meta">
                  <span className="etiqueta">
                    <Icone nome="relogio" tamanho={14} />
                    {a.inicio.slice(11)} às {a.fim.slice(11)}
                  </span>
                  <span className="etiqueta">{a.barbeiro}</span>
                  <span className="etiqueta">{MOEDA.format(a.precoCobrado)}</span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
