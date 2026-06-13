import React, { createContext, useContext, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { API_TOKEN_KEY } from '../api/axios'
import type { ReactNode } from 'react'

interface AuthContextValue {
  token: string | null
  login: (token: string) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(() => {
    try {
      return localStorage.getItem(API_TOKEN_KEY)
    } catch {
      return null
    }
  })

  const navigate = useNavigate()

  useEffect(() => {
    try {
      if (token) {
        localStorage.setItem(API_TOKEN_KEY, token)
      } else {
        localStorage.removeItem(API_TOKEN_KEY)
      }
    } catch {
      // noop
    }
  }, [token])

  const login = (t: string) => {
    setToken(t)
    navigate('/projects')
  }

  const logout = () => {
    setToken(null)
    navigate('/login')
  }

  return (
    <AuthContext.Provider value={{ token, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
