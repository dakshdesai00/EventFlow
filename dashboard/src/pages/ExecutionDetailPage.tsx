import React from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { executionsApi } from '../api/client'

function statusColor(status: string) {
  switch (status) {
    case 'SUCCESS': return 'text-green-700'
    case 'RUNNING': return 'text-blue-600'
    case 'PENDING': return 'text-gray-600'
    case 'FAILED': return 'text-red-600'
    case 'FAILED_PERMANENTLY': return 'text-red-800'
    default: return 'text-gray-700'
  }
}

function logColor(level: string) {
  switch (level) {
    case 'ERROR': return 'text-red-600'
    case 'WARN': return 'text-yellow-600'
    case 'DEBUG': return 'text-blue-600'
    case 'INFO':
    default:
      return 'text-gray-700'
  }
}

export default function ExecutionDetailPage() {
  const { executionId } = useParams<{ executionId: string }>()
  const id = Number(executionId)
  const { data, isLoading } = useQuery(['execution', id], () => executionsApi.get(id), { enabled: !!id })
  const { data: logs } = useQuery(['execution-logs', id], () => executionsApi.logs(id), { enabled: !!id })

  if (isLoading) return <div>Loading...</div>
  if (!data) return <div>Execution not found</div>

  return (
    <div className="bg-white p-4 rounded shadow">
      <h2 className="text-xl font-semibold mb-2">Execution #{data.id}</h2>
      <div className="space-y-2 text-sm">
        <div><strong>Status:</strong> <span className={`${statusColor(data.status)} font-mono`}>{data.status}</span></div>
        <div><strong>Function ID:</strong> {data.functionId}</div>
        <div><strong>Event ID:</strong> {data.eventId}</div>
        <div><strong>Started At:</strong> {data.startedAt ?? '—'}</div>
        <div><strong>Ended At:</strong> {data.endedAt ?? '—'}</div>
        <div><strong>Duration (ms):</strong> {data.durationMs ?? '—'}</div>
        <div><strong>Error Message:</strong> <pre className="bg-gray-100 p-2 rounded">{data.errorMessage ?? 'No error'}</pre></div>
      </div>

      <div className="mt-4">
        <h3 className="font-medium mb-2">Logs</h3>
        <div className="space-y-2 max-h-80 overflow-auto">
          {(logs ?? []).map((l) => (
            <div key={l.id} className="p-2 border rounded">
              <div className={`text-xs ${logColor(l.level)} font-semibold`}>{l.level} — {l.timestamp}</div>
              <div className="text-sm font-mono">{l.message}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
