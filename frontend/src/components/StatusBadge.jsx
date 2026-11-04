const ROTULOS = {
  PENDENTE: 'Pendente',
  EM_ANALISE: 'Em análise',
  APROVADO: 'Aprovado',
  REPROVADO: 'Reprovado',
  CANCELADO: 'Cancelado',
  CONTRATADO: 'Contratado',
}

export default function StatusBadge({ status }) {
  return <span className={`status status-${status.toLowerCase()}`}>{ROTULOS[status] ?? status}</span>
}
