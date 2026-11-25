import { useEffect, useState } from "react";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import { diaPorExtenso, hojeIso, somarDias } from "../../compartilhado/formato/data";
import Icone from "../../compartilhado/ui/Icone";
import { consultarAgenda } from "./api";
import { ESTADO } from "./estado";

export default function TelaAgenda() {
  const [data, setData] = useState(hojeIso);
  const [resultado, setResultado] = useState({ data: null, itens: [], erro: "" });

  useEffect(() => {
    let ativo = true;
    consultarAgenda(data)
      .then((itens) => { if (ativo) setResultado({ data, itens, erro: "" }); })
      .catch((e) => { if (ativo) setResultado({ data, itens: [], erro: mensagemDe(e) }); });
    return () => { ativo = false; };
  }, [data]);

  const carregando = resultado.data !== data;
  const hoje = data === hojeIso();

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-agenda">
        <div className="cabecalho-texto">
          <span className="etiqueta">{hoje ? "Hoje" : "Agenda"}</span>
          <h1 id="titulo-agenda" className="titulo">Minha agenda</h1>
          <p className="texto-suave inicial-maiuscula" aria-live="polite">{diaPorExtenso(data)}</p>
        </div>
        <div className="usuario-acoes">
          <button type="button" className="botao-pequeno botao-claro" onClick={() => setData(somarDias(data, -1))}>
            Dia anterior
          </button>
          {!hoje && (
            <button type="button" className="botao-pequeno" onClick={() => setData(hojeIso())}>Hoje</button>
          )}
          <button type="button" className="botao-pequeno botao-claro" onClick={() => setData(somarDias(data, 1))}>
            Próximo dia
          </button>
          <input type="date" className="seletor seletor-pequeno" aria-label="Escolher o dia" value={data}
            onChange={(e) => { if (e.target.value) setData(e.target.value); }} />
        </div>
      </section>

      {resultado.erro && !carregando && <p className="alerta" role="alert">{resultado.erro}</p>}

      <section aria-label="Agendamentos do dia">
        {carregando ? (
          <div className="esqueleto-grade" aria-hidden="true">
            <div className="esqueleto esqueleto-linha" />
            <div className="esqueleto esqueleto-linha" />
          </div>
        ) : resultado.itens.length === 0 ? (
          !resultado.erro && (
            <div className="vazio">
              <span className="vazio-icone"><Icone nome="calendario" tamanho={24} /></span>
              <strong>Nenhum agendamento neste dia</strong>
              <p className="texto-suave">Use os botões acima para ver outro dia.</p>
            </div>
          )
        ) : (
          <ol className="lista-usuarios">
            {resultado.itens.map((item) => (
              <li key={item.id}
                className={`usuario-linha agenda-linha${item.estado === "AGENDADO" ? "" : " agenda-encerrada"}`}>
                <div className="agenda-hora">
                  <strong>{item.inicio}</strong>
                  <span className="texto-suave">até {item.fim}</span>
                </div>
                <div className="usuario-dados agenda-dados">
                  <strong>{item.cliente}</strong>
                  <span className="texto-suave">
                    {item.servico}{item.telefone ? ` · ${item.telefone}` : ""}
                  </span>
                  {item.tipoCorte && <span className="texto-suave">Corte habitual: {item.tipoCorte}</span>}
                  {item.observacoes && <span className="agenda-nota">{item.observacoes}</span>}
                </div>
                <span className={`situacao ${ESTADO[item.estado].classe}`}>{ESTADO[item.estado].rotulo}</span>
              </li>
            ))}
          </ol>
        )}
      </section>
    </>
  );
}
