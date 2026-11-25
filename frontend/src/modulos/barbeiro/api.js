import { requisitar } from "../../compartilhado/api/cliente";

export function criarBarbeiro(dados) {
  return requisitar("/api/admin/barbeiros", {
    method: "POST",
    body: JSON.stringify(dados),
  });
}

export function listarEquipe() {
  return requisitar("/api/admin/barbeiros");
}

export function alterarBarbeiro(id, dados) {
  return requisitar(`/api/admin/barbeiros/${id}`, {
    method: "PUT",
    body: JSON.stringify(dados),
  });
}

export function consultarEscala(id) {
  return requisitar(`/api/admin/barbeiros/${id}/escala`);
}

export function salvarEscala(id, intervalos) {
  return requisitar(`/api/admin/barbeiros/${id}/escala`, {
    method: "PUT",
    body: JSON.stringify({ intervalos }),
  });
}

export function listarFolgas(id) {
  return requisitar(`/api/admin/barbeiros/${id}/folgas`);
}

export function criarFolga(id, dados) {
  return requisitar(`/api/admin/barbeiros/${id}/folgas`, {
    method: "POST",
    body: JSON.stringify(dados),
  });
}

export function removerFolga(id, folgaId) {
  return requisitar(`/api/admin/barbeiros/${id}/folgas/${folgaId}`, { method: "DELETE" });
}

/** Barbeiros ativos (só id e nome), para qualquer usuário logado. */
export function listarBarbeirosAtivos() {
  return requisitar("/api/barbeiros");
}
