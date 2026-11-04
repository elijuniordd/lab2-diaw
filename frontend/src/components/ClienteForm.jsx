import { useState } from 'react'
import { extrairErro } from '../services/api'
import Alerta from './Alerta'
import Campo from './Campo'

const MAX_RENDIMENTOS = 3
const vazio = { rg: '', cpf: '', nome: '', endereco: '', profissao: '', rendimentos: [] }

/** Formulário de criação/edição de cliente. `inicial` preenche os campos ao editar. */
export default function ClienteForm({ inicial, onSalvar, onCancelar }) {
  const [form, setForm] = useState(() =>
    inicial
      ? { ...inicial, rendimentos: inicial.rendimentos.map((r) => ({ entidadeEmpregadora: r.entidadeEmpregadora, valor: String(r.valor) })) }
      : vazio,
  )
  const [erro, setErro] = useState({ mensagem: '', campos: {} })
  const [enviando, setEnviando] = useState(false)

  const alterar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  function alterarRendimento(i, campo, valor) {
    const rendimentos = form.rendimentos.map((r, idx) => (idx === i ? { ...r, [campo]: valor } : r))
    setForm({ ...form, rendimentos })
  }

  const adicionarRendimento = () =>
    setForm({ ...form, rendimentos: [...form.rendimentos, { entidadeEmpregadora: '', valor: '' }] })

  const removerRendimento = (i) =>
    setForm({ ...form, rendimentos: form.rendimentos.filter((_, idx) => idx !== i) })

  async function aoEnviar(e) {
    e.preventDefault()
    setErro({ mensagem: '', campos: {} })
    setEnviando(true)
    try {
      await onSalvar({
        rg: form.rg,
        cpf: form.cpf,
        nome: form.nome,
        endereco: form.endereco,
        profissao: form.profissao,
        rendimentos: form.rendimentos.map((r) => ({
          entidadeEmpregadora: r.entidadeEmpregadora,
          valor: r.valor === '' ? null : Number(r.valor),
        })),
      })
    } catch (err) {
      setErro(extrairErro(err))
      setEnviando(false)
    }
  }

  const c = erro.campos
  // O back-end devolve erros de itens como "rendimentos[0].valor".
  const erroRendimento = (i, campo) => c[`rendimentos[${i}].${campo}`]

  return (
    <form className="cartao" onSubmit={aoEnviar}>
      <h2>{inicial ? 'Editar cliente' : 'Novo cliente'}</h2>
      <Alerta>{erro.mensagem}</Alerta>
      <div className="grade">
        <Campo label="Nome" value={form.nome} onChange={alterar('nome')} erro={c.nome} required />
        <Campo label="Profissão" value={form.profissao} onChange={alterar('profissao')} erro={c.profissao} required />
        <Campo label="CPF" value={form.cpf} onChange={alterar('cpf')} erro={c.cpf} placeholder="000.000.000-00" required />
        <Campo label="RG" value={form.rg} onChange={alterar('rg')} erro={c.rg} required />
      </div>
      <Campo label="Endereço" value={form.endereco} onChange={alterar('endereco')} erro={c.endereco} required />

      <fieldset className="rendimentos">
        <legend>Rendimentos ({form.rendimentos.length}/{MAX_RENDIMENTOS})</legend>
        {c.rendimentos && <small className="erro-campo">{c.rendimentos}</small>}
        {form.rendimentos.map((r, i) => (
          <div className="linha-rendimento" key={i}>
            <Campo label="Entidade empregadora" value={r.entidadeEmpregadora} erro={erroRendimento(i, 'entidadeEmpregadora')} onChange={(e) => alterarRendimento(i, 'entidadeEmpregadora', e.target.value)} required />
            <Campo label="Valor (R$)" type="number" step="0.01" min="0.01" value={r.valor} erro={erroRendimento(i, 'valor')} onChange={(e) => alterarRendimento(i, 'valor', e.target.value)} required />
            <button type="button" className="btn btn-link perigo" onClick={() => removerRendimento(i)}>
              Remover
            </button>
          </div>
        ))}
        <button type="button" className="btn" onClick={adicionarRendimento} disabled={form.rendimentos.length >= MAX_RENDIMENTOS}>
          + Adicionar rendimento
        </button>
      </fieldset>

      <div className="acoes">
        <button className="btn btn-primario" disabled={enviando}>
          {enviando ? 'Salvando…' : 'Salvar'}
        </button>
        <button type="button" className="btn" onClick={onCancelar}>
          Cancelar
        </button>
      </div>
    </form>
  )
}
