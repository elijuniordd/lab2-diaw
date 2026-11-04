import api from './api'

export const listarPedidos = () => api.get('/pedidos').then((r) => r.data)
export const criarPedido = (automovelId) => api.post('/pedidos', { automovelId }).then((r) => r.data)
export const modificarPedido = (id, automovelId) => api.put(`/pedidos/${id}`, { automovelId }).then((r) => r.data)
export const cancelarPedido = (id) => api.put(`/pedidos/${id}/cancelar`).then((r) => r.data)
export const avaliarPedido = (id, aprovado, parecer) =>
  api.put(`/pedidos/${id}/avaliar`, { aprovado, parecer }).then((r) => r.data)
