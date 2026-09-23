import { useEffect, useState } from 'react'
import { FileCode2 } from 'lucide-react'
import { api } from '../api'

interface CodePanelProps {
  projectId: number
  revision: number
}

export default function CodePanel({ projectId, revision }: CodePanelProps) {
  const [files, setFiles] = useState<string[]>([])
  const [selected, setSelected] = useState<string | null>(null)
  const [content, setContent] = useState('')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .files(projectId)
      .then(({ files: list }) => {
        const paths = list.map((file) => file.path).sort()
        setFiles(paths)
        setSelected((current) => current ?? paths.find((p) => p.endsWith('App.tsx')) ?? paths[0] ?? null)
      })
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Could not load files'))
  }, [projectId, revision])

  useEffect(() => {
    if (!selected) return
    api
      .fileContent(projectId, selected)
      .then((text) => {
        setContent(text)
        setError(null)
      })
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Could not load file'))
  }, [projectId, selected, revision])

  return (
    <div className="code">
      <ul className="file-list" aria-label="Project files">
        {files.map((path) => (
          <li key={path}>
            <button className={path === selected ? 'active' : ''} onClick={() => setSelected(path)} title={path}>
              <FileCode2 size={14} aria-hidden="true" />
              <span>{path}</span>
            </button>
          </li>
        ))}
      </ul>
      <div className="file-view">
        {error ? <p className="error">{error}</p> : <pre><code>{content}</code></pre>}
      </div>
    </div>
  )
}
