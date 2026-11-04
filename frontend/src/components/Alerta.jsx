import { Children } from 'react'

export default function Alerta({ tipo = 'erro', children }) {
  // Ignora filhos vazios ('' / false / null), mesmo quando vêm em mais de um filho.
  if (Children.toArray(children).every((c) => c === '')) return null
  return (
    <div className={`alerta alerta-${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      {children}
    </div>
  )
}
