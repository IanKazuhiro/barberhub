import { useEffect, useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { ROTULO_PERFIL, itensDoPerfil } from "../../compartilhado/navegacao/menu";
import Icone from "../../compartilhado/ui/Icone";
import { useSessao } from "./useSessao";

/** "Rafael Souza" -> "RS". Usado no quadradinho do rodapé do menu. */
function iniciais(nome) {
  const partes = nome.trim().split(/\s+/);
  const primeira = partes[0]?.[0] ?? "";
  const ultima = partes.length > 1 ? partes[partes.length - 1][0] : "";
  return (primeira + ultima).toUpperCase();
}

export default function LayoutAutenticado() {
  const { usuario, sair } = useSessao();
  const navigate = useNavigate();
  const local = useLocation();
  const [aberto, setAberto] = useState(false);
  const itens = itensDoPerfil(usuario.perfil);

  // No celular o menu é uma gaveta: fecha com Esc (cada item do menu já fecha ao ser clicado).
  useEffect(() => {
    if (!aberto) return undefined;
    function aoTeclar(evento) {
      if (evento.key === "Escape") setAberto(false);
    }
    document.addEventListener("keydown", aoTeclar);
    return () => document.removeEventListener("keydown", aoTeclar);
  }, [aberto]);

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
          <Icone nome={aberto ? "fechar" : "menu"} tamanho={18} />
          {aberto ? "Fechar" : "Menu"}
        </button>
      </header>

      {aberto && (
        <button
          type="button"
          className="lateral-fundo"
          aria-label="Fechar o menu"
          onClick={() => setAberto(false)}
        />
      )}

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
              <Icone nome={item.icone} tamanho={18} />
              {item.rotulo}
            </NavLink>
          ))}
        </nav>

        <div className="lateral-usuario">
          <span className="perfil-etiqueta">{ROTULO_PERFIL[usuario.perfil]}</span>
          <div className="usuario-resumo">
            <span className="avatar" aria-hidden="true">{iniciais(usuario.nome)}</span>
            <div className="usuario-resumo-dados">
              <strong>{usuario.nome}</strong>
              <span className="texto-suave">{usuario.email}</span>
            </div>
          </div>
          <button type="button" className="botao botao-sair" onClick={aoSair}>
            <Icone nome="sair" tamanho={17} />
            Sair
          </button>
        </div>
      </aside>

      <main className="conteudo">
        {/* A key muda a cada rota: é isso que faz a animação de entrada rodar de novo. */}
        <div className="pagina-transicao" key={local.pathname}>
          <Outlet />
        </div>
      </main>
    </div>
  );
}
