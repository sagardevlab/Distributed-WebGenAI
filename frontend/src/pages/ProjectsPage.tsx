import { useEffect, useState, type FormEvent } from 'react'
import { LogOut, Plus, Sparkles, Trash2 } from 'lucide-react'
import { api, session, type Plan, type Project } from '../api'
import { navigate } from '../router'

export default function ProjectsPage() {
  const [projects, setProjects] = useState<Project[] | null>(null)
  const [plan, setPlan] = useState<Plan | null>(null)
  const [name, setName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isCreating, setIsCreating] = useState(false)

  async function load() {
    try {
      const [list, subscription] = await Promise.all([api.projects(), api.subscription()])
      setProjects(list)
      setPlan(subscription.plan)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not load projects')
      setProjects([])
    }
  }

  useEffect(() => {
    void load()
  }, [])

  async function create(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setIsCreating(true)
    try {
      const project = await api.createProject(name.trim())
      navigate(`#/projects/${project.id}`)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not create project')
    } finally {
      setIsCreating(false)
    }
  }

  async function remove(project: Project) {
    if (!window.confirm(`Delete "${project.name}"?`)) return
    try {
      await api.deleteProject(project.id)
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not delete project')
    }
  }

  function logout() {
    session.clear()
    navigate('#/login')
  }

  return (
    <div className="page">
      <header className="topbar">
        <div className="brand">
          <Sparkles size={20} aria-hidden="true" />
          <span>WebGenAI</span>
        </div>
        <div className="topbar-right">
          {plan && <span className="pill">{plan.name} plan · {plan.maxProjects ?? '∞'} projects</span>}
          <span className="muted">{session.user?.name}</span>
          <button className="btn ghost icon" onClick={logout} aria-label="Log out">
            <LogOut size={16} />
          </button>
        </div>
      </header>

      <main className="projects">
        <h1>What do you want to build?</h1>
        <form className="create" onSubmit={create}>
          <input
            placeholder="Project name, e.g. Coffee shop landing page"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <button className="btn primary" disabled={isCreating || !name.trim()}>
            <Plus size={16} aria-hidden="true" /> {isCreating ? 'Creating…' : 'New project'}
          </button>
        </form>
        {error && <p className="error">{error}</p>}

        {projects === null ? (
          <p className="muted">Loading…</p>
        ) : projects.length === 0 ? (
          <p className="muted">No projects yet. Create your first one above.</p>
        ) : (
          <ul className="grid">
            {projects.map((project) => (
              <li key={project.id} className="card">
                <button className="card-main" onClick={() => navigate(`#/projects/${project.id}`)}>
                  <strong>{project.name}</strong>
                  <span className="muted small">Updated {new Date(project.updatedAt).toLocaleString()}</span>
                  <span className="pill small">{project.role}</span>
                </button>
                {project.role === 'OWNER' && (
                  <button className="btn ghost icon" onClick={() => remove(project)} aria-label={`Delete ${project.name}`}>
                    <Trash2 size={16} />
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  )
}
