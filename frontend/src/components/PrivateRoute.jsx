import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/** Sem token, redireciona para /login (guardando a página pedida). */
export default function PrivateRoute() {
  const { autenticado } = useAuth()
  const location = useLocation()
  return autenticado ? <Outlet /> : <Navigate to="/login" replace state={{ de: location }} />
}
