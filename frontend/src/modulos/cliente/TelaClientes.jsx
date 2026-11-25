import { useEffect, useRef, useState } from "react";
import { mensagemDe } from "../../compartilhado/api/mensagem";
import Campo from "../../compartilhado/ui/Campo";
import Icone from "../../compartilhado/ui/Icone";
import { buscarClientes } from "./api";
import PainelCliente from "./PainelCliente";

const ESPERA_DA_DIGITACAO = 300;

export default function TelaClientes() {
  const [busca, setBusca] = useState("");
  const [clientes, setClientes] = useState(null);
  const [erro, setErro] = useState("");
  const [selecionado, setSelecionado] = useState(null);
  const [versao, setVersao] = useState(0);
  const areaPainel = useRef(null);

  // Espera a pessoa parar de digitar; a resposta de uma busca antiga é descartada.
  useEffect(() => {
    let ativo = true;
    const relogio = setTimeout(() => {
      buscarClientes(busca)
        .then((lista) => {
          if (!ativo) return;
          setClientes(lista);
          setErro("");
        })
        .catch((e) => { if (ativo) setErro(mensagemDe(e)); });
    }, ESPERA_DA_DIGITACAO);
    return () => {
      ativo = false;
      clearTimeout(relogio);
    };
  }, [busca, versao]);

  useEffect(() => {
    if (selecionado === null || !areaPainel.current) return;
    const semMovimento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    areaPainel.current.scrollIntoView({ behavior: semMovimento ? "auto" : "smooth", block: "start" });
  }, [selecionado]);

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-clientes">
        <div className="cabecalho-texto">
          <span className="etiqueta">Clientes</span>
          <h1 id="titulo-clientes" className="titulo">Clientes</h1>
          <p className="texto-suave">Busque por parte do nome ou do telefone e registre as preferências.</p>
        </div>
        <div className="formulario">
          <Campo id="busca" rotulo="Buscar cliente" type="search" placeholder="Carla ou 92 98888"
            value={busca} onChange={(e) => setBusca(e.target.value)} />
        </div>
      </section>

      {erro && <p className="alerta" role="alert">{erro}</p>}

      {selecionado !== null && (
        <section className="cartao ancora-painel" ref={areaPainel} aria-label="Dados do cliente">
          <PainelCliente key={selecionado} id={selecionado}
            aoAlterar={() => setVersao((v) => v + 1)}
            aoFechar={() => setSelecionado(null)} />
        </section>
      )}

      <section aria-labelledby="titulo-resultado">
        <div className="barra-filtro">
          <h2 id="titulo-resultado" className="subtitulo">Resultado</h2>
        </div>
        {clientes === null ? (
          <div className="esqueleto-grade" aria-hidden="true">
            <div className="esqueleto esqueleto-linha" />
            <div className="esqueleto esqueleto-linha" />
          </div>
        ) : clientes.length === 0 ? (
          <div className="vazio">
            <span className="vazio-icone"><Icone nome="acessos" tamanho={24} /></span>
            <strong>{busca.trim() === "" ? "Nenhum cliente cadastrado" : "Nenhum cliente encontrado"}</strong>
            <p className="texto-suave">
              {busca.trim() === ""
                ? "Os clientes aparecem aqui quando criam a conta."
                : "Confira o nome ou o telefone digitado."}
            </p>
          </div>
        ) : (
          <ul className="lista-usuarios">
            {clientes.map((c) => (
              <li key={c.id} className="usuario-linha">
                <div className="usuario-dados">
                  <strong>{c.nome}</strong>
                  <span className="texto-suave">{c.telefone || "Sem telefone"} · {c.email}</span>
                  {!c.ativo && (
                    <div className="usuario-meta">
                      <span className="situacao situacao-inativa">Inativo</span>
                    </div>
                  )}
                </div>
                <button type="button" className="botao-pequeno botao-claro"
                  aria-label={`Abrir ${c.nome}`} onClick={() => setSelecionado(c.id)}>
                  Abrir
                  <Icone nome="seta" tamanho={15} />
                </button>
              </li>
            ))}
          </ul>
        )}
        {clientes?.length === 50 && (
          <p className="texto-suave">Mostrando os 50 primeiros. Digite mais para refinar a busca.</p>
        )}
      </section>
    </>
  );
}
