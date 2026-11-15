export default function Campo({ id, rotulo, erro, ...entrada }) {
  const idErro = `${id}-erro`;
  return (
    <div className="campo">
      <label htmlFor={id}>{rotulo}</label>
      <input
        id={id}
        name={id}
        aria-invalid={erro ? "true" : "false"}
        aria-describedby={erro ? idErro : undefined}
        {...entrada}
      />
      {erro && (
        <p id={idErro} className="campo-erro">
          {erro}
        </p>
      )}
    </div>
  );
}