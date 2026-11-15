import { useEffect, useState } from "react";
import { requisitar } from "./compartilhado/api/cliente";

export default function App() {
  const [saude, setSaude] = useState({ situacao: "verificando" });

  useEffect(() => {
    requisitar("/api/saude")
      .then((dados) => setSaude({ situacao: "ok", dados }))
      .catch(() => setSaude({ situacao: "erro" }));
  }, []);

  return (
    <main>
      <h1>BarberHub</h1>
      <p>Sistema de gestão para barbearias.</p>
      {saude.situacao === "verificando" && <p>Verificando o servidor...</p>}
      {saude.situacao === "ok" && (
        <p>Servidor: {saude.dados.aplicacao}. Banco: {saude.dados.banco}.</p>
      )}
      {saude.situacao === "erro" && (
        <p>Não foi possível falar com o servidor.</p>
      )}
    </main>
  );
}