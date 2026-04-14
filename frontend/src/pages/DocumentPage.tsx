import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import {
  ArrowLeft, RefreshCw, Trash2, ChevronDown, ChevronUp,
  AlertTriangle, BookOpen, Lightbulb, Scale, Filter
} from 'lucide-react'
import toast from 'react-hot-toast'
import Navbar from '../components/shared/Navbar'
import { SeverityBadge, RiskGauge, Skeleton } from '../components/shared/UIComponents'
import { documentsApi, getErrorMessage } from '../api'
import type { DocumentResponse, ClauseResponse, Severity } from '../types'

const SEVERITY_ORDER: Severity[] = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']

export default function DocumentPage() {
  const { id }   = useParams<{ id: string }>()
  const navigate  = useNavigate()
  const docId     = Number(id)

  const [doc,        setDoc]        = useState<DocumentResponse | null>(null)
  const [clauses,    setClauses]    = useState<ClauseResponse[]>([])
  const [loading,    setLoading]    = useState(true)
  const [filter,     setFilter]     = useState<Severity | 'ALL'>('ALL')
  const [expanded,   setExpanded]   = useState<Set<number>>(new Set())
  const [reanalyzing, setReanalyzing] = useState(false)
  const [activeTab,  setActiveTab]  = useState<'clauses' | 'report'>('clauses')

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const [docRes, clauseRes] = await Promise.all([
          documentsApi.get(docId),
          documentsApi.getClauses(docId),
        ])
        setDoc(docRes.data)
        setClauses(clauseRes.data)
      } catch (err) {
        toast.error(getErrorMessage(err))
        navigate('/dashboard')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [docId, navigate])

  const handleReanalyze = async () => {
    setReanalyzing(true)
    try {
      await documentsApi.reanalyze(docId)
      toast.success('Re-analysis started. Refresh in a moment.')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setReanalyzing(false)
    }
  }

  const handleDelete = async () => {
    if (!confirm('Delete this document and all analysis data?')) return
    try {
      await documentsApi.delete(docId)
      toast.success('Document deleted')
      navigate('/dashboard')
    } catch (err) {
      toast.error(getErrorMessage(err))
    }
  }

  const toggleExpand = (id: number) => {
    setExpanded(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  const filtered = filter === 'ALL'
    ? clauses
    : clauses.filter(c => c.severity === filter)

  const report = doc?.report

  if (loading) return (
    <div className="min-h-screen bg-ink-50">
      <Navbar />
      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8 space-y-4">
        {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-24 w-full" />)}
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-ink-50">
      <Navbar />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
        {/* Back + actions */}
        <div className="flex items-center justify-between mb-6 animate-fade-up">
          <Link to="/dashboard" className="btn-ghost text-sm -ml-2">
            <ArrowLeft size={15} /> Dashboard
          </Link>
          <div className="flex items-center gap-2">
            <button onClick={handleReanalyze} disabled={reanalyzing} className="btn-secondary text-sm">
              <RefreshCw size={14} className={reanalyzing ? 'animate-spin' : ''} />
              Re-analyze
            </button>
            <button onClick={handleDelete} className="btn-ghost text-sm text-red-500 hover:bg-red-50 hover:text-red-600">
              <Trash2 size={14} />
              Delete
            </button>
          </div>
        </div>

        {/* Document header */}
        <div className="card p-6 mb-6 animate-fade-up stagger-1">
          <div className="flex flex-col sm:flex-row sm:items-start gap-4">
            {/* Left: doc info */}
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1 flex-wrap">
                <h1 className="text-lg font-serif font-semibold text-ink-800 truncate">
                  {doc?.originalName}
                </h1>
                {doc?.documentType && (
                  <span className="text-xs bg-ink-100 text-ink-500 px-2 py-0.5 rounded-full font-medium">
                    {doc.documentType}
                  </span>
                )}
              </div>
              <p className="text-sm text-ink-400">{report?.summary}</p>
            </div>
            {/* Right: gauge */}
            {report && (
              <div className="flex-shrink-0">
                <RiskGauge score={report.overallRiskScore} size={100} />
              </div>
            )}
          </div>

          {/* Counts */}
          {report && (
            <div className="grid grid-cols-4 gap-2 mt-5 pt-5 border-t border-ink-100">
              {[
                { label: 'Critical', count: report.criticalCount, color: 'text-purple-700 bg-purple-50' },
                { label: 'High',     count: report.highCount,     color: 'text-red-700 bg-red-50' },
                { label: 'Medium',   count: report.mediumCount,   color: 'text-amber-700 bg-amber-50' },
                { label: 'Low',      count: report.lowCount,      color: 'text-green-700 bg-green-50' },
              ].map(s => (
                <button
                  key={s.label}
                  onClick={() => setFilter(s.label.toUpperCase() as Severity)}
                  className={`rounded-xl p-3 text-center transition-all
                              ${filter === s.label.toUpperCase() ? 'ring-2 ring-offset-1 ring-ink-400' : 'hover:opacity-80'}
                              ${s.color}`}
                >
                  <p className="text-xl font-bold font-mono">{s.count}</p>
                  <p className="text-xs font-medium mt-0.5">{s.label}</p>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Tabs */}
        <div className="flex gap-1 p-1 bg-ink-100 rounded-xl mb-6 w-fit animate-fade-up stagger-2">
          {(['clauses', 'report'] as const).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-5 py-2 text-sm font-medium rounded-lg transition-all capitalize
                          ${activeTab === tab
                            ? 'bg-white text-ink-800 shadow-sm'
                            : 'text-ink-500 hover:text-ink-700'}`}
            >
              {tab === 'clauses' ? `Clauses (${clauses.length})` : 'Full Report'}
            </button>
          ))}
        </div>

        {/* ─── CLAUSES TAB ─────────────────────────────────────────────────── */}
        {activeTab === 'clauses' && (
          <>
            {/* Filter chips */}
            <div className="flex items-center gap-2 mb-4 flex-wrap animate-fade-up stagger-2">
              <Filter size={13} className="text-ink-400" />
              {(['ALL', ...SEVERITY_ORDER] as const).map(s => (
                <button
                  key={s}
                  onClick={() => setFilter(s)}
                  className={`text-xs px-3 py-1.5 rounded-full border font-medium transition-all
                              ${filter === s
                                ? 'bg-ink-800 text-white border-ink-800'
                                : 'bg-white text-ink-500 border-ink-200 hover:border-ink-300'}`}
                >
                  {s === 'ALL' ? `All (${clauses.length})` : s}
                </button>
              ))}
            </div>

            {/* Clause cards */}
            <div className="space-y-3 animate-fade-up stagger-3">
              {filtered.length === 0 ? (
                <p className="text-center text-sm text-ink-400 py-12">
                  No {filter.toLowerCase()} risk clauses found.
                </p>
              ) : (
                filtered.map((clause, idx) => (
                  <ClauseCard
                    key={clause.id}
                    clause={clause}
                    isExpanded={expanded.has(clause.id)}
                    onToggle={() => toggleExpand(clause.id)}
                    index={idx}
                  />
                ))
              )}
            </div>
          </>
        )}

        {/* ─── REPORT TAB ──────────────────────────────────────────────────── */}
        {activeTab === 'report' && report && (
          <div className="space-y-4 animate-fade-up">
            {/* Top concerns */}
            {report.topConcerns.length > 0 && (
              <div className="card p-6">
                <h3 className="text-sm font-semibold text-ink-700 flex items-center gap-2 mb-4">
                  <AlertTriangle size={16} className="text-red-500" />
                  Top concerns
                </h3>
                <div className="space-y-2">
                  {report.topConcerns.map((concern, i) => (
                    <div key={i} className="flex items-start gap-3 p-3 bg-red-50 rounded-lg">
                      <span className="text-xs font-bold text-red-600 mt-0.5 min-w-[18px]">{i + 1}</span>
                      <p className="text-sm text-red-800">{concern}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Recommendations */}
            {report.recommendations.length > 0 && (
              <div className="card p-6">
                <h3 className="text-sm font-semibold text-ink-700 flex items-center gap-2 mb-4">
                  <Lightbulb size={16} className="text-gold-500" />
                  Recommendations
                </h3>
                <div className="space-y-2">
                  {report.recommendations.map((rec, i) => (
                    <div key={i} className="flex items-start gap-3 p-3 bg-amber-50 rounded-lg border border-amber-100">
                      <span className="text-amber-600 mt-0.5 flex-shrink-0">→</span>
                      <p className="text-sm text-amber-900">{rec}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Stats */}
            <div className="card p-6">
              <h3 className="text-sm font-semibold text-ink-700 flex items-center gap-2 mb-4">
                <Scale size={16} className="text-ink-500" />
                Analysis details
              </h3>
              <div className="grid grid-cols-2 gap-3 text-sm">
                {[
                  { label: 'Total clauses analyzed', value: report.totalClauses },
                  { label: 'Overall risk score',      value: `${report.overallRiskScore}/100` },
                  { label: 'Risk level',              value: report.riskLevel },
                  { label: 'Analysis date',           value: new Date(report.createdAt).toLocaleDateString('en-IN') },
                ].map(r => (
                  <div key={r.label} className="flex justify-between py-2 border-b border-ink-100 last:border-0">
                    <span className="text-ink-400">{r.label}</span>
                    <span className="font-medium text-ink-800">{r.value}</span>
                  </div>
                ))}
              </div>
              <p className="text-xs text-ink-300 mt-4">
                © 2025 VakilSahay · Clause severity scoring algorithm is proprietary and patent-pending
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

// ─── ClauseCard ──────────────────────────────────────────────────────────────
function ClauseCard({ clause, isExpanded, onToggle, index }: {
  clause: ClauseResponse
  isExpanded: boolean
  onToggle: () => void
  index: number
}) {
  const severityBorder: Record<Severity, string> = {
    CRITICAL: 'border-l-purple-500',
    HIGH:     'border-l-red-500',
    MEDIUM:   'border-l-amber-500',
    LOW:      'border-l-green-500',
  }

  return (
    <div
      className={`card border-l-4 ${severityBorder[clause.severity]} overflow-hidden
                  transition-all duration-200`}
      style={{ animationDelay: `${index * 30}ms` }}
    >
      {/* Clause header */}
      <button
        onClick={onToggle}
        className="w-full px-5 py-4 flex items-start gap-4 text-left hover:bg-ink-50 transition-colors"
      >
        <span className="text-xs font-mono text-ink-300 mt-0.5 min-w-[28px]">
          §{clause.clauseNumber}
        </span>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1.5 flex-wrap">
            <SeverityBadge severity={clause.severity} score={clause.severityScore} size="sm" />
            {clause.clauseType && (
              <span className="text-[11px] text-ink-400 bg-ink-100 px-2 py-0.5 rounded-full">
                {clause.clauseType.replace(/_/g, ' ')}
              </span>
            )}
          </div>
          <p className="text-sm text-ink-600 line-clamp-2 leading-relaxed">
            {clause.originalText}
          </p>
        </div>
        <div className="flex-shrink-0 text-ink-300 mt-1">
          {isExpanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
        </div>
      </button>

      {/* Expanded detail */}
      {isExpanded && (
        <div className="px-5 pb-5 border-t border-ink-100 animate-fade-in">
          {/* Original text */}
          <div className="mt-4">
            <p className="text-xs font-medium text-ink-400 uppercase tracking-wide mb-2">Original clause</p>
            <div className="bg-ink-50 border border-ink-100 rounded-xl p-4">
              <p className="text-sm text-ink-700 leading-relaxed font-mono text-[13px]">
                {clause.originalText}
              </p>
            </div>
          </div>

          {/* Plain English */}
          <div className="mt-4">
            <p className="text-xs font-medium text-ink-400 uppercase tracking-wide mb-2 flex items-center gap-1.5">
              <BookOpen size={12} />
              What this means
            </p>
            <div className="bg-blue-50 border border-blue-100 rounded-xl p-4">
              <p className="text-sm text-blue-900 leading-relaxed">{clause.plainEnglish}</p>
            </div>
          </div>

          {/* Law reference */}
          {clause.lawReference && (
            <div className="mt-4">
              <p className="text-xs font-medium text-ink-400 uppercase tracking-wide mb-2 flex items-center gap-1.5">
                <Scale size={12} />
                Indian law reference
              </p>
              <div className="bg-ink-100 rounded-xl px-4 py-3">
                <p className="text-sm text-ink-600 font-medium">{clause.lawReference}</p>
              </div>
            </div>
          )}

          {/* Recommendation */}
          {clause.recommendation && (
            <div className="mt-4">
              <p className="text-xs font-medium text-ink-400 uppercase tracking-wide mb-2 flex items-center gap-1.5">
                <Lightbulb size={12} />
                Recommendation
              </p>
              <div className="bg-amber-50 border border-amber-100 rounded-xl p-4">
                <p className="text-sm text-amber-900 leading-relaxed">{clause.recommendation}</p>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}