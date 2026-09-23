import type { ChatEvent } from './api'

export type Segment =
  | { kind: 'thought'; text: string }
  | { kind: 'message'; text: string }
  | { kind: 'tool'; text: string; args: string }
  | { kind: 'file'; path: string; done: boolean; lines: number }

// <message ...>..</message>, <tool ...>..</tool>, <file ...>..</file> — the last one may still be open while streaming
const TAG_PATTERN = /<(message|file|tool)([^>]*)>([\s\S]*?)(<\/\1>|$)/gi

function attribute(attrs: string, name: string): string {
  return new RegExp(`${name}="([^"]*)"`).exec(attrs)?.[1] ?? ''
}

/** Turns the model's raw (possibly incomplete) XML-ish output into display segments. */
export function parseStream(text: string): Segment[] {
  const segments: Segment[] = []
  for (const match of text.matchAll(TAG_PATTERN)) {
    const [, tag, attrs, body, closing] = match
    switch (tag.toLowerCase()) {
      case 'message':
        if (body.trim()) segments.push({ kind: 'message', text: body.trim() })
        break
      case 'tool':
        segments.push({ kind: 'tool', text: body.trim(), args: attribute(attrs, 'args') })
        break
      case 'file':
        segments.push({
          kind: 'file',
          path: attribute(attrs, 'path'),
          done: closing.length > 0,
          lines: body.trim().split('\n').length,
        })
        break
    }
  }
  return segments
}

/** Turns persisted chat events (from the history endpoint) into the same display segments. */
export function eventsToSegments(events: ChatEvent[]): Segment[] {
  return [...events]
    .sort((a, b) => a.sequenceOrder - b.sequenceOrder)
    .map((event): Segment => {
      const content = event.content ?? ''
      switch (event.type) {
        case 'THOUGHT':
          return { kind: 'thought', text: content }
        case 'TOOL_LOG':
          return { kind: 'tool', text: content, args: event.metadata ?? '' }
        case 'FILE_EDIT':
          return { kind: 'file', path: event.filePath ?? '', done: true, lines: content.split('\n').length }
        default:
          return { kind: 'message', text: content }
      }
    })
}
