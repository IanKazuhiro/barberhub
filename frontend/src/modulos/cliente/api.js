import { requisitar } from "../../compartilhado/api/cliente";

export function buscarClientes(busca) {
  return requisitar(`/api/clientes?busca=${encodeURIComponent(busca)}`);
}

export function detalharCliente(id) {
  return requisitar(`/api/clientes/${id}`);
}

export function alterarDadosDoCliente(id, dados) {
  return requisitar(`/api/admin/clientes/${id}`, { method: "PUT", body: JSON.stringify(dados) });
}

export function alterarPreferencias(id, dados) {
  return requisitar(`/api/clientes/${id}/preferencias`, { method: "PUT", body: JSON.stringify(dados) });
}

export function consultarMeusDados() {
  return requisitar("/api/cliente/dados");
}

export function alterarMeusDados(dados) {
  return requisitar("/api/cliente/dados", { method: "PUT", body: JSON.stringify(dados) });
}

/** Atendimentos concluídos, do mais recente para o mais antigo. */
export function consultarHistorico(id) {
  return requisitar(`/api/clientes/${id}/historico`);
}
