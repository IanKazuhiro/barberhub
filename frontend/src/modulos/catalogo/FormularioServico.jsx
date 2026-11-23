import { useState } from "react";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import Campo from "../../compartilhado/ui/Campo";
import { alterarServico, criarServico } from "./api";
import { MOEDA, duracaoLegivel } from "./formato";

const VAZIO = { nome: "", duracaoMinutos: "", preco: "" };

function valoresIniciais(servico) {
  if (!servico) return VAZIO;
  return {
    nome: servico.nome,
    duracaoMinutos: String(servico.duracaoMinutos),
    preco: String(servico.preco),
  };
}

/** Cria um serviço novo ou, se receber `servico`, altera o existente. */
export default function FormularioServico({ servico, aoSalvar, aoCancelar }) {
  const [valores, setValores] = useState(() => valoresIniciais(servico));
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState("");
  const [enviando, setEnviando] = useState(false);

  function alterar(evento) {
    setValores({ ...valores, [evento.target.name]: evento.target.value });
  }

  async function enviar(evento) {
    evento.preventDefault();
    setErros({});
    setErroGeral("");
    setEnviando(true);
    try {
      // Campo vazio vai como null: o back-end responde "Informe..." no campo certo.
      const dados = {
        nome: valores.nome,
        duracaoMinutos: valores.duracaoMinutos === "" ? null : Number(valores.duracaoMinutos),
        preco: valores.preco === "" ? null : Number(valores.preco),
      };
      const salvo = servico ? await alterarServico(servico.id, dados) : await criarServico(dados);
      aoSalvar(salvo);
    } catch (erro) {
      if (erro instanceof ErroDaApi && erro.campos.length > 0) {
        setErros(Object.fromEntries(erro.campos.map((c) => [c.campo, c.mensagem])));
      } else if (erro instanceof ErroDaApi) {
        setErroGeral(erro.message);
      } else {
        setErroGeral("Não foi possível falar com o servidor. Tente novamente em instantes.");
      }
    } finally {
      setEnviando(false);
    }
  }

  const minutos = Number(valores.duracaoMinutos);
  const preco = Number(valores.preco);
  const duracaoPrevia = valores.duracaoMinutos !== "" && Number.isFinite(minutos) && minutos > 0
    ? duracaoLegivel(minutos)
    : "duração a definir";
  const precoPrevia = valores.preco !== "" && Number.isFinite(preco) && preco >= 0
    ? MOEDA.format(preco)
    : "preço a definir";

  return (
    <>
      <div className="cabecalho-texto">
        <h2 className="subtitulo">{servico ? "Editar serviço" : "Novo serviço"}</h2>
        <p className="texto-suave">
          {servico
            ? "O preço novo vale para os próximos agendamentos: os atendimentos já feitos guardam o valor da época."
            : "Depois de salvar, o serviço já pode ser escolhido num agendamento."}
        </p>
      </div>

      {erroGeral && <p className="alerta" role="alert">{erroGeral}</p>}

      <form className="formulario" onSubmit={enviar} noValidate>
        <div className="grade-campos">
          <Campo
            id="nome"
            rotulo="Nome"
            classe="campo-largo"
            autoFocus
            maxLength={100}
            placeholder="Corte masculino"
            dica="É o nome que o cliente vê no catálogo. Não pode repetir outro serviço."
            value={valores.nome}
            onChange={alterar}
            erro={erros.nome}
          />
          <Campo
            id="duracaoMinutos"
            rotulo="Duração (minutos)"
            type="number"
            inputMode="numeric"
            min="1"
            max="480"
            step="1"
            placeholder="30"
            dica="De 1 a 480 minutos. É o tempo que a agenda reserva."
            value={valores.duracaoMinutos}
            onChange={alterar}
            erro={erros.duracaoMinutos}
          />
          <Campo
            id="preco"
            rotulo="Preço (R$)"
            type="number"
            inputMode="decimal"
            min="0"
            step="0.01"
            placeholder="40.00"
            dica="Use ponto nos centavos. Por exemplo: 39.90."
            value={valores.preco}
            onChange={alterar}
            erro={erros.preco}
          />
        </div>

        <div className="previa">
          <span className="previa-rotulo">Como vai aparecer no catálogo</span>
          <strong className="previa-nome">{valores.nome.trim() || "Nome do serviço"}</strong>
          <span className="etiqueta">{duracaoPrevia}</span>
          <span className="servico-preco">{precoPrevia}</span>
        </div>

        <div className="acoes-formulario">
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? "Salvando..." : servico ? "Salvar alterações" : "Cadastrar serviço"}
          </button>
          <button type="button" className="botao botao-claro" onClick={aoCancelar} disabled={enviando}>
            Cancelar
          </button>
        </div>
      </form>
    </>
  );
}
