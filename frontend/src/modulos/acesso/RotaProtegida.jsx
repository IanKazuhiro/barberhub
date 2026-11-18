import { Navigate, Outlet, useLocation } from "react-router-dom";
import TelaAcessoNegado from "../../compartilhado/ui/TelaAcessoNegado";
import { useSessao } from "./useSessao";

/** Só deixa passar quem está logado. Com `perfis`, só quem tem um desses perfis. */
export default function RotaProtegida({ perfis }) {
  const { usuario } = useSessao();
  const local = useLocation();

  if (!usuario) {
    return <Navigate to="/entrar" replace state={{ de: local.pathname }} />;
  }
  if (perfis && !perfis.includes(usuario.perfil)) {
    return <TelaAcessoNegado />;
  }
  return <Outlet />;
}