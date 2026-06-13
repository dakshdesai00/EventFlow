Developer notes:
- The client implements assumed REST endpoints for environment variables, secrets, domains, allowed-functions, and CRUD operations for functions/projects/events. If your backend paths differ, update `src/api/client.ts`.
- The inline JS editor uses `react-simple-code-editor` + `prismjs`. It saves edited code by creating a Blob and uploading as a `.js` file to the versions upload endpoint.
- Tokens are stored in localStorage key `ef_token` and automatically attached as `Authorization: Bearer <token>` by axios interceptor.
- For the public `POST /api/events/trigger/{token}` endpoint, the client calls it through the api client which sends Authorization header if present. The backend should accept the request without auth.
