import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { Scale, Eye, EyeOff, ArrowRight } from 'lucide-react'
import toast from 'react-hot-toast'
import { authApi } from '../api'
import { useAuthStore } from '../store/authStore'

interface FormValues { email: string; password: string }

export default function LoginPage() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>()
  const [showPw, setShowPw] = useState(false)
  const { setAuth } = useAuthStore()
  const navigate = useNavigate()

  const onSubmit = async (data: FormValues) => {
    try {
      const res = await authApi.login(data.email, data.password)
      setAuth(res.data.token, res.data.user)
      toast.success(`Welcome back, ${res.data.user.fullName.split(' ')[0]}!`)
      navigate('/dashboard')
    } catch (err: any) {
      const status = err?.response?.status
      const message = err?.response?.data?.message

      if (status === 401 || status === 403) {
        toast.error('Incorrect email or password. No account? Create one below.')
      } else if (status === 404) {
        toast.error('No account found with this email. Please register first.')
      } else if (status === 400) {
        toast.error(message || 'Invalid email or password.')
      } else {
        toast.error(message || 'Something went wrong. Please try again.')
      }
    }
  }

  return (
    <div className="min-h-screen bg-ink-50 flex">
      {/* Left panel */}
      <div className="hidden lg:flex w-1/2 bg-ink-900 flex-col justify-between p-12">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 bg-gold-500/20 border border-gold-500/30 rounded-lg flex items-center justify-center">
            <Scale size={16} className="text-gold-400" />
          </div>
          <span className="font-serif text-xl font-semibold text-ink-50 tracking-tight">
            Vakil<span className="text-gold-400">Sahay</span>
          </span>
        </div>
        <div>
          <blockquote className="text-2xl font-serif text-ink-100 leading-relaxed mb-6">
            "Most Indians sign agreements they don't understand.<br />
            <span className="text-gold-400">We're changing that.</span>"
          </blockquote>
          <p className="text-sm text-ink-500">
            Proprietary clause-severity scoring · Indian law references · Plain English explanations
          </p>
        </div>
        <p className="text-xs text-ink-700">© 2025 VakilSahay. All rights reserved.</p>
      </div>

      {/* Right panel */}
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="w-full max-w-[400px] animate-fade-up">
          {/* Mobile logo */}
          <div className="flex items-center gap-2 mb-8 lg:hidden">
            <Scale size={20} className="text-ink-600" />
            <span className="font-serif text-xl font-semibold text-ink-800">
              Vakil<span className="text-gold-600">Sahay</span>
            </span>
          </div>

          <h1 className="text-2xl font-serif font-semibold text-ink-800 mb-1">Welcome back</h1>
          <p className="text-sm text-ink-400 mb-8">Sign in to your account</p>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-ink-600 mb-1.5">Email address</label>
              <input
                type="email"
                placeholder="you@example.com"
                className={`input-field ${errors.email ? 'border-red-300 focus:border-red-400' : ''}`}
                {...register('email', {
                  required: 'Email is required',
                  pattern: { value: /\S+@\S+\.\S+/, message: 'Invalid email' }
                })}
              />
              {errors.email && <p className="text-xs text-red-500 mt-1">{errors.email.message}</p>}
            </div>

            <div>
              <label className="block text-xs font-medium text-ink-600 mb-1.5">Password</label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  placeholder="••••••••"
                  className={`input-field pr-10 ${errors.password ? 'border-red-300' : ''}`}
                  {...register('password', { required: 'Password is required' })}
                />
                <button type="button" onClick={() => setShowPw(v => !v)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-400 hover:text-ink-600">
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <p className="text-xs text-red-500 mt-1">{errors.password.message}</p>}
            </div>

            <button type="submit" disabled={isSubmitting} className="btn-primary w-full justify-center py-3 mt-2">
              {isSubmitting
                ? <span className="flex items-center gap-2"><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Signing in…</span>
                : <span className="flex items-center gap-2">Sign in <ArrowRight size={15} /></span>
              }
            </button>
          </form>

          <p className="text-sm text-center text-ink-400 mt-6">
            No account?{' '}
            <Link to="/register" className="text-ink-700 font-medium hover:underline">
              Create one free
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}