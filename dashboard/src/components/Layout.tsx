import React from 'react'
import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'
import { useQueryClient } from '@tanstack/react-query'

export default function Layout() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const qc = useQueryClient()

  const handleLogout = () => {
    logout()
    qc.clear()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <header className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <Link to="/projects" className="text-xl font-semibold">
            EventFlow
          </Link>
          <nav className="flex items-center gap-4">
            <Link to="/projects" className="text-sm text-gray-700 hover:underline">
              Projects
            </Link>
            <button
              onClick={handleLogout}
              className="text-sm text-red-600 hover:underline"
            >
              Logout
            </button>
          </nav>
        </div>
      </header>

      <main className="flex-1 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
