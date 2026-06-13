import { api } from "./axios";
import type {
  RegisterRequest,
  LoginRequest,
  AuthResponse,
  ProjectResponse,
  CreateProjectRequest,
  FunctionResponse,
  CreateFunctionRequest,
  EventResponse,
  CreateEventRequest,
  TriggerEventResponse,
  CreateEventSubscriptionRequest,
  EventSubscriptionResponse,
  FunctionVersionResponse,
  ExecutionResponse,
  ExecutionLogResponse,
  EnvironmentVariableResponse,
} from "./types";

/* Auth */
export const authApi = {
  register: (payload: RegisterRequest) =>
    api.post<AuthResponse>("/api/auth/register", payload).then((r) => r.data),
  login: (payload: LoginRequest) =>
    api.post<AuthResponse>("/api/auth/login", payload).then((r) => r.data),
};

/* Projects */
export const projectsApi = {
  list: () => api.get<ProjectResponse[]>("/api/projects").then((r) => r.data),
  create: (payload: CreateProjectRequest) =>
    api.post<ProjectResponse>("/api/projects", payload).then((r) => r.data),
  get: (projectId: number) =>
    api.get<ProjectResponse>(`/api/projects/${projectId}`).then((r) => r.data),
  update: (projectId: number, payload: CreateProjectRequest) =>
    api
      .put<ProjectResponse>(`/api/projects/${projectId}`, payload)
      .then((r) => r.data),
  delete: (projectId: number) =>
    api.delete<void>(`/api/projects/${projectId}`).then((r) => r.data),
};

/* Functions */
export const functionsApi = {
  listForProject: (projectId: number) =>
    api
      .get<FunctionResponse[]>(`/api/projects/${projectId}/functions`)
      .then((r) => r.data),
  create: (projectId: number, payload: CreateFunctionRequest) =>
    api
      .post<FunctionResponse>(`/api/projects/${projectId}/functions`, payload)
      .then((r) => r.data),
  get: (functionId: number) =>
    api
      .get<FunctionResponse>(`/api/functions/${functionId}`)
      .then((r) => r.data),
  update: (functionId: number, payload: Partial<CreateFunctionRequest>) =>
    api
      .put<FunctionResponse>(`/api/functions/${functionId}`, payload)
      .then((r) => r.data),
  delete: (functionId: number) =>
    api.delete<void>(`/api/functions/${functionId}`).then((r) => r.data),

  /* versions */
  listVersions: (functionId: number) =>
    api
      .get<FunctionVersionResponse[]>(`/api/functions/${functionId}/versions`)
      .then((r) => r.data),
  uploadVersion: (functionId: number, file: File) => {
    const fd = new FormData();
    fd.append("file", file);
    // Let the browser set the Content-Type (including the multipart boundary).
    // Explicitly setting Content-Type without a boundary can cause servers to fail parsing multipart payloads.
    return api
      .post<FunctionVersionResponse>(
        `/api/functions/${functionId}/versions`,
        fd,
      )
      .then((r) => r.data);
  },
  getVersionCode: (versionId: number) =>
    api
      .get<string>(`/api/function-versions/${versionId}/code`)
      .then((r) => r.data),
  setActiveVersion: (functionId: number, versionId: number) =>
    api
      .put<void>(`/api/functions/${functionId}/active-version/${versionId}`)
      .then((r) => r.data),
  deleteVersion: (versionId: number) =>
    api.delete<void>(`/api/function-versions/${versionId}`).then((r) => r.data),
};

/* Events */
export const eventsApi = {
  listForProject: (projectId: number) =>
    api
      .get<EventResponse[]>(`/api/projects/${projectId}/events`)
      .then((r) => r.data),
  create: (projectId: number, payload: CreateEventRequest) =>
    api
      .post<EventResponse>(`/api/projects/${projectId}/events`, payload)
      .then((r) => r.data),
  get: (eventId: number) =>
    api.get<EventResponse>(`/api/events/${eventId}`).then((r) => r.data),
  update: (eventId: number, payload: CreateEventRequest) =>
    api
      .put<EventResponse>(`/api/events/${eventId}`, payload)
      .then((r) => r.data),
  delete: (eventId: number) =>
    api.delete<void>(`/api/events/${eventId}`).then((r) => r.data),

  /* trigger public endpoint (no auth) will be called directly against base URL when testing webhook */
  trigger: (token: string, body: any, origin?: string) =>
    api
      .post<TriggerEventResponse>(`/api/events/trigger/${token}`, body, {
        headers: origin ? { Origin: origin } : undefined,
      })
      .then((r) => r.data),
};

