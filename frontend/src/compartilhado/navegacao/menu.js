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
  {
    rotulo: "Agendar",
    caminho: "/agendar",
    icone: "calendario",
    perfis: ["CLIENTE"],
    descricao: "Escolha serviço, barbeiro e horário e reserve seu atendimento.",
  },
  {
    rotulo: "Meus agendamentos",
    caminho: "/agendamentos",
    icone: "lista",
    perfis: ["CLIENTE"],
    descricao: "Veja seus horários, remarque ou cancele.",
  },
  {
    rotulo: "Catálogo",
    caminho: "/catalogo",
    icone: "tesoura",
    perfis: ["CLIENTE"],
    descricao: "Veja os serviços e combos com duração e preço.",
  },
  {
    rotulo: "Equipe",
    caminho: "/equipe",
    icone: "equipe",
    perfis: ["ADMINISTRADOR"],
    descricao: "Defina a comissão, a escala e as folgas dos barbeiros.",
  },
  {
    rotulo: "Minha agenda",
    caminho: "/agenda",
    icone: "calendario",
    perfis: ["BARBEIRO"],
    descricao: "Veja quem você atende hoje e nos próximos dias.",
  },
  {
    rotulo: "Clientes",
    caminho: "/clientes",
    icone: "clientes",
    perfis: ["ADMINISTRADOR", "BARBEIRO"],
    descricao: "Busque clientes e registre preferências e observações.",
  },
  {
    rotulo: "Meus dados",
    caminho: "/meus-dados",
    icone: "equipe",
    perfis: ["CLIENTE"],
    descricao: "Atualize seu nome, telefone e a opção de receber mensagens.",
  },
];

export function itensDoPerfil(perfil) {
  return ITENS_MENU.filter((item) => item.perfis.includes(perfil));
}
