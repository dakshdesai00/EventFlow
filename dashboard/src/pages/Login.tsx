import React from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { authApi } from '../api/client'
import { useAuth } from '../auth/AuthProvider'
import { Link } from 'react-router-dom'
import LoadingSpinner from '../components/LoadingSpinner'

const schema = z.object({
  email: z.string().email().nonempty(),
  password: z.string().nonempty()
})

type FormData = z.infer<typeof schema>

export default function LoginPage() {
  const { login } = useAuth()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  async function onSubmit(values: FormData) {
    try {
      const res = await authApi.login(values)
      login(res.token)
    } catch (err: any) {
      alert(err?.response?.data?.message ?? 'Login failed')
    }
  }

  return (
    <div className="max-w-md mx-auto mt-16 bg-white p-6 rounded shadow">
      <h2 className="text-2xl font-semibold mb-4">Login</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="block text-sm font-medium">Email</label>
          <input {...register('email')} className="mt-1 w-full border rounded px-3 py-2" />
          {errors.email && <div className="text-red-600 text-sm mt-1">{errors.email.message}</div>}
        </div>
        <div>
          <label className="block text-sm font-medium">Password</label>
          <input type="password" {...register('password')} className="mt-1 w-full border rounded px-3 py-2" />
          {errors.password && <div className="text-red-600 text-sm mt-1">{errors.password.message}</div>}
        </div>
        <div className="flex items-center justify-between">
          <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded flex items-center gap-2" disabled={isSubmitting}>
            {isSubmitting ? <LoadingSpinner className="h-4 w-4" /> : 'Login'}
          </button>
          <Link to="/register" className="text-sm text-gray-600 hover:underline">Register</Link>
        </div>
      </form>
    </div>
  )
}
