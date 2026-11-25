import { useEffect, useState } from "react";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import { diaCurto } from "../../compartilhado/formato/data";
import { listarAgendamentosFuturos } from "./api";

/** Aviso no cartão do barbeiro inativo: o que ficou marcado com ele e precisa ser resolvido. */
export default function AgendamentosDoInativo({ barbeiro }) {
  const [agendamentos, setAgendamentos] = useState([]);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;
    listarAgendamentosFuturos(barbeiro.id)
      .then((lista) => { if (ativo) setAgendamentos(lista); })
      .catch((e) => { if (ativo) setErro(mensagemDe(e)); });
    return () => { ativo = false; };
  }, [barbeiro.id]);

  if (erro) return <p className="alerta" role="alert">{erro}</p>;
  if (agendamentos.length === 0) return null;

  return (
    <details className="pendencias">
      <summary>
        {agendamentos.length === 1
          ? "1 agendamento futuro em aberto"
          : `${agendamentos.length} agendamentos futuros em aberto`}
      </summary>
      <p className="campo-dica">Fale com os clientes para remarcar ou cancelar.</p>
      <ul>
        {agendamentos.map((a) => (
          <li key={a.id}>
            <strong>{diaCurto(a.inicio.slice(0, 10))} às {a.inicio.slice(11)}</strong>
            <span>{a.cliente}{a.telefone ? ` · ${a.telefone}` : ""} · {a.servico}</span>
          </li>
        ))}
      </ul>
    </details>
  );
}
