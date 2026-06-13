// DTOs that match the server field names and types
export interface RegisterRequest {
  email: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
}

/* Projects */
export interface CreateProjectRequest {
  name: string
  description?: string
}

export interface ProjectResponse {
  id: number
  name: string
  description?: string | null
}

/* Functions */
export interface CreateFunctionRequest {
  name: string
  description?: string
  timeoutMs: number
  memoryLimitMb: number
  cacheEnabled: boolean
  cacheTtlSeconds?: number | null
}

export interface FunctionResponse {
  id: number
  projectId: number
  name: string
  description?: string | null
  timeoutMs: number
  memoryLimitMb: number
  cacheEnabled: boolean
  cacheTtlSeconds?: number | null
  activeVersionId?: number | null
}

/* Events */
export interface CreateEventRequest {
  name: string
  description?: string
  exposeWebhook?: boolean
}

export interface EventResponse {
  id: number
  name: string
  description?: string | null
  webhookToken?: string | null
  projectId: number
}

/* Trigger event response */
export interface TriggeredFunctionResponse {
  functionId: number // note: backend uses functionId but returns execution id here (see spec)
  functionName?: string
}

export interface TriggerEventResponse {
  eventId: number
  eventName: string
  payload: Record<string, any>
  functions: TriggeredFunctionResponse[]
}

/* Event subscriptions */
export interface CreateEventSubscriptionRequest {
  functionId: number
}

export interface EventSubscriptionResponse {
  id: number
  eventId: number
  functionId: number
}

/* Function versions */
export interface FunctionVersionResponse {
  id: number
  functionId: number
  versionNumber: number
  storageKey: string
  fileHash: string
  fileSizeBytes: number
}

/* Execution */
export type ExecutionStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'FAILED_PERMANENTLY'

export interface ExecutionResponse {
  id: number
  eventId?: number | null
  functionId?: number | null
  status: ExecutionStatus
  startedAt?: string | null
  endedAt?: string | null
  durationMs?: number | null
  errorMessage?: string | null
}

/* Execution logs */
export type LogLevel = 'INFO' | 'WARN' | 'ERROR' | 'DEBUG'

export interface ExecutionLogResponse {
  id: number
  executionId: number
  level: LogLevel
  timestamp: string
  message: string
}

/* Environment variable responses (server uses snake_case here) */
export interface EnvironmentVariableResponse {
  id: number
  key: string
  value: string
  project_id?: number | null
  function_id?: number | null
}

/* Mapping helper types (camelCase) */
export interface EnvironmentVariable {
  id: number
  key: string
  value: string
  projectId?: number | null
  functionId?: number | null
}
