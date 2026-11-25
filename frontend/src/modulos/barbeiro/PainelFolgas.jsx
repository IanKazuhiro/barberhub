import { useEffect, useState } from "react";
import { errosDoFormulario, mensagemDe } from "../../compartilhado/api/mensagem";
import { diaPorExtenso, hojeIso } from "../../compartilhado/formato/data";
import Campo from "../../compartilhado/ui/Campo";
import Icone from "../../compartilhado/ui/Icone";
import { criarFolga, listarFolgas, removerFolga } from "./api";

const VAZIO = { data: "", motivo: "" };

export default function PainelFolgas({ barbeiro, aoFechar }) {
  const [folgas, setFolgas] = useState(null);
  const [valores, setValores] = useState(VAZIO);
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState("");
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    let ativo = true;
    listarFolgas(barbeiro.id)
      .then((lista) => { if (ativo) setFolgas(lista); })
      .catch((e) => { if (ativo) setErroGeral(mensagemDe(e)); });
    return () => { ativo = false; };
  }, [barbeiro.id]);

  function alterar(evento) {
    setValores({ ...valores, [evento.target.name]: evento.target.value });
  }

  async function executar(acao) {
    setErros({});
    setErroGeral("");
    setEnviando(true);
    try {
      await acao();
      setFolgas(await listarFolgas(barbeiro.id));
    } catch (erro) {
      const { campos, geral } = errosDoFormulario(erro);
      setErros(campos);
      setErroGeral(geral);
    } finally {
      setEnviando(false);
    }
  }

  function enviar(evento) {
    evento.preventDefault();
    executar(async () => {
      await criarFolga(barbeiro.id, { data: valores.data || null, motivo: valores.motivo });
      setValores(VAZIO);
    });
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <div className="cabecalho-texto">
          <h2 className="subtitulo">Folgas de {barbeiro.nome}</h2>
          <p className="texto-suave">
            Folga, férias ou atestado. Nesses dias nenhum horário dele é oferecido; os agendamentos
            já marcados continuam.
          </p>
        </div>
        <button type="button" className="botao-pequeno botao-claro" onClick={aoFechar}>Fechar</button>
      </div>

      {erroGeral && <p className="alerta" role="alert">{erroGeral}</p>}

      <form className="formulario" onSubmit={enviar} noValidate>
        <div className="grade-campos">
          <Campo id="data" rotulo="Data" type="date" min={hojeIso()}
            value={valores.data} onChange={alterar} erro={erros.data} />
          <Campo id="motivo" rotulo="Motivo — opcional" maxLength={200} placeholder="Consulta médica"
            value={valores.motivo} onChange={alterar} erro={erros.motivo} />
        </div>
        <div className="acoes-formulario">
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? "Aguarde..." : "Registrar folga"}
          </button>
        </div>
      </form>

      {folgas && (folgas.length === 0 ? (
        <p className="texto-suave">Nenhuma folga marcada de hoje em diante.</p>
      ) : (
        <ul className="lista-usuarios lista-cheia">
          {folgas.map((folga) => (
            <li key={folga.id} className="usuario-linha">
              <div className="usuario-dados">
                <strong className="inicial-maiuscula">{diaPorExtenso(folga.data)}</strong>
                {folga.motivo && <span className="texto-suave">{folga.motivo}</span>}
              </div>
              <button type="button" className="botao-pequeno botao-claro" disabled={enviando}
                aria-label={`Remover a folga de ${diaPorExtenso(folga.data)}`}
                onClick={() => executar(() => removerFolga(barbeiro.id, folga.id))}>
                <Icone nome="fechar" tamanho={15} />
                Remover
              </button>
            </li>
          ))}
        </ul>
      ))}
    </>
  );
}
