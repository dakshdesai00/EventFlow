import React, { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { functionsApi, envVarApi, secretVarApi, executionsApi } from '../api/client'
import type { FunctionResponse, EnvironmentVariableResponse, ExecutionResponse } from '../api/types'
import Modal from '../components/Modal'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import CodeEditorModal from '../components/CodeEditorModal'

const envSchema = z.object({ key: z.string().nonempty(), value: z.string().nonempty() })

type EnvForm = z.infer<typeof envSchema>

export default function FunctionDetailPage() {
  const { functionId } = useParams<{ functionId: string }>()
  const fid = Number(functionId)
  const qc = useQueryClient()

  const { data: fn, isLoading } = useQuery(['function', fid], () => functionsApi.get(fid), { enabled: !!fid })
  const { data: versions } = useQuery(['versions', fid], () => functionsApi.listVersions(fid), { enabled: !!fid })

  const { data: envVars } = useQuery(['fnEnv', fid], () => envVarApi.listForFunction(fid), { enabled: !!fid })
  const createEnv = useMutation((payload: EnvForm) => envVarApi.createForFunction(fid, payload), { onSuccess: () => qc.invalidateQueries(['fnEnv', fid]) })
  const deleteEnv = useMutation((id: number) => envVarApi.deleteFunctionVar(fid, id), { onSuccess: () => qc.invalidateQueries(['fnEnv', fid]) })

  const { data: secretVars } = useQuery(['fnSecrets', fid], () => secretVarApi.listForFunction(fid), { enabled: !!fid })
  const createSecret = useMutation((payload: EnvForm) => secretVarApi.createForFunction(fid, payload), { onSuccess: () => qc.invalidateQueries(['fnSecrets', fid]) })
  const deleteSecret = useMutation((id: number) => secretVarApi.deleteFunctionVar(fid, id), { onSuccess: () => qc.invalidateQueries(['fnSecrets', fid]) })

  const { data: executions } = useQuery<ExecutionResponse[]>(['fnExecs', fid], () => executionsApi.listForFunction(fid), { enabled: !!fid })

  const [envOpen, setEnvOpen] = useState(false)
  const { register, handleSubmit, reset, formState: { errors } } = useForm<EnvForm>({ resolver: zodResolver(envSchema) })

  const [codeModal, setCodeModal] = useState(false)

  async function onCreateEnv(values: EnvForm) {
    try {
      await createEnv.mutateAsync(values)
      setEnvOpen(false)
    } catch (err:any) { alert(err?.response?.data ?? String(err)) }
  }

  async function onCreateSecret(values: EnvForm) {
    try {
      await createSecret.mutateAsync(values)
      setEnvOpen(false)
    } catch (err:any) { alert(err?.response?.data ?? String(err)) }
  }

  if (isLoading) return <div>Loading...</div>
  if (!fn) return <div>Function not found</div>

  return (
    <div className="bg-white p-4 rounded shadow">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="text-xl font-semibold">{fn.name}</h2>
          <div className="text-sm text-gray-600">{fn.description}</div>
          <div className="text-xs text-gray-500">timeout: {fn.timeoutMs}ms · memory: {fn.memoryLimitMb}MB · cache: {fn.cacheEnabled ? `yes (ttl ${fn.cacheTtlSeconds ?? 'n/a'})` : 'no'}</div>
          <div className="text-xs text-gray-500">activeVersionId: {fn.activeVersionId ?? '—'}</div>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={() => setCodeModal(true)} className="px-3 py-2 border rounded">Manage Versions / Edit</button>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-6">
        <div>
          <h4 className="font-medium mb-2">Versions</h4>
          <ul className="space-y-2">
            {(versions ?? []).map(v => (
              <li key={v.id} className="p-2 border rounded">
                <div>v{v.versionNumber} — {v.fileSizeBytes} bytes</div>
              </li>
            ))}
          </ul>
        </div>

        <div>
          <h4 className="font-medium mb-2">Environment Variables</h4>
          <button onClick={() => { reset(); setEnvOpen(true) }} className="px-2 py-1 bg-blue-600 text-white rounded text-sm mb-2">New</button>
          <ul className="space-y-2">
            {(envVars ?? []).map(v => (
              <li key={v.id} className="p-2 border rounded flex items-center justify-between">
                <div>
                  <div className="font-mono text-sm">{v.key}</div>
                  <div className="text-sm text-gray-600">{v.value}</div>
                </div>
                <div>
                  <button onClick={() => deleteEnv.mutate(v.id)} className="px-2 py-1 border rounded text-sm text-red-600">Delete</button>
                </div>
              </li>
            ))}
          </ul>
        </div>

        <div>
          <h4 className="font-medium mb-2">Secret Variables</h4>
          <button onClick={() => { reset(); setEnvOpen(true) }} className="px-2 py-1 bg-blue-600 text-white rounded text-sm mb-2">New</button>
          <ul className="space-y-2">
            {(secretVars ?? []).map(v => (
              <li key={v.id} className="p-2 border rounded flex items-center justify-between">
                <div>
                  <div className="font-mono text-sm">{v.key}</div>
                  <div className="text-sm text-gray-600">***</div>
                </div>
                <div>
                  <button onClick={() => deleteSecret.mutate(v.id)} className="px-2 py-1 border rounded text-sm text-red-600">Delete</button>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="mt-6">
        <h4 className="font-medium mb-2">Executions</h4>
        <ul className="space-y-2">
          {(executions ?? []).map(ex => (
            <li key={ex.id} className="p-2 border rounded flex items-center justify-between">
              <div>
                <div>Execution #{ex.id} — <span className="font-mono">{ex.status}</span></div>
                <div className="text-sm text-gray-600">Duration: {ex.durationMs ?? '—'}ms</div>
              </div>
              <div>
                <Link to={`/executions/${ex.id}`} className="px-2 py-1 border rounded text-sm">View</Link>
              </div>
            </li>
          ))}
        </ul>
      </div>

      {envOpen && (
        <Modal onClose={() => setEnvOpen(false)} title="Create Variable">
          <form onSubmit={(e) => { e.preventDefault(); handleSubmit(async (vals: EnvForm) => { try { await onCreateEnv(vals) } catch (err:any) { alert(err?.response?.data ?? String(err)) } })(); }} className="space-y-3">
            <div>
              <label className="block text-sm font-medium">Key</label>
              <input {...register('key')} className="mt-1 w-full border rounded px-3 py-2" />
              {errors.key && <div className="text-red-600 text-sm mt-1">{String(errors.key?.message)}</div>}
            </div>
            <div>
              <label className="block text-sm font-medium">Value</label>
              <input {...register('value')} className="mt-1 w-full border rounded px-3 py-2" />
              {errors.value && <div className="text-red-600 text-sm mt-1">{String(errors.value?.message)}</div>}
            </div>
            <div className="text-right">
              <button type="submit" className="px-3 py-2 bg-green-600 text-white rounded">Create</button>
            </div>
          </form>
        </Modal>
      )}

      {codeModal && <CodeEditorModal functionId={fid} name={fn.name} onClose={() => { setCodeModal(false); qc.invalidateQueries(['versions', fid]) }} />}
    </div>
  )
}
