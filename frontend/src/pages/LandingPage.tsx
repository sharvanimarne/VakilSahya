import { Link } from 'react-router-dom'
import { Scale, ArrowRight, Shield, Zap, Globe } from 'lucide-react'

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-ink-900 text-ink-50 overflow-hidden relative">

      {/* Background grain texture */}
      <div className="absolute inset-0 opacity-[0.03] pointer-events-none"
           style={{ backgroundImage: 'url("data:image/svg+xml,%3Csvg viewBox=\'0 0 256 256\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Cfilter id=\'noise\'%3E%3CfeTurbulence type=\'fractalNoise\' baseFrequency=\'0.9\' numOctaves=\'4\' stitchTiles=\'stitch\'/%3E%3C/filter%3E%3Crect width=\'100%25\' height=\'100%25\' filter=\'url(%23noise)\'/%3E%3C/svg%3E")' }} />

      {/* Top accent line */}
      <div className="h-px bg-gradient-to-r from-transparent via-gold-500 to-transparent opacity-60" />

      {/* Nav */}
      <nav className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 bg-gold-500/20 border border-gold-500/30 rounded-lg
                          flex items-center justify-center">
            <Scale size={16} className="text-gold-400" />
          </div>
          <span className="font-serif text-xl font-semibold tracking-tight">
            Vakil<span className="text-gold-400">Sahay</span>
          </span>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/login"    className="text-sm text-ink-400 hover:text-ink-200 transition-colors px-3 py-1.5">Sign in</Link>
          <Link to="/register" className="text-sm font-medium bg-gold-500 text-ink-900 px-4 py-2 rounded-lg
                                          hover:bg-gold-400 transition-colors">
            Start Free
          </Link>
        </div>
      </nav>

      {/* Hero */}
      <div className="max-w-4xl mx-auto px-6 pt-24 pb-20 text-center">
        {/* Tag */}
        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full
                        bg-gold-500/10 border border-gold-500/20 text-gold-400 text-xs font-medium mb-8">
          <span className="w-1.5 h-1.5 rounded-full bg-gold-400" />
          Plain-language legal analysis · Powered by AI
        </div>

        <h1 className="font-serif text-5xl sm:text-6xl font-bold text-ink-50 leading-tight mb-6 text-balance">
          Know exactly what<br />
          <span className="text-gold-400">you're signing.</span>
        </h1>

        <p className="text-lg text-ink-400 max-w-xl mx-auto mb-10 leading-relaxed">
          Upload any legal document — rental agreement, employment contract, loan paper —
          and get a plain-English explanation of every clause with risk scores and
          references to Indian law.
        </p>

        <div className="flex items-center justify-center gap-3 flex-wrap">
          <Link to="/register"
                className="inline-flex items-center gap-2 px-6 py-3 bg-gold-500 text-ink-900
                           font-semibold rounded-xl hover:bg-gold-400 transition-all
                           hover:shadow-lg hover:shadow-gold-500/20 active:scale-95">
            Analyze a document free
            <ArrowRight size={16} />
          </Link>
          <Link to="/login"
                className="inline-flex items-center gap-2 px-6 py-3 border border-ink-700
                           text-ink-300 font-medium rounded-xl hover:border-ink-500
                           hover:text-ink-100 transition-colors text-sm">
            Sign in
          </Link>
        </div>
      </div>

      {/* Feature cards */}
      <div className="max-w-5xl mx-auto px-6 pb-24 grid grid-cols-1 sm:grid-cols-3 gap-4">
        {[
          {
            icon: <Shield size={20} />,
            color: 'text-green-400',
            bg:    'bg-green-500/10 border-green-500/20',
            title: 'Risk scoring',
            desc:  'Every clause scored 0–100. Critical clauses flagged before you sign.',
          },
          {
            icon: <Globe size={20} />,
            color: 'text-blue-400',
            bg:    'bg-blue-500/10 border-blue-500/20',
            title: 'Indian law references',
            desc:  'Each clause mapped to the relevant Indian statute — ICA 1872, MTA 2021, and more.',
          },
          {
            icon: <Zap size={20} />,
            color: 'text-gold-400',
            bg:    'bg-gold-500/10 border-gold-500/20',
            title: 'Instant analysis',
            desc:  'Upload PDF or DOCX and get a full report in under 30 seconds.',
          },
        ].map((f, i) => (
          <div key={i} className="bg-ink-800/60 border border-ink-700/60 rounded-2xl p-6
                                  hover:border-ink-600 transition-colors">
            <div className={`w-10 h-10 rounded-xl border ${f.bg} flex items-center justify-center mb-4 ${f.color}`}>
              {f.icon}
            </div>
            <h3 className="text-sm font-semibold text-ink-100 mb-1.5">{f.title}</h3>
            <p className="text-sm text-ink-500 leading-relaxed">{f.desc}</p>
          </div>
        ))}
      </div>

      {/* Bottom bar */}
      <div className="border-t border-ink-800 py-5 text-center">
        <p className="text-xs text-ink-600">
          © 2025 VakilSahay · Proprietary clause-severity algorithm protected under Indian Copyright Act 1957
        </p>
      </div>
    </div>
  )
}