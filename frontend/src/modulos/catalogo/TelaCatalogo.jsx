import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import Icone from "../../compartilhado/ui/Icone";
import { consultarCatalogo } from "./api";
import { MOEDA, duracaoLegivel } from "./formato";

const ABAS = [
  { valor: "TODOS", rotulo: "Todos" },
  { valor: "SERVICOS", rotulo: "Serviços" },
  { valor: "COMBOS", rotulo: "Combos" },
];

export default function TelaCatalogo() {
  const [itens, setItens] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [filtro, setFiltro] = useState("TODOS");

  useEffect(() => {
    let ativo = true;
    consultarCatalogo()
      .then((lista) => { if (ativo) setItens(lista); })
      .catch((e) => {
        if (!ativo) return;
        setErro(e instanceof ErroDaApi
          ? e.message
          : "Não foi possível falar com o servidor. Tente novamente em instantes.");
      })
      .finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, []);

  const visiveis = filtro === "TODOS"
    ? itens
    : itens.filter((item) => item.ehCombo === (filtro === "COMBOS"));

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-catalogo">
        <div className="cabecalho-texto">
          <span className="etiqueta">Catálogo</span>
          <h1 id="titulo-catalogo" className="titulo">Serviços e preços</h1>
          <p className="texto-suave">Veja o que a barbearia oferece, quanto dura e quanto custa.</p>
        </div>
      </section>

      {erro && <p className="alerta" role="alert">{erro}</p>}

      <section aria-labelledby="titulo-lista-catalogo">
        <div className="barra-filtro">
          <h2 id="titulo-lista-catalogo" className="subtitulo">O que oferecemos</h2>
          <div className="abas" role="group" aria-label="Filtrar o catálogo">
            {ABAS.map((aba) => (
              <button
                key={aba.valor}
                type="button"
                className={`aba${filtro === aba.valor ? " ativa" : ""}`}
                aria-pressed={filtro === aba.valor}
                onClick={() => setFiltro(aba.valor)}
              >
                {aba.rotulo}
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
            <strong>{itens.length === 0 ? "Nenhum serviço disponível" : "Nada neste filtro"}</strong>
            <p className="texto-suave">
              {itens.length === 0
                ? "A barbearia ainda não publicou os serviços. Volte em breve."
                : "Troque o filtro acima para ver os outros itens."}
            </p>
          </div>
        ) : (
          <ul className="grade-servicos">
            {visiveis.map((item) => (
              <li key={item.id} className="cartao-servico">
                <div className="servico-topo">
                  <h3 className="servico-nome">{item.nome}</h3>
                  <span className="servico-preco">{MOEDA.format(item.preco)}</span>
                </div>
                <div className="servico-meta">
                  <span className="etiqueta">
                    <Icone nome="relogio" tamanho={14} />
                    {duracaoLegivel(item.duracaoMinutos)}
                  </span>
                  {item.ehCombo && <span className="etiqueta">Combo</span>}
                </div>
                {item.ehCombo && <p className="texto-suave">{item.itens.join(" + ")}</p>}
                <div className="servico-acoes">
                  <Link className="botao-pequeno botao-link" to={`/agendar?servico=${item.id}`}
                    aria-label={`Agendar ${item.nome}`}>
                    <Icone nome="calendario" tamanho={15} />
                    Agendar
                  </Link>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
