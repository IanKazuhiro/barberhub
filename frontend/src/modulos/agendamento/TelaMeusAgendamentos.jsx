import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import { diaPorExtenso } from "../../compartilhado/formato/data";
import Dialogo from "../../compartilhado/ui/Dialogo";
import Icone from "../../compartilhado/ui/Icone";
import { MOEDA } from "../catalogo/formato";
import { cancelarAgendamento, listarMeusAgendamentos } from "./api";
import { ESTADO } from "./estado";
import PainelRemarcar from "./PainelRemarcar";

const ABAS = [
  { valor: "proximos", rotulo: "Próximos" },
  { valor: "anteriores", rotulo: "Anteriores" },
];

export default function TelaMeusAgendamentos() {
  const [agendamentos, setAgendamentos] = useState(null);
  const [aba, setAba] = useState("proximos");
  const [erro, setErro] = useState("");
  const [aviso, setAviso] = useState("");
  const [aCancelar, setACancelar] = useState(null);
  const [aRemarcar, setARemarcar] = useState(null);
  const [emAcao, setEmAcao] = useState(null);
  const areaPainel = useRef(null);

  useEffect(() => {
    let ativo = true;
    listarMeusAgendamentos()
      .then((dados) => { if (ativo) setAgendamentos(dados); })
      .catch((e) => { if (ativo) setErro(mensagemDe(e)); });
    return () => { ativo = false; };
  }, []);

  useEffect(() => {
    if (!aviso) return undefined;
    const relogio = setTimeout(() => setAviso(""), 6000);
    return () => clearTimeout(relogio);
  }, [aviso]);

  useEffect(() => {
    if (aRemarcar === null || !areaPainel.current) return;
    const semMovimento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    areaPainel.current.scrollIntoView({ behavior: semMovimento ? "auto" : "smooth", block: "start" });
  }, [aRemarcar]);

  // Mesmo com erro a lista é buscada de novo: o prazo pode ter vencido com a tela aberta.
  async function recarregar() {
    try {
      setAgendamentos(await listarMeusAgendamentos());
    } catch (e) {
      setErro(mensagemDe(e));
    }
  }

  async function confirmarCancelamento() {
    const alvo = aCancelar;
    setACancelar(null);
    setARemarcar(null);
    setErro("");
    setAviso("");
    setEmAcao(alvo.id);
    try {
      await cancelarAgendamento(alvo.id);
      setAviso("Agendamento cancelado. O horário foi liberado.");
    } catch (e) {
      setErro(mensagemDe(e));
    }
    await recarregar();
    setEmAcao(null);
  }

  async function aoRemarcar() {
    setARemarcar(null);
    setErro("");
    setAviso("Agendamento remarcado.");
    await recarregar();
  }

  const lista = agendamentos?.[aba] ?? [];

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-meus">
        <div className="cabecalho-pagina">
          <div className="cabecalho-texto">
            <span className="etiqueta">Agendamento</span>
            <h1 id="titulo-meus" className="titulo">Meus agendamentos</h1>
            <p className="texto-suave">
              Acompanhe o que você marcou.
              {agendamentos && ` Dá para cancelar ou remarcar até ${agendamentos.antecedenciaMinimaHoras} h antes.`}
            </p>
          </div>
          <Link className="botao-pequeno botao-link" to="/agendar">
            <Icone nome="mais" tamanho={16} />
            Agendar
          </Link>
        </div>
      </section>

      {aviso && <p className="alerta alerta-sucesso" role="status">{aviso}</p>}
      {erro && <p className="alerta" role="alert">{erro}</p>}

      {aRemarcar && (
        <section className="cartao ancora-painel" ref={areaPainel} aria-label="Remarcar">
          <PainelRemarcar key={aRemarcar.id} agendamento={aRemarcar}
            aoRemarcar={aoRemarcar} aoFechar={() => setARemarcar(null)} />
        </section>
      )}

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
                {a.podeAlterar && (
                  <div className="servico-acoes">
                    <button type="button" className="botao-pequeno botao-claro" disabled={emAcao === a.id}
                      onClick={() => setARemarcar(a)}>
                      <Icone nome="calendario" tamanho={15} />
                      Remarcar
                    </button>
                    <button type="button" className="botao-pequeno botao-claro" disabled={emAcao === a.id}
                      onClick={() => setACancelar(a)}>
                      <Icone nome="fechar" tamanho={15} />
                      {emAcao === a.id ? "Aguarde..." : "Cancelar"}
                    </button>
                  </div>
                )}
                {aba === "proximos" && !a.podeAlterar && (
                  <p className="texto-suave">O prazo para cancelar ou remarcar já passou.</p>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>

      {aCancelar && (
        <Dialogo
          titulo="Cancelar este agendamento?"
          descricao={`${aCancelar.servico} com ${aCancelar.barbeiro}, ${diaPorExtenso(aCancelar.inicio.slice(0, 10))}, às ${aCancelar.inicio.slice(11)}. O horário volta a ficar livre para outras pessoas.`}
          rotuloConfirmar="Cancelar agendamento"
          rotuloCancelar="Manter"
          perigo
          aoConfirmar={confirmarCancelamento}
          aoCancelar={() => setACancelar(null)}
        />
      )}
    </>
  );
}
