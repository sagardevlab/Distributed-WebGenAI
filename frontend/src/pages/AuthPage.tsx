import { useState, type FormEvent } from 'react'
import { Sparkles } from 'lucide-react'
import { api, session } from '../api'

interface AuthPageProps {
  onAuthenticated: () => void
}

export default function AuthPage({ onAuthenticated }: AuthPageProps) {
  const [mode, setMode] = useState<'login' | 'signup'>('login')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isBusy, setIsBusy] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setIsBusy(true)
    try {
      const auth = mode === 'login' ? await api.login(email, password) : await api.signup(name, email, password)
      session.save(auth)
      onAuthenticated()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong')
    } finally {
      setIsBusy(false)
    }
  }

  return (
    <main className="auth">
      <section className="auth-card">
        <div className="brand">
          <Sparkles size={22} aria-hidden="true" />
          <span>WebGenAI</span>
        </div>
        <h1>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h1>
        <p className="muted">Describe a web app in plain words and watch it being built live.</p>

        <form onSubmit={submit} className="stack">
          {mode === 'signup' && (
            <label>
              Name
              <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={30} />
            </label>
          )}
          <label>
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={4}
            />
          </label>
          {error && <p className="error">{error}</p>}
          <button className="btn primary" disabled={isBusy}>
            {isBusy ? 'Please wait…' : mode === 'login' ? 'Log in' : 'Sign up'}
          </button>
        </form>

        <button className="link" onClick={() => setMode(mode === 'login' ? 'signup' : 'login')}>
          {mode === 'login' ? "Don't have an account? Sign up" : 'Already have an account? Log in'}
        </button>
      </section>
    </main>
  )
}
