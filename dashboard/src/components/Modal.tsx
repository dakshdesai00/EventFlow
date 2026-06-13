import React from 'react'
import { createPortal } from 'react-dom'

export default function Modal({ children, onClose, title }: { children: React.ReactNode; onClose: () => void; title?: string }) {
  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black opacity-40" onClick={onClose} />
      <div className="bg-white rounded shadow-lg z-10 w-full max-w-2xl p-6">
        {title && <h3 className="text-lg font-semibold mb-4">{title}</h3>}
        <div>{children}</div>
        <div className="mt-4 text-right">
          <button onClick={onClose} className="px-3 py-1 bg-gray-200 rounded">Close</button>
        </div>
      </div>
    </div>,
    document.body
  )
}
