import "@testing-library/jest-dom/vitest";

// Default env stubs for Vitest (jsdom environment).
const env = process.env as Record<string, string | undefined>;
env.SESSION_SECRET ??= "test-secret-test-secret-test-secret-test-secret-test";
env.KSM_BASE_URL ??= "http://localhost:8080";
env.KSM_CLIENT_ID ??= "test-client";
env.KSM_API_KEY ??= "test-key";
