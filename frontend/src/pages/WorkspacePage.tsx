import { useEffect, useState } from 'react'
import { ArrowLeft, Code2, MonitorPlay } from 'lucide-react'
import { api, type Project } from '../api'
import { navigate } from '../router'
import ChatPanel from '../components/ChatPanel'
import PreviewPanel from '../components/PreviewPanel'
import CodePanel from '../components/CodePanel'

interface WorkspacePageProps {
  projectId: number
}

export default function WorkspacePage({ projectId }: WorkspacePageProps) {
  const [project, setProject] = useState<Project | null>(null)
  const [tab, setTab] = useState<'preview' | 'code'>('preview')
  // Bumped whenever the assistant finishes a turn, so the code view refreshes
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    api.project(projectId).then(setProject).catch(() => navigate('#/projects'))
  }, [projectId])

  return (
    <div className="workspace">
      <header className="topbar">
        <div className="topbar-left">
          <button className="btn ghost icon" onClick={() => navigate('#/projects')} aria-label="Back to projects">
            <ArrowLeft size={16} />
          </button>
          <strong>{project?.name ?? 'Loading…'}</strong>
        </div>
        <nav className="tabs" aria-label="Workspace view">
          <button className={tab === 'preview' ? 'active' : ''} onClick={() => setTab('preview')}>
            <MonitorPlay size={15} aria-hidden="true" /> Preview
          </button>
          <button className={tab === 'code' ? 'active' : ''} onClick={() => setTab('code')}>
            <Code2 size={15} aria-hidden="true" /> Code
          </button>
        </nav>
      </header>

      <div className="workspace-body">
        <ChatPanel projectId={projectId} onTurnComplete={() => setRevision((r) => r + 1)} />
        <section className="stage">
          {/* Keep the preview mounted so switching tabs does not restart it */}
          <div className="stage-view" hidden={tab !== 'preview'}>
            <PreviewPanel projectId={projectId} />
          </div>
          <div className="stage-view" hidden={tab !== 'code'}>
            <CodePanel projectId={projectId} revision={revision} />
          </div>
        </section>
      </div>
    </div>
  )
}
