import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDropzone } from 'react-dropzone'
import { Upload, FileText, CheckCircle, X, ArrowLeft } from 'lucide-react'
import toast from 'react-hot-toast'
import { Link } from 'react-router-dom'
import Navbar from '../components/shared/Navbar'
import { documentsApi, getErrorMessage } from '../api'

type Phase = 'idle' | 'uploading' | 'processing' | 'done'

export default function UploadPage() {
  const navigate = useNavigate()
  const [file,     setFile]     = useState<File | null>(null)
  const [phase,    setPhase]    = useState<Phase>('idle')
  const [progress, setProgress] = useState(0)
  const [docId,    setDocId]    = useState<number | null>(null)

  const onDrop = useCallback((accepted: File[]) => {
    if (accepted.length === 0) return
    setFile(accepted[0])
    setPhase('idle')
    setProgress(0)
  }, [])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/pdf': ['.pdf'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'text/plain': ['.txt'],
    },
    maxFiles: 1,
    maxSize: 10 * 1024 * 1024,
    onDropRejected: (rejections) => {
      const err = rejections[0]?.errors[0]?.message ?? 'File rejected'
      toast.error(err)
    }
  })

  const handleUpload = async () => {
    if (!file) return
    setPhase('uploading')
    setProgress(0)

    try {
      const res = await documentsApi.upload(file, pct => setProgress(pct))
      setDocId(res.data.id)
      setPhase('processing')

      // Poll until analysis is done
      const interval = setInterval(async () => {
        try {
          const docRes = await documentsApi.get(res.data.id)
          if (docRes.data.status === 'COMPLETED') {
            clearInterval(interval)
            setPhase('done')
            toast.success('Analysis complete!')
          } else if (docRes.data.status === 'FAILED') {
            clearInterval(interval)
            toast.error('Analysis failed. Please try again.')
            setPhase('idle')
          }
        } catch { clearInterval(interval) }
      }, 2000)
    } catch (err) {
      toast.error(getErrorMessage(err))
      setPhase('idle')
    }
  }

  const formatSize = (bytes: number) =>
    bytes < 1024 * 1024
      ? `${(bytes / 1024).toFixed(0)} KB`
      : `${(bytes / 1024 / 1024).toFixed(1)} MB`

  return (
    <div className="min-h-screen bg-ink-50">
      <Navbar />

      <div className="max-w-2xl mx-auto px-4 sm:px-6 py-10">
        {/* Back */}
        <Link to="/dashboard" className="btn-ghost text-sm mb-6 inline-flex -ml-2">
          <ArrowLeft size={15} /> Dashboard
        </Link>

        <h1 className="text-2xl font-serif font-semibold text-ink-800 mb-1 animate-fade-up">
          Analyze a document
        </h1>
        <p className="text-sm text-ink-400 mb-8 animate-fade-up stagger-1">
          Upload a PDF, DOCX, or TXT file. We'll decode every clause and flag risks for you.
        </p>

        {/* Drop zone */}
        {phase === 'idle' || phase === 'uploading' ? (
          <>
            <div
              {...getRootProps()}
              className={`relative border-2 border-dashed rounded-2xl p-12 text-center
                          transition-all duration-200 cursor-pointer animate-fade-up stagger-2
                          ${isDragActive
                            ? 'border-ink-400 bg-ink-100'
                            : file
                            ? 'border-green-300 bg-green-50'
                            : 'border-ink-200 bg-white hover:border-ink-300 hover:bg-ink-50'}`}
            >
              <input {...getInputProps()} />
              <div className="flex flex-col items-center gap-3">
                <div className={`w-16 h-16 rounded-2xl flex items-center justify-center transition-colors
                                 ${isDragActive ? 'bg-ink-800' : file ? 'bg-green-100' : 'bg-ink-100'}`}>
                  {file
                    ? <FileText size={28} className="text-green-600" />
                    : <Upload size={28} className={isDragActive ? 'text-ink-50' : 'text-ink-400'} />
                  }
                </div>
                {file ? (
                  <>
                    <p className="text-sm font-semibold text-ink-800">{file.name}</p>
                    <p className="text-xs text-ink-400">{formatSize(file.size)}</p>
                    <p className="text-xs text-ink-300">Drop another file to replace</p>
                  </>
                ) : (
                  <>
                    <p className="text-sm font-medium text-ink-600">
                      {isDragActive ? 'Drop it here…' : 'Drop your document here'}
                    </p>
                    <p className="text-xs text-ink-400">PDF, DOCX, or TXT · Max 10 MB</p>
                  </>
                )}
              </div>
            </div>

            {/* Progress bar */}
            {phase === 'uploading' && (
              <div className="mt-4">
                <div className="flex justify-between text-xs text-ink-400 mb-1.5">
                  <span>Uploading…</span>
                  <span>{progress}%</span>
                </div>
                <div className="h-1.5 bg-ink-100 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-ink-800 rounded-full transition-all duration-300"
                    style={{ width: `${progress}%` }}
                  />
                </div>
              </div>
            )}

            {/* CTA */}
            {file && phase === 'idle' && (
              <button
                onClick={handleUpload}
                className="btn-primary w-full justify-center py-3.5 mt-4 text-base animate-fade-up"
              >
                <Upload size={16} />
                Analyze this document
              </button>
            )}
          </>
        ) : phase === 'processing' ? (
          /* Processing state */
          <div className="card p-10 text-center animate-fade-in">
            <div className="relative w-20 h-20 mx-auto mb-6">
              <div className="w-20 h-20 rounded-full border-4 border-ink-100" />
              <div className="absolute inset-0 w-20 h-20 rounded-full border-4 border-t-ink-800
                              border-r-transparent border-b-transparent border-l-transparent
                              animate-spin" />
              <FileText size={22} className="absolute inset-0 m-auto text-ink-500" />
            </div>
            <h2 className="text-lg font-semibold text-ink-800 mb-2">Analyzing your document…</h2>
            <p className="text-sm text-ink-400 max-w-sm mx-auto">
              VakilSahay is reading every clause, scoring severity, and generating plain-English
              explanations. This takes 10–30 seconds.
            </p>
            <div className="flex justify-center gap-1 mt-6">
              {[0, 1, 2].map(i => (
                <span key={i} className="w-2 h-2 rounded-full bg-ink-300 animate-bounce"
                      style={{ animationDelay: `${i * 150}ms` }} />
              ))}
            </div>
          </div>
        ) : (
          /* Done state */
          <div className="card p-10 text-center animate-fade-in">
            <div className="w-20 h-20 bg-green-50 rounded-full flex items-center justify-center mx-auto mb-6">
              <CheckCircle size={36} className="text-green-500" />
            </div>
            <h2 className="text-xl font-serif font-semibold text-ink-800 mb-2">Analysis complete</h2>
            <p className="text-sm text-ink-400 mb-8">
              Your document has been analyzed. View the full clause-by-clause report below.
            </p>
            <div className="flex gap-3 justify-center">
              <button
                onClick={() => navigate(`/documents/${docId}`)}
                className="btn-primary px-8 py-3"
              >
                View report →
              </button>
              <button
                onClick={() => { setFile(null); setPhase('idle'); setProgress(0) }}
                className="btn-secondary px-6 py-3"
              >
                Analyze another
              </button>
            </div>
          </div>
        )}

        {/* Supported types info */}
        {phase === 'idle' && (
          <div className="mt-8 grid grid-cols-3 gap-3 animate-fade-up stagger-4">
            {['Rental agreement', 'Employment contract', 'Loan agreement'].map(t => (
              <div key={t} className="text-center py-3 px-2 bg-white border border-ink-100 rounded-xl">
                <p className="text-xs text-ink-500 font-medium">{t}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}