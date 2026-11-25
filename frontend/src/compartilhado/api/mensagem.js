import { ErroDaApi } from "./cliente";

const SEM_SERVIDOR = "Não foi possível falar com o servidor. Tente novamente em instantes.";

export function mensagemDe(erro) {
  return erro instanceof ErroDaApi ? erro.message : SEM_SERVIDOR;
}

/** Separa o erro da API em { campos: {campo: mensagem}, geral } para os formulários. */
export function errosDoFormulario(erro) {
  if (erro instanceof ErroDaApi && erro.campos.length > 0) {
    return { campos: Object.fromEntries(erro.campos.map((c) => [c.campo, c.mensagem])), geral: "" };
  }
  return { campos: {}, geral: mensagemDe(erro) };
}
