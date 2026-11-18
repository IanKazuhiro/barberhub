import { requisitar } from "../../compartilhado/api/cliente";

export function criarBarbeiro(dados) {
  return requisitar("/api/admin/barbeiros", {
    method: "POST",
    body: JSON.stringify(dados),
  });
}