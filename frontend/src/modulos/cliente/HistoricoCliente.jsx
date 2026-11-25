import { useEffect, useState } from "react";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import { diaCurto } from "../../compartilhado/formato/data";
import { MOEDA } from "../catalogo/formato";
import { consultarHistorico } from "./api";

export default function HistoricoCliente({ id }) {
  const [atendimentos, setAtendimentos] = useState(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;
    consultarHistorico(id)
      .then((lista) => { if (ativo) setAtendimentos(lista); })
      .catch((e) => { if (ativo) setErro(mensagemDe(e)); });
    return () => { ativo = false; };
  }, [id]);

  return (
    <section className="historico" aria-labelledby="titulo-historico">
      <h3 id="titulo-historico" className="subtitulo">Histórico de atendimentos</h3>
      {erro && <p className="alerta" role="alert">{erro}</p>}
      {!erro && atendimentos === null && <p className="texto-suave">Carregando...</p>}
      {atendimentos?.length === 0 && (
        <p className="texto-suave">Nenhum atendimento concluído até agora.</p>
      )}
      {atendimentos?.length > 0 && (
        <ul className="lista-usuarios">
          {atendimentos.map((a) => (
            <li key={a.id} className="usuario-linha">
              <div className="usuario-dados">
                <strong>{a.servico}</strong>
                <span className="texto-suave">
                  {diaCurto(a.inicio.slice(0, 10))} às {a.inicio.slice(11)} · {a.barbeiro}
                </span>
              </div>
              <span className="etiqueta">{MOEDA.format(a.valor)}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
