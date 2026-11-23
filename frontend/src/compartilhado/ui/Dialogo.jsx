import { useEffect, useId, useRef } from "react";
import { createPortal } from "react-dom";

/**
 * Pergunta de confirmação no lugar do window.confirm: segue o estilo do sistema,
 * entra com animação e devolve o foco ao teclado (Esc fecha, Tab circula dentro).
 * Vai para o fim do body, com createPortal, para ficar acima de tudo.
 */
export default function Dialogo({
  titulo,
  descricao,
  rotuloConfirmar = "Confirmar",
  rotuloCancelar = "Cancelar",
  perigo = false,
  ocupado = false,
  aoConfirmar,
  aoCancelar,
}) {
  const idTitulo = useId();
  const caixa = useRef(null);
  const confirmar = useRef(null);

  // Foca o botão principal e trava a rolagem do fundo, só na abertura.
  useEffect(() => {
    confirmar.current?.focus();
    const rolagemAnterior = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = rolagemAnterior;
    };
  }, []);

  useEffect(() => {
    function aoTeclar(evento) {
      if (evento.key === "Escape") {
        aoCancelar();
        return;
      }
      if (evento.key !== "Tab") return;

      const focaveis = caixa.current?.querySelectorAll("button:not([disabled])");
      if (!focaveis || focaveis.length === 0) return;
      const primeiro = focaveis[0];
      const ultimo = focaveis[focaveis.length - 1];
      if (evento.shiftKey && document.activeElement === primeiro) {
        evento.preventDefault();
        ultimo.focus();
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault();
        primeiro.focus();
      }
    }

    document.addEventListener("keydown", aoTeclar);
    return () => document.removeEventListener("keydown", aoTeclar);
  }, [aoCancelar]);

  return createPortal(
    <div
      className="dialogo-fundo"
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget) aoCancelar();
      }}
    >
      <div className="dialogo" ref={caixa} role="dialog" aria-modal="true" aria-labelledby={idTitulo}>
        <h2 id={idTitulo} className="subtitulo">{titulo}</h2>
        {descricao && <p className="texto-suave">{descricao}</p>}
        <div className="dialogo-acoes">
          <button
            ref={confirmar}
            type="button"
            className={`botao${perigo ? " botao-perigo" : ""}`}
            onClick={aoConfirmar}
            disabled={ocupado}
          >
            {ocupado ? "Aguarde..." : rotuloConfirmar}
          </button>
          <button type="button" className="botao botao-claro" onClick={aoCancelar} disabled={ocupado}>
            {rotuloCancelar}
          </button>
        </div>
      </div>
    </div>,
    document.body,
  );
}
