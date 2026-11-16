import { useContext } from "react";
import { SessaoContexto } from "./contexto";

export function useSessao() {
  const contexto = useContext(SessaoContexto);
  if (!contexto) {
    throw new Error("useSessao precisa estar dentro do SessaoProvedor.");
  }
  return contexto;
}