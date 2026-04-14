import { create } from 'zustand'
import type { UserSummary } from '../types'

interface AuthState {
  token: string | null
  user:  UserSummary | null
  setAuth: (token: string, user: UserSummary) => void
  logout: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>((set, get) => ({
  // Persist token in sessionStorage (safer than localStorage for JWTs)
  token: sessionStorage.getItem('vs_token'),
  user:  (() => {
    try { return JSON.parse(sessionStorage.getItem('vs_user') ?? 'null') }
    catch { return null }
  })(),

  setAuth: (token, user) => {
    sessionStorage.setItem('vs_token', token)
    sessionStorage.setItem('vs_user', JSON.stringify(user))
    set({ token, user })
  },

  logout: () => {
    sessionStorage.removeItem('vs_token')
    sessionStorage.removeItem('vs_user')
    set({ token: null, user: null })
  },

  isAuthenticated: () => !!get().token,
}))