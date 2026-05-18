import { test, expect } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Phase 7 — Recrutement", () => {
  test("recruitment page renders with tabs", async ({ page }) => {
    await login(page);
    await page.goto("/recruitment");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Recrutement");
    await expect(page.getByRole("tab", { name: /Offres/i })).toBeVisible();
    await expect(page.getByRole("tab", { name: /Candidatures/i })).toBeVisible();
  });

  test("create job offer end-to-end", async ({ page }) => {
    await login(page);
    const suffix = Math.random().toString(36).slice(2, 8);
    const res = await page.request.post("/api/hrm/job-offers", {
      data: { poste: `Dev FullStack ${suffix}`, departement: "IT", localisation: "Yaoundé" },
      headers: { "Content-Type": "application/json" },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.data.poste).toBe(`Dev FullStack ${suffix}`);
    expect(body.data.status).toBe("DRAFT");
  });
});
