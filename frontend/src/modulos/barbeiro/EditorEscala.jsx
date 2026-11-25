import { useEffect, useState } from "react";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import { DIAS_DA_SEMANA } from "../../compartilhado/formato/data";
import Icone from "../../compartilhado/ui/Icone";
import { consultarEscala, salvarEscala } from "./api";

// Segunda primeiro, domingo por último. Os números são os do banco (0 = domingo).
const ORDEM = [1, 2, 3, 4, 5, 6, 0];
const SEGUNDA_A_SABADO = [1, 2, 3, 4, 5, 6];

function agrupar(intervalos) {
  const dias = DIAS_DA_SEMANA.map(() => []);
  intervalos.forEach(({ diaSemana, inicio, fim }) => dias[diaSemana].push({ inicio, fim }));
  return dias;
}

export default function EditorEscala({ barbeiro, aoSalvar, aoCancelar }) {
  const [dias, setDias] = useState(null);
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    let ativo = true;
    consultarEscala(barbeiro.id)
      .then((lista) => { if (ativo) setDias(agrupar(lista)); })
      .catch((e) => { if (ativo) setErro(mensagemDe(e)); });
    return () => { ativo = false; };
  }, [barbeiro.id]);

  function trocarDia(dia, intervalos) {
    setDias(dias.map((atual, indice) => (indice === dia ? intervalos : atual)));
  }

  function acrescentar(dia) {
    const novo = dias[dia].length === 0
      ? { inicio: "09:00", fim: "12:00" }
      : { inicio: "13:00", fim: "19:00" };
    trocarDia(dia, [...dias[dia], novo]);
  }

  function mudarHora(dia, posicao, campo, valor) {
    trocarDia(dia, dias[dia].map((i, n) => (n === posicao ? { ...i, [campo]: valor } : i)));
  }

  function copiarParaSegundaASabado(dia) {
    setDias(dias.map((atual, indice) => (
      SEGUNDA_A_SABADO.includes(indice) ? dias[dia].map((i) => ({ ...i })) : atual
    )));
  }

  async function enviar(evento) {
    evento.preventDefault();
    const intervalos = dias.flatMap((lista, diaSemana) => lista.map((i) => ({ diaSemana, ...i })));
    if (intervalos.some((i) => !i.inicio || !i.fim)) {
      setErro("Preencha o início e o fim de todos os intervalos.");
      return;
    }
    setErro("");
    setEnviando(true);
    try {
      await salvarEscala(barbeiro.id, intervalos);
      aoSalvar();
    } catch (e) {
      setErro(mensagemDe(e));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <div className="cabecalho-texto">
        <h2 className="subtitulo">Escala de {barbeiro.nome}</h2>
        <p className="texto-suave">
          Horários de trabalho na semana. Para a pausa do almoço, use dois intervalos no mesmo dia.
          Dia sem intervalo é dia sem atendimento.
        </p>
      </div>

      {erro && <p className="alerta" role="alert">{erro}</p>}

      {dias && (
        <form className="formulario" onSubmit={enviar} noValidate>
          <ul className="escala">
            {ORDEM.map((dia) => (
              <li key={dia} className="escala-dia">
                <strong className="escala-nome">{DIAS_DA_SEMANA[dia]}</strong>
                <div className="escala-intervalos">
                  {dias[dia].length === 0 && <span className="texto-suave">Não atende</span>}
                  {dias[dia].map((intervalo, posicao) => (
                    <div key={posicao} className="escala-intervalo">
                      <input type="time" className="seletor" step="900" value={intervalo.inicio}
                        aria-label={`${DIAS_DA_SEMANA[dia]}, início do intervalo ${posicao + 1}`}
                        onChange={(e) => mudarHora(dia, posicao, "inicio", e.target.value)} />
                      <span aria-hidden="true">às</span>
                      <input type="time" className="seletor" step="900" value={intervalo.fim}
                        aria-label={`${DIAS_DA_SEMANA[dia]}, fim do intervalo ${posicao + 1}`}
                        onChange={(e) => mudarHora(dia, posicao, "fim", e.target.value)} />
                      <button type="button" className="botao-icone"
                        aria-label={`Remover o intervalo ${posicao + 1} de ${DIAS_DA_SEMANA[dia]}`}
                        onClick={() => trocarDia(dia, dias[dia].filter((_, n) => n !== posicao))}>
                        <Icone nome="fechar" tamanho={16} />
                      </button>
                    </div>
                  ))}
                </div>
                <div className="escala-acoes">
                  {dias[dia].length < 4 && (
                    <button type="button" className="botao-pequeno botao-claro"
                      aria-label={`Acrescentar intervalo em ${DIAS_DA_SEMANA[dia]}`}
                      onClick={() => acrescentar(dia)}>
                      <Icone nome="mais" tamanho={15} />
                      Intervalo
                    </button>
                  )}
                  {dias[dia].length > 0 && dia !== 0 && (
                    <button type="button" className="botao-pequeno botao-claro"
                      aria-label={`Copiar ${DIAS_DA_SEMANA[dia]} para segunda a sábado`}
                      onClick={() => copiarParaSegundaASabado(dia)}>
                      Copiar p/ seg a sáb
                    </button>
                  )}
                </div>
              </li>
            ))}
          </ul>

          <div className="acoes-formulario">
            <button type="submit" className="botao" disabled={enviando}>
              {enviando ? "Salvando..." : "Salvar escala"}
            </button>
            <button type="button" className="botao botao-claro" onClick={aoCancelar} disabled={enviando}>
              Cancelar
            </button>
          </div>
        </form>
      )}
    </>
  );
}
