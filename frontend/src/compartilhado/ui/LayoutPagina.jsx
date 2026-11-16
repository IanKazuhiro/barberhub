import { useEffect, useState } from "react";
import { Outlet } from "react-router-dom";
import { requisitar } from "../api/cliente";

export default function LayoutPagina() {
  const [saude, setSaude] = useState({ situacao: "verificando" });

  useEffect(() => {
    requisitar("/api/saude", { semToken: true })
      .then((dados) => setSaude({ situacao: "ok", dados }))
      .catch(() => setSaude({ situacao: "erro" }));
  }, []);

  return (
    <main className="pagina">
      <Outlet />
      <p className="status-servidor">
        {saude.situacao === "verificando" && "Verificando o servidor..."}
        {saude.situacao === "ok" && `Servidor: ${saude.dados.aplicacao}. Banco: ${saude.dados.banco}.`}
        {saude.situacao === "erro" && "Não foi possível falar com o servidor."}
      </p>
    </main>
  );
}