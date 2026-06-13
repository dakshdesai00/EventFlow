import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Proxy API requests to the backend to avoid CORS during local development
      "/api": {
        target: process.env.VITE_BASE_API_URL || "http://localhost:8080",
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path,
      },
    },
  },
});
