import { Link, useNavigate, useLocation } from 'react-router-dom'
import { Scale, Upload, LayoutDashboard, LogOut, User } from 'lucide-react'
import { useAuthStore } from '../../store/authStore'
import toast from 'react-hot-toast'

export default function Navbar() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = () => {
    logout()
    toast.success('Signed out successfully')
    navigate('/login')
  }

  const isActive = (path: string) => location.pathname === path

  return (
    <nav className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-ink-100">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">
        {/* Logo */}
        <Link to="/dashboard" className="flex items-center gap-2.5 group">
          <div className="w-8 h-8 bg-ink-800 rounded-lg flex items-center justify-center
                          group-hover:bg-ink-700 transition-colors">
            <Scale size={16} className="text-gold-400" />
          </div>
          <span className="font-serif text-lg font-semibold text-ink-800 tracking-tight">
            Vakil<span className="text-gold-600">Sahay</span>
          </span>
        </Link>

        {/* Nav links */}
        <div className="flex items-center gap-1">
          <Link
            to="/dashboard"
            className={`btn-ghost text-xs ${isActive('/dashboard') ? 'bg-ink-100 text-ink-800' : ''}`}
          >
            <LayoutDashboard size={15} />
            Dashboard
          </Link>
          <Link
            to="/upload"
            className={`btn-ghost text-xs ${isActive('/upload') ? 'bg-ink-100 text-ink-800' : ''}`}
          >
            <Upload size={15} />
            Analyze
          </Link>
        </div>

        {/* User menu */}
        <div className="flex items-center gap-3">
          <div className="hidden sm:flex items-center gap-2 text-xs text-ink-500">
            <div className="w-7 h-7 rounded-full bg-ink-800 flex items-center justify-center">
              <User size={13} className="text-ink-50" />
            </div>
            <span className="font-medium text-ink-700">{user?.fullName?.split(' ')[0]}</span>
          </div>
          <button onClick={handleLogout} className="btn-ghost text-xs text-ink-400 hover:text-red-600">
            <LogOut size={14} />
          </button>
        </div>
      </div>
    </nav>
  )
}