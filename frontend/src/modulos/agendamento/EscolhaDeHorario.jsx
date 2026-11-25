import { useEffect, useState } from "react";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import { diaPorExtenso, hojeIso, somarDias } from "../../compartilhado/formato/data";

/** O mesmo limite do servidor: hoje e mais 30 dias. */
const DIAS_A_FRENTE = 30;
const SEMANA = ["dom", "seg", "ter", "qua", "qui", "sex", "sáb"];

function diasOferecidos() {
  const hoje = hojeIso();
  return Array.from({ length: DIAS_A_FRENTE + 1 }, (_, n) => {
    const iso = somarDias(hoje, n);
    return { iso, semana: SEMANA[new Date(`${iso}T00:00:00Z`).getUTCDay()], dia: `${iso.slice(8)}/${iso.slice(5, 7)}` };
  });
}

/**
 * Dia e horário livre. `chave` identifica o que está sendo consultado (serviço, barbeiro...):
 * quando ela ou o dia mudam, a lista é buscada de novo. `buscar(data)` devolve ["09:00", ...].
 */
export default function EscolhaDeHorario({ chave, buscar, data, hora, aoEscolher }) {
  const [dias] = useState(diasOferecidos);
  const [resultado, setResultado] = useState({ pedido: null, horarios: [], erro: "" });
  const pedido = data ? `${chave}|${data}` : null;

  useEffect(() => {
    if (!data) return undefined;
    let ativo = true;
    buscar(data)
      .then((horarios) => { if (ativo) setResultado({ pedido, horarios, erro: "" }); })
      .catch((e) => { if (ativo) setResultado({ pedido, horarios: [], erro: mensagemDe(e) }); });
    return () => { ativo = false; };
    // `buscar` muda a cada desenho da tela; quem diz o que mudou de verdade é o `pedido`.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pedido]);

  const carregando = pedido !== null && resultado.pedido !== pedido;

  return (
    <>
      <div className="dias" role="group" aria-label="Escolha o dia">
        {dias.map((d) => (
          <button key={d.iso} type="button" className={`dia${data === d.iso ? " ativo" : ""}`}
            aria-pressed={data === d.iso} aria-label={diaPorExtenso(d.iso)}
            onClick={() => aoEscolher(d.iso, null)}>
            <span>{d.semana}</span>
            <strong>{d.dia}</strong>
          </button>
        ))}
      </div>

      <div aria-live="polite">
        {!data && <p className="texto-suave">Escolha um dia para ver os horários livres.</p>}
        {carregando && <p className="texto-suave">Buscando horários...</p>}
        {data && !carregando && resultado.erro && <p className="alerta" role="alert">{resultado.erro}</p>}
        {data && !carregando && !resultado.erro && resultado.horarios.length === 0 && (
          <p className="texto-suave">Não há horário livre neste dia. Tente outro dia ou outro barbeiro.</p>
        )}
      </div>

      {data && !carregando && resultado.horarios.length > 0 && (
        <div className="horarios" role="group" aria-label={`Horários livres em ${diaPorExtenso(data)}`}>
          {resultado.horarios.map((h) => (
            <button key={h} type="button" className={`horario${hora === h ? " ativo" : ""}`}
              aria-pressed={hora === h} onClick={() => aoEscolher(data, h)}>
              {h}
            </button>
          ))}
        </div>
      )}
    </>
  );
}
