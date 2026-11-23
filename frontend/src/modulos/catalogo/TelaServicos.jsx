import { useEffect, useMemo, useRef, useState } from "react";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import Dialogo from "../../compartilhado/ui/Dialogo";
import Icone from "../../compartilhado/ui/Icone";
import { ativarServico, inativarServico, listarCombos, listarServicos } from "./api";
import FormularioCombo from "./FormularioCombo";
import FormularioServico from "./FormularioServico";
import { MOEDA, duracaoLegivel } from "./formato";

const VISOES = [
  { valor: "SERVICOS", rotulo: "Serviços" },
  { valor: "COMBOS", rotulo: "Combos" },
];

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

function rolarAte(elemento) {
  if (!elemento) return;
  const semMovimento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  elemento.scrollIntoView({ behavior: semMovimento ? "auto" : "smooth", block: "center" });
}

function contar(lista) {
  return {
    TODOS: lista.length,
    ATIVOS: lista.filter((item) => item.ativo).length,
    INATIVOS: lista.filter((item) => !item.ativo).length,
  };
}

export default function TelaServicos() {
  const [visao, setVisao] = useState("SERVICOS");
  const [servicos, setServicos] = useState([]);
  const [combos, setCombos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [aviso, setAviso] = useState("");
  const [filtro, setFiltro] = useState("TODOS");
  const [formulario, setFormulario] = useState(null);
  const [emAcao, setEmAcao] = useState(null);
  const [aDesativar, setADesativar] = useState(null);
  const areaFormulario = useRef(null);

  useEffect(() => {
    let ativo = true;
    Promise.all([listarServicos(), listarCombos()])
      .then(([listaServicos, listaCombos]) => {
        if (!ativo) return;
        setServicos(listaServicos);
        setCombos(listaCombos);
      })
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
    if (formulario !== null) rolarAte(areaFormulario.current);
  }, [formulario]);

  const servicosParaCombo = useMemo(
    () => servicos.filter((s) => s.ativo && !s.ehCombo),
    [servicos],
  );

  const contagem = useMemo(
    () => (visao === "SERVICOS" ? contar(servicos) : contar(combos)),
    [visao, servicos, combos],
  );

  async function recarregar() {
    const [listaServicos, listaCombos] = await Promise.all([listarServicos(), listarCombos()]);
    setServicos(listaServicos);
    setCombos(listaCombos);
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
    const rotulo = visao === "SERVICOS" ? "Serviço" : "Combo";
    setFormulario(null);
    setErro("");
    setAviso(criando ? `${rotulo} ${salvo.nome} criado.` : `${rotulo} ${salvo.nome} atualizado.`);
    try {
      await recarregar();
    } catch (e) {
      setErro(mensagemDe(e));
    }
  }

  function mudarVisao(novaVisao) {
    setVisao(novaVisao);
    setFiltro("TODOS");
    setFormulario(null);
  }

  const lista = visao === "SERVICOS" ? servicos : combos;
  const visiveis = filtro === "TODOS" ? lista : lista.filter((item) => item.ativo === (filtro === "ATIVOS"));
  const idEmEdicao = formulario && formulario !== "novo" ? formulario.id : null;

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-servicos">
        <div className="cabecalho-pagina">
          <div className="cabecalho-texto">
            <span className="etiqueta">Catálogo</span>
            <h1 id="titulo-servicos" className="titulo">Serviços e combos</h1>
            <p className="texto-suave">
              Defina o que a barbearia oferece, com duração e preço. O que sai de linha é
              desativado, nunca apagado, para preservar o histórico dos atendimentos.
            </p>
          </div>
          {formulario === null && (
            <button type="button" className="botao-pequeno" onClick={() => setFormulario("novo")}>
              <Icone nome="mais" tamanho={16} />
              {visao === "SERVICOS" ? "Novo serviço" : "Novo combo"}
            </button>
          )}
        </div>

        <div className="abas" role="group" aria-label="Serviços ou combos">
          {VISOES.map((v) => (
            <button
              key={v.valor}
              type="button"
              className={`aba${visao === v.valor ? " ativa" : ""}`}
              aria-pressed={visao === v.valor}
              onClick={() => mudarVisao(v.valor)}
            >
              {v.rotulo}
            </button>
          ))}
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
          aria-label={formulario === "novo" ? "Novo item" : "Editar item"}
        >
          {visao === "SERVICOS" ? (
            <FormularioServico
              key={formulario === "novo" ? "novo" : formulario.id}
              servico={formulario === "novo" ? undefined : formulario}
              aoSalvar={aoSalvar}
              aoCancelar={() => setFormulario(null)}
            />
          ) : (
            <FormularioCombo
              key={formulario === "novo" ? "novo" : formulario.id}
              combo={formulario === "novo" ? undefined : formulario}
              servicosDisponiveis={servicosParaCombo}
              aoSalvar={aoSalvar}
              aoCancelar={() => setFormulario(null)}
            />
          )}
        </section>
      )}

      <section aria-labelledby="titulo-lista-servicos">
        <div className="barra-filtro">
          <h2 id="titulo-lista-servicos" className="subtitulo">
            {visao === "SERVICOS" ? "Lista de serviços" : "Lista de combos"}
          </h2>
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
              {lista.length === 0
                ? visao === "SERVICOS" ? "Nenhum serviço cadastrado" : "Nenhum combo cadastrado"
                : "Nada neste filtro"}
            </strong>
            <p className="texto-suave">
              {lista.length === 0
                ? visao === "SERVICOS"
                  ? "Comece pelos cortes mais pedidos: nome, duração e preço."
                  : "Junte 2 ou mais serviços simples e ativos num combo com preço próprio."
                : "Troque o filtro acima para ver os outros itens."}
            </p>
            {lista.length === 0 && formulario === null && (
              <button type="button" className="botao-pequeno" onClick={() => setFormulario("novo")}>
                <Icone nome="mais" tamanho={16} />
                {visao === "SERVICOS" ? "Cadastrar o primeiro" : "Cadastrar o primeiro combo"}
              </button>
            )}
          </div>
        ) : (
          <ul className="grade-servicos">
            {visiveis.map((item) => (
              <li
                key={item.id}
                className={`cartao-servico${item.ativo ? "" : " desativado"}${idEmEdicao === item.id ? " em-edicao" : ""}`}
              >
                <div className="servico-topo">
                  <h3 className="servico-nome">{item.nome}</h3>
                  <span className="servico-preco">{MOEDA.format(item.preco)}</span>
                </div>

                <div className="servico-meta">
                  <span className="etiqueta">
                    <Icone nome="relogio" tamanho={14} />
                    {duracaoLegivel(item.duracaoMinutos)}
                  </span>
                  {visao === "SERVICOS" && item.ehCombo && <span className="etiqueta">Combo</span>}
                  <span className={`situacao ${item.ativo ? "situacao-ativa" : "situacao-inativa"}`}>
                    {item.ativo ? "No catálogo" : "Desativado"}
                  </span>
                </div>

                {visao === "COMBOS" && (
                  <p className="texto-suave">{item.itens.map((i) => i.nome).join(" + ")}</p>
                )}

                <div className="servico-acoes">
                  <button
                    type="button"
                    className="botao-pequeno botao-claro"
                    disabled={emAcao === item.id}
                    onClick={() => setFormulario(item)}
                  >
                    <Icone nome="lapis" tamanho={15} />
                    Editar
                  </button>
                  <button
                    type="button"
                    className="botao-pequeno botao-claro"
                    disabled={emAcao === item.id}
                    onClick={() => (item.ativo ? setADesativar(item) : mudarSituacao(item, true))}
                  >
                    <Icone nome="energia" tamanho={15} />
                    {emAcao === item.id ? "Aguarde..." : item.ativo ? "Desativar" : "Reativar"}
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
          descricao="O item sai do catálogo e deixa de ser oferecido para agendamento. Os atendimentos já registrados continuam intactos, e você pode reativá-lo quando quiser."
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