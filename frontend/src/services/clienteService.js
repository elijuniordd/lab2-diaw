import api from './api'

export const listarClientes = () => api.get('/clientes').then((r) => r.data)
export const criarCliente = (dados) => api.post('/clientes', dados).then((r) => r.data)
export const atualizarCliente = (id, dados) => api.put(`/clientes/${id}`, dados).then((r) => r.data)
export const removerCliente = (id) => api.delete(`/clientes/${id}`)
