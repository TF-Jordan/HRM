import { test, expect } from "@playwright/test";

test.describe("Phase 1 — auth flow", () => {
  test("login as seeded admin grants access to dashboard", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
    await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
    await page.getByRole("button", { name: /Se connecter/i }).click();

    await page.waitForURL(/\/dashboard$/);
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Bienvenue");
  });

  test("session cookie persists across navigations", async ({ page, context }) => {
    await page.goto("/login");
    await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
    await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
    await page.getByRole("button", { name: /Se connecter/i }).click();
    await page.waitForURL(/\/dashboard$/);

    const cookies = await context.cookies();
    const session = cookies.find((c) => c.name === "hrm_session");
    expect(session?.httpOnly).toBe(true);

    const meRes = await page.request.get("/api/auth/me");
    expect(meRes.status()).toBe(200);
    const body = await meRes.json();
    expect(body.success).toBe(true);
    expect(body.user.email).toBe("admin@hrcore.local");
    expect(body.permissions.length).toBeGreaterThan(40);
  });

  test("logout clears the session", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
    await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
    await page.getByRole("button", { name: /Se connecter/i }).click();
    await page.waitForURL(/\/dashboard$/);

    await page.request.post("/api/auth/logout");
    const meRes = await page.request.get("/api/auth/me");
    expect(meRes.status()).toBe(401);
  });

  test("login with bad password yields a clear error", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
    await page.getByLabel(/Mot de passe/i).fill("nope-not-real-password");
    await page.getByRole("button", { name: /Se connecter/i }).click();
    await expect(page.getByRole("alert")).toBeVisible({ timeout: 10_000 });
  });
});
