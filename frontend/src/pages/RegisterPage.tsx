import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { Scale, Eye, EyeOff, ArrowRight } from 'lucide-react'
import toast from 'react-hot-toast'
import { authApi, getErrorMessage } from '../api'
import { useAuthStore } from '../store/authStore'

interface FormValues { fullName: string; email: string; password: string; confirmPassword: string }

export default function RegisterPage() {
  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm<FormValues>()
  const [showPw, setShowPw] = useState(false)
  const { setAuth } = useAuthStore()
  const navigate = useNavigate()
  const password = watch('password')

  const onSubmit = async (data: FormValues) => {
    try {
      const res = await authApi.register(data.email, data.fullName, data.password)
      setAuth(res.data.token, res.data.user)
      toast.success('Account created! Welcome to VakilSahay.')
      navigate('/dashboard')
    } catch (err) {
      toast.error(getErrorMessage(err))
    }
  }

  return (
    <div className="min-h-screen bg-ink-50 flex items-center justify-center p-6">
      <div className="w-full max-w-[420px] animate-fade-up">
        <div className="flex items-center gap-2 mb-8">
          <div className="w-8 h-8 bg-ink-800 rounded-lg flex items-center justify-center">
            <Scale size={15} className="text-gold-400" />
          </div>
          <span className="font-serif text-xl font-semibold text-ink-800">
            Vakil<span className="text-gold-600">Sahay</span>
          </span>
        </div>

        <h1 className="text-2xl font-serif font-semibold text-ink-800 mb-1">Create your account</h1>
        <p className="text-sm text-ink-400 mb-8">Start understanding what you sign — free.</p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-ink-600 mb-1.5">Full name</label>
            <input
              type="text" placeholder="Priya Sharma"
              className={`input-field ${errors.fullName ? 'border-red-300' : ''}`}
              {...register('fullName', {
                required: 'Full name is required',
                minLength: { value: 2, message: 'Name too short' }
              })}
            />
            {errors.fullName && <p className="text-xs text-red-500 mt-1">{errors.fullName.message}</p>}
          </div>

          <div>
            <label className="block text-xs font-medium text-ink-600 mb-1.5">Email address</label>
            <input
              type="email" placeholder="you@example.com"
              className={`input-field ${errors.email ? 'border-red-300' : ''}`}
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
                type={showPw ? 'text' : 'password'} placeholder="Min. 8 characters"
                className={`input-field pr-10 ${errors.password ? 'border-red-300' : ''}`}
                {...register('password', {
                  required: 'Password is required',
                  minLength: { value: 8, message: 'Minimum 8 characters' }
                })}
              />
              <button type="button" onClick={() => setShowPw(v => !v)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-400 hover:text-ink-600">
                {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
            {errors.password && <p className="text-xs text-red-500 mt-1">{errors.password.message}</p>}
          </div>

          <div>
            <label className="block text-xs font-medium text-ink-600 mb-1.5">Confirm password</label>
            <input
              type="password" placeholder="••••••••"
              className={`input-field ${errors.confirmPassword ? 'border-red-300' : ''}`}
              {...register('confirmPassword', {
                required: 'Please confirm your password',
                validate: val => val === password || 'Passwords do not match'
              })}
            />
            {errors.confirmPassword && <p className="text-xs text-red-500 mt-1">{errors.confirmPassword.message}</p>}
          </div>

          <button type="submit" disabled={isSubmitting} className="btn-primary w-full justify-center py-3 mt-2">
            {isSubmitting
              ? <span className="flex items-center gap-2"><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Creating account…</span>
              : <span className="flex items-center gap-2">Create account <ArrowRight size={15} /></span>
            }
          </button>
        </form>

        <p className="text-xs text-center text-ink-400 mt-6">
          Already have an account?{' '}
          <Link to="/login" className="text-ink-700 font-medium hover:underline">Sign in</Link>
        </p>
        <p className="text-xs text-center text-ink-300 mt-3">
          © 2025 VakilSahay · Proprietary algorithm protected under Indian Copyright Act 1957
        </p>
      </div>
    </div>
  )
}