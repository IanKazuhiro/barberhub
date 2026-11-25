import { requisitar } from "../../compartilhado/api/cliente";

export function consultarHorarios({ barbeiroId, servicoId, data }) {
  const filtros = new URLSearchParams({ barbeiroId, servicoId, data });
  return requisitar(`/api/cliente/disponibilidade?${filtros}`);
}

export function agendar(dados) {
  return requisitar("/api/cliente/agendamentos", {
    method: "POST",
    body: JSON.stringify(dados),
  });
}

/** { proximos: [...], anteriores: [...] } */
export function listarMeusAgendamentos() {
  return requisitar("/api/cliente/agendamentos");
}
