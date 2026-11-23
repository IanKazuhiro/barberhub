import { useState } from "react";
import { ErroDaApi } from "../../compartilhado/api/cliente";
import Campo from "../../compartilhado/ui/Campo";
import { alterarCombo, criarCombo } from "./api";
import { MOEDA, duracaoLegivel } from "./formato";

function valoresIniciais(combo) {
  if (!combo) return { nome: "", preco: "" };
  return { nome: combo.nome, preco: String(combo.preco) };
}

function idsIniciais(combo) {
  return new Set(combo ? combo.itens.map((i) => i.id) : []);
}
 
export default function FormularioCombo({ combo, servicosDisponiveis, aoSalvar, aoCancelar }) {
  const [valores, setValores] = useState(() => valoresIniciais(combo));
  const [selecionados, setSelecionados] = useState(() => idsIniciais(combo));
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState("");
  const [enviando, setEnviando] = useState(false);

  function alterar(evento) {
    setValores({ ...valores, [evento.target.name]: evento.target.value });
  }

  function alternarItem(id) {
    const novoConjunto = new Set(selecionados);
    if (novoConjunto.has(id)) novoConjunto.delete(id);
    else novoConjunto.add(id);
    setSelecionados(novoConjunto);
  }

  async function enviar(evento) {
    evento.preventDefault();
    setErros({});
    setErroGeral("");
    setEnviando(true);
    try {
      const dados = {
        nome: valores.nome,
        preco: valores.preco === "" ? null : Number(valores.preco),
        servicoIds: Array.from(selecionados),
      };
      const salvo = combo ? await alterarCombo(combo.id, dados) : await criarCombo(dados);
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

  const itensSelecionados = servicosDisponiveis.filter((s) => selecionados.has(s.id));
  const duracaoTotal = itensSelecionados.reduce((soma, s) => soma + s.duracaoMinutos, 0);
  const preco = Number(valores.preco);
  const precoPrevia = valores.preco !== "" && Number.isFinite(preco) && preco >= 0
    ? MOEDA.format(preco)
    : "preço a definir";

  return (
    <>
      <div className="cabecalho-texto">
        <h2 className="subtitulo">{combo ? "Editar combo" : "Novo combo"}</h2>
        <p className="texto-suave">
          Escolha ao menos 2 serviços simples e ativos. O preço do combo é próprio: não precisa
          ser a soma dos itens.
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
            placeholder="Combo corte e barba"
            dica="É o nome que o cliente vê no catálogo. Não pode repetir outro serviço ou combo."
            value={valores.nome}
            onChange={alterar}
            erro={erros.nome}
          />
          <Campo
            id="preco"
            rotulo="Preço (R$)"
            type="number"
            inputMode="decimal"
            min="0"
            step="0.01"
            placeholder="60.00"
            dica="Use ponto nos centavos. Por exemplo: 59.90."
            value={valores.preco}
            onChange={alterar}
            erro={erros.preco}
          />

          <div className="campo campo-largo">
            <label htmlFor="servicos-do-combo">Serviços do combo</label>
            {servicosDisponiveis.length === 0 ? (
              <p className="texto-suave">
                Não há serviços simples ativos. Cadastre ou reative pelo menos 2 na aba Serviços.
              </p>
            ) : (
              <ul id="servicos-do-combo" className="lista-itens-combo">
                {servicosDisponiveis.map((s) => (
                  <li key={s.id}>
                    <label className="item-combo">
                      <input
                        type="checkbox"
                        checked={selecionados.has(s.id)}
                        onChange={() => alternarItem(s.id)}
                      />
                      <span className="item-combo-nome">{s.nome}</span>
                      <span className="etiqueta">{duracaoLegivel(s.duracaoMinutos)}</span>
                    </label>
                  </li>
                ))}
              </ul>
            )}
            {erros.servicoIds && <p className="campo-erro">{erros.servicoIds}</p>}
            <p className="campo-dica">Mínimo de 2 serviços. Um combo não pode conter outro combo.</p>
          </div>
        </div>

        <div className="previa">
          <span className="previa-rotulo">Como vai aparecer no catálogo</span>
          <strong className="previa-nome">{valores.nome.trim() || "Nome do combo"}</strong>
          <span className="etiqueta">
            {itensSelecionados.length > 0 ? duracaoLegivel(duracaoTotal) : "duração a definir"}
          </span>
          <span className="servico-preco">{precoPrevia}</span>
        </div>

        <div className="acoes-formulario">
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? "Salvando..." : combo ? "Salvar alterações" : "Cadastrar combo"}
          </button>
          <button type="button" className="botao botao-claro" onClick={aoCancelar} disabled={enviando}>
            Cancelar
          </button>
        </div>
      </form>
    </>
  );
}