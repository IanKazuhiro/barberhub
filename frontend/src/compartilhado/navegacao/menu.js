export const ROTULO_PERFIL = {
  CLIENTE: "Cliente",
  BARBEIRO: "Barbeiro",
  ADMINISTRADOR: "Administrador",
};

const TODOS = ["CLIENTE", "BARBEIRO", "ADMINISTRADOR"];

/**
 * Itens do menu lateral. Cada cartão novo acrescenta o seu aqui.
 * `icone` é o nome de um traço do componente Icone (sem ele, sai um círculo).
 * Exemplo (UC03):
 * { rotulo: "Acessos", caminho: "/acessos", icone: "acessos", perfis: ["ADMINISTRADOR"],
 *   descricao: "Crie acessos de barbeiros e inative contas." }
 */
export const ITENS_MENU = [
  { rotulo: "Início", caminho: "/inicio", icone: "inicio", perfis: TODOS },
  {
    rotulo: "Acessos",
    caminho: "/acessos",
    icone: "acessos",
    perfis: ["ADMINISTRADOR"],
    descricao: "Crie acessos de barbeiros e inative ou reative contas.",
  },
  {
    rotulo: "Serviços",
    caminho: "/servicos",
    icone: "tesoura",
    perfis: ["ADMINISTRADOR"],
    descricao: "Cadastre serviços com duração e preço e desative os que saírem de linha.",
  },
];

export function itensDoPerfil(perfil) {
  return ITENS_MENU.filter((item) => item.perfis.includes(perfil));
}
