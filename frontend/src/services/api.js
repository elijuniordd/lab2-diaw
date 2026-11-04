import axios from 'axios'

export const STORAGE_KEY = 'aluguelcarros.auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
})

function lerSessao() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY))
  } catch {
    return null
  }
}

// Anexa o JWT em toda requisição.
api.interceptors.request.use((config) => {
  const sessao = lerSessao()
  if (sessao?.token) {
    config.headers.Authorization = `Bearer ${sessao.token}`
  }
  return config
})

// Token expirado/inválido: limpa a sessão e avisa a aplicação (o login em si também responde 401, então é ignorado).
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url ?? ''
    if (error.response?.status === 401 && !url.startsWith('/auth/login') && lerSessao()) {
      localStorage.removeItem(STORAGE_KEY)
      window.dispatchEvent(new Event('auth:logout'))
    }
    return Promise.reject(error)
  },
)

/** Extrai mensagem e erros por campo no formato devolvido pelo back-end. */
export function extrairErro(error) {
  const data = error.response?.data
  if (!error.response) {
    return { mensagem: 'Não foi possível falar com o servidor. A API está no ar?', campos: {} }
  }
  return { mensagem: data?.erro || 'Erro inesperado', campos: data?.campos || {} }
}

export default api
