/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['DM Sans', 'system-ui', 'sans-serif'],
        serif: ['Playfair Display', 'Georgia', 'serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      colors: {
        ink: {
          50:  '#F7F6F3',
          100: '#EDEBE4',
          200: '#D9D5C8',
          300: '#B8B2A0',
          400: '#8E8776',
          500: '#6B6458',
          600: '#524D44',
          700: '#3A3730',
          800: '#252320',
          900: '#131210',
        },
        gold: {
          50:  '#FFFBEB',
          100: '#FEF3C7',
          200: '#FDE68A',
          300: '#FCD34D',
          400: '#FBBF24',
          500: '#F59E0B',
          600: '#D97706',
          700: '#B45309',
        },
        risk: {
          low:      '#16A34A',
          medium:   '#D97706',
          high:     '#DC2626',
          critical: '#7C3AED',
        }
      },
      animation: {
        'fade-up':    'fadeUp 0.4s ease-out forwards',
        'fade-in':    'fadeIn 0.3s ease-out forwards',
        'pulse-slow': 'pulse 3s ease-in-out infinite',
        'spin-slow':  'spin 8s linear infinite',
      },
      keyframes: {
        fadeUp:  { '0%': { opacity: 0, transform: 'translateY(16px)' }, '100%': { opacity: 1, transform: 'translateY(0)' } },
        fadeIn:  { '0%': { opacity: 0 }, '100%': { opacity: 1 } },
      }
    },
  },
  plugins: [],
}