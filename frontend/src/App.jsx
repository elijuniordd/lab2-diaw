import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import PrivateRoute from './components/PrivateRoute'
import { AuthProvider } from './context/AuthContext'
import Automoveis from './pages/Automoveis'
import Cadastro from './pages/Cadastro'
import Clientes from './pages/Clientes'
import Contratos from './pages/Contratos'
import Login from './pages/Login'
import Pedidos from './pages/Pedidos'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/cadastro" element={<Cadastro />} />
          <Route element={<PrivateRoute />}>
            <Route element={<Layout />}>
              <Route path="/clientes" element={<Clientes />} />
              <Route path="/automoveis" element={<Automoveis />} />
              <Route path="/pedidos" element={<Pedidos />} />
              <Route path="/contratos" element={<Contratos />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/clientes" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