/* Event subscriptions */
export const subscriptionsApi = {
  create: (eventId: number, payload: CreateEventSubscriptionRequest) =>
    api
      .post<EventSubscriptionResponse>(
        `/api/events/${eventId}/subscriptions`,
        payload,
      )
      .then((r) => r.data),
  listForEvent: (eventId: number) =>
    api
      .get<EventSubscriptionResponse[]>(`/api/events/${eventId}/subscriptions`)
      .then((r) => r.data),
  delete: (subscriptionId: number) =>
    api
      .delete<void>(`/api/subscriptions/${subscriptionId}`)
      .then((r) => r.data),
};

/* Domains & allowed functions for events */
export const eventsExtrasApi = {
  listDomains: (eventId: number) =>
    api.get<string[]>(`/api/events/${eventId}/domains`).then((r) => r.data),
  addDomain: (eventId: number, domain: { domain: string }) =>
    api.post(`/api/events/${eventId}/domains`, domain).then((r) => r.data),
  deleteDomain: (domainId: number) =>
    api.delete(`/api/event-domains/${domainId}`).then((r) => r.data),

  listAllowedFunctions: (eventId: number) =>
    api
      .get<any[]>(`/api/events/${eventId}/allowed-functions`)
      .then((r) => r.data),
  addAllowedFunction: (eventId: number, payload: { functionId: number }) =>
    api
      .post(`/api/events/${eventId}/allowed-functions`, payload)
      .then((r) => r.data),
  deleteAllowedFunction: (id: number) =>
    api.delete(`/api/event-allowed-functions/${id}`).then((r) => r.data),
};

/* Environment & secret variables (project-level and function-level) */
export const envVarApi = {
  listForProject: (projectId: number) =>
    api
      .get<
        EnvironmentVariableResponse[]
      >(`/api/projects/${projectId}/environment-variables`)
      .then((r) => r.data),
  createForProject: (
    projectId: number,
    payload: { key: string; value: string },
  ) =>
    api
      .post<EnvironmentVariableResponse>(
        `/api/projects/${projectId}/environment-variables`,
        payload,
      )
      .then((r) => r.data),
  deleteProjectVar: (projectId: number, id: number) =>
    api
      .delete(`/api/projects/${projectId}/environment-variables/${id}`)
      .then((r) => r.data),

  listForFunction: (functionId: number) =>
    api
      .get<
        EnvironmentVariableResponse[]
      >(`/api/functions/${functionId}/environment-variables`)
      .then((r) => r.data),
  createForFunction: (
    functionId: number,
    payload: { key: string; value: string },
  ) =>
    api
      .post<EnvironmentVariableResponse>(
        `/api/functions/${functionId}/environment-variables`,
        payload,
      )
      .then((r) => r.data),
  deleteFunctionVar: (functionId: number, id: number) =>
    api
      .delete(`/api/functions/${functionId}/environment-variables/${id}`)
      .then((r) => r.data),
};

/* Secrets - same shape but under secret endpoints */
export const secretVarApi = {
  listForProject: (projectId: number) =>
    api
      .get<
        EnvironmentVariableResponse[]
      >(`/api/projects/${projectId}/secret-variables`)
      .then((r) => r.data),
  createForProject: (
    projectId: number,
    payload: { key: string; value: string },
  ) =>
    api
      .post<EnvironmentVariableResponse>(
        `/api/projects/${projectId}/secret-variables`,
        payload,
      )
      .then((r) => r.data),
  deleteProjectVar: (projectId: number, id: number) =>
    api
      .delete(`/api/projects/${projectId}/secret-variables/${id}`)
      .then((r) => r.data),

  listForFunction: (functionId: number) =>
    api
      .get<
        EnvironmentVariableResponse[]
      >(`/api/functions/${functionId}/secret-variables`)
      .then((r) => r.data),
  createForFunction: (
    functionId: number,
    payload: { key: string; value: string },
  ) =>
    api
      .post<EnvironmentVariableResponse>(
        `/api/functions/${functionId}/secret-variables`,
        payload,
      )
      .then((r) => r.data),
  deleteFunctionVar: (functionId: number, id: number) =>
    api
      .delete(`/api/functions/${functionId}/secret-variables/${id}`)
      .then((r) => r.data),
};

/* Executions + logs */
export const executionsApi = {
  get: (executionId: number) =>
    api
      .get<ExecutionResponse>(`/api/executions/${executionId}`)
      .then((r) => r.data),
  listForFunction: (functionId: number) =>
    api
      .get<ExecutionResponse[]>(`/api/functions/${functionId}/executions`)
      .then((r) => r.data),
  listForEvent: (eventId: number) =>
    api
      .get<ExecutionResponse[]>(`/api/events/${eventId}/executions`)
      .then((r) => r.data),
  logs: (executionId: number) =>
    api
      .get<ExecutionLogResponse[]>(`/api/executions/${executionId}/logs`)
      .then((r) => r.data),
};
