import { useEffect, useState } from "react";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import { ROTULO_PERFIL } from "../../compartilhado/navegacao/menu";
import Dialogo from "../../compartilhado/ui/Dialogo";
import Icone from "../../compartilhado/ui/Icone";
import FormularioNovoBarbeiro from "../barbeiro/FormularioNovoBarbeiro";
import { ativarUsuario, inativarUsuario, listarUsuarios, trocarPerfilDoUsuario } from "./api";
import { useSessao } from "./useSessao";

const ADMINISTRADOR_INICIAL = "admin@barberhub.local";

function mensagemDe(erro) {
  return erro instanceof ErroDaApi
    ? erro.message
    : "Não foi possível falar com o servidor. Tente novamente em instantes.";
}

export default function TelaAcessos() {
  const { usuario: eu } = useSessao();
  const [usuarios, setUsuarios] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [aviso, setAviso] = useState("");
  const [filtro, setFiltro] = useState("TODOS");
  const [formularioAberto, setFormularioAberto] = useState(false);
  const [emAcao, setEmAcao] = useState(null);
  // { tipo: "INATIVAR" | "PERFIL", alvo, novoPerfil }
  const [confirmacao, setConfirmacao] = useState(null);

  useEffect(() => {
    let ativo = true;
    listarUsuarios()
      .then((lista) => { if (ativo) setUsuarios(lista); })
      .catch((e) => { if (ativo) setErro(mensagemDe(e)); })
      .finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, []);

  // O aviso de sucesso sai sozinho, para não acumular mensagem na tela.
  useEffect(() => {
    if (!aviso) return undefined;
    const relogio = setTimeout(() => setAviso(""), 6000);
    return () => clearTimeout(relogio);
  }, [aviso]);

  async function recarregar() {
    setUsuarios(await listarUsuarios());
  }

  async function alterarSituacao(alvo, ativar) {
    setErro("");
    setAviso("");
    setEmAcao(alvo.id);
    try {
      if (ativar) await ativarUsuario(alvo.id);
      else await inativarUsuario(alvo.id);
      await recarregar();
      setAviso(ativar ? `Acesso de ${alvo.nome} reativado.` : `Acesso de ${alvo.nome} inativado.`);
    } catch (e) {
      setErro(mensagemDe(e));
    } finally {
      setEmAcao(null);
    }
  }

  async function trocarPerfil(alvo, novoPerfil) {
    setErro("");
    setAviso("");
    setEmAcao(alvo.id);
    try {
      await trocarPerfilDoUsuario(alvo.id, novoPerfil);
      await recarregar();
      setAviso(`Perfil de ${alvo.nome} trocado para ${ROTULO_PERFIL[novoPerfil]}.`);
    } catch (e) {
      setErro(mensagemDe(e));
    } finally {
      setEmAcao(null);
    }
  }

  async function confirmar() {
    const pedido = confirmacao;
    setConfirmacao(null);
    if (pedido.tipo === "INATIVAR") await alterarSituacao(pedido.alvo, false);
    else await trocarPerfil(pedido.alvo, pedido.novoPerfil);
  }

  async function aoCriarBarbeiro(criado) {
    setFormularioAberto(false);
    setErro("");
    setAviso(`Barbeiro ${criado.nome} criado. Ele já pode entrar com o e-mail e a senha inicial.`);
    try {
      await recarregar();
    } catch (e) {
      setErro(mensagemDe(e));
    }
  }

  const visiveis = filtro === "TODOS" ? usuarios : usuarios.filter((u) => u.perfil === filtro);

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-acessos">
        <div className="cabecalho-pagina">
          <div className="cabecalho-texto">
            <span className="etiqueta">Acesso e perfis</span>
            <h1 id="titulo-acessos" className="titulo">Acessos</h1>
            <p className="texto-suave">Crie o acesso dos barbeiros e controle quem usa o sistema.</p>
          </div>
          {!formularioAberto && (
            <button type="button" className="botao-pequeno" onClick={() => setFormularioAberto(true)}>
              <Icone nome="mais" tamanho={16} />
              Novo barbeiro
            </button>
          )}
        </div>
      </section>

      {aviso && <p className="alerta alerta-sucesso" role="status">{aviso}</p>}
      {erro && <p className="alerta" role="alert">{erro}</p>}

      {formularioAberto && (
        <section className="cartao" aria-label="Novo barbeiro">
          <FormularioNovoBarbeiro
            aoCriar={aoCriarBarbeiro}
            aoCancelar={() => setFormularioAberto(false)}
          />
        </section>
      )}

      <section aria-labelledby="titulo-usuarios">
        <div className="barra-filtro">
          <h2 id="titulo-usuarios" className="subtitulo">Usuários</h2>
          <select className="seletor" value={filtro} onChange={(e) => setFiltro(e.target.value)}
            aria-label="Filtrar por perfil">
            <option value="TODOS">Todos os perfis</option>
            <option value="ADMINISTRADOR">Administradores</option>
            <option value="BARBEIRO">Barbeiros</option>
            <option value="CLIENTE">Clientes</option>
          </select>
        </div>

        {carregando ? (
          <div className="esqueleto-grade" aria-hidden="true">
            <div className="esqueleto esqueleto-linha" />
            <div className="esqueleto esqueleto-linha" />
            <div className="esqueleto esqueleto-linha" />
          </div>
        ) : visiveis.length === 0 ? (
          <div className="vazio">
            <span className="vazio-icone"><Icone nome="acessos" tamanho={24} /></span>
            <strong>Nenhum usuário neste filtro</strong>
            <p className="texto-suave">Troque o filtro acima para ver os outros perfis.</p>
          </div>
        ) : (
          <ul className="lista-usuarios">
            {visiveis.map((u) => {
              const protegido = u.id === eu.id || u.email.toLowerCase() === ADMINISTRADOR_INICIAL;
              return (
                <li key={u.id} className="usuario-linha">
                  <div className="usuario-dados">
                    <strong>{u.nome}</strong>
                    <span className="texto-suave">{u.email}</span>
                    <div className="usuario-meta">
                      <span className="perfil-etiqueta">{ROTULO_PERFIL[u.perfil]}</span>
                      <span className={`situacao ${u.ativo ? "situacao-ativa" : "situacao-inativa"}`}>
                        {u.ativo ? "Ativo" : "Inativo"}
                      </span>
                    </div>
                  </div>
                  <div className="usuario-acoes">
                    {!protegido && (
                      <select className="seletor seletor-pequeno" value={u.perfil}
                        aria-label={`Perfil de ${u.nome}`} disabled={emAcao === u.id}
                        onChange={(e) => setConfirmacao({
                          tipo: "PERFIL", alvo: u, novoPerfil: e.target.value,
                        })}>
                        {Object.entries(ROTULO_PERFIL).map(([valor, rotulo]) => (
                          <option key={valor} value={valor}>{rotulo}</option>
                        ))}
                      </select>
                    )}
                    {!(protegido && u.ativo) && (
                      <button type="button" className="botao-pequeno botao-claro"
                        disabled={emAcao === u.id}
                        onClick={() => (u.ativo
                          ? setConfirmacao({ tipo: "INATIVAR", alvo: u })
                          : alterarSituacao(u, true))}>
                        <Icone nome="energia" tamanho={15} />
                        {emAcao === u.id ? "Aguarde..." : u.ativo ? "Inativar" : "Reativar"}
                      </button>
                    )}
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </section>

      {confirmacao?.tipo === "INATIVAR" && (
        <Dialogo
          titulo={`Inativar o acesso de ${confirmacao.alvo.nome}?`}
          descricao="A pessoa não consegue mais entrar no sistema, e é desconectada na próxima ação. O histórico dela é preservado e você pode reativar o acesso depois."
          rotuloConfirmar="Inativar"
          rotuloCancelar="Manter ativo"
          perigo
          aoConfirmar={confirmar}
          aoCancelar={() => setConfirmacao(null)}
        />
      )}

      {confirmacao?.tipo === "PERFIL" && (
        <Dialogo
          titulo={`Trocar o perfil de ${confirmacao.alvo.nome}?`}
          descricao={`De ${ROTULO_PERFIL[confirmacao.alvo.perfil]} para ${ROTULO_PERFIL[confirmacao.novoPerfil]}. Os dados do perfil atual são removidos, e a troca vale na próxima ação da pessoa.`}
          rotuloConfirmar="Trocar perfil"
          aoConfirmar={confirmar}
          aoCancelar={() => setConfirmacao(null)}
        />
      )}
    </>
  );
}
