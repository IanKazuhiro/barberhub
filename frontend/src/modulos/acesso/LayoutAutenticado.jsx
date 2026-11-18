import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { ROTULO_PERFIL, itensDoPerfil } from "../../compartilhado/navegacao/menu";
import { useSessao } from "./useSessao";

export default function LayoutAutenticado() {
  const { usuario, sair } = useSessao();
  const navigate = useNavigate();
  const [aberto, setAberto] = useState(false);
  const itens = itensDoPerfil(usuario.perfil);

  function aoSair() {
    sair();
    navigate("/entrar", { replace: true });
  }

  return (
    <div className="app">
      <header className="topo">
        <div className="marca">
          <span className="marca-icone" aria-hidden="true">B</span>
          BarberHub
        </div>
        <button
          type="button"
          className="topo-botao"
          aria-expanded={aberto}
          aria-controls="menu-lateral"
          onClick={() => setAberto(!aberto)}
        >
          {aberto ? "Fechar" : "Menu"}
        </button>
      </header>

      <aside id="menu-lateral" className={`lateral${aberto ? " aberta" : ""}`}>
        <div className="marca lateral-marca">
          <span className="marca-icone" aria-hidden="true">B</span>
          BarberHub
        </div>

        <nav className="menu" aria-label="Menu principal">
          {itens.map((item) => (
            <NavLink
              key={item.caminho}
              to={item.caminho}
              end
              className={({ isActive }) => `menu-item${isActive ? " ativo" : ""}`}
              onClick={() => setAberto(false)}
            >
              {item.rotulo}
            </NavLink>
          ))}
        </nav>

        <div className="lateral-usuario">
          <span className="perfil-etiqueta">{ROTULO_PERFIL[usuario.perfil]}</span>
          <strong>{usuario.nome}</strong>
          <span className="texto-suave">{usuario.email}</span>
          <button type="button" className="botao" onClick={aoSair}>Sair</button>
        </div>
      </aside>

      <main className="conteudo">
        <Outlet />
      </main>
    </div>
  );
}