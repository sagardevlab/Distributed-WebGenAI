import { Sparkles } from 'lucide-react'

export default function App() {
  return (
    <main className="min-h-screen bg-base-200 flex items-center justify-center p-6">
      <section className="card bg-base-100 shadow-xl max-w-lg w-full">
        <div className="card-body items-center text-center gap-4">
          <Sparkles className="size-10 text-primary" aria-hidden="true" />
          <h1 className="card-title text-3xl">Your app starts here</h1>
          <p className="text-base-content/70">
            Describe what you want to build in the chat, and it will appear on this page.
          </p>
        </div>
      </section>
    </main>
  )
}
