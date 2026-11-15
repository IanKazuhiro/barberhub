import { requisitar } from "../../compartilhado/api/cliente";

export function cadastrar(dados) {
  return requisitar("/api/acesso/cadastro", {
    method: "POST",
    body: JSON.stringify(dados),
  });
}