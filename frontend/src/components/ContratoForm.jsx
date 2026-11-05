import { useState } from 'react'
import { extrairErro } from '../services/api'
import Alerta from './Alerta'
import Campo from './Campo'

/** O cliente confirma o pedido aprovado informando período e valor do aluguel. */
export default function ContratoForm({ onSalvar, onCancelar }) {
  const [form, setForm] = useState({ dataInicio: '', dataFim: '', valor: '' })
  const [erro, setErro] = useState({ mensagem: '', campos: {} })
  const [enviando, setEnviando] = useState(false)

  const alterar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  async function enviar(e) {
    e.preventDefault()
    setErro({ mensagem: '', campos: {} })
    setEnviando(true)
    try {
      await onSalvar({ ...form, valor: form.valor === '' ? null : Number(form.valor) })
    } catch (err) {
      setErro(extrairErro(err))
      setEnviando(false)
    }
  }

  return (
    <form className="avaliacao" onSubmit={enviar}>
      <Alerta>{erro.mensagem}</Alerta>
      <div className="grade">
        <Campo label="Início" type="date" value={form.dataInicio} onChange={alterar('dataInicio')} erro={erro.campos.dataInicio} required />
        <Campo label="Fim" type="date" value={form.dataFim} onChange={alterar('dataFim')} erro={erro.campos.dataFim} required />
        <Campo label="Valor do aluguel (R$)" type="number" min="0.01" step="0.01" value={form.valor} onChange={alterar('valor')} erro={erro.campos.valor} required />
      </div>
      <div className="acoes">
        <button className="btn btn-primario" disabled={enviando}>Confirmar contrato</button>
        <button type="button" className="btn" onClick={onCancelar}>Fechar</button>
      </div>
    </form>
  )
}
