import { useState } from 'react'
import { extrairErro } from '../services/api'
import Alerta from './Alerta'

/** Escolha do automóvel: usado para criar um pedido novo e para trocar o automóvel de um existente. */
export default function PedidoForm({ titulo, automoveis, inicial = '', onSalvar, onCancelar }) {
  const [automovelId, setAutomovelId] = useState(inicial ? String(inicial) : '')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function aoEnviar(e) {
    e.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await onSalvar(Number(automovelId))
    } catch (err) {
      setErro(extrairErro(err).mensagem)
      setEnviando(false)
    }
  }

  return (
    <form className="cartao" onSubmit={aoEnviar}>
      <h2>{titulo}</h2>
      <Alerta>{erro}</Alerta>
      {automoveis.length === 0 ? (
        <p className="sub">Nenhum automóvel disponível ainda. Um agente precisa cadastrar automóveis.</p>
      ) : (
        <label className="campo">
          <span>Automóvel</span>
          <select value={automovelId} onChange={(e) => setAutomovelId(e.target.value)} required>
            <option value="" disabled>Selecione…</option>
            {automoveis.map((a) => (
              <option key={a.id} value={a.id}>
                {a.marca} {a.modelo} {a.ano} — {a.placa}
              </option>
            ))}
          </select>
        </label>
      )}
      <div className="acoes">
        <button className="btn btn-primario" disabled={enviando || !automovelId}>
          {enviando ? 'Salvando…' : 'Salvar'}
        </button>
        <button type="button" className="btn" onClick={onCancelar}>Cancelar</button>
      </div>
    </form>
  )
}
