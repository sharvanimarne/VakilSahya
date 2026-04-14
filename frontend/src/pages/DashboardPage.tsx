import { useEffect, useState, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { FileText, Upload, Trash2, ChevronRight, Search, RefreshCw } from 'lucide-react'
import toast from 'react-hot-toast'
import Navbar from '../components/shared/Navbar'
import { SeverityBadge, StatusBadge, Skeleton, EmptyState } from '../components/shared/UIComponents'
import { documentsApi, getErrorMessage } from '../api'
import { useAuthStore } from '../store/authStore'
import type { DocumentResponse } from '../types'

const DOC_TYPE_LABELS: Record<string, string> = {
  RENTAL: 'Rental', EMPLOYMENT: 'Employment', LOAN: 'Loan',
  NDA: 'NDA', PARTNERSHIP: 'Partnership', GENERAL: 'General',
}

export default function DashboardPage() {
  const { user } = useAuthStore()
  const navigate  = useNavigate()
  const [docs,     setDocs]     = useState<DocumentResponse[]>([])
  const [loading,  setLoading]  = useState(true)
  const [search,   setSearch]   = useState('')
  const [deleting, setDeleting] = useState<number | null>(null)

  const fetchDocs = useCallback(async () => {
    setLoading(true)
    try {
      const res = await documentsApi.list(0, 50)
      setDocs(res.data.content)
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchDocs() }, [fetchDocs])

  // Auto-refresh if any doc is analyzing
  useEffect(() => {
    const analyzing = docs.some(d => d.status === 'ANALYZING' || d.status === 'UPLOADED')
    if (!analyzing) return
    const timer = setInterval(fetchDocs, 4000)
    return () => clearInterval(timer)
  }, [docs, fetchDocs])

  const handleDelete = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation()
    if (!confirm('Delete this document and its analysis?')) return
    setDeleting(id)
    try {
      await documentsApi.delete(id)
      setDocs(prev => prev.filter(d => d.id !== id))
      toast.success('Document deleted')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setDeleting(null)
    }
  }

  const filtered = docs.filter(d =>
    d.originalName.toLowerCase().includes(search.toLowerCase()) ||
    (d.documentType ?? '').toLowerCase().includes(search.toLowerCase())
  )

  const stats = {
    total:     docs.length,
    completed: docs.filter(d => d.status === 'COMPLETED').length,
    highRisk:  docs.filter(d => d.report && d.report.overallRiskScore >= 51).length,
  }

  return (
    <div className="min-h-screen bg-ink-50">
      <Navbar />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8 animate-fade-up">
          <div>
            <h1 className="text-2xl font-serif font-semibold text-ink-800">
              Good {getGreeting()}, {user?.fullName?.split(' ')[0]}.
            </h1>
            <p className="text-sm text-ink-400 mt-0.5">Your legal documents</p>
          </div>
          <Link to="/upload" className="btn-primary">
            <Upload size={15} />
            Analyze new
          </Link>
        </div>

        {/* Stats row */}
        <div className="grid grid-cols-3 gap-3 mb-8 animate-fade-up stagger-1">
          {[
            { label: 'Total documents', value: stats.total,     color: 'text-ink-800' },
            { label: 'Analyzed',        value: stats.completed, color: 'text-green-600' },
            { label: 'High / Critical', value: stats.highRisk,  color: 'text-red-600' },
          ].map((s, i) => (
            <div key={i} className="card px-4 py-4">
              <p className="text-xs text-ink-400 mb-1">{s.label}</p>
              <p className={`text-2xl font-semibold font-mono ${s.color}`}>{s.value}</p>
            </div>
          ))}
        </div>

        {/* Search + refresh */}
        <div className="flex items-center gap-3 mb-4 animate-fade-up stagger-2">
          <div className="relative flex-1">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-ink-300" />
            <input
              type="text"
              placeholder="Search documents…"
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="input-field pl-9 py-2.5 text-sm"
            />
          </div>
          <button onClick={fetchDocs} className="btn-secondary py-2.5 px-3" title="Refresh">
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          </button>
        </div>

        {/* Document list */}
        {loading ? (
          <div className="space-y-3">
            {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-20 w-full" />)}
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<FileText size={24} />}
            title={search ? 'No matches found' : 'No documents yet'}
            description={search ? 'Try a different search term.' : 'Upload your first legal document to get a plain-English analysis.'}
            action={!search && (
              <Link to="/upload" className="btn-primary text-sm">
                <Upload size={14} /> Analyze a document
              </Link>
            )}
          />
        ) : (
          <div className="space-y-2 animate-fade-up stagger-3">
            {filtered.map((doc, idx) => (
              <div
                key={doc.id}
                onClick={() => doc.status === 'COMPLETED' && navigate(`/documents/${doc.id}`)}
                className={`card px-5 py-4 flex items-center gap-4 transition-all duration-150
                            ${doc.status === 'COMPLETED'
                              ? 'hover:border-ink-200 hover:shadow-md cursor-pointer'
                              : 'opacity-80 cursor-default'}`}
                style={{ animationDelay: `${idx * 40}ms` }}
              >
                {/* File icon */}
                <div className="w-10 h-10 bg-ink-100 rounded-xl flex items-center justify-center flex-shrink-0">
                  <FileText size={18} className="text-ink-500" />
                </div>

                {/* Info */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1 flex-wrap">
                    <p className="text-sm font-medium text-ink-800 truncate max-w-xs">
                      {doc.originalName}
                    </p>
                    {doc.documentType && (
                      <span className="text-[11px] px-2 py-0.5 bg-ink-100 text-ink-500 rounded-full font-medium">
                        {DOC_TYPE_LABELS[doc.documentType] ?? doc.documentType}
                      </span>
                    )}
                  </div>
                  <div className="flex items-center gap-2 flex-wrap">
                    <StatusBadge status={doc.status} />
                    {doc.report && (
                      <SeverityBadge
                        severity={doc.report.riskLevel as any}
                        score={doc.report.overallRiskScore}
                        size="sm"
                      />
                    )}
                    <span className="text-xs text-ink-300">{formatDate(doc.createdAt)}</span>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-1 flex-shrink-0">
                  <button
                    onClick={e => handleDelete(e, doc.id)}
                    disabled={deleting === doc.id}
                    className="w-8 h-8 flex items-center justify-center rounded-lg text-ink-300
                               hover:bg-red-50 hover:text-red-500 transition-colors"
                  >
                    {deleting === doc.id
                      ? <span className="w-3.5 h-3.5 border-2 border-red-300 border-t-red-500 rounded-full animate-spin" />
                      : <Trash2 size={14} />
                    }
                  </button>
                  {doc.status === 'COMPLETED' && (
                    <ChevronRight size={16} className="text-ink-300" />
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function getGreeting() {
  const h = new Date().getHours()
  if (h < 12) return 'morning'
  if (h < 17) return 'afternoon'
  return 'evening'
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}