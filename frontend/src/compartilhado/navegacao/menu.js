export const ROTULO_PERFIL = {
  CLIENTE: "Cliente",
  BARBEIRO: "Barbeiro",
  ADMINISTRADOR: "Administrador",
};

const TODOS = ["CLIENTE", "BARBEIRO", "ADMINISTRADOR"];

/**
 * Itens do menu lateral. Cada cartão novo acrescenta o seu aqui.
 * Exemplo (UC03):
 * { rotulo: "Acessos", caminho: "/acessos", perfis: ["ADMINISTRADOR"],
 *   descricao: "Crie acessos de barbeiros e inative contas." }
 */
export const ITENS_MENU = [
  { rotulo: "Início", caminho: "/inicio", perfis: TODOS },
  {
    rotulo: "Acessos",
    caminho: "/acessos",
    perfis: ["ADMINISTRADOR"],
    descricao: "Crie acessos de barbeiros e inative ou reative contas.",
  },
];

export function itensDoPerfil(perfil) {
  return ITENS_MENU.filter((item) => item.perfis.includes(perfil));
}