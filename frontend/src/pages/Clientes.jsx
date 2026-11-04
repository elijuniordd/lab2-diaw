import { useCallback, useEffect, useState } from 'react'
import Alerta from '../components/Alerta'
import ClienteForm from '../components/ClienteForm'
import { useAuth } from '../context/AuthContext'
import { extrairErro } from '../services/api'
import { atualizarCliente, criarCliente, listarClientes, removerCliente } from '../services/clienteService'

const moeda = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

function formatarCpf(cpf) {
  return cpf.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4')
}

export default function Clientes() {
  const { ehAgente } = useAuth()
  const [clientes, setClientes] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [formulario, setFormulario] = useState(null) // null = fechado | {} = novo | cliente = editar

  const carregar = useCallback(async () => {
    try {
      setClientes(await listarClientes())
      setErro('')
    } catch (err) {
      setErro(extrairErro(err).mensagem)
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    carregar()
  }, [carregar])

  async function salvar(dados) {
    const editando = formulario?.id
    if (editando) await atualizarCliente(formulario.id, dados)
    else await criarCliente(dados)
    setFormulario(null)
    setAviso(editando ? 'Cliente atualizado.' : 'Cliente cadastrado.')
    await carregar()
  }

  async function remover(c) {
    if (!window.confirm(`Remover o cadastro de ${c.nome}?`)) return
    try {
      await removerCliente(c.id)
      setAviso('Cliente removido.')
      await carregar()
    } catch (err) {
      setErro(extrairErro(err).mensagem)
    }
  }

  // CLIENTE só tem um cadastro: depois de criado, só edita.
  const podeCriar = !ehAgente && clientes.length === 0 && !formulario

  return (
    <section>
      <div className="cabecalho">
        <h1>{ehAgente ? 'Clientes' : 'Meu cadastro'}</h1>
        {podeCriar && (
          <button className="btn btn-primario" onClick={() => { setAviso(''); setFormulario({}) }}>
            + Cadastrar meus dados
          </button>
        )}
      </div>

      <Alerta>{erro}</Alerta>
      <Alerta tipo="ok">{aviso}</Alerta>

      {formulario && (
        <ClienteForm
          key={formulario.id ?? 'novo'}
          inicial={formulario.id ? formulario : null}
          onSalvar={salvar}
          onCancelar={() => setFormulario(null)}
        />
      )}

      {carregando ? (
        <p>Carregando…</p>
      ) : clientes.length === 0 ? (
        !formulario && (
          <p className="vazio">
            {ehAgente ? 'Nenhum cliente cadastrado ainda.' : 'Você ainda não preencheu seus dados de cliente.'}
          </p>
        )
      ) : (
        <div className="lista">
          {clientes.map((c) => (
            <article className="cartao" key={c.id}>
              <header className="cartao-topo">
                <div>
                  <h2>{c.nome}</h2>
                  <p className="sub">{c.profissao}</p>
                </div>
                {!ehAgente && (
                  <div className="acoes">
                    <button className="btn" onClick={() => { setAviso(''); setFormulario(c) }}>Editar</button>
                    <button className="btn btn-perigo" onClick={() => remover(c)}>Remover</button>
                  </div>
                )}
              </header>
              <dl className="dados">
                <div><dt>CPF</dt><dd>{formatarCpf(c.cpf)}</dd></div>
                <div><dt>RG</dt><dd>{c.rg}</dd></div>
                <div><dt>Endereço</dt><dd>{c.endereco}</dd></div>
              </dl>
              <h3>Rendimentos</h3>
              {c.rendimentos.length === 0 ? (
                <p className="sub">Nenhum rendimento informado.</p>
              ) : (
                <ul className="rend-lista">
                  {c.rendimentos.map((r) => (
                    <li key={r.id}>
                      <span>{r.entidadeEmpregadora}</span>
                      <strong>{moeda(r.valor)}</strong>
                    </li>
                  ))}
                </ul>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
