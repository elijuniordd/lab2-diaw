import api from './api'

export const listarAutomoveis = () => api.get('/automoveis').then((r) => r.data)
export const criarAutomovel = (dados) => api.post('/automoveis', dados).then((r) => r.data)
