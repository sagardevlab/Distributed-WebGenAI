import { Brain, Check, FileCode2, Loader2, Search } from 'lucide-react'
import type { ReactNode } from 'react'
import type { Segment } from '../parse'

/** Tiny markdown subset used by the assistant: **bold** and `code`. */
function renderInline(text: string): ReactNode[] {
  return text.split(/(\*\*[^*]+\*\*|`[^`]+`)/g).map((part, index) => {
    if (part.startsWith('**') && part.endsWith('**')) return <strong key={index}>{part.slice(2, -2)}</strong>
    if (part.startsWith('`') && part.endsWith('`')) return <code key={index}>{part.slice(1, -1)}</code>
    return part
  })
}

interface SegmentsProps {
  segments: Segment[]
}

export default function Segments({ segments }: SegmentsProps) {
  return (
    <div className="segments">
      {segments.map((segment, index) => {
        switch (segment.kind) {
          case 'thought':
            return (
              <div key={index} className="chip muted">
                <Brain size={13} aria-hidden="true" /> {segment.text}
              </div>
            )
          case 'tool':
            return (
              <div key={index} className="chip">
                <Search size={13} aria-hidden="true" /> {renderInline(segment.text || `Reading ${segment.args}`)}
              </div>
            )
          case 'file':
            return (
              <div key={index} className={`chip file ${segment.done ? 'done' : ''}`}>
                {segment.done ? <Check size={13} aria-hidden="true" /> : <Loader2 size={13} className="spin" aria-hidden="true" />}
                <FileCode2 size={13} aria-hidden="true" />
                <code>{segment.path}</code>
                <span className="muted small">{segment.lines} lines</span>
              </div>
            )
          default:
            return (
              <p key={index} className="assistant-text">
                {renderInline(segment.text)}
              </p>
            )
        }
      })}
    </div>
  )
}
