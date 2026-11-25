/** Datas e horas no fuso da barbearia (RNF10), seja qual for o fuso do aparelho. */
export const FUSO = "America/Manaus";

export const DIAS_DA_SEMANA = [
  "Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado",
];

const DIA_ISO = new Intl.DateTimeFormat("en-CA", { timeZone: FUSO });
const DIA_LONGO = new Intl.DateTimeFormat("pt-BR", {
  timeZone: "UTC", weekday: "long", day: "2-digit", month: "long",
});
const DIA_CURTO = new Intl.DateTimeFormat("pt-BR", {
  timeZone: "UTC", day: "2-digit", month: "2-digit", year: "numeric",
});

/** Hoje na barbearia, como "2026-11-25". */
export function hojeIso() {
  return DIA_ISO.format(new Date());
}

/** "2026-11-25" + 3 -> "2026-11-28". A conta é em UTC para não sofrer com fuso. */
export function somarDias(iso, dias) {
  const data = new Date(`${iso}T00:00:00Z`);
  data.setUTCDate(data.getUTCDate() + dias);
  return data.toISOString().slice(0, 10);
}

/** "2026-11-25" -> "quarta-feira, 25 de novembro". */
export function diaPorExtenso(iso) {
  return DIA_LONGO.format(new Date(`${iso}T00:00:00Z`));
}

/** "2026-11-25" -> "25/11/2026". */
export function diaCurto(iso) {
  return DIA_CURTO.format(new Date(`${iso}T00:00:00Z`));
}
