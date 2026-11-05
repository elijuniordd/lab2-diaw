import { useState } from 'react'
import { extrairErro } from '../services/api'
import Alerta from './Alerta'
import Campo from './Campo'

/** O banco informa valor e juros (% ao mês) do crédito do contrato. */
export default function CreditoForm({ onSalvar, onCancelar }) {
  const [form, setForm] = useState({ valor: '', juros: '' })
  const [erro, setErro] = useState({ mensagem: '', campos: {} })
  const [enviando, setEnviando] = useState(false)

  const alterar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  async function enviar(e) {
    e.preventDefault()
    setErro({ mensagem: '', campos: {} })
    setEnviando(true)
    try {
      await onSalvar(Number(form.valor), Number(form.juros))
    } catch (err) {
      setErro(extrairErro(err))
      setEnviando(false)
    }
  }

  return (
    <form className="avaliacao" onSubmit={enviar}>
      <Alerta>{erro.mensagem}</Alerta>
      <div className="grade">
        <Campo label="Valor do crédito (R$)" type="number" min="0.01" step="0.01" value={form.valor} onChange={alterar('valor')} erro={erro.campos.valor} required />
        <Campo label="Juros (% ao mês)" type="number" min="0" step="0.01" value={form.juros} onChange={alterar('juros')} erro={erro.campos.juros} required />
      </div>
      <div className="acoes">
        <button className="btn btn-primario" disabled={enviando}>Associar crédito</button>
        <button type="button" className="btn" onClick={onCancelar}>Fechar</button>
      </div>
    </form>
  )
}
