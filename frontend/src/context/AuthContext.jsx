import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import * as auth from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [sessao, setSessao] = useState(() => auth.sessaoAtual())

  // O interceptor do axios dispara este evento quando o token expira.
  useEffect(() => {
    const aoExpirar = () => setSessao(null)
    window.addEventListener('auth:logout', aoExpirar)
    return () => window.removeEventListener('auth:logout', aoExpirar)
  }, [])

  const entrar = useCallback(async (login, senha) => {
    setSessao(await auth.entrar(login, senha))
  }, [])

  const sair = useCallback(() => {
    auth.sair()
    setSessao(null)
  }, [])

  const valor = useMemo(
    () => ({
      sessao,
      autenticado: Boolean(sessao?.token),
      ehAgente: sessao?.role === 'AGENTE',
      entrar,
      sair,
    }),
    [sessao, entrar, sair],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext)
}
