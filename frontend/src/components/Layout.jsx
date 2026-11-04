import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { sessao, sair } = useAuth()
  const navigate = useNavigate()

  function aoSair() {
    sair()
    navigate('/login', { replace: true })
  }

  return (
    <>
      <header className="topbar">
        <strong className="marca">🚗 Aluguel de Carros</strong>
        <nav>
          <NavLink to="/clientes">Clientes</NavLink>
          <NavLink to="/pedidos">Pedidos</NavLink>
        </nav>
        <div className="usuario">
          <span>
            {sessao.login} <small className="badge">{sessao.role}</small>
          </span>
          <button className="btn btn-link" onClick={aoSair}>
            Sair
          </button>
        </div>
      </header>
      <main className="conteudo">
        <Outlet />
      </main>
    </>
  )
}
