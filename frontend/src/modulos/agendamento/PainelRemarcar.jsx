import { useState } from "react";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import { diaPorExtenso } from "../../compartilhado/formato/data";
import { consultarHorariosParaRemarcar, remarcar } from "./api";
import EscolhaDeHorario from "./EscolhaDeHorario";

export default function PainelRemarcar({ agendamento, aoRemarcar, aoFechar }) {
  const [data, setData] = useState(null);
  const [hora, setHora] = useState(null);
  const [versao, setVersao] = useState(0);
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function confirmar() {
    setErro("");
    setEnviando(true);
    try {
      await remarcar(agendamento.id, `${data}T${hora}`);
      aoRemarcar();
    } catch (e) {
      setErro(mensagemDe(e));
      if (e instanceof ErroDaApi) {
        setHora(null);
        setVersao((v) => v + 1);
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <div className="cabecalho-texto">
          <h2 className="subtitulo">Remarcar {agendamento.servico}</h2>
          <p className="texto-suave">
            Com {agendamento.barbeiro}. Hoje marcado para {diaPorExtenso(agendamento.inicio.slice(0, 10))},
            às {agendamento.inicio.slice(11)}.
          </p>
        </div>
        <button type="button" className="botao-pequeno botao-claro" onClick={aoFechar}>Fechar</button>
      </div>

      {erro && <p className="alerta" role="alert">{erro}</p>}

      <EscolhaDeHorario
        chave={`${agendamento.id}|${versao}`}
        buscar={(dia) => consultarHorariosParaRemarcar(agendamento.id, dia)}
        data={data}
        hora={hora}
        aoEscolher={(novaData, novaHora) => { setData(novaData); setHora(novaHora); setErro(""); }}
      />

      {data && hora && (
        <div className="acoes-formulario">
          <button type="button" className="botao" disabled={enviando} onClick={confirmar}>
            {enviando ? "Remarcando..." : `Remarcar para ${data.slice(8)}/${data.slice(5, 7)} às ${hora}`}
          </button>
        </div>
      )}
    </>
  );
}
