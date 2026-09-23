import { useCallback, useEffect, useRef, useState } from 'react'
import { ExternalLink, Loader2, Play, RotateCw } from 'lucide-react'
import { api, waitForUrl } from '../api'

type PreviewState =
  | { status: 'idle' }
  | { status: 'starting' }
  | { status: 'ready'; url: string }
  | { status: 'error'; message: string }

interface PreviewPanelProps {
  projectId: number
}

export default function PreviewPanel({ projectId }: PreviewPanelProps) {
  const [state, setState] = useState<PreviewState>({ status: 'idle' })
  const [frameKey, setFrameKey] = useState(0)
  const abortRef = useRef<AbortController | null>(null)

  const start = useCallback(async () => {
    abortRef.current?.abort()
    const controller = new AbortController()
    abortRef.current = controller
    setState({ status: 'starting' })
    try {
      const { previewUrl } = await api.deploy(projectId)
      const isUp = await waitForUrl(previewUrl, controller.signal)
      if (controller.signal.aborted) return
      setState(isUp ? { status: 'ready', url: previewUrl } : { status: 'error', message: 'The preview did not start in time.' })
    } catch (e) {
      if (!controller.signal.aborted) {
        setState({ status: 'error', message: e instanceof Error ? e.message : 'Could not start the preview' })
      }
    }
  }, [projectId])

  useEffect(() => {
    void start()
    return () => abortRef.current?.abort()
  }, [start])

  return (
    <div className="preview">
      <div className="preview-bar">
        <span className="url">{state.status === 'ready' ? state.url : 'Live preview'}</span>
        {state.status === 'ready' && (
          <>
            <button className="btn ghost icon" onClick={() => setFrameKey((k) => k + 1)} aria-label="Reload preview">
              <RotateCw size={15} />
            </button>
            <a className="btn ghost icon" href={state.url} target="_blank" rel="noreferrer" aria-label="Open preview in new tab">
              <ExternalLink size={15} />
            </a>
          </>
        )}
      </div>

      {state.status === 'ready' ? (
        <iframe key={frameKey} src={state.url} title="App preview" className="preview-frame" />
      ) : (
        <div className="preview-empty">
          {state.status === 'starting' && (
            <>
              <Loader2 size={28} className="spin" aria-hidden="true" />
              <p>Installing dependencies and starting the dev server…</p>
              <p className="muted small">The first start of a project can take a minute.</p>
            </>
          )}
          {state.status === 'error' && (
            <>
              <p className="error">{state.message}</p>
              <button className="btn primary" onClick={() => void start()}>
                <Play size={15} aria-hidden="true" /> Try again
              </button>
            </>
          )}
          {state.status === 'idle' && (
            <button className="btn primary" onClick={() => void start()}>
              <Play size={15} aria-hidden="true" /> Start preview
            </button>
          )}
        </div>
      )}
    </div>
  )
}
