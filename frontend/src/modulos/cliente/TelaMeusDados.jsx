import { useEffect, useState } from "react";
import { errosDoFormulario, mensagemDe } from "../../compartilhado/api/mensagem";
import Campo from "../../compartilhado/ui/Campo";
import { useSessao } from "../acesso/useSessao";
import { alterarMeusDados, consultarMeusDados } from "./api";

export default function TelaMeusDados() {
  const { atualizarUsuario } = useSessao();
  const [valores, setValores] = useState(null);
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState("");
  const [aviso, setAviso] = useState("");
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    let ativo = true;
    consultarMeusDados()
      .then((dados) => { if (ativo) setValores({ ...dados, telefone: dados.telefone ?? "" }); })
      .catch((e) => { if (ativo) setErroGeral(mensagemDe(e)); });
    return () => { ativo = false; };
  }, []);

  function alterar(evento) {
    const { name, type, value, checked } = evento.target;
    setValores({ ...valores, [name]: type === "checkbox" ? checked : value });
  }

  async function enviar(evento) {
    evento.preventDefault();
    setErros({});
    setErroGeral("");
    setAviso("");
    setEnviando(true);
    try {
      const salvo = await alterarMeusDados({
        nome: valores.nome,
        telefone: valores.telefone.replace(/\D/g, ""),
        aceitaMensagens: valores.aceitaMensagens,
      });
      setValores(salvo);
      atualizarUsuario({ nome: salvo.nome, telefone: salvo.telefone });
      setAviso("Seus dados foram atualizados.");
    } catch (erro) {
      const { campos, geral } = errosDoFormulario(erro);
      setErros(campos);
      setErroGeral(geral);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <section className="cartao" aria-labelledby="titulo-meus-dados">
        <div className="cabecalho-texto">
          <span className="etiqueta">Minha conta</span>
          <h1 id="titulo-meus-dados" className="titulo">Meus dados</h1>
          <p className="texto-suave">Mantenha seu nome e telefone em dia para a barbearia falar com você.</p>
        </div>
      </section>

      {aviso && <p className="alerta alerta-sucesso" role="status">{aviso}</p>}
      {erroGeral && <p className="alerta" role="alert">{erroGeral}</p>}

      {valores && (
        <section className="cartao" aria-label="Editar meus dados">
          <form className="formulario" onSubmit={enviar} noValidate>
            <div className="grade-campos">
              <Campo id="nome" rotulo="Nome" classe="campo-largo" autoComplete="name" maxLength={120}
                value={valores.nome} onChange={alterar} erro={erros.nome} />
              <Campo id="telefone" rotulo="Telefone" type="tel" inputMode="numeric" autoComplete="tel"
                placeholder="92 99999-8888" value={valores.telefone} onChange={alterar} erro={erros.telefone} />
              <Campo id="email" rotulo="E-mail" value={valores.email} disabled
                dica="É o seu acesso ao sistema e não muda por aqui." />
            </div>
            <label className="opcao">
              <input type="checkbox" name="aceitaMensagens" checked={valores.aceitaMensagens}
                onChange={alterar} />
              <span>
                <strong>Aceito receber mensagens da barbearia</strong>
                <span className="texto-suave">
                  Desmarque para não receber convites de retorno. Você pode mudar quando quiser.
                </span>
              </span>
            </label>
            <div className="acoes-formulario">
              <button type="submit" className="botao" disabled={enviando}>
                {enviando ? "Salvando..." : "Salvar alterações"}
              </button>
            </div>
          </form>
        </section>
      )}
    </>
  );
}
