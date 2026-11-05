import { useCallback, useEffect, useState } from 'react'
import Alerta from '../components/Alerta'
import CreditoForm from '../components/CreditoForm'
import { useAuth } from '../context/AuthContext'
import { extrairErro } from '../services/api'
import { associarCredito, listarContratos } from '../services/contratoService'

const moeda = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
const dataBr = (iso) => new Date(`${iso}T00:00:00`).toLocaleDateString('pt-BR')

export default function Contratos() {
  const { ehAgente, ehBanco } = useAuth()
  const [contratos, setContratos] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [aberto, setAberto] = useState(null) // id do contrato com o formulário de crédito aberto

  const carregar = useCallback(async () => {
    try {
      setContratos(await listarContratos())
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

  return (
    <section>
      <div className="cabecalho">
        <h1>{ehAgente ? 'Contratos' : 'Meus contratos'}</h1>
      </div>

      <Alerta>{erro}</Alerta>
      <Alerta tipo="ok">{aviso}</Alerta>

      {carregando ? (
        <p>Carregando…</p>
      ) : contratos.length === 0 ? (
        <p className="vazio">
          {ehAgente ? 'Nenhum contrato executado ainda.' : 'Execute um pedido aprovado em "Pedidos" para gerar um contrato.'}
        </p>
      ) : (
        <div className="lista">
          {contratos.map((c) => (
            <article className="cartao" key={c.id}>
              <header className="cartao-topo">
                <div>
                  <h2>{c.automovel.marca} {c.automovel.modelo} {c.automovel.ano}</h2>
                  <p className="sub">Contrato #{c.id} · pedido #{c.pedidoId} · placa {c.automovel.placa}</p>
                </div>
                <span className="badge">{c.tipo === 'ALUGUEL_COM_CREDITO' ? 'Aluguel com crédito' : 'Aluguel'}</span>
              </header>

              <dl className="dados">
                {ehAgente && <div><dt>Cliente</dt><dd>{c.cliente}</dd></div>}
                <div><dt>Período</dt><dd>{dataBr(c.dataInicio)} a {dataBr(c.dataFim)}</dd></div>
                <div><dt>Valor</dt><dd>{moeda(c.valor)}</dd></div>
                {c.credito && (
                  <>
                    <div><dt>Crédito ({c.credito.banco})</dt><dd>{moeda(c.credito.valor)}</dd></div>
                    <div><dt>Juros</dt><dd>{Number(c.credito.juros).toLocaleString('pt-BR')}% ao mês</dd></div>
                  </>
                )}
              </dl>

              {ehBanco && !c.credito && aberto !== c.id && (
                <div className="acoes">
                  <button className="btn btn-primario" onClick={() => { setAviso(''); setAberto(c.id) }}>Associar crédito</button>
                </div>
              )}
              {aberto === c.id && (
                <CreditoForm
                  onSalvar={async (valor, juros) => {
                    await associarCredito(c.id, valor, juros)
                    setAberto(null)
                    setAviso('Contrato de crédito associado.')
                    await carregar()
                  }}
                  onCancelar={() => setAberto(null)}
                />
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
