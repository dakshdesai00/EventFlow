import React, { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  projectsApi,
  functionsApi,
  eventsApi,
  envVarApi,
  secretVarApi,
  eventsExtrasApi,
  subscriptionsApi as subsApi,
  executionsApi,
} from "../api/client";
import type {
  FunctionResponse,
  EventResponse,
  EnvironmentVariableResponse,
} from "../api/types";
import Modal from "../components/Modal";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { copyToClipboard } from "../utils/copy";
import CodeEditorModal from "../components/CodeEditorModal";

const functionSchema = z.object({
  name: z.string().nonempty(),
  description: z.string().optional(),
  timeoutMs: z.number().int(),
  memoryLimitMb: z.number().int(),
  cacheEnabled: z.boolean(),
  cacheTtlSeconds: z.number().int().optional().nullable(),
});

type FunctionForm = z.infer<typeof functionSchema>;

const eventSchema = z.object({
  name: z.string().nonempty(),
  description: z.string().optional(),
  exposeWebhook: z.boolean().optional(),
});

type EventForm = z.infer<typeof eventSchema>;

const envSchema = z.object({
  key: z.string().nonempty(),
  value: z.string().nonempty(),
});

type EnvForm = z.infer<typeof envSchema>;

export default function ProjectDetailPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const pid = Number(projectId);
  const qc = useQueryClient();
  const { data: project } = useQuery(
    ["project", pid],
    () => projectsApi.get(pid),
    { enabled: !!pid },
  );
  const { data: functions } = useQuery(
    ["functions", pid],
    () => functionsApi.listForProject(pid),
    { enabled: !!pid },
  );
  const { data: events } = useQuery(
    ["events", pid],
    () => eventsApi.listForProject(pid),
    { enabled: !!pid },
  );

  const createFunction = useMutation(
    (payload: FunctionForm) => functionsApi.create(pid, payload),
    {
      onSuccess: () => qc.invalidateQueries(["functions", pid]),
    },
  );
  const deleteFunction = useMutation((id: number) => functionsApi.delete(id), {
    onSuccess: () => qc.invalidateQueries(["functions", pid]),
  });

  const createEvent = useMutation(
    (payload: EventForm) => eventsApi.create(pid, payload),
    { onSuccess: () => qc.invalidateQueries(["events", pid]) },
  );
  const deleteEvent = useMutation((id: number) => eventsApi.delete(id), {
    onSuccess: () => qc.invalidateQueries(["events", pid]),
  });

  // env vars
  const { data: projectEnv } = useQuery(
    ["projectEnv", pid],
    () => envVarApi.listForProject(pid),
    { enabled: !!pid },
  );
  const createProjectEnv = useMutation(
    (payload: EnvForm) => envVarApi.createForProject(pid, payload),
    { onSuccess: () => qc.invalidateQueries(["projectEnv", pid]) },
  );
  const deleteProjectEnv = useMutation(
    (id: number) => envVarApi.deleteProjectVar(pid, id),
    { onSuccess: () => qc.invalidateQueries(["projectEnv", pid]) },
  );

  const { data: projectSecrets } = useQuery(
    ["projectSecrets", pid],
    () => secretVarApi.listForProject(pid),
    { enabled: !!pid },
  );
  const createProjectSecret = useMutation(
    (payload: EnvForm) => secretVarApi.createForProject(pid, payload),
    { onSuccess: () => qc.invalidateQueries(["projectSecrets", pid]) },
  );
  const deleteProjectSecret = useMutation(
    (id: number) => secretVarApi.deleteProjectVar(pid, id),
    { onSuccess: () => qc.invalidateQueries(["projectSecrets", pid]) },
  );

  const [tab, setTab] = useState<"functions" | "events" | "env">("functions");

  // function modal
  const [fnOpen, setFnOpen] = useState(false);
  const {
    register: registerFn,
    handleSubmit: handleFnSubmit,
    reset: resetFn,
    formState: { errors: fnErrors },
  } = useForm<FunctionForm>({
    resolver: zodResolver(functionSchema),
    defaultValues: {
      timeoutMs: 3000,
      memoryLimitMb: 128,
      cacheEnabled: false,
      cacheTtlSeconds: null,
    },
  });

  // event modal
  const [evOpen, setEvOpen] = useState(false);
  const {
    register: registerEv,
    handleSubmit: handleEvSubmit,
    reset: resetEv,
    formState: { errors: evErrors },
  } = useForm<EventForm>({
    resolver: zodResolver(eventSchema),
    defaultValues: { exposeWebhook: false },
  });

  // env modal
  const [envOpen, setEnvOpen] = useState(false);
  const {
    register: registerEnv,
    handleSubmit: handleEnvSubmit,
    reset: resetEnv,
    formState: { errors: envErrors },
  } = useForm<EnvForm>({
    resolver: zodResolver(envSchema),
  });

  // webhook token modal & test
  const [tokenModal, setTokenModal] = useState<{ token: string } | null>(null);
  const [testModal, setTestModal] = useState<{
    token: string;
    eventId?: number;
  } | null>(null);
  const [testResult, setTestResult] = useState<any | null>(null);

  const [codeModal, setCodeModal] = useState<{
    functionId: number;
    name: string;
  } | null>(null);

  // subscriptions modal state
  const [subsModal, setSubsModal] = useState<{ eventId: number } | null>(null);
  const [eventSubscriptions, setEventSubscriptions] = useState<any[] | null>(
    null,
  );

  // extras modal (domains & allowed functions)
  const [extrasModal, setExtrasModal] = useState<{ eventId: number } | null>(
    null,
  );
  const [eventDomains, setEventDomains] = useState<any[] | null>(null);
  const [allowedFunctions, setAllowedFunctions] = useState<any[] | null>(null);
  const [newDomain, setNewDomain] = useState("");
  const [newAllowedFunctionId, setNewAllowedFunctionId] = useState<
    number | null
  >(null);

  useEffect(() => {
    if (subsModal) loadSubscriptions(subsModal.eventId);
  }, [subsModal]);

  useEffect(() => {
    if (extrasModal) loadEventExtras(extrasModal.eventId);
  }, [extrasModal]);

  async function loadSubscriptions(eventId: number) {
    try {
      const subs = await subsApi.listForEvent(eventId);
      setEventSubscriptions(subs);
    } catch (err) {
      alert("Failed to load subscriptions");
    }
  }

  async function addSubscription(eventId: number, functionId: number) {
    try {
      await subsApi.create(eventId, { functionId });
      await loadSubscriptions(eventId);
      qc.invalidateQueries(["events", pid]);
    } catch (err: any) {
      alert(err?.response?.data ?? String(err));
    }
  }

  async function removeSubscription(id: number) {
    try {
      await subsApi.delete(id);
      if (subsModal) loadSubscriptions(subsModal.eventId);
    } catch (err: any) {
      alert(err?.response?.data ?? String(err));
    }
  }

  async function loadEventExtras(eventId: number) {
    try {
      const domains = await eventsExtrasApi.listDomains(eventId);
      const allowed = await eventsExtrasApi.listAllowedFunctions(eventId);
      setEventDomains(domains);
      setAllowedFunctions(allowed || []);
    } catch (err: any) {
      alert(err?.response?.data ?? String(err));
    }
  }

  async function addDomain(eventId: number, domain: string) {
    try {
      await eventsExtrasApi.addDomain(eventId, { domain });
      await loadEventExtras(eventId);
    } catch (err: any) {
      alert(err?.response?.data ?? String(err));
    }
  }

  async function removeDomain(id: number) {
    try {
      await eventsExtrasApi.deleteDomain(id);
      if (extrasModal) loadEventExtras(extrasModal.eventId);
    } catch (err: any) {
      alert(err?.response?.data ?? String(err));
    }
  }

  async function addAllowedFunction(eventId: number, functionId: number) {
    try {
      await eventsExtrasApi.addAllowedFunction(eventId, { functionId });
      await loadEventExtras(eventId);
    } catch (err: any) {
      alert(err?.response?.data ?? String(err));
    }
  }

  async function removeAllowedFunction(id: number) {
    try {
      await eventsExtrasApi.deleteAllowedFunction(id);
      if (extrasModal) loadEventExtras(extrasModal.eventId);
    } catch (err: any) {
      alert(err?.response?.data ?? String(err));
    }
  }

  async function onCreateFunction(values: FunctionForm) {
    try {
      await createFunction.mutateAsync(values);
      setFnOpen(false);
    } catch (err: any) {
      alert(err?.response?.data?.message ?? "Create function failed");
    }
  }

  async function onCreateEvent(values: EventForm) {
    try {
      const ev = await createEvent.mutateAsync(values);
      if (values.exposeWebhook && ev.webhookToken) {
        setTokenModal({ token: ev.webhookToken });
      }
      setEvOpen(false);
    } catch (err: any) {
      alert(err?.response?.data?.message ?? "Create event failed");
    }
  }

  async function createEnv(values: EnvForm) {
    try {
      await createProjectEnv.mutateAsync(values);
      setEnvOpen(false);
    } catch (err: any) {
      alert(err?.response?.data ?? String(err));
    }
  }

  async function createSecret(values: EnvForm) {
    try {
      await createProjectSecret.mutateAsync(values);
      setEnvOpen(false);
    } catch (err: any) {
      alert(err?.response?.data ?? String(err));
    }
  }

  async function copyToken(token: string) {
    copyToClipboard(token);
    alert("Webhook token copied to clipboard");
  }

  async function runTest(token: string, body: string, origin?: string) {
    try {
      const parsed = body ? JSON.parse(body) : {};
      const res = await eventsApi.trigger(token, parsed, origin);
      setTestResult(res);
    } catch (err: any) {
      setTestResult({ error: err?.response?.data ?? String(err) });
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-2xl font-semibold">
            {project?.name ?? "Project"}
          </h1>
          <div className="text-sm text-gray-600">{project?.description}</div>
        </div>
        <div>
          <div className="inline-flex border rounded">
            <button
              onClick={() => setTab("functions")}
              className={`px-3 py-2 ${tab === "functions" ? "bg-white" : "bg-gray-50"}`}
            >
              Functions
            </button>
            <button
              onClick={() => setTab("events")}
              className={`px-3 py-2 ${tab === "events" ? "bg-white" : "bg-gray-50"}`}
            >
              Events
            </button>
            <button
              onClick={() => setTab("env")}
              className={`px-3 py-2 ${tab === "env" ? "bg-white" : "bg-gray-50"}`}
            >
              Env & Secrets
            </button>
          </div>
        </div>
      </div>

      {tab === "functions" && (
        <div className="bg-white p-4 rounded shadow">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium">Functions</h3>
            <button
              onClick={() => {
                resetFn();
                setFnOpen(true);
              }}
              className="px-3 py-2 bg-blue-600 text-white rounded"
            >
              New Function
            </button>
          </div>

          <ul className="space-y-3">
            {(functions ?? []).map((f: FunctionResponse) => (
              <li
                key={f.id}
                className="p-3 border rounded flex items-center justify-between"
              >
                <div>
                  <div className="text-lg font-medium">
                    <Link to={`/functions/${f.id}`}>{f.name}</Link>
                  </div>
                  <div className="text-sm text-gray-600">{f.description}</div>
                  <div className="text-xs text-gray-500">
                    timeout: {f.timeoutMs}ms · memory: {f.memoryLimitMb}MB ·
                    cache:{" "}
                    {f.cacheEnabled
                      ? `yes (ttl ${f.cacheTtlSeconds ?? "n/a"})`
                      : "no"}
                  </div>
                  <div className="text-xs text-gray-500">
                    activeVersionId: {f.activeVersionId ?? "—"}
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() =>
                      setCodeModal({ functionId: f.id, name: f.name })
                    }
                    className="text-sm px-2 py-1 border rounded"
                  >
                    Open Editor / Versions
                  </button>

                  <label className="inline-flex items-center gap-2 border rounded px-2 py-1 cursor-pointer">
                    <input
                      type="file"
                      accept=".js"
                      onChange={async (e) => {
                        const file = e.target.files?.[0];
                        if (!file) return;
                        if (!file.name.endsWith(".js")) {
                          alert("Only .js files are allowed");
                          return;
                        }
                        try {
                          await functionsApi.uploadVersion(f.id, file);
                          alert("Version uploaded");
                          await qc.invalidateQueries(["functions", pid]);
                          await qc.invalidateQueries([
                            "functions",
                            f.id,
                            "versions",
                          ]);
                        } catch (err: any) {
                          alert(
                            err?.response?.data?.message ?? "Upload failed",
                          );
                        } finally {
                          e.currentTarget.value = "";
                        }
                      }}
                    />
                    <span className="text-sm">Upload Version (.js)</span>
                  </label>

                  <button
                    onClick={() => {
                      if (!confirm("Delete function?")) return;
                      deleteFunction.mutate(f.id);
                    }}
                    className="text-sm px-2 py-1 border rounded text-red-600"
                  >
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>

          {fnOpen && (
            <Modal onClose={() => setFnOpen(false)} title="Create Function">
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  handleFnSubmit(onCreateFunction)();
                }}
                className="space-y-3"
              >
                <div>
                  <label className="block text-sm font-medium">Name</label>
                  <input
                    {...registerFn("name", { valueAsNumber: false })}
                    className="mt-1 w-full border rounded px-3 py-2"
                  />
                  {fnErrors.name && (
                    <div className="text-red-600 text-sm mt-1">
                      {String(fnErrors.name?.message)}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium">
                    Description
                  </label>
                  <textarea
                    {...registerFn("description")}
                    className="mt-1 w-full border rounded px-3 py-2"
                  />
                </div>

                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block text-sm font-medium">
                      Timeout (ms)
                    </label>
                    <input
                      type="number"
                      {...registerFn("timeoutMs", { valueAsNumber: true })}
                      className="mt-1 w-full border rounded px-3 py-2"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium">
                      Memory (MB)
                    </label>
                    <input
                      type="number"
                      {...registerFn("memoryLimitMb", { valueAsNumber: true })}
                      className="mt-1 w-full border rounded px-3 py-2"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium">
                      Cache TTL (s)
                    </label>
                    <input
                      type="number"
                      {...registerFn("cacheTtlSeconds", {
                        valueAsNumber: true,
                      })}
                      className="mt-1 w-full border rounded px-3 py-2"
                    />
                    <div className="text-xs text-gray-500">Optional</div>
                  </div>
                </div>

                <div>
                  <label className="inline-flex items-center gap-2">
                    <input type="checkbox" {...registerFn("cacheEnabled")} />
                    <span>Enable cache</span>
                  </label>
                </div>

                <div className="text-right">
                  <button
                    type="submit"
                    className="px-3 py-2 bg-green-600 text-white rounded"
                  >
                    Create
                  </button>
                </div>
              </form>
            </Modal>
          )}

          {codeModal && (
            <CodeEditorModal
              functionId={codeModal.functionId}
              name={codeModal.name}
              onClose={() => {
                setCodeModal(null);
                qc.invalidateQueries(["functions", pid]);
              }}
            />
          )}
        </div>
      )}

      {tab === "events" && (
        <div className="bg-white p-4 rounded shadow">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium">Events</h3>
            <div className="flex items-center gap-2">
              <button
                onClick={() => {
                  resetEv();
                  setEvOpen(true);
                }}
                className="px-3 py-2 bg-blue-600 text-white rounded"
              >
                New Event
              </button>
            </div>
          </div>

          <ul className="space-y-3">
            {(events ?? []).map((ev: EventResponse) => (
              <li
                key={ev.id}
                className="p-3 border rounded flex items-center justify-between"
              >
                <div>
                  <div className="text-lg font-medium">{ev.name}</div>
                  <div className="text-sm text-gray-600">{ev.description}</div>
                  <div className="text-xs text-gray-500">
                    Webhook:{" "}
                    {ev.webhookToken ? (
                      <span className="font-mono text-xs">
                        {ev.webhookToken}
                      </span>
                    ) : (
                      <span>hidden</span>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  {ev.webhookToken && (
                    <>
                      <button
                        onClick={() => {
                          copyToClipboard(ev.webhookToken!);
                          alert("Webhook token copied");
                        }}
                        className="text-sm px-2 py-1 border rounded"
                      >
                        Copy Token
                      </button>
                      <button
                        onClick={() =>
                          setTestModal({
                            token: ev.webhookToken!,
                            eventId: ev.id,
                          })
                        }
                        className="text-sm px-2 py-1 border rounded"
                      >
                        Test Webhook
                      </button>
                    </>
                  )}
                  <button
                    onClick={() => setSubsModal({ eventId: ev.id })}
                    className="text-sm px-2 py-1 border rounded"
                  >
                    Subscriptions
                  </button>
                  <button
                    onClick={() => setExtrasModal({ eventId: ev.id })}
                    className="text-sm px-2 py-1 border rounded"
                  >
                    Manage
                  </button>
                  <button
                    onClick={() => {
                      if (!confirm("Delete event?")) return;
                      deleteEvent.mutate(ev.id);
                    }}
                    className="text-sm px-2 py-1 border rounded text-red-600"
                  >
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>

          {evOpen && (
            <Modal onClose={() => setEvOpen(false)} title="Create Event">
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  handleEvSubmit(onCreateEvent)();
                }}
                className="space-y-3"
              >
                <div>
                  <label className="block text-sm font-medium">Name</label>
                  <input
                    {...registerEv("name")}
                    className="mt-1 w-full border rounded px-3 py-2"
                  />
                  {evErrors.name && (
                    <div className="text-red-600 text-sm mt-1">
                      {String(evErrors.name?.message)}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium">
                    Description
                  </label>
                  <textarea
                    {...registerEv("description")}
                    className="mt-1 w-full border rounded px-3 py-2"
                  />
                </div>

                <div>
                  <label className="inline-flex items-center gap-2">
                    <input type="checkbox" {...registerEv("exposeWebhook")} />
                    <span>Expose webhook (create a token)</span>
                  </label>
                </div>

                <div className="text-right">
                  <button
                    type="submit"
                    className="px-3 py-2 bg-green-600 text-white rounded"
                  >
                    Create
                  </button>
                </div>
              </form>
            </Modal>
          )}

          {tokenModal && (
            <Modal onClose={() => setTokenModal(null)} title="Webhook Token">
              <div>
                <p className="text-sm">Webhook token (copy and keep secure):</p>
                <pre className="bg-gray-100 p-3 rounded mt-2 font-mono">
                  {tokenModal.token}
                </pre>
                <div className="mt-3 flex items-center gap-2">
                  <button
                    onClick={() => {
                      copyToClipboard(tokenModal.token);
                      alert("Copied");
                    }}
                    className="px-3 py-1 bg-blue-600 text-white rounded"
                  >
                    Copy
                  </button>
                  <button
                    onClick={() => setTestModal({ token: tokenModal.token })}
                    className="px-3 py-1 border rounded"
                  >
                    Test webhook
                  </button>
                </div>
              </div>
            </Modal>
          )}

          {testModal && (
            <Modal
              onClose={() => {
                setTestModal(null);
                setTestResult(null);
              }}
              title="Test Webhook"
            >
              <WebhookTester
                token={testModal.token}
                onRun={runTest}
                result={testResult}
              />
            </Modal>
          )}

          {subsModal && (
            <Modal
              onClose={() => {
                setSubsModal(null);
                setEventSubscriptions(null);
              }}
              title="Subscriptions"
            >
              <div className="space-y-3">
                <div>
                  <strong>Event Subscriptions</strong>
                </div>
                <div>
                  <ul className="space-y-2">
                    {(eventSubscriptions ?? []).map((s: any) => (
                      <li
                        key={s.id}
                        className="p-2 border rounded flex items-center justify-between"
                      >
                        <div>Function ID: {s.functionId}</div>
                        <div>
                          <button
                            onClick={() => removeSubscription(s.id)}
                            className="px-2 py-1 border rounded text-sm text-red-600"
                          >
                            Remove
                          </button>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>

                <div>
                  <label className="block text-sm font-medium">
                    Add subscription (function)
                  </label>
                  <select
                    className="mt-1 w-full border rounded px-3 py-2"
                    defaultValue=""
                    onChange={(e) => {
                      const fid = Number(e.target.value);
                      if (fid) addSubscription(subsModal!.eventId, fid);
                    }}
                  >
                    <option value="">Select function...</option>
                    {(functions ?? []).map((f) => (
                      <option key={f.id} value={f.id}>
                        {f.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </Modal>
          )}

          {extrasModal && (
            <Modal
              onClose={() => {
                setExtrasModal(null);
                setEventDomains(null);
                setAllowedFunctions(null);
                setNewDomain("");
                setNewAllowedFunctionId(null);
              }}
              title="Event Domains & Allowed Functions"
            >
              <div className="space-y-4">
                <div>
                  <h4 className="font-medium">Allowed Domains</h4>
                  <ul className="space-y-2">
                    {(eventDomains ?? []).map((d: any) => (
                      <li
                        key={d.id ?? d}
                        className="p-2 border rounded flex items-center justify-between"
                      >
                        <div>{typeof d === "string" ? d : d.domain}</div>
                        <div>
                          <button
                            onClick={() => removeDomain(d.id ?? d)}
                            className="px-2 py-1 border rounded text-sm text-red-600"
                          >
                            Delete
                          </button>
                        </div>
                      </li>
                    ))}
                  </ul>
                  <div className="mt-2 flex gap-2">
                    <input
                      value={newDomain}
                      onChange={(e) => setNewDomain(e.target.value)}
                      className="border rounded px-2 py-1 flex-1"
                    />
                    <button
                      onClick={() => {
                        if (!extrasModal) return;
                        addDomain(extrasModal.eventId, newDomain);
                        setNewDomain("");
                      }}
                      className="px-2 py-1 bg-blue-600 text-white rounded"
                    >
                      Add
                    </button>
                  </div>
                </div>

                <div>
                  <h4 className="font-medium">Allowed Functions</h4>
                  <ul className="space-y-2">
                    {(allowedFunctions ?? []).map((af: any) => (
                      <li
                        key={af.id ?? af.functionId ?? af}
                        className="p-2 border rounded flex items-center justify-between"
                      >
                        <div>
                          {af.functionName ?? `Function ${af.functionId ?? af}`}
                        </div>
                        <div>
                          <button
                            onClick={() =>
                              removeAllowedFunction(
                                af.id ?? af.functionId ?? af,
                              )
                            }
                            className="px-2 py-1 border rounded text-sm text-red-600"
                          >
                            Remove
                          </button>
                        </div>
                      </li>
                    ))}
                  </ul>

                  <div className="mt-2">
                    <select
                      value={newAllowedFunctionId ?? ""}
                      onChange={(e) =>
                        setNewAllowedFunctionId(Number(e.target.value) || null)
                      }
                      className="border rounded px-2 py-1 w-full"
                    >
                      <option value="">Select function to allow</option>
                      {(functions ?? []).map((f) => (
                        <option key={f.id} value={f.id}>
                          {f.name}
                        </option>
                      ))}
                    </select>
                    <div className="mt-2 text-right">
                      <button
                        onClick={() => {
                          if (!extrasModal || !newAllowedFunctionId) return;
                          addAllowedFunction(
                            extrasModal.eventId,
                            newAllowedFunctionId,
                          );
                          setNewAllowedFunctionId(null);
                        }}
                        className="px-2 py-1 bg-blue-600 text-white rounded"
                      >
                        Add Allowed Function
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </Modal>
          )}
        </div>
      )}

      {tab === "env" && (
        <div className="bg-white p-4 rounded shadow">
          <div className="grid grid-cols-2 gap-6">
            <div>
              <div className="flex items-center justify-between mb-2">
                <h4 className="font-medium">Environment Variables</h4>
                <button
                  onClick={() => {
                    resetEnv();
                    setEnvOpen(true);
                  }}
                  className="px-2 py-1 bg-blue-600 text-white rounded text-sm"
                >
                  New
                </button>
              </div>

              <ul className="space-y-2">
                {(projectEnv ?? []).map((v: EnvironmentVariableResponse) => (
                  <li
                    key={v.id}
                    className="p-2 border rounded flex items-center justify-between"
                  >
                    <div>
                      <div className="font-mono text-sm">{v.key}</div>
                      <div className="text-sm text-gray-600">{v.value}</div>
                    </div>
                    <div>
                      <button
                        onClick={() => deleteProjectEnv.mutate(v.id)}
                        className="text-sm px-2 py-1 border rounded text-red-600"
                      >
                        Delete
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            </div>

            <div>
              <div className="flex items-center justify-between mb-2">
                <h4 className="font-medium">Secret Variables</h4>
                <button
                  onClick={() => {
                    resetEnv();
                    setEnvOpen(true);
                  }}
                  className="px-2 py-1 bg-blue-600 text-white rounded text-sm"
                >
                  New
                </button>
              </div>

              <ul className="space-y-2">
                {(projectSecrets ?? []).map(
                  (v: EnvironmentVariableResponse) => (
                    <li
                      key={v.id}
                      className="p-2 border rounded flex items-center justify-between"
                    >
                      <div>
                        <div className="font-mono text-sm">{v.key}</div>
                        <div className="text-sm text-gray-600">***</div>
                      </div>
                      <div>
                        <button
                          onClick={() => deleteProjectSecret.mutate(v.id)}
                          className="text-sm px-2 py-1 border rounded text-red-600"
                        >
                          Delete
                        </button>
                      </div>
                    </li>
                  ),
                )}
              </ul>
            </div>
          </div>

          {envOpen && (
            <Modal onClose={() => setEnvOpen(false)} title="Create Variable">
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  handleEnvSubmit(async (vals) => {
                    try {
                      await createProjectEnv.mutateAsync(vals);
                      setEnvOpen(false);
                    } catch (err: any) {
                      alert(err?.response?.data ?? String(err));
                    }
                  })();
                }}
                className="space-y-3"
              >
                <div>
                  <label className="block text-sm font-medium">Key</label>
                  <input
                    {...registerEnv("key")}
                    className="mt-1 w-full border rounded px-3 py-2"
                  />
                  {envErrors.key && (
                    <div className="text-red-600 text-sm mt-1">
                      {String(envErrors.key?.message)}
                    </div>
                  )}
                </div>
                <div>
                  <label className="block text-sm font-medium">Value</label>
                  <input
                    {...registerEnv("value")}
                    className="mt-1 w-full border rounded px-3 py-2"
                  />
                  {envErrors.value && (
                    <div className="text-red-600 text-sm mt-1">
                      {String(envErrors.value?.message)}
                    </div>
                  )}
                </div>
                <div className="text-right">
                  <button
                    type="submit"
                    className="px-3 py-2 bg-green-600 text-white rounded"
                  >
                    Create
                  </button>
                </div>
              </form>
            </Modal>
          )}
        </div>
      )}
    </div>
  );
}

function WebhookTester({
  token,
  onRun,
  result,
}: {
  token: string;
  onRun: (token: string, body: string, origin?: string) => void;
  result: any;
}) {
  const [body, setBody] = useState("{}");
  const [origin, setOrigin] = useState("");

  return (
    <div className="space-y-3">
      <div>
        <label className="block text-sm font-medium">
          Origin header (optional)
        </label>
        <input
          value={origin}
          onChange={(e) => setOrigin(e.target.value)}
          className="mt-1 w-full border rounded px-3 py-2"
          placeholder="https://example.com"
        />
      </div>
      <div>
        <label className="block text-sm font-medium">JSON payload</label>
        <textarea
          value={body}
          onChange={(e) => setBody(e.target.value)}
          className="mt-1 w-full border rounded px-3 py-2 h-40 font-mono"
        />
      </div>

      <div className="flex items-center gap-2">
        <button
          onClick={() => onRun(token, body, origin || undefined)}
          className="px-3 py-2 bg-blue-600 text-white rounded"
        >
          Send
        </button>
      </div>

      <div>
        <h4 className="font-medium">Result</h4>
        <pre className="bg-gray-100 p-3 rounded text-sm">
          {result ? JSON.stringify(result, null, 2) : "No result"}
        </pre>
        {result?.functions && Array.isArray(result.functions) && (
          <div className="mt-2">
            <div className="text-sm text-gray-600">
              Note: returned functions[].functionId is an execution id (per
              backend spec)
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
