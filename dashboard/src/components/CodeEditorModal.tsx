import React, { useEffect, useState } from 'react'
import Modal from './Modal'
import { functionsApi } from '../api/client'
import LoadingSpinner from './LoadingSpinner'
import Editor from 'react-simple-code-editor'
import Prism from 'prismjs'
import 'prismjs/components/prism-javascript'

export default function CodeEditorModal({ functionId, name, onClose }: { functionId: number; name: string; onClose: () => void }) {
  const [versions, setVersions] = useState<any[] | null>(null)
  const [loading, setLoading] = useState(false)
  const [selected, setSelected] = useState<number | null>(null)
  const [code, setCode] = useState('// select version or load code')
  const [editing, setEditing] = useState(false)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadVersions()
  }, [])

  async function loadVersions() {
    setLoading(true)
    try {
      const vs = await functionsApi.listVersions(functionId)
      setVersions(vs)
    } catch (err) {
      alert('Failed to load versions')
    } finally {
      setLoading(false)
    }
  }

  async function loadCode(versionId: number) {
    setLoading(true)
    try {
      const c = await functionsApi.getVersionCode(versionId)
      setCode(c)
      setSelected(versionId)
    } catch (err) {
      alert('Failed to load code')
    } finally {
      setLoading(false)
    }
  }

  async function saveAsNewVersion() {
    // create blob and upload as file
    const blob = new Blob([code], { type: 'application/javascript' })
    const file = new File([blob], `${name || 'function'}-edited.js`, { type: 'application/javascript' })
    setSaving(true)
    try {
      await functionsApi.uploadVersion(functionId, file)
      alert('Uploaded new version')
      await loadVersions()
      setEditing(false)
    } catch (err:any) {
      alert(err?.response?.data ?? String(err))
    } finally {
      setSaving(false)
    }
  }

  async function setActive(vId: number) {
    try {
      await functionsApi.setActiveVersion(functionId, vId)
      alert('Set active version')
      await loadVersions()
    } catch (err:any) {
      alert(err?.response?.data ?? String(err))
    }
  }

  async function deleteVersion(vId: number) {
    if (!confirm('Delete version?')) return
    try {
      await functionsApi.deleteVersion(vId)
      await loadVersions()
      if (selected === vId) setSelected(null)
    } catch (err:any) {
      alert(err?.response?.data ?? String(err))
    }
  }

  return (
    <Modal onClose={onClose} title={`Versions for ${name}`}>
      <div className="grid grid-cols-3 gap-4">
        <div>
          <div className="font-medium mb-2">Versions</div>
          {loading ? <LoadingSpinner /> : (
            <ul className="space-y-2 max-h-[400px] overflow-auto">
              {versions?.map((v) => (
                <li key={v.id} className="p-2 border rounded flex items-center justify-between">
                  <div>
                    <div>v{v.versionNumber} ({v.fileSizeBytes} bytes)</div>
                  </div>
                  <div className="flex items-center gap-1">
                    <button onClick={() => loadCode(v.id)} className="px-2 py-1 border rounded text-sm">View</button>
                    <button onClick={() => setActive(v.id)} className="px-2 py-1 border rounded text-sm">Set Active</button>
                    <button onClick={() => deleteVersion(v.id)} className="px-2 py-1 border rounded text-sm text-red-600">Delete</button>
                  </div>
                </li>
              ))}
            </ul>
          )}

          <div className="mt-3">
            <label className="inline-flex items-center gap-2">
              <input type="file" accept=".js" onChange={async (e) => {
                const file = e.target.files?.[0]
                if (!file) return
                if (!file.name.endsWith('.js')) { alert('Only .js allowed'); return }
                try {
                  await functionsApi.uploadVersion(functionId, file)
                  await loadVersions()
                  alert('Uploaded')
                } catch (err:any) { alert(err?.response?.data ?? String(err)) }
                finally { e.currentTarget.value = '' }
              }} />
            </label>
          </div>
        </div>

        <div className="col-span-2">
          <div className="flex items-center justify-between mb-2">
            <div className="font-medium">Code</div>
            <div className="flex items-center gap-2">
              <button onClick={() => { setEditing(!editing) }} className="px-2 py-1 border rounded">{editing ? 'Disable Edit' : 'Edit'}</button>
              <button onClick={() => saveAsNewVersion()} className="px-2 py-1 bg-green-600 text-white rounded" disabled={!editing || saving}>{saving ? 'Saving...' : 'Save as New Version'}</button>
            </div>
          </div>

          <div className="border rounded p-2">
            <Editor
              value={code}
              onValueChange={(v) => setCode(v)}
              highlight={(code) => Prism.highlight(code, Prism.languages.javascript, 'javascript')}
              padding={10}
              style={{ minHeight: 400, fontFamily: 'monospace', fontSize: 13 }}
              textareaId="code-editor"
              textareaClassName="outline-none w-full"
              readOnly={!editing}
            />
          </div>
        </div>
      </div>
    </Modal>
  )
}
