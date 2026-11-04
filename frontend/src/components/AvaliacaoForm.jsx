import { useState } from 'react'
import { extrairErro } from '../services/api'
import Alerta from './Alerta'

/** Parecer do agente sobre o pedido (aprovar ou reprovar). */
export default function AvaliacaoForm({ onAvaliar, onCancelar }) {
  const [parecer, setParecer] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(aprovado) {
    setErro('')
    setEnviando(true)
    try {
      await onAvaliar(aprovado, parecer.trim())
    } catch (err) {
      const e = extrairErro(err)
      setErro(e.campos.parecer ? `Parecer: ${e.campos.parecer}` : e.mensagem)
      setEnviando(false)
    }
  }

  return (
    <div className="avaliacao">
      <Alerta>{erro}</Alerta>
      <label className="campo">
        <span>Parecer financeiro</span>
        <textarea rows={3} maxLength={500} value={parecer} onChange={(e) => setParecer(e.target.value)} placeholder="Ex.: renda compatível com o valor do aluguel" />
      </label>
      <div className="acoes">
        <button className="btn btn-primario" disabled={enviando || !parecer.trim()} onClick={() => enviar(true)}>
          Aprovar
        </button>
        <button className="btn btn-perigo" disabled={enviando || !parecer.trim()} onClick={() => enviar(false)}>
          Reprovar
        </button>
        <button className="btn" onClick={onCancelar}>Fechar</button>
      </div>
    </div>
  )
}
