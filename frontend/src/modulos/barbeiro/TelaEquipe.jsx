import { useEffect, useRef, useState } from "react";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import Dialogo from "../../compartilhado/ui/Dialogo";
import Icone from "../../compartilhado/ui/Icone";
import { ativarUsuario, inativarUsuario } from "../acesso/api";
import { listarEquipe } from "./api";
import AgendamentosDoInativo from "./AgendamentosDoInativo";
import EditorEscala from "./EditorEscala";
import FormularioBarbeiro from "./FormularioBarbeiro";
import FormularioNovoBarbeiro from "./FormularioNovoBarbeiro";
import PainelFolgas from "./PainelFolgas";

const PERCENTUAL = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 2 });

export default function TelaEquipe() {
  const [equipe, setEquipe] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [aviso, setAviso] = useState("");
  // { tipo: "NOVO" | "DADOS" | "ESCALA" | "FOLGAS", barbeiro }
  const [painel, setPainel] = useState(null);
  const [aInativar, setAInativar] = useState(null);
  const [emAcao, setEmAcao] = useState(null);
  const areaPainel = useRef(null);

  useEffect(() => {
    let ativo = true;
    listarEquipe()
      .then((lista) => { if (ativo) setEquipe(lista); })
      .catch((e) => { if (ativo) setErro(mensagemDe(e)); })
      .finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, []);

  useEffect(() => {
    if (!aviso) return undefined;
    const relogio = setTimeout(() => setAviso(""), 6000);
    return () => clearTimeout(relogio);
  }, [aviso]);

  useEffect(() => {
    if (painel === null || !areaPainel.current) return;
    const semMovimento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    areaPainel.current.scrollIntoView({ behavior: semMovimento ? "auto" : "smooth", block: "start" });
  }, [painel]);

  async function concluir(mensagem) {
    setPainel(null);
    setErro("");
    setAviso(mensagem);
    try {
      setEquipe(await listarEquipe());
    } catch (e) {
      setErro(mensagemDe(e));
    }
  }

  async function mudarSituacao(alvo, ativar) {
    setErro("");
    setAviso("");
    setEmAcao(alvo.id);
    try {
      if (ativar) await ativarUsuario(alvo.id);
      else await inativarUsuario(alvo.id);
      await concluir(ativar
        ? `${alvo.nome} voltou a aparecer para agendamento.`
        : `${alvo.nome} foi inativado e não aparece mais para novos agendamentos.`);
    } catch (e) {
      setErro(mensagemDe(e));
    } finally {
      setEmAcao(null);
    }
  }

  async function confirmarInativacao() {
    const alvo = aInativar;
    setAInativar(null);
    await mudarSituacao(alvo, false);
  }

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-equipe">
        <div className="cabecalho-pagina">
          <div className="cabecalho-texto">
            <span className="etiqueta">Funcionários</span>
            <h1 id="titulo-equipe" className="titulo">Equipe</h1>
            <p className="texto-suave">
              Cadastre os barbeiros, defina a comissão, a escala da semana e as folgas.
            </p>
          </div>
          {painel?.tipo !== "NOVO" && (
            <button type="button" className="botao-pequeno" onClick={() => setPainel({ tipo: "NOVO" })}>
              <Icone nome="mais" tamanho={16} />
              Novo barbeiro
            </button>
          )}
        </div>
      </section>

      {aviso && <p className="alerta alerta-sucesso" role="status">{aviso}</p>}
      {erro && <p className="alerta" role="alert">{erro}</p>}

      {painel && (
        <section className="cartao ancora-painel" ref={areaPainel} aria-label="Edição da equipe"
          key={`${painel.tipo}-${painel.barbeiro?.id ?? "novo"}`}>
          {painel.tipo === "NOVO" && (
            <FormularioNovoBarbeiro
              aoCriar={(criado) => concluir(`Barbeiro ${criado.nome} criado. Agora defina a escala dele.`)}
              aoCancelar={() => setPainel(null)}
            />
          )}
          {painel.tipo === "DADOS" && (
            <FormularioBarbeiro
              barbeiro={painel.barbeiro}
              aoSalvar={(salvo) => concluir(`Dados de ${salvo.nome} atualizados.`)}
              aoCancelar={() => setPainel(null)}
            />
          )}
          {painel.tipo === "ESCALA" && (
            <EditorEscala
              barbeiro={painel.barbeiro}
              aoSalvar={() => concluir(`Escala de ${painel.barbeiro.nome} salva.`)}
              aoCancelar={() => setPainel(null)}
            />
          )}
          {painel.tipo === "FOLGAS" && (
            <PainelFolgas barbeiro={painel.barbeiro} aoFechar={() => setPainel(null)} />
          )}
        </section>
      )}

      <section aria-labelledby="titulo-barbeiros">
        <div className="barra-filtro">
          <h2 id="titulo-barbeiros" className="subtitulo">Barbeiros</h2>
        </div>

        {carregando ? (
          <div className="esqueleto-grade" aria-hidden="true">
            <div className="esqueleto" />
            <div className="esqueleto" />
          </div>
        ) : equipe.length === 0 ? (
          <div className="vazio">
            <span className="vazio-icone"><Icone nome="acessos" tamanho={24} /></span>
            <strong>Nenhum barbeiro cadastrado</strong>
            <p className="texto-suave">Crie o primeiro para poder montar a escala.</p>
          </div>
        ) : (
          <ul className="grade-servicos">
            {equipe.map((b) => (
              <li key={b.id} className={`cartao-servico${b.ativo ? "" : " desativado"}`}>
                <div className="servico-topo">
                  <h3 className="servico-nome">{b.nome}</h3>
                  <span className={`situacao ${b.ativo ? "situacao-ativa" : "situacao-inativa"}`}>
                    {b.ativo ? "Ativo" : "Inativo"}
                  </span>
                </div>
                <div className="usuario-dados">
                  <span className="texto-suave">{b.email}</span>
                  {b.telefone && <span className="texto-suave">{b.telefone}</span>}
                </div>
                <div className="servico-meta">
                  <span className="etiqueta">Comissão {PERCENTUAL.format(b.percentualComissao)}%</span>
                  {!b.possuiEscala && <span className="etiqueta etiqueta-alerta">Sem escala</span>}
                </div>
                {!b.ativo && <AgendamentosDoInativo barbeiro={b} />}
                <div className="servico-acoes">
                  <button type="button" className="botao-pequeno botao-claro"
                    onClick={() => setPainel({ tipo: "DADOS", barbeiro: b })}>
                    <Icone nome="lapis" tamanho={15} />
                    Editar
                  </button>
                  <button type="button" className="botao-pequeno botao-claro"
                    onClick={() => setPainel({ tipo: "ESCALA", barbeiro: b })}>
                    <Icone nome="relogio" tamanho={15} />
                    Escala
                  </button>
                  <button type="button" className="botao-pequeno botao-claro"
                    onClick={() => setPainel({ tipo: "FOLGAS", barbeiro: b })}>
                    <Icone nome="calendario" tamanho={15} />
                    Folgas
                  </button>
                  <button type="button" className="botao-pequeno botao-claro" disabled={emAcao === b.id}
                    onClick={() => (b.ativo ? setAInativar(b) : mudarSituacao(b, true))}>
                    <Icone nome="energia" tamanho={15} />
                    {emAcao === b.id ? "Aguarde..." : b.ativo ? "Inativar" : "Reativar"}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      {aInativar && (
        <Dialogo
          titulo={`Inativar ${aInativar.nome}?`}
          descricao="O barbeiro não entra mais no sistema e sai dos novos agendamentos. O histórico dele é preservado e você pode reativá-lo depois."
          rotuloConfirmar="Inativar"
          rotuloCancelar="Manter ativo"
          perigo
          aoConfirmar={confirmarInativacao}
          aoCancelar={() => setAInativar(null)}
        />
      )}
    </>
  );
}
