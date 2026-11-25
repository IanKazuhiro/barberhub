/**
 * Ícones de traço, desenhados em SVG aqui mesmo (sem biblioteca externa).
 * Usam `currentColor`, então acompanham a cor do texto ao lado.
 * São decorativos: quem precisa do significado lê o rótulo, por isso aria-hidden.
 */
const TRACOS = {
  inicio: (
    <>
      <path d="M3 9.6 12 3l9 6.6V20a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1z" />
    </>
  ),
  acessos: (
    <>
      <path d="M16 20v-1.5a3.5 3.5 0 0 0-3.5-3.5h-5A3.5 3.5 0 0 0 4 18.5V20" />
      <circle cx="10" cy="7.5" r="3.5" />
      <path d="M20 20v-1.5a3.5 3.5 0 0 0-2.6-3.4" />
      <path d="M15.5 4.2a3.5 3.5 0 0 1 0 6.6" />
    </>
  ),
  tesoura: (
    <>
      <circle cx="6.5" cy="17.5" r="2.5" />
      <circle cx="6.5" cy="6.5" r="2.5" />
      <path d="M8.3 8.3 20 20" />
      <path d="M20 4 8.3 15.7" />
    </>
  ),
  relogio: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5.2l3.4 2" />
    </>
  ),
  mais: (
    <>
      <path d="M12 5v14" />
      <path d="M5 12h14" />
    </>
  ),
  lapis: (
    <>
      <path d="M4 20h4L19.5 8.5a2.1 2.1 0 0 0-3-3L5 17z" />
      <path d="M14.5 5.5l4 4" />
    </>
  ),
  energia: (
    <>
      <path d="M18.4 6.6a9 9 0 1 1-12.8 0" />
      <path d="M12 3v9" />
    </>
  ),
  seta: (
    <>
      <path d="M5 12h14" />
      <path d="M13 6l6 6-6 6" />
    </>
  ),
  sair: (
    <>
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
      <path d="M16 7l5 5-5 5" />
      <path d="M21 12H9" />
    </>
  ),
  menu: (
    <>
      <path d="M4 7h16" />
      <path d="M4 12h16" />
      <path d="M4 17h16" />
    </>
  ),
  fechar: (
    <>
      <path d="M6 6l12 12" />
      <path d="M18 6 6 18" />
    </>
  ),
  equipe: (
    <>
      <circle cx="12" cy="7" r="3.5" />
      <path d="M5 20v-1.5A4.5 4.5 0 0 1 9.5 14h5a4.5 4.5 0 0 1 4.5 4.5V20" />
    </>
  ),
  calendario: (
    <>
      <rect x="3.5" y="5" width="17" height="15.5" rx="2.5" />
      <path d="M3.5 10h17" />
      <path d="M8 3v4" />
      <path d="M16 3v4" />
    </>
  ),
  clientes: (
    <>
      <rect x="4" y="3.5" width="16" height="17" rx="2.5" />
      <circle cx="12" cy="10" r="2.6" />
      <path d="M7.8 17.2a4.4 4.4 0 0 1 8.4 0" />
    </>
  ),
  lista: (
    <>
      <path d="M9 6h11" />
      <path d="M9 12h11" />
      <path d="M9 18h11" />
      <path d="M4.5 6h.01" />
      <path d="M4.5 12h.01" />
      <path d="M4.5 18h.01" />
    </>
  ),
  padrao: (
    <>
      <circle cx="12" cy="12" r="9" />
    </>
  ),
};

export default function Icone({ nome, tamanho = 20 }) {
  return (
    <svg
      className="icone"
      width={tamanho}
      height={tamanho}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      {TRACOS[nome] ?? TRACOS.padrao}
    </svg>
  );
}
