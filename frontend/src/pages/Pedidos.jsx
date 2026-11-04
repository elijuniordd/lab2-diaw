import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Alerta from '../components/Alerta'
import AvaliacaoForm from '../components/AvaliacaoForm'
import PedidoForm from '../components/PedidoForm'
import StatusBadge from '../components/StatusBadge'
import { useAuth } from '../context/AuthContext'
import { extrairErro } from '../services/api'
import { listarAutomoveis } from '../services/automovelService'
import { avaliarPedido, cancelarPedido, criarPedido, listarPedidos, modificarPedido } from '../services/pedidoService'

const CANCELAVEIS = ['PENDENTE', 'EM_ANALISE', 'APROVADO']
const AVALIAVEIS = ['PENDENTE', 'EM_ANALISE']

const moeda = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
const dataBr = (iso) => new Date(`${iso}T00:00:00`).toLocaleDateString('pt-BR')

export default function Pedidos() {
  const { ehAgente } = useAuth()
  const [pedidos, setPedidos] = useState([])
  const [automoveis, setAutomoveis] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [novo, setNovo] = useState(false)
  const [acao, setAcao] = useState(null) // { tipo: 'editar' | 'avaliar', id }

  const carregar = useCallback(async () => {
    try {
      const [p, a] = await Promise.all([listarPedidos(), listarAutomoveis()])
      setPedidos(p)
      setAutomoveis(a)
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

  async function concluir(mensagem) {
    setNovo(false)
    setAcao(null)
    setAviso(mensagem)
    await carregar()
  }

  async function cancelar(p) {
    if (!window.confirm('Cancelar este pedido?')) return
    try {
      await cancelarPedido(p.id)
      await concluir('Pedido cancelado.')
    } catch (err) {
      setErro(extrairErro(err).mensagem)
    }
  }

  const abrirNovo = () => { setAviso(''); setAcao(null); setNovo(true) }
  const abrirAcao = (tipo, id) => { setAviso(''); setNovo(false); setAcao({ tipo, id }) }

  return (
    <section>
      <div className="cabecalho">
        <h1>{ehAgente ? 'Pedidos recebidos' : 'Meus pedidos'}</h1>
        {!ehAgente && !novo && (
          <button className="btn btn-primario" onClick={abrirNovo} disabled={carregando}>+ Novo pedido</button>
        )}
      </div>

      <Alerta>{erro}{erro.includes('cadastro de cliente') && <> <Link to="/clientes">Ir para meu cadastro</Link></>}</Alerta>
      <Alerta tipo="ok">{aviso}</Alerta>

      {novo && (
        <PedidoForm
          titulo="Novo pedido de aluguel"
          automoveis={automoveis}
          onSalvar={async (id) => { await criarPedido(id); await concluir('Pedido criado com status Pendente.') }}
          onCancelar={() => setNovo(false)}
        />
      )}

      {carregando ? (
        <p>Carregando…</p>
      ) : pedidos.length === 0 ? (
        !novo && (
          <p className="vazio">
            {ehAgente ? 'Nenhum pedido recebido ainda.' : 'Você ainda não fez nenhum pedido.'}
          </p>
        )
      ) : (
        <div className="lista">
          {pedidos.map((p) => {
            const editando = acao?.id === p.id
            const podeAvaliar = ehAgente && AVALIAVEIS.includes(p.status)
            const podeEditar = ehAgente ? AVALIAVEIS.includes(p.status) : p.status === 'PENDENTE'
            const podeCancelar = !ehAgente && CANCELAVEIS.includes(p.status)
            const renda = p.cliente.rendimentos.reduce((soma, r) => soma + Number(r.valor), 0)
            return (
              <article className="cartao" key={p.id}>
                <header className="cartao-topo">
                  <div>
                    <h2>{p.automovel.marca} {p.automovel.modelo} {p.automovel.ano}</h2>
                    <p className="sub">Pedido #{p.id} · {dataBr(p.data)} · placa {p.automovel.placa}</p>
                  </div>
                  <StatusBadge status={p.status} />
                </header>

                {ehAgente && (
                  <dl className="dados">
                    <div><dt>Cliente</dt><dd>{p.cliente.nome}</dd></div>
                    <div><dt>Profissão</dt><dd>{p.cliente.profissao}</dd></div>
                    <div><dt>Renda declarada</dt><dd>{moeda(renda)} ({p.cliente.rendimentos.length} fonte(s))</dd></div>
                  </dl>
                )}

                {p.parecerAgente && (
                  <p className="parecer"><strong>Parecer{p.agente ? ` (${p.agente})` : ''}:</strong> {p.parecerAgente}</p>
                )}

                {editando && acao.tipo === 'avaliar' && (
                  <AvaliacaoForm
                    onAvaliar={async (aprovado, parecer) => { await avaliarPedido(p.id, aprovado, parecer); await concluir(aprovado ? 'Pedido aprovado.' : 'Pedido reprovado.') }}
                    onCancelar={() => setAcao(null)}
                  />
                )}
                {editando && acao.tipo === 'editar' && (
                  <PedidoForm
                    titulo="Trocar automóvel"
                    automoveis={automoveis}
                    inicial={p.automovel.id}
                    onSalvar={async (id) => { await modificarPedido(p.id, id); await concluir('Pedido atualizado.') }}
                    onCancelar={() => setAcao(null)}
                  />
                )}

                {!editando && (podeAvaliar || podeEditar || podeCancelar) && (
                  <div className="acoes">
                    {podeAvaliar && <button className="btn btn-primario" onClick={() => abrirAcao('avaliar', p.id)}>Avaliar</button>}
                    {podeEditar && <button className="btn" onClick={() => abrirAcao('editar', p.id)}>Trocar automóvel</button>}
                    {podeCancelar && <button className="btn btn-perigo" onClick={() => cancelar(p)}>Cancelar pedido</button>}
                  </div>
                )}
              </article>
            )
          })}
        </div>
      )}
    </section>
  )
}
