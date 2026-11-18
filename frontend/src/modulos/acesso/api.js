import { requisitar } from "../../compartilhado/api/cliente";

export function cadastrar(dados) {
  return requisitar("/api/acesso/cadastro", {
    method: "POST",
    body: JSON.stringify(dados),
    semToken: true,
  });
}

export function entrarNoSistema(dados) {
  return requisitar("/api/acesso/login", {
    method: "POST",
    body: JSON.stringify(dados),
    semToken: true,
  });
}

export function buscarUsuarioAtual() {
  return requisitar("/api/acesso/eu");
}

export function listarUsuarios() {
  return requisitar("/api/admin/usuarios");
}

export function inativarUsuario(id) {
  return requisitar(`/api/admin/usuarios/${id}/inativar`, { method: "PATCH" });
}

export function ativarUsuario(id) {
  return requisitar(`/api/admin/usuarios/${id}/ativar`, { method: "PATCH" });
}