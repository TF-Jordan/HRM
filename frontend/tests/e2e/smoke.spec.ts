import { test, expect } from "@playwright/test";

test.describe("Phase 0 smoke", () => {
  test("unauthenticated /dashboard redirects to /login", async ({ page }) => {
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login(\?|$)/);
  });

  test("FR login page renders core elements", async ({ page }) => {
    await page.goto("/login");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Connexion");
    await expect(page.getByLabel(/Adresse e-mail/i)).toBeVisible();
    await expect(page.getByLabel(/Mot de passe/i)).toBeVisible();
    await expect(page.getByRole("button", { name: /Se connecter/i })).toBeVisible();
  });

  test("EN login page renders core elements", async ({ page }) => {
    await page.goto("/en/login");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Sign in");
    await expect(page.getByLabel(/Email address/i)).toBeVisible();
  });

  test("/api/health returns 200 and frontend up", async ({ request }) => {
    const res = await request.get("/api/health");
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.success).toBe(true);
    expect(body.frontend.status).toBe("up");
  });

  test("login form validates input on the server", async ({ request }) => {
    const res = await request.post("/api/auth/login", {
      data: { email: "not-an-email", password: "" },
      headers: { "Content-Type": "application/json" },
    });
    expect(res.status()).toBe(400);
    const body = await res.json();
    expect(body.success).toBe(false);
    expect(body.errorCode).toBe("VALIDATION");
  });
});
