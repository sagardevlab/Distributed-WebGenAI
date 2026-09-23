import { useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import { ArrowUp, Square } from 'lucide-react'
import { api, streamChat } from '../api'
import { eventsToSegments, parseStream, type Segment } from '../parse'
import Segments from './Segments'

type Turn = { role: 'user'; text: string } | { role: 'assistant'; segments: Segment[]; isStreaming?: boolean }

interface ChatPanelProps {
  projectId: number
  onTurnComplete: () => void
}

export default function ChatPanel({ projectId, onTurnComplete }: ChatPanelProps) {
  const [turns, setTurns] = useState<Turn[]>([])
  const [input, setInput] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isStreaming, setIsStreaming] = useState(false)
  const abortRef = useRef<AbortController | null>(null)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    api
      .chatHistory(projectId)
      .then((messages) =>
        setTurns(
          messages.map((message): Turn =>
            message.role === 'USER'
              ? { role: 'user', text: message.content ?? '' }
              : { role: 'assistant', segments: eventsToSegments(message.events ?? []) },
          ),
        ),
      )
      .catch(() => setTurns([]))
    return () => abortRef.current?.abort()
  }, [projectId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ block: 'end' })
  }, [turns])

  async function send(event?: FormEvent) {
    event?.preventDefault()
    const message = input.trim()
    if (!message || isStreaming) return

    setInput('')
    setError(null)
    setIsStreaming(true)
    setTurns((previous) => [...previous, { role: 'user', text: message }, { role: 'assistant', segments: [], isStreaming: true }])

    const controller = new AbortController()
    abortRef.current = controller
    let raw = ''
    try {
      await streamChat(
        projectId,
        message,
        (chunk) => {
          raw += chunk
          const segments = parseStream(raw)
          setTurns((previous) => [...previous.slice(0, -1), { role: 'assistant', segments, isStreaming: true }])
        },
        controller.signal,
      )
    } catch (e) {
      if (!controller.signal.aborted) setError(e instanceof Error ? e.message : 'Generation failed')
    } finally {
      setTurns((previous) => [...previous.slice(0, -1), { role: 'assistant', segments: parseStream(raw) }])
      setIsStreaming(false)
      abortRef.current = null
      onTurnComplete()
    }
  }

  function onKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      void send()
    }
  }

  return (
    <aside className="chat">
      <div className="chat-log">
        {turns.length === 0 && (
          <div className="empty">
            <h2>Describe your app</h2>
            <p className="muted">
              For example: “A landing page for a specialty coffee roaster with a menu, opening hours and a contact form.”
            </p>
          </div>
        )}
        {turns.map((turn, index) =>
          turn.role === 'user' ? (
            <div key={index} className="bubble user">
              {turn.text}
            </div>
          ) : (
            <div key={index} className="bubble assistant">
              {turn.segments.length === 0 && turn.isStreaming ? (
                <span className="muted typing">Thinking…</span>
              ) : (
                <Segments segments={turn.segments} />
              )}
            </div>
          ),
        )}
        <div ref={bottomRef} />
      </div>

      {error && <p className="error chat-error">{error}</p>}

      <form className="composer" onSubmit={send}>
        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={onKeyDown}
          placeholder="Ask for a change…"
          rows={3}
          disabled={isStreaming}
        />
        {isStreaming ? (
          <button type="button" className="btn icon" onClick={() => abortRef.current?.abort()} aria-label="Stop generating">
            <Square size={16} />
          </button>
        ) : (
          <button className="btn primary icon" disabled={!input.trim()} aria-label="Send">
            <ArrowUp size={16} />
          </button>
        )}
      </form>
    </aside>
  )
}
