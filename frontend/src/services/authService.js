import api, { STORAGE_KEY } from './api'

export async function entrar(login, senha) {
  const { data } = await api.post('/auth/login', { login, senha })
  const sessao = { token: data.token, login: data.login, role: data.role, tipoAgente: data.tipoAgente }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(sessao))
  return sessao
}

export async function registrar(login, senha, role, tipoAgente) {
  await api.post('/auth/register', { login, senha, role, tipoAgente })
}

export function sair() {
  localStorage.removeItem(STORAGE_KEY)
}

export function sessaoAtual() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY))
  } catch {
    return null
  }
}
