import { useEffect, useState } from "react";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import Icone from "../../compartilhado/ui/Icone";
import { useSessao } from "../acesso/useSessao";
import { listarBarbeirosAtivos } from "../barbeiro/api";
import { detalharCliente } from "./api";
import FormularioDadosCliente from "./FormularioDadosCliente";
import FormularioPreferencias from "./FormularioPreferencias";
import HistoricoCliente from "./HistoricoCliente";

export default function PainelCliente({ id, aoAlterar, aoFechar }) {
  const { usuario } = useSessao();
  const [cliente, setCliente] = useState(null);
  const [barbeiros, setBarbeiros] = useState([]);
  const [erro, setErro] = useState("");
  const [aviso, setAviso] = useState("");
  const [editando, setEditando] = useState(false);

  useEffect(() => {
    let ativo = true;
    Promise.all([detalharCliente(id), listarBarbeirosAtivos()])
      .then(([detalhe, ativos]) => {
        if (!ativo) return;
        setCliente(detalhe);
        setBarbeiros(ativos);
      })
      .catch((e) => { if (ativo) setErro(mensagemDe(e)); });
    return () => { ativo = false; };
  }, [id]);

  function aoSalvar(salvo, mensagem) {
    setCliente(salvo);
    setEditando(false);
    setAviso(mensagem);
    aoAlterar();
  }

  if (erro) return <p className="alerta" role="alert">{erro}</p>;
  if (!cliente) return <p className="texto-suave">Carregando...</p>;

  return (
    <>
      <div className="cabecalho-pagina">
        <div className="cabecalho-texto">
          <h2 className="subtitulo">{cliente.nome}</h2>
          <div className="usuario-meta">
            <span className={`situacao ${cliente.ativo ? "situacao-ativa" : "situacao-inativa"}`}>
              {cliente.ativo ? "Ativo" : "Inativo"}
            </span>
            <span className="etiqueta">
              {cliente.aceitaMensagens ? "Aceita mensagens" : "Não quer mensagens"}
            </span>
          </div>
        </div>
        <div className="usuario-acoes">
          {usuario.perfil === "ADMINISTRADOR" && !editando && (
            <button type="button" className="botao-pequeno botao-claro" onClick={() => setEditando(true)}>
              <Icone nome="lapis" tamanho={15} />
              Editar dados
            </button>
          )}
          <button type="button" className="botao-pequeno botao-claro" onClick={aoFechar}>Fechar</button>
        </div>
      </div>

      {aviso && <p className="alerta alerta-sucesso" role="status">{aviso}</p>}

      {editando ? (
        <FormularioDadosCliente cliente={cliente}
          aoSalvar={(salvo) => aoSalvar(salvo, "Dados do cliente atualizados.")}
          aoCancelar={() => setEditando(false)} />
      ) : (
        <dl className="dados">
          <div><dt>E-mail</dt><dd>{cliente.email}</dd></div>
          <div><dt>Telefone</dt><dd>{cliente.telefone || "Não informado"}</dd></div>
        </dl>
      )}

      <FormularioPreferencias cliente={cliente} barbeiros={barbeiros}
        aoSalvar={(salvo) => aoSalvar(salvo, "Preferências salvas.")} />

      <HistoricoCliente id={id} />
    </>
  );
}
