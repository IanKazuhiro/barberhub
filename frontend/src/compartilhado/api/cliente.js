const URL_BASE = import.meta.env.VITE_API_URL;

export class ErroDaApi extends Error {
  constructor(resposta) {
    super(resposta.mensagem ?? "Erro ao falar com o servidor.");
    this.status = resposta.status;
    this.codigo = resposta.codigo;
    this.campos = resposta.campos ?? [];
  }
}

export async function requisitar(caminho, opcoes = {}) {
  const resposta = await fetch(`${URL_BASE}${caminho}`, {
    ...opcoes,
    headers: { "Content-Type": "application/json", ...opcoes.headers },
  });

  if (resposta.status === 204) return null;

  const corpo = await resposta.json().catch(() => ({}));
  if (!resposta.ok) throw new ErroDaApi({ status: resposta.status, ...corpo });
  return corpo;
}