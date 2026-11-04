import { useEffect, useState } from 'react'
import api from '../services/api'

/** Chama GET /health do back-end e mostra se a API está no ar. */
export default function StatusApi() {
  const [status, setStatus] = useState('verificando')

  useEffect(() => {
    let ativo = true
    api
      .get('/health')
      .then((r) => ativo && setStatus(r.data.status === 'UP' ? 'online' : 'offline'))
      .catch(() => ativo && setStatus('offline'))
    return () => {
      ativo = false
    }
  }, [])

  return (
    <span className={`status-api status-${status}`}>
      API: {status === 'online' ? 'online' : status === 'offline' ? 'offline' : 'verificando…'}
    </span>
  )
}
