// import axios, { AxiosError } from 'axios'
// import toast from 'react-hot-toast'
// import type {
//   AuthResponse, DocumentResponse, PageResponse,
//   AnalysisReportResponse, ClauseResponse, UserProfile, Severity
// } from '../types'
//
// // ─── Axios instance ───────────────────────────────────────────────────────────
// const api = axios.create({
//   baseURL: '/api',
//   headers: { 'Content-Type': 'application/json' },
// })
//
// // Attach JWT token to every request
// api.interceptors.request.use(config => {
//   const token = useAuthStore.getState().token
//   if (token) config.headers.Authorization = `Bearer ${token}`
//   return config
// })
//
// // Handle 401 globally — clear auth and redirect
// api.interceptors.response.use(
//   res => res,
//   (error: AxiosError) => {
//     if (error.response?.status === 401) {
//       useAuthStore.getState().logout()
//       window.location.href = '/login'
//     }
//     return Promise.reject(error)
//   }
// )
//
// // Lazy import to break circular dependency
// import { useAuthStore } from '../store/authStore'
//
// // ─── Auth ─────────────────────────────────────────────────────────────────────
// export const authApi = {
//   register: (email: string, fullName: string, password: string) =>
//     api.post<AuthResponse>('/auth/register', { email, fullName, password }),
//
//   login: (email: string, password: string) =>
//     api.post<AuthResponse>('/auth/login', { email, password }),
// }
//
// // ─── Documents ────────────────────────────────────────────────────────────────
// export const documentsApi = {
//   upload: (file: File, onProgress?: (pct: number) => void) => {
//     const form = new FormData()
//     form.append('file', file)
//     return api.post<DocumentResponse>('/documents/upload', form, {
//       headers: { 'Content-Type': 'multipart/form-data' },
//       onUploadProgress: e => {
//         if (onProgress && e.total) onProgress(Math.round((e.loaded * 100) / e.total))
//       }
//     })
//   },
//
//   list: (page = 0, size = 10) =>
//     api.get<PageResponse<DocumentResponse>>('/documents', { params: { page, size } }),
//
//   get: (id: number) =>
//     api.get<DocumentResponse>(`/documents/${id}`),
//
//   getClauses: (id: number, severity?: Severity) =>
//     api.get<ClauseResponse[]>(`/documents/${id}/clauses`, {
//       params: severity ? { severity } : {}
//     }),
//
//   getReport: (id: number) =>
//     api.get<AnalysisReportResponse>(`/documents/${id}/report`),
//
//   reanalyze: (id: number) =>
//     api.put<DocumentResponse>(`/documents/${id}/reanalyze`),
//
//   delete: (id: number) =>
//     api.delete(`/documents/${id}`),
// }
//
// // ─── Users ────────────────────────────────────────────────────────────────────
// export const usersApi = {
//   me: () => api.get<UserProfile>('/users/me'),
// }
//
// // ─── Error helper ─────────────────────────────────────────────────────────────
// export const getErrorMessage = (error: unknown): string => {
//   if (axios.isAxiosError(error)) {
//     return error.response?.data?.message ?? error.message
//   }
//   return 'An unexpected error occurred'
// }
import axios, { AxiosError } from 'axios'
import { useAuthStore } from '../store/authStore'
import type {
  AuthResponse, DocumentResponse, PageResponse,
  AnalysisReportResponse, ClauseResponse, UserProfile, Severity
} from '../types'

// ─── Axios instance ───────────────────────────────────────────────────────────
const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Attach JWT token to every request
api.interceptors.request.use(config => {
  const token = useAuthStore.getState().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Handle 401 globally — clear auth and redirect
api.interceptors.response.use(
  res => res,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// ─── Auth ─────────────────────────────────────────────────────────────────────
export const authApi = {
  register: (email: string, fullName: string, password: string) =>
    api.post<AuthResponse>('/auth/register', { email, fullName, password }),

  login: (email: string, password: string) =>
    api.post<AuthResponse>('/auth/login', { email, password }),
}

// ─── Documents ────────────────────────────────────────────────────────────────
export const documentsApi = {
  upload: (file: File, onProgress?: (pct: number) => void) => {
    const form = new FormData()
    form.append('file', file)
    return api.post<DocumentResponse>('/documents/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: e => {
        if (onProgress && e.total) onProgress(Math.round((e.loaded * 100) / e.total))
      }
    })
  },

  list: (page = 0, size = 10) =>
    api.get<PageResponse<DocumentResponse>>('/documents', { params: { page, size } }),

  get: (id: number) =>
    api.get<DocumentResponse>(`/documents/${id}`),

  getClauses: (id: number, severity?: Severity) =>
    api.get<ClauseResponse[]>(`/documents/${id}/clauses`, {
      params: severity ? { severity } : {}
    }),

  getReport: (id: number) =>
    api.get<AnalysisReportResponse>(`/documents/${id}/report`),

  reanalyze: (id: number) =>
    api.put<DocumentResponse>(`/documents/${id}/reanalyze`),

  delete: (id: number) =>
    api.delete(`/documents/${id}`),
}

// ─── Users ────────────────────────────────────────────────────────────────────
export const usersApi = {
  me: () => api.get<UserProfile>('/users/me'),
}

// ─── Error helper ─────────────────────────────────────────────────────────────
export const getErrorMessage = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? error.message
  }
  return 'An unexpected error occurred'
}