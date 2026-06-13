import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/Login'
import RegisterPage from './pages/Register'
import ProjectsPage from './pages/ProjectsPage'
import ProjectDetailPage from './pages/ProjectDetailPage'
import Layout from './components/Layout'
import { useAuth } from './auth/AuthProvider'
import ExecutionDetailPage from './pages/ExecutionDetailPage'
import FunctionDetailPage from './pages/FunctionDetailPage'

function PrivateRoute({ children }: { children: JSX.Element }) {
  const { token } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route
        path="/"
        element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="/projects" replace />} />
        <Route path="projects" element={<ProjectsPage />} />
        <Route path="projects/:projectId" element={<ProjectDetailPage />} />
        <Route path="functions/:functionId" element={<FunctionDetailPage />} />
        <Route path="executions/:executionId" element={<ExecutionDetailPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/projects" replace />} />
    </Routes>
  )
}
