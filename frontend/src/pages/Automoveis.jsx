import { useCallback, useEffect, useState } from 'react'
import Alerta from '../components/Alerta'
import Campo from '../components/Campo'
import { useAuth } from '../context/AuthContext'
import { extrairErro } from '../services/api'
import { criarAutomovel, listarAutomoveis } from '../services/automovelService'

const vazio = { matricula: '', ano: '', marca: '', modelo: '', placa: '' }

export default function Automoveis() {
  const { ehAgente } = useAuth()
  const [automoveis, setAutomoveis] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [form, setForm] = useState(vazio)
  const [aberto, setAberto] = useState(false)
  const [erro, setErro] = useState({ mensagem: '', campos: {} })
  const [aviso, setAviso] = useState('')

  const carregar = useCallback(async () => {
    try {
      setAutomoveis(await listarAutomoveis())
    } catch (err) {
      setErro({ mensagem: extrairErro(err).mensagem, campos: {} })
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    carregar()
  }, [carregar])

  const alterar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  async function salvar(e) {
    e.preventDefault()
    setErro({ mensagem: '', campos: {} })
    try {
      await criarAutomovel({ ...form, ano: form.ano === '' ? null : Number(form.ano) })
      setForm(vazio)
      setAberto(false)
      setAviso('Automóvel cadastrado.')
      await carregar()
    } catch (err) {
      setErro(extrairErro(err))
    }
  }

  const c = erro.campos

  return (
    <section>
      <div className="cabecalho">
        <h1>Automóveis</h1>
        {ehAgente && !aberto && (
          <button className="btn btn-primario" onClick={() => { setAviso(''); setAberto(true) }}>+ Cadastrar automóvel</button>
        )}
      </div>

      <Alerta tipo="ok">{aviso}</Alerta>
      {!aberto && <Alerta>{erro.mensagem}</Alerta>}

      {aberto && (
        <form className="cartao" onSubmit={salvar}>
          <h2>Novo automóvel</h2>
          <Alerta>{erro.mensagem}</Alerta>
          <div className="grade">
            <Campo label="Marca" value={form.marca} onChange={alterar('marca')} erro={c.marca} required />
            <Campo label="Modelo" value={form.modelo} onChange={alterar('modelo')} erro={c.modelo} required />
            <Campo label="Ano" type="number" min="1950" max="2100" value={form.ano} onChange={alterar('ano')} erro={c.ano} required />
            <Campo label="Placa" value={form.placa} onChange={alterar('placa')} erro={c.placa} placeholder="ABC1D23" required />
            <Campo label="Matrícula" value={form.matricula} onChange={alterar('matricula')} erro={c.matricula} required />
          </div>
          <div className="acoes">
            <button className="btn btn-primario">Salvar</button>
            <button type="button" className="btn" onClick={() => { setAberto(false); setErro({ mensagem: '', campos: {} }) }}>Cancelar</button>
          </div>
        </form>
      )}

      {carregando ? (
        <p>Carregando…</p>
      ) : automoveis.length === 0 ? (
        <p className="vazio">{ehAgente ? 'Nenhum automóvel cadastrado ainda.' : 'Nenhum automóvel disponível no momento.'}</p>
      ) : (
        <div className="lista">
          {automoveis.map((a) => (
            <article className="cartao" key={a.id}>
              <header className="cartao-topo">
                <h2>{a.marca} {a.modelo}</h2>
                <span className="badge">{a.ano}</span>
              </header>
              <dl className="dados">
                <div><dt>Placa</dt><dd>{a.placa}</dd></div>
                <div><dt>Matrícula</dt><dd>{a.matricula}</dd></div>
                {a.proprietario && (
                  <div><dt>Proprietário</dt><dd>{a.proprietario.login} ({a.proprietario.tipo.toLowerCase()})</dd></div>
                )}
              </dl>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
