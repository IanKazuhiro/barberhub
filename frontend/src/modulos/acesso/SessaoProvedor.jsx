import { useCallback, useEffect, useMemo, useState } from "react";
import { definirAoSessaoExpirar } from "../../compartilhado/api/cliente";
import {
  lerSessao,
  lerToken,
  limparSessao,
  salvarSessao,
} from "../../compartilhado/sessao/armazenamento";
import { buscarUsuarioAtual } from "./api";
import { SessaoContexto } from "./contexto";

export default function SessaoProvedor({ children }) {
  const [sessao, setSessao] = useState(() => lerSessao());

  const entrar = useCallback((resposta) => {
    const nova = {
      token: resposta.token,
      usuario: resposta.usuario,
      expiraEm: Date.now() + resposta.expiraEmSegundos * 1000,
    };
    salvarSessao(nova);
    setSessao(nova);
  }, []);

  const sair = useCallback(() => {
    limparSessao();
    setSessao(null);
  }, []);

  // Se o servidor recusar o token em qualquer chamada, a sessão é encerrada.
  useEffect(() => {
    definirAoSessaoExpirar(sair);
    return () => definirAoSessaoExpirar(() => {});
  }, [sair]);

  // Ao abrir a página com uma sessão guardada, confere com o servidor quem é o usuário.
  useEffect(() => {
    if (!lerToken()) return;
    buscarUsuarioAtual()
      .then((usuario) => setSessao((atual) => (atual ? { ...atual, usuario } : atual)))
      .catch(() => {
        // 401 já encerra a sessão; falha de rede mantém a sessão como está.
      });
  }, []);

  const valor = useMemo(
    () => ({ usuario: sessao?.usuario ?? null, entrar, sair }),
    [sessao, entrar, sair],
  );

  return <SessaoContexto.Provider value={valor}>{children}</SessaoContexto.Provider>;
}