import { useEffect, useState } from "react";
import { requisitar } from "./compartilhado/api/cliente";
import TelaCadastro from "./modulos/acesso/TelaCadastro";

export default function App() {
  const [saude, setSaude] = useState({ situacao: "verificando" });

  useEffect(() => {
    requisitar("/api/saude")
      .then((dados) => setSaude({ situacao: "ok", dados }))
      .catch(() => setSaude({ situacao: "erro" }));
  }, []);

  return (
    <main className="pagina">
      <TelaCadastro />
      <p className="status-servidor">
        {saude.situacao === "verificando" && "Verificando o servidor..."}
        {saude.situacao === "ok" && `Servidor: ${saude.dados.aplicacao}. Banco: ${saude.dados.banco}.`}
        {saude.situacao === "erro" && "Não foi possível falar com o servidor."}
      </p>
    </main>
  );
}