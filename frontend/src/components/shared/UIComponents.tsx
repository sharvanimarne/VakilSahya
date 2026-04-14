import type { Severity } from '../../types'

// ─── SeverityBadge ────────────────────────────────────────────────────────────
interface SeverityBadgeProps {
  severity: Severity
  score?: number
  size?: 'sm' | 'md'
}

const SEVERITY_CONFIG: Record<Severity, { label: string; dot: string; class: string }> = {
  LOW:      { label: 'Low Risk',      dot: 'bg-green-500',  class: 'severity-low'      },
  MEDIUM:   { label: 'Moderate',      dot: 'bg-amber-500',  class: 'severity-medium'   },
  HIGH:     { label: 'High Risk',     dot: 'bg-red-500',    class: 'severity-high'     },
  CRITICAL: { label: 'Critical',      dot: 'bg-purple-600', class: 'severity-critical' },
}

export function SeverityBadge({ severity, score, size = 'md' }: SeverityBadgeProps) {
  const cfg = SEVERITY_CONFIG[severity]
  return (
    <span className={`severity-badge ${cfg.class} ${size === 'sm' ? 'text-[11px] px-2 py-0.5' : ''}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${cfg.dot} flex-shrink-0`} />
      {cfg.label}
      {score !== undefined && <span className="opacity-60 font-normal ml-0.5">· {score}</span>}
    </span>
  )
}

// ─── RiskGauge ────────────────────────────────────────────────────────────────
interface RiskGaugeProps {
  score: number
  size?: number
}

export function RiskGauge({ score, size = 120 }: RiskGaugeProps) {
  const radius   = (size / 2) - 12
  const circ     = 2 * Math.PI * radius
  const arc      = circ * 0.75              // use 270° arc
  const offset   = arc - (arc * score / 100)
  const color    = score >= 76 ? '#7C3AED' : score >= 51 ? '#DC2626' : score >= 26 ? '#D97706' : '#16A34A'
  const label    = score >= 76 ? 'Critical' : score >= 51 ? 'High' : score >= 26 ? 'Moderate' : 'Low'

  return (
    <div className="flex flex-col items-center gap-1">
      <svg width={size} height={size * 0.85} viewBox={`0 0 ${size} ${size * 0.85}`}>
        {/* Track */}
        <circle
          cx={size / 2} cy={size / 2} r={radius}
          fill="none" stroke="#E5E5E0" strokeWidth="8"
          strokeDasharray={`${arc} ${circ}`}
          strokeDashoffset={-circ * 0.125}
          strokeLinecap="round"
          transform={`rotate(-225 ${size/2} ${size/2})`}
        />
        {/* Progress */}
        <circle
          cx={size / 2} cy={size / 2} r={radius}
          fill="none" stroke={color} strokeWidth="8"
          strokeDasharray={`${arc} ${circ}`}
          strokeDashoffset={offset + circ * 0.125}
          strokeLinecap="round"
          transform={`rotate(-225 ${size/2} ${size/2})`}
          style={{ transition: 'stroke-dashoffset 1s ease-out' }}
        />
        {/* Score */}
        <text x={size/2} y={size/2 - 2} textAnchor="middle" dominantBaseline="middle"
              fontSize={size * 0.2} fontWeight="600" fill={color} fontFamily="DM Sans, sans-serif">
          {score}
        </text>
        <text x={size/2} y={size/2 + size * 0.13} textAnchor="middle" dominantBaseline="middle"
              fontSize={size * 0.1} fill="#8E8776" fontFamily="DM Sans, sans-serif">
          / 100
        </text>
      </svg>
      <span className="text-xs font-medium" style={{ color }}>{label} Risk</span>
    </div>
  )
}

// ─── StatusBadge ─────────────────────────────────────────────────────────────
interface StatusBadgeProps { status: string }

export function StatusBadge({ status }: StatusBadgeProps) {
  const cfg: Record<string, string> = {
    UPLOADED:  'bg-blue-50 text-blue-700 border-blue-100',
    ANALYZING: 'bg-amber-50 text-amber-700 border-amber-100 animate-pulse',
    COMPLETED: 'bg-green-50 text-green-700 border-green-100',
    FAILED:    'bg-red-50 text-red-700 border-red-100',
  }
  return (
    <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-medium border ${cfg[status] ?? ''}`}>
      {status === 'ANALYZING' && <span className="w-1.5 h-1.5 rounded-full bg-amber-500 mr-1.5 animate-pulse" />}
      {status.charAt(0) + status.slice(1).toLowerCase()}
    </span>
  )
}

// ─── Skeleton ─────────────────────────────────────────────────────────────────
export function Skeleton({ className = '' }: { className?: string }) {
  return <div className={`animate-pulse bg-ink-100 rounded-lg ${className}`} />
}

// ─── EmptyState ───────────────────────────────────────────────────────────────
export function EmptyState({ icon, title, description, action }: {
  icon: React.ReactNode
  title: string
  description: string
  action?: React.ReactNode
}) {
  return (
    <div className="flex flex-col items-center justify-center py-20 px-6 text-center">
      <div className="w-14 h-14 bg-ink-100 rounded-2xl flex items-center justify-center mb-4 text-ink-400">
        {icon}
      </div>
      <h3 className="text-base font-semibold text-ink-700 mb-1">{title}</h3>
      <p className="text-sm text-ink-400 max-w-xs mb-5">{description}</p>
      {action}
    </div>
  )
}