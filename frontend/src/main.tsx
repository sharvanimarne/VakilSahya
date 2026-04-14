import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import App from './App'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            fontFamily: 'DM Sans, sans-serif',
            fontSize: '14px',
            background: '#252320',
            color: '#F7F6F3',
            borderRadius: '10px',
            padding: '12px 16px',
          },
          success: { iconTheme: { primary: '#16A34A', secondary: '#F7F6F3' } },
          error:   { iconTheme: { primary: '#DC2626', secondary: '#F7F6F3' } },
        }}
      />
    </BrowserRouter>
  </React.StrictMode>
)