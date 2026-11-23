import { Navigate, Route, Routes } from "react-router-dom";
import TelaNaoEncontrada from "./compartilhado/ui/TelaNaoEncontrada";
import LayoutPagina from "./compartilhado/ui/LayoutPagina";
import LayoutAutenticado from "./modulos/acesso/LayoutAutenticado";
import RotaProtegida from "./modulos/acesso/RotaProtegida";
import TelaAcessos from "./modulos/acesso/TelaAcessos";
import TelaCadastro from "./modulos/acesso/TelaCadastro";
import TelaInicial from "./modulos/acesso/TelaInicial";
import TelaLogin from "./modulos/acesso/TelaLogin";
import TelaServicos from "./modulos/catalogo/TelaServicos";

export default function App() {
  return (
    <Routes>
      <Route element={<LayoutPagina />}>
        <Route path="/entrar" element={<TelaLogin />} />
        <Route path="/cadastro" element={<TelaCadastro />} />
      </Route>

      {/* Tudo daqui para baixo exige login e usa o layout com menu. */}
      <Route element={<RotaProtegida />}>
        <Route element={<LayoutAutenticado />}>
          <Route path="/inicio" element={<TelaInicial />} />
          <Route element={<RotaProtegida perfis={["ADMINISTRADOR"]} />}>
            <Route path="/acessos" element={<TelaAcessos />} />
            <Route path="/servicos" element={<TelaServicos />} />
          </Route>
          <Route path="*" element={<TelaNaoEncontrada />} />
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/inicio" replace />} />
    </Routes>
  );
}
