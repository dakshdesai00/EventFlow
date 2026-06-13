import axios from "axios";

// For local development we default to a relative base URL so Vite's dev server proxy (configured in vite.config.ts)
// can forward `/api` requests to the backend and avoid CORS issues. In production set VITE_BASE_API_URL.
const baseURL = import.meta.env.VITE_BASE_API_URL ?? "";

export const API_TOKEN_KEY = "ef_token";

export const api = axios.create({
  baseURL,
});

// Attach token from localStorage on every request
api.interceptors.request.use((config) => {
  try {
    const token = localStorage.getItem(API_TOKEN_KEY);
    if (token) {
      if (!config.headers) config.headers = {};
      config.headers["Authorization"] = `Bearer ${token}`;
    }
  } catch (e) {
    // noop
  }
  return config;
});

// DEV: log requests/responses to the console (mask Authorization)
if (import.meta.env.DEV) {
  api.interceptors.request.use(
    (config) => {
      try {
        const safeHeaders = { ...(config.headers || {}) } as Record<
          string,
          any
        >;
        if (safeHeaders["Authorization"])
          safeHeaders["Authorization"] = "Bearer **masked**";
        console.debug("[API request]", config.method, config.url, {
          params: config.params,
          data: config.data,
          headers: safeHeaders,
        });
      } catch (e) {
        // ignore
      }
      return config;
    },
    (err) => {
      console.error("[API request error]", err);
      return Promise.reject(err);
    },
  );

  api.interceptors.response.use(
    (res) => {
      try {
        console.debug("[API response]", res.status, res.config.url, res.data);
      } catch (e) {}
      return res;
    },
    (err) => {
      try {
        console.error(
          "[API response error]",
          err?.response?.status,
          err?.response?.data ?? err.message,
        );
      } catch (e) {}
      return Promise.reject(err);
    },
  );
}
