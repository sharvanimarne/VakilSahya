// ─── Auth ────────────────────────────────────────────────────────────────────
export interface UserSummary {
  id: number
  email: string
  fullName: string
  role: 'USER' | 'ADMIN'
  createdAt: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  user: UserSummary
}

// ─── Documents ───────────────────────────────────────────────────────────────
export type DocumentType   = 'RENTAL' | 'EMPLOYMENT' | 'LOAN' | 'NDA' | 'PARTNERSHIP' | 'GENERAL'
export type DocumentStatus = 'UPLOADED' | 'ANALYZING' | 'COMPLETED' | 'FAILED'
export type Severity       = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface DocumentResponse {
  id: number
  originalName: string
  fileType: string
  fileSize: number
  documentType: DocumentType | null
  status: DocumentStatus
  createdAt: string
  updatedAt: string
  report: AnalysisReportResponse | null
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

// ─── Analysis ─────────────────────────────────────────────────────────────────
export interface AnalysisReportResponse {
  id: number
  overallRiskScore: number
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  totalClauses: number
  criticalCount: number
  highCount: number
  mediumCount: number
  lowCount: number
  summary: string
  topConcerns: string[]
  recommendations: string[]
  createdAt: string
}

export interface ClauseResponse {
  id: number
  clauseNumber: number
  originalText: string
  plainEnglish: string
  clauseType: string
  severity: Severity
  severityScore: number
  lawReference: string
  recommendation: string
}

// ─── User profile ─────────────────────────────────────────────────────────────
export interface UserProfile {
  id: number
  email: string
  fullName: string
  role: string
  createdAt: string
  documentCount: number
  totalActions: number
}

// ─── Error ────────────────────────────────────────────────────────────────────
export interface ApiError {
  status: number
  error: string
  message: string
  timestamp: string
}