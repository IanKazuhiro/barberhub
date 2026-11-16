import { lerToken } from "../sessao/armazenamento";

const URL_BASE = import.meta.env.VITE_API_URL;

export class ErroDaApi extends Error {
  constructor(resposta) {
    super(resposta.mensagem ?? "Erro ao falar com o servidor.");
    this.status = resposta.status;
    this.codigo = resposta.codigo;
    this.campos = resposta.campos ?? [];
  }
}

let aoSessaoExpirar = () => {};

/** Define o que fazer quando o servidor recusar o token (sessão vencida ou inválida). */
export function definirAoSessaoExpirar(funcao) {
  aoSessaoExpirar = funcao;
}

/**
 * Chamada à API. Envia o token da sessão, se houver.
 * Use `semToken: true` nas rotas públicas (login, cadastro, saúde): um token vencido
 * enviado a elas seria recusado pelo servidor mesmo sendo rotas abertas.
 */
export async function requisitar(caminho, { semToken = false, ...opcoes } = {}) {
  const token = semToken ? null : lerToken();
  const resposta = await fetch(`${URL_BASE}${caminho}`, {
    ...opcoes,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...opcoes.headers,
    },
  });

  if (resposta.status === 204) return null;

  const corpo = await resposta.json().catch(() => ({}));
  if (resposta.status === 401 && token) aoSessaoExpirar();
  if (!resposta.ok) throw new ErroDaApi({ status: resposta.status, ...corpo });
  return corpo;
}