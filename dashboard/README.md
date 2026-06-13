# EventFlow Dashboard (Frontend)

A Single Page App built with React + TypeScript for the EventFlow control-plane backend.

Features
- Register / Login (JWT stored in localStorage)
- Projects: list, create, edit, delete
- Project detail: Functions, Events, Environment Variables, Secret Variables
- Functions: list, create, edit, delete, versions list, upload new version (.js files), inline JS editor to submit code as a new version
- Events: list, create, delete, show webhook token + test webhook
- Subscriptions: add subscriptions to events and list/delete subscriptions
- Domain & allowed-function management
- Executions + logs
- Axios instance automatically attaches Authorization header when logged in
- React Query for server state, React Hook Form + Zod for forms, TailwindCSS for styling

Requirements
- Node 18+ (recommended)
- Backend (EventFlow control plane) running, default base URL http://localhost:8080

Getting started
1. Install dependencies
   npm install

2. Run dev server
   npm run dev

3. Open browser
   http://localhost:5173

Configuration
- The front-end reads the backend base URL from Vite env var `VITE_BASE_API_URL`.
- To override, create a `.env` file in project root:
  VITE_BASE_API_URL=http://localhost:8080

Notes
- The client expects the exact endpoints described in the EventFlow spec. If your backend uses different paths, update `src/api/axios.ts` and `src/api/client.ts`.
- File uploads for function versions require `.js` file extension. The client enforces that before sending.
- Some endpoints (PUT/DELETE for some resources) are assumed to follow RESTful patterns. If your backend differs, point out the exact path and I'll adapt the client.
