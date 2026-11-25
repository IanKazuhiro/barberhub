import { useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import { diaPorExtenso } from "../../compartilhado/formato/data";
import Icone from "../../compartilhado/ui/Icone";
import { listarBarbeirosAtivos } from "../barbeiro/api";
import { consultarCatalogo } from "../catalogo/api";
import { MOEDA, duracaoLegivel } from "../catalogo/formato";
import { agendar, consultarHorarios } from "./api";
import EscolhaDeHorario from "./EscolhaDeHorario";

export default function TelaAgendar() {
  const [parametros] = useSearchParams();
  const [servicos, setServicos] = useState(null);
  const [barbeiros, setBarbeiros] = useState([]);
  const [servicoId, setServicoId] = useState(() => Number(parametros.get("servico")) || null);
  const [barbeiroId, setBarbeiroId] = useState(null);
  const [data, setData] = useState(null);
  const [hora, setHora] = useState(null);
  // Sobe quando um horário é perdido para outra pessoa, para buscar a lista de novo.
  const [versao, setVersao] = useState(0);
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [confirmado, setConfirmado] = useState(null);
  const areaErro = useRef(null);

  useEffect(() => {
    let ativo = true;
    Promise.all([consultarCatalogo(), listarBarbeirosAtivos()])
      .then(([listaServicos, listaBarbeiros]) => {
        if (!ativo) return;
        setServicos(listaServicos);
        setBarbeiros(listaBarbeiros);
        if (listaBarbeiros.length === 1) setBarbeiroId(listaBarbeiros[0].id);
      })
      .catch((e) => { if (ativo) setErro(mensagemDe(e)); });
    return () => { ativo = false; };
  }, []);

  useEffect(() => {
    if (erro) areaErro.current?.scrollIntoView({ block: "center" });
  }, [erro]);

  const servico = servicos?.find((s) => s.id === servicoId) ?? null;
  const barbeiro = barbeiros.find((b) => b.id === barbeiroId) ?? null;

  function escolherServico(id) {
    setServicoId(id);
    setHora(null);
  }

  function escolherBarbeiro(id) {
    setBarbeiroId(id);
    setHora(null);
  }

  function escolherHorario(novaData, novaHora) {
    setData(novaData);
    setHora(novaHora);
    setErro("");
  }

  async function confirmar() {
    setErro("");
    setEnviando(true);
    try {
      setConfirmado(await agendar({ servicoId, barbeiroId, inicio: `${data}T${hora}` }));
    } catch (e) {
      setErro(mensagemDe(e));
      if (e instanceof ErroDaApi && (e.status === 409 || e.status === 422)) {
        setHora(null);
        setVersao((v) => v + 1);
      }
    } finally {
      setEnviando(false);
    }
  }

  function recomecar() {
    setConfirmado(null);
    setServicoId(null);
    setData(null);
    setHora(null);
    setVersao((v) => v + 1);
  }

  if (confirmado) {
    return (
      <section className="cartao" aria-labelledby="titulo-confirmado">
        <span className="situacao situacao-ativa">Agendado</span>
        <h1 id="titulo-confirmado" className="titulo">Horário reservado</h1>
        <dl className="dados" role="status">
          <div><dt>Serviço</dt><dd>{confirmado.servico}</dd></div>
          <div><dt>Barbeiro</dt><dd>{confirmado.barbeiro}</dd></div>
          <div>
            <dt>Dia</dt>
            <dd className="inicial-maiuscula">{diaPorExtenso(confirmado.inicio.slice(0, 10))}</dd>
          </div>
          <div><dt>Horário</dt><dd>{confirmado.inicio.slice(11)} às {confirmado.fim.slice(11)}</dd></div>
          <div><dt>Valor</dt><dd>{MOEDA.format(confirmado.precoCobrado)}</dd></div>
        </dl>
        <div className="acoes-formulario">
          <Link className="botao botao-link" to="/agendamentos">Ver meus agendamentos</Link>
          <button type="button" className="botao botao-claro" onClick={recomecar}>Agendar outro</button>
        </div>
      </section>
    );
  }

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-agendar">
        <div className="cabecalho-texto">
          <span className="etiqueta">Agendamento</span>
          <h1 id="titulo-agendar" className="titulo">Agendar horário</h1>
          <p className="texto-suave">Escolha o serviço, o barbeiro e o horário. Depois é só confirmar.</p>
        </div>
      </section>

      {erro && <p className="alerta" role="alert" ref={areaErro}>{erro}</p>}

      <section className="cartao" aria-labelledby="passo-servico">
        <h2 id="passo-servico" className="subtitulo">1. Serviço</h2>
        {servicos === null ? (
          <p className="texto-suave">Carregando...</p>
        ) : servicos.length === 0 ? (
          <p className="texto-suave">A barbearia ainda não publicou serviços.</p>
        ) : (
          <div className="escolhas" role="group" aria-labelledby="passo-servico">
            {servicos.map((s) => (
              <button key={s.id} type="button" className={`escolha${s.id === servicoId ? " ativa" : ""}`}
                aria-pressed={s.id === servicoId} onClick={() => escolherServico(s.id)}>
                <strong>{s.nome}</strong>
                {s.ehCombo && <span>{s.itens.join(" + ")}</span>}
                <span>{duracaoLegivel(s.duracaoMinutos)} · {MOEDA.format(s.preco)}</span>
              </button>
            ))}
          </div>
        )}
      </section>

      {servico && (
        <section className="cartao" aria-labelledby="passo-barbeiro">
          <h2 id="passo-barbeiro" className="subtitulo">2. Barbeiro</h2>
          {barbeiros.length === 0 ? (
            <p className="texto-suave">Nenhum barbeiro disponível no momento.</p>
          ) : (
            <div className="escolhas" role="group" aria-labelledby="passo-barbeiro">
              {barbeiros.map((b) => (
                <button key={b.id} type="button" className={`escolha${b.id === barbeiroId ? " ativa" : ""}`}
                  aria-pressed={b.id === barbeiroId} onClick={() => escolherBarbeiro(b.id)}>
                  <strong>{b.nome}</strong>
                </button>
              ))}
            </div>
          )}
        </section>
      )}

      {servico && barbeiro && (
        <section className="cartao" aria-labelledby="passo-horario">
          <h2 id="passo-horario" className="subtitulo">3. Dia e horário</h2>
          <EscolhaDeHorario
            chave={`${servicoId}|${barbeiroId}|${versao}`}
            buscar={(dia) => consultarHorarios({ servicoId, barbeiroId, data: dia })}
            data={data}
            hora={hora}
            aoEscolher={escolherHorario}
          />
        </section>
      )}

      {servico && barbeiro && data && hora && (
        <section className="cartao" aria-labelledby="passo-confirmar">
          <h2 id="passo-confirmar" className="subtitulo">4. Confirmação</h2>
          <p className="texto-suave">
            <strong>{servico.nome}</strong> com <strong>{barbeiro.nome}</strong>,{" "}
            {diaPorExtenso(data)}, às <strong>{hora}</strong>. Valor: {MOEDA.format(servico.preco)}.
          </p>
          <div className="acoes-formulario">
            <button type="button" className="botao" disabled={enviando} onClick={confirmar}>
              <Icone nome="calendario" tamanho={18} />
              {enviando ? "Reservando..." : "Confirmar agendamento"}
            </button>
          </div>
        </section>
      )}
    </>
  );
}
