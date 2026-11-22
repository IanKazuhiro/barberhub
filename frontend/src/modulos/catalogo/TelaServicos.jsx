import { useEffect, useMemo, useRef, useState } from "react";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import Dialogo from "../../compartilhado/ui/Dialogo";
import Icone from "../../compartilhado/ui/Icone";
import { ativarServico, inativarServico, listarServicos } from "./api";
import FormularioServico from "./FormularioServico";
import { MOEDA, duracaoLegivel } from "./formato";

const ABAS = [
  { valor: "TODOS", rotulo: "Todos" },
  { valor: "ATIVOS", rotulo: "No catálogo" },
  { valor: "INATIVOS", rotulo: "Desativados" },
];

function mensagemDe(erro) {
  return erro instanceof ErroDaApi
    ? erro.message
    : "Não foi possível falar com o servidor. Tente novamente em instantes.";
}

/** Rola até o elemento, respeitando quem pede menos movimento no sistema. */
function rolarAte(elemento) {
  if (!elemento) return;
  const semMovimento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  elemento.scrollIntoView({ behavior: semMovimento ? "auto" : "smooth", block: "center" });
}

export default function TelaServicos() {
  const [servicos, setServicos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [aviso, setAviso] = useState("");
  const [filtro, setFiltro] = useState("TODOS");
  // null = formulário fechado; "novo" = cadastro; um serviço = edição desse serviço.
  const [formulario, setFormulario] = useState(null);
  const [emAcao, setEmAcao] = useState(null);
  const [aDesativar, setADesativar] = useState(null);
  const areaFormulario = useRef(null);

  useEffect(() => {
    let ativo = true;
    listarServicos()
      .then((lista) => { if (ativo) setServicos(lista); })
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

  // Ao abrir o formulário, leva a tela até ele (útil ao editar um item lá embaixo).
  useEffect(() => {
    if (formulario !== null) rolarAte(areaFormulario.current);
  }, [formulario]);

  const contagem = useMemo(() => ({
    TODOS: servicos.length,
    ATIVOS: servicos.filter((s) => s.ativo).length,
    INATIVOS: servicos.filter((s) => !s.ativo).length,
  }), [servicos]);

  async function recarregar() {
    setServicos(await listarServicos());
  }

  async function mudarSituacao(alvo, ativar) {
    setErro("");
    setAviso("");
    setEmAcao(alvo.id);
    try {
      if (ativar) await ativarServico(alvo.id);
      else await inativarServico(alvo.id);
      await recarregar();
      setAviso(ativar
        ? `${alvo.nome} voltou para o catálogo.`
        : `${alvo.nome} saiu do catálogo. O histórico foi preservado.`);
    } catch (e) {
      setErro(mensagemDe(e));
    } finally {
      setEmAcao(null);
    }
  }

  async function confirmarDesativacao() {
    const alvo = aDesativar;
    setADesativar(null);
    await mudarSituacao(alvo, false);
  }

  async function aoSalvar(salvo) {
    const criando = formulario === "novo";
    setFormulario(null);
    setErro("");
    setAviso(criando ? `Serviço ${salvo.nome} criado.` : `Serviço ${salvo.nome} atualizado.`);
    try {
      await recarregar();
    } catch (e) {
      setErro(mensagemDe(e));
    }
  }

  const visiveis = filtro === "TODOS"
    ? servicos
    : servicos.filter((s) => s.ativo === (filtro === "ATIVOS"));
  const idEmEdicao = formulario && formulario !== "novo" ? formulario.id : null;

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-servicos">
        <div className="cabecalho-pagina">
          <div className="cabecalho-texto">
            <span className="etiqueta">Catálogo</span>
            <h1 id="titulo-servicos" className="titulo">Serviços</h1>
            <p className="texto-suave">
              Defina o que a barbearia oferece, com duração e preço. O que sai de linha é
              desativado, nunca apagado, para preservar o histórico dos atendimentos.
            </p>
          </div>
          {formulario === null && (
            <button type="button" className="botao-pequeno" onClick={() => setFormulario("novo")}>
              <Icone nome="mais" tamanho={16} />
              Novo serviço
            </button>
          )}
        </div>

        {!carregando && (
          <div className="resumo">
            <div className="resumo-item">
              <span className="resumo-valor">{contagem.TODOS}</span>
              <span className="resumo-rotulo">Cadastrados</span>
            </div>
            <div className="resumo-item">
              <span className="resumo-valor">{contagem.ATIVOS}</span>
              <span className="resumo-rotulo">No catálogo</span>
            </div>
            <div className="resumo-item">
              <span className="resumo-valor">{contagem.INATIVOS}</span>
              <span className="resumo-rotulo">Desativados</span>
            </div>
          </div>
        )}
      </section>

      {aviso && <p className="alerta alerta-sucesso" role="status">{aviso}</p>}
      {erro && <p className="alerta" role="alert">{erro}</p>}

      {formulario !== null && (
        <section
          className="cartao"
          ref={areaFormulario}
          aria-label={formulario === "novo" ? "Novo serviço" : "Editar serviço"}
        >
          <FormularioServico
            key={formulario === "novo" ? "novo" : formulario.id}
            servico={formulario === "novo" ? undefined : formulario}
            aoSalvar={aoSalvar}
            aoCancelar={() => setFormulario(null)}
          />
        </section>
      )}

      <section aria-labelledby="titulo-lista-servicos">
        <div className="barra-filtro">
          <h2 id="titulo-lista-servicos" className="subtitulo">Lista de serviços</h2>
          <div className="abas" role="group" aria-label="Filtrar por situação">
            {ABAS.map((aba) => (
              <button
                key={aba.valor}
                type="button"
                className={`aba${filtro === aba.valor ? " ativa" : ""}`}
                aria-pressed={filtro === aba.valor}
                onClick={() => setFiltro(aba.valor)}
              >
                {aba.rotulo}
                <span className="aba-contagem">{contagem[aba.valor]}</span>
              </button>
            ))}
          </div>
        </div>

        {carregando ? (
          <div className="esqueleto-grade" aria-hidden="true">
            <div className="esqueleto" />
            <div className="esqueleto" />
            <div className="esqueleto" />
            <div className="esqueleto" />
          </div>
        ) : visiveis.length === 0 ? (
          <div className="vazio">
            <span className="vazio-icone"><Icone nome="tesoura" tamanho={24} /></span>
            <strong>
              {servicos.length === 0 ? "Nenhum serviço cadastrado" : "Nada neste filtro"}
            </strong>
            <p className="texto-suave">
              {servicos.length === 0
                ? "Comece pelos cortes mais pedidos: nome, duração e preço."
                : "Troque o filtro acima para ver os outros serviços."}
            </p>
            {servicos.length === 0 && formulario === null && (
              <button type="button" className="botao-pequeno" onClick={() => setFormulario("novo")}>
                <Icone nome="mais" tamanho={16} />
                Cadastrar o primeiro
              </button>
            )}
          </div>
        ) : (
          <ul className="grade-servicos">
            {visiveis.map((s) => (
              <li
                key={s.id}
                className={`cartao-servico${s.ativo ? "" : " desativado"}${idEmEdicao === s.id ? " em-edicao" : ""}`}
              >
                <div className="servico-topo">
                  <h3 className="servico-nome">{s.nome}</h3>
                  <span className="servico-preco">{MOEDA.format(s.preco)}</span>
                </div>

                <div className="servico-meta">
                  <span className="etiqueta">
                    <Icone nome="relogio" tamanho={14} />
                    {duracaoLegivel(s.duracaoMinutos)}
                  </span>
                  {s.ehCombo && <span className="etiqueta">Combo</span>}
                  <span className={`situacao ${s.ativo ? "situacao-ativa" : "situacao-inativa"}`}>
                    {s.ativo ? "No catálogo" : "Desativado"}
                  </span>
                </div>

                <div className="servico-acoes">
                  <button
                    type="button"
                    className="botao-pequeno botao-claro"
                    disabled={emAcao === s.id}
                    onClick={() => setFormulario(s)}
                  >
                    <Icone nome="lapis" tamanho={15} />
                    Editar
                  </button>
                  <button
                    type="button"
                    className="botao-pequeno botao-claro"
                    disabled={emAcao === s.id}
                    onClick={() => (s.ativo ? setADesativar(s) : mudarSituacao(s, true))}
                  >
                    <Icone nome="energia" tamanho={15} />
                    {emAcao === s.id ? "Aguarde..." : s.ativo ? "Desativar" : "Reativar"}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      {aDesativar && (
        <Dialogo
          titulo={`Desativar ${aDesativar.nome}?`}
          descricao="O serviço sai do catálogo e deixa de ser oferecido para agendamento. Os atendimentos já registrados continuam intactos, e você pode reativá-lo quando quiser."
          rotuloConfirmar="Desativar"
          rotuloCancelar="Manter no catálogo"
          perigo
          aoConfirmar={confirmarDesativacao}
          aoCancelar={() => setADesativar(null)}
        />
      )}
    </>
  );
}
