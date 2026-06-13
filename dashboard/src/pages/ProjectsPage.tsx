import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { projectsApi } from '../api/client'
import { Link } from 'react-router-dom'
import Modal from '../components/Modal'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'

const createSchema = z.object({
  name: z.string().nonempty(),
  description: z.string().optional()
})

type CreateForm = z.infer<typeof createSchema>

export default function ProjectsPage() {
  const qc = useQueryClient()
  const { data: projects, isLoading } = useQuery(['projects'], () => projectsApi.list())
  const createMutation = useMutation((payload: CreateForm) => projectsApi.create(payload), {
    onSuccess: () => qc.invalidateQueries(['projects'])
  })
  const updateMutation = useMutation(({ id, payload }: { id: number; payload: CreateForm }) => projectsApi.update(id, payload), {
    onSuccess: () => qc.invalidateQueries(['projects'])
  })
  const deleteMutation = useMutation((id: number) => projectsApi.delete(id), {
    onSuccess: () => qc.invalidateQueries(['projects'])
  })

  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<{ id: number; name: string; description?: string } | null>(null)

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<CreateForm>({
    resolver: zodResolver(createSchema)
  })

  function openCreate() {
    setEditing(null)
    reset()
    setOpen(true)
  }

  function openEdit(p: any) {
    setEditing(p)
    reset({ name: p.name, description: p.description ?? '' })
    setOpen(true)
  }

  async function onSubmit(values: CreateForm) {
    try {
      if (editing) {
        await updateMutation.mutateAsync({ id: editing.id, payload: values })
      } else {
        await createMutation.mutateAsync(values)
      }
      setOpen(false)
    } catch (err: any) {
      alert(err?.response?.data?.message ?? 'Save failed')
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-semibold">Projects</h1>
        <div className="flex items-center gap-2">
          <button onClick={openCreate} className="px-3 py-2 bg-blue-600 text-white rounded">New Project</button>
        </div>
      </div>

      <div className="bg-white rounded shadow p-4">
        {isLoading ? (
          <div>Loading…</div>
        ) : (
          <ul className="space-y-3">
            {projects && projects.length ? projects.map((p) => (
              <li key={p.id} className="flex items-center justify-between p-3 border rounded">
                <div>
                  <Link to={`/projects/${p.id}`} className="text-lg font-medium text-blue-700 hover:underline">{p.name}</Link>
                  <div className="text-sm text-gray-600">{p.description}</div>
                </div>
                <div className="flex items-center gap-2">
                  <button onClick={() => openEdit(p)} className="text-sm px-2 py-1 border rounded">Edit</button>
                  <button onClick={() => {
                    if (!confirm('Delete project?')) return
                    deleteMutation.mutate(p.id)
                  }} className="text-sm px-2 py-1 border rounded text-red-600">Delete</button>
                </div>
              </li>
            )) : (
              <div className="text-gray-600">No projects yet</div>
            )}
          </ul>
        )}
      </div>

      {open && (
        <Modal onClose={() => setOpen(false)} title={editing ? 'Edit Project' : 'Create Project'}>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium">Name</label>
              <input className="mt-1 w-full border rounded px-3 py-2" {...register('name')} />
              {errors.name && <div className="text-red-600 text-sm mt-1">{errors.name.message}</div>}
            </div>
            <div>
              <label className="block text-sm font-medium">Description</label>
              <textarea className="mt-1 w-full border rounded px-3 py-2" {...register('description')} />
            </div>
            <div className="text-right">
              <button type="submit" className="px-3 py-2 bg-green-600 text-white rounded" disabled={isSubmitting}>
                Save
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
