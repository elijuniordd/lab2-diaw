/** Input com rótulo e mensagem de erro do campo. */
export default function Campo({ label, erro, ...props }) {
  return (
    <label className="campo">
      <span>{label}</span>
      <input {...props} aria-invalid={Boolean(erro)} />
      {erro && <small className="erro-campo">{erro}</small>}
    </label>
  )
}
