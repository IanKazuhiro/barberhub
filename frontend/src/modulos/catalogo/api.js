import { requisitar } from "../../compartilhado/api/cliente";

export function listarServicos() {
  return requisitar("/api/admin/servicos");
}

export function criarServico(dados) {
  return requisitar("/api/admin/servicos", {
    method: "POST",
    body: JSON.stringify(dados),
  });
}

export function alterarServico(id, dados) {
  return requisitar(`/api/admin/servicos/${id}`, {
    method: "PUT",
    body: JSON.stringify(dados),
  });
}

export function inativarServico(id) {
  return requisitar(`/api/admin/servicos/${id}/inativar`, { method: "PATCH" });
}

export function ativarServico(id) {
  return requisitar(`/api/admin/servicos/${id}/ativar`, { method: "PATCH" });
}