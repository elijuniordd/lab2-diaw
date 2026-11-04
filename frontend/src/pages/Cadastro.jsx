import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import Alerta from '../components/Alerta'
import Campo from '../components/Campo'
import { useAuth } from '../context/AuthContext'
import { extrairErro } from '../services/api'
import { registrar } from '../services/authService'

export default function Cadastro() {
  const { autenticado } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ login: '', senha: '', role: 'CLIENTE' })
  const [erro, setErro] = useState({ mensagem: '', campos: {} })
  const [enviando, setEnviando] = useState(false)

  if (autenticado) return <Navigate to="/clientes" replace />

  const alterar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  async function aoEnviar(e) {
    e.preventDefault()
    setErro({ mensagem: '', campos: {} })
    setEnviando(true)
    try {
      await registrar(form.login.trim(), form.senha, form.role)
      navigate('/login', { replace: true, state: { mensagem: 'Conta criada! Faça login para continuar.' } })
    } catch (err) {
      setErro(extrairErro(err))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-pagina">
      <form className="cartao auth-cartao" onSubmit={aoEnviar}>
        <h1>Criar conta</h1>
        <p className="sub">Cadastre um usuário para acessar o sistema.</p>
        <Alerta>{erro.mensagem}</Alerta>
        <Campo label="Login" value={form.login} onChange={alterar('login')} erro={erro.campos.login} autoComplete="username" required autoFocus />
        <Campo label="Senha (mín. 6 caracteres)" type="password" value={form.senha} onChange={alterar('senha')} erro={erro.campos.senha} autoComplete="new-password" required />
        <label className="campo">
          <span>Perfil</span>
          <select value={form.role} onChange={alterar('role')}>
            <option value="CLIENTE">Cliente</option>
            <option value="AGENTE">Agente (empresa/banco)</option>
          </select>
        </label>
        <button className="btn btn-primario" disabled={enviando}>
          {enviando ? 'Criando…' : 'Criar conta'}
        </button>
        <p className="rodape-form">
          Já tem conta? <Link to="/login">Entrar</Link>
        </p>
      </form>
    </div>
  )
}
