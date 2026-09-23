import { useEffect, useState } from 'react'
import { session } from './api'
import AuthPage from './pages/AuthPage'
import ProjectsPage from './pages/ProjectsPage'
import WorkspacePage from './pages/WorkspacePage'
import { navigate } from './router'

type Route = { page: 'login' } | { page: 'projects' } | { page: 'workspace'; projectId: number }

function parseRoute(hash: string): Route {
  if (!session.token) return { page: 'login' }
  const match = /^#\/projects\/(\d+)/.exec(hash)
  if (match) return { page: 'workspace', projectId: Number(match[1]) }
  if (hash.startsWith('#/login')) return { page: 'login' }
  return { page: 'projects' }
}

export default function App() {
  const [route, setRoute] = useState<Route>(() => parseRoute(window.location.hash))

  useEffect(() => {
    const onChange = () => setRoute(parseRoute(window.location.hash))
    window.addEventListener('hashchange', onChange)
    return () => window.removeEventListener('hashchange', onChange)
  }, [])

  switch (route.page) {
    case 'login':
      return <AuthPage onAuthenticated={() => navigate('#/projects')} />
    case 'workspace':
      return <WorkspacePage key={route.projectId} projectId={route.projectId} />
    default:
      return <ProjectsPage />
  }
}
