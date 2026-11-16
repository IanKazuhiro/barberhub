import { Navigate, Route, Routes } from "react-router-dom";
import LayoutPagina from "./compartilhado/ui/LayoutPagina";
import RotaProtegida from "./modulos/acesso/RotaProtegida";
import TelaCadastro from "./modulos/acesso/TelaCadastro";
import TelaInicial from "./modulos/acesso/TelaInicial";
import TelaLogin from "./modulos/acesso/TelaLogin";

export default function App() {
  return (
    <Routes>
      <Route element={<LayoutPagina />}>
        <Route path="/entrar" element={<TelaLogin />} />
        <Route path="/cadastro" element={<TelaCadastro />} />
        <Route element={<RotaProtegida />}>
          <Route path="/inicio" element={<TelaInicial />} />
        </Route>
        <Route path="*" element={<Navigate to="/entrar" replace />} />
      </Route>
    </Routes>
  );
}