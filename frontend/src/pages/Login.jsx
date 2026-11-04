import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import Alerta from '../components/Alerta'
import Campo from '../components/Campo'
import StatusApi from '../components/StatusApi'
import { useAuth } from '../context/AuthContext'
import { extrairErro } from '../services/api'

export default function Login() {
  const { autenticado, entrar } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (autenticado) return <Navigate to="/clientes" replace />

  async function aoEnviar(e) {
    e.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await entrar(login.trim(), senha)
      navigate(location.state?.de?.pathname ?? '/clientes', { replace: true })
    } catch (err) {
      setErro(extrairErro(err).mensagem)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-pagina">
      <form className="cartao auth-cartao" onSubmit={aoEnviar}>
        <h1>Entrar</h1>
        <p className="sub">Acesse o sistema de aluguel de carros.</p>
        <Alerta tipo="ok">{location.state?.mensagem}</Alerta>
        <Alerta>{erro}</Alerta>
        <Campo label="Login" value={login} onChange={(e) => setLogin(e.target.value)} autoComplete="username" required autoFocus />
        <Campo label="Senha" type="password" value={senha} onChange={(e) => setSenha(e.target.value)} autoComplete="current-password" required />
        <button className="btn btn-primario" disabled={enviando}>
          {enviando ? 'Entrando…' : 'Entrar'}
        </button>
        <p className="rodape-form">
          Não tem conta? <Link to="/cadastro">Cadastre-se</Link>
        </p>
        <StatusApi />
      </form>
    </div>
  )
}
