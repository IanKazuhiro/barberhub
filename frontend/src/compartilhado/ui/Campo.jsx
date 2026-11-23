/**
 * Campo de formulário com rótulo, dica opcional e mensagem de erro.
 * `classe` serve para posicionar o campo numa grade (ex.: "campo-largo").
 */
export default function Campo({ id, rotulo, erro, dica, classe = "", ...entrada }) {
  const idErro = `${id}-erro`;
  const idDica = `${id}-dica`;
  const descricao = [erro ? idErro : null, dica ? idDica : null].filter(Boolean).join(" ");

  return (
    <div className={`campo${classe ? ` ${classe}` : ""}`}>
      <label htmlFor={id}>{rotulo}</label>
      <input
        id={id}
        name={id}
        aria-invalid={erro ? "true" : "false"}
        aria-describedby={descricao || undefined}
        {...entrada}
      />
      {erro && (
        <p id={idErro} className="campo-erro">
          {erro}
        </p>
      )}
      {dica && (
        <p id={idDica} className="campo-dica">
          {dica}
        </p>
      )}
    </div>
  );
}
