import api from './api'

export const listarContratos = () => api.get('/contratos').then((r) => r.data)
export const associarCredito = (id, valor, juros) =>
  api.post(`/contratos/${id}/credito`, { valor, juros }).then((r) => r.data)
