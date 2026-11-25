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

export function cancelarAgendamento(id) {
  return requisitar(`/api/cliente/agendamentos/${id}/cancelar`, { method: "PATCH" });
}

export function consultarHorariosParaRemarcar(id, data) {
  return requisitar(`/api/cliente/agendamentos/${id}/horarios?data=${data}`);
}

export function remarcar(id, inicio) {
  return requisitar(`/api/cliente/agendamentos/${id}/remarcar`, {
    method: "PATCH",
    body: JSON.stringify({ inicio }),
  });
}

/** Agenda do barbeiro logado, em ordem de horário. */
export function consultarAgenda(data) {
  return requisitar(`/api/barbeiro/agenda?data=${data}`);
}
