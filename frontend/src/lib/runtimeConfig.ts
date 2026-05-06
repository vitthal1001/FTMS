export interface RuntimeConfig {
  apiBaseUrl: string;
  environment: string;
}

export const runtimeConfig: RuntimeConfig = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
  environment: import.meta.env.MODE
};

