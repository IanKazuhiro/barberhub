const CHAVE = "barberhub.sessao";

/** Lê a sessão guardada. Se estiver vencida ou corrompida, descarta e devolve null. */
export function lerSessao() {
  try {
    const bruto = sessionStorage.getItem(CHAVE);
    if (!bruto) return null;
    const sessao = JSON.parse(bruto);
    if (!sessao?.token || !sessao?.usuario || Date.now() >= sessao.expiraEm) {
      sessionStorage.removeItem(CHAVE);
      return null;
    }
    return sessao;
  } catch {
    return null;
  }
}

export function salvarSessao(sessao) {
  try {
    sessionStorage.setItem(CHAVE, JSON.stringify(sessao));
  } catch {
    // Sem armazenamento disponível: a sessão vale só até recarregar a página.
  }
}

export function limparSessao() {
  try {
    sessionStorage.removeItem(CHAVE);
  } catch {
    // Nada a limpar.
  }
}

export function lerToken() {
  return lerSessao()?.token ?? null;
}