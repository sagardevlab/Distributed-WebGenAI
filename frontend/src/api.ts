export const API_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:8080'

export interface User {
  id: number
  username: string
  name: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface Plan {
  id: number
  name: string
  maxProjects: number | null
  maxTokensPerDay: number | null
  unlimitedAi: boolean | null
}

export interface Project {
  id: number
  name: string
  createdAt: string
  updatedAt: string
  role?: 'OWNER' | 'EDITOR' | 'VIEWER'
}

export type ChatEventType = 'THOUGHT' | 'MESSAGE' | 'FILE_EDIT' | 'TOOL_LOG'

export interface ChatEvent {
  id: number
  type: ChatEventType
  sequenceOrder: number
  content: string | null
  filePath: string | null
  metadata: string | null
}

export interface ChatMessage {
  id: number
  role: 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL'
  events: ChatEvent[] | null
  content: string | null
  tokensUsed: number | null
  createdAt: string
}

const TOKEN_KEY = 'webgenai.token'
const USER_KEY = 'webgenai.user'

function readStorage(key: string): string | null {
  try {
    return localStorage.getItem(key)
  } catch {
    return null
  }
}

export const session = {
  get token(): string | null {
    return readStorage(TOKEN_KEY)
  },
  get user(): User | null {
    const raw = readStorage(USER_KEY)
    return raw ? (JSON.parse(raw) as User) : null
  },
  save(auth: AuthResponse) {
    localStorage.setItem(TOKEN_KEY, auth.token)
    localStorage.setItem(USER_KEY, JSON.stringify(auth.user))
  },
  clear() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  },
}

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function errorMessage(res: Response): Promise<string> {
  try {
    const body = (await res.json()) as { message?: string; error?: string }
    return body.message ?? body.error ?? res.statusText
  } catch {
    return res.statusText || `Request failed (${res.status})`
  }
}

async function send(path: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers)
  if (init.body) headers.set('Content-Type', 'application/json')
  const token = session.token
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const res = await fetch(API_URL + path, { ...init, headers })
  if (res.status === 401 && token) {
    session.clear()
    window.location.hash = '#/login'
  }
  if (!res.ok) throw new ApiError(res.status, await errorMessage(res))
  return res
}

async function json<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await send(path, init)
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export const api = {
  login: (username: string, password: string) =>
    json<AuthResponse>('/account/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),
  signup: (name: string, username: string, password: string) =>
    json<AuthResponse>('/account/auth/signup', { method: 'POST', body: JSON.stringify({ name, username, password }) }),
  subscription: () => json<{ plan: Plan | null }>('/account/api/me/subscription'),

  projects: () => json<Project[]>('/workspace/projects'),
  project: (id: number) => json<Project>(`/workspace/projects/${id}`),
  createProject: (name: string) =>
    json<Project>('/workspace/projects', { method: 'POST', body: JSON.stringify({ name }) }),
  deleteProject: (id: number) => send(`/workspace/projects/${id}`, { method: 'DELETE' }),
  files: (id: number) => json<{ files: { path: string }[] }>(`/workspace/projects/${id}/files`),
  fileContent: async (id: number, path: string) =>
    (await send(`/workspace/projects/${id}/files/content?path=${encodeURIComponent(path)}`)).text(),
  deploy: (id: number) => json<{ previewUrl: string }>(`/workspace/projects/${id}/deploy`, { method: 'POST' }),

  chatHistory: (id: number) => json<ChatMessage[]>(`/intelligence/chat/projects/${id}`),
}

/** Streams the assistant's raw output (server-sent events) and calls onText for every chunk. */
export async function streamChat(
  projectId: number,
  message: string,
  onText: (chunk: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  const res = await fetch(`${API_URL}/intelligence/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      Authorization: `Bearer ${session.token ?? ''}`,
    },
    body: JSON.stringify({ message, projectId }),
    signal,
  })
  if (!res.ok || !res.body) throw new ApiError(res.status, await errorMessage(res))

  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  for (;;) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true }).replace(/\r/g, '')

    let boundary = buffer.indexOf('\n\n')
    while (boundary !== -1) {
      const rawEvent = buffer.slice(0, boundary)
      buffer = buffer.slice(boundary + 2)
      boundary = buffer.indexOf('\n\n')

      const data = rawEvent
        .split('\n')
        .filter((line) => line.startsWith('data:'))
        .map((line) => line.slice(5))
        .join('\n')
      if (!data.trim()) continue

      try {
        const parsed = JSON.parse(data) as { text?: string }
        if (parsed.text) onText(parsed.text)
      } catch {
        // not a JSON payload (keep-alive etc.)
      }
    }
  }
}

/** Resolves once the preview server answers (opaque no-cors responses count as "up"). */
export async function waitForUrl(url: string, signal: AbortSignal, timeoutMs = 240_000): Promise<boolean> {
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline && !signal.aborted) {
    try {
      await fetch(url, { mode: 'no-cors', cache: 'no-store', signal })
      return true
    } catch {
      await new Promise((resolve) => setTimeout(resolve, 2000))
    }
  }
  return false
}
