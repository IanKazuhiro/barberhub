/** Formatos do catálogo, usados na tela e no formulário. */

export const MOEDA = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

/** 45 -> "45 min"; 60 -> "1 h"; 90 -> "1 h 30 min". */
export function duracaoLegivel(minutos) {
  if (minutos < 60) return `${minutos} min`;
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`;
}
