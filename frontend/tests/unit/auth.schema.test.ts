import { describe, expect, it } from "vitest";
import { loginSchema } from "@/lib/validation/auth.schema";

describe("loginSchema", () => {
  it("accepts a valid login payload", () => {
    const result = loginSchema.safeParse({ email: "user@example.com", password: "secret123" });
    expect(result.success).toBe(true);
  });

  it("accepts a matricule as principal (login by matricule)", () => {
    const result = loginSchema.safeParse({ email: "HRC-00007", password: "secret123" });
    expect(result.success).toBe(true);
  });

  it("rejects an empty principal", () => {
    const result = loginSchema.safeParse({ email: "", password: "secret123" });
    expect(result.success).toBe(false);
  });

  it("rejects short password", () => {
    const result = loginSchema.safeParse({ email: "user@example.com", password: "x" });
    expect(result.success).toBe(false);
  });
});
