import { test, expect } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Phase 5 — Comptable/DAF", () => {
  test("payroll page renders + launch form opens", async ({ page }) => {
    await login(page);
    await page.goto("/payroll");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Cycles de paie");
    await page.getByRole("button", { name: /Lancer un cycle/i }).click();
    await expect(page.locator("#periode")).toBeVisible();
  });

  test("approve loans page renders", async ({ page }) => {
    await login(page);
    await page.goto("/loans/approve");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Avances à approuver");
  });

  test("run payroll cycle end-to-end", async ({ page }) => {
    await login(page);
    const periode = `2026-${String(Math.floor(Math.random() * 12) + 1).padStart(2, "0")}`;
    const res = await page.request.post("/api/hrm/payroll/run", {
      data: { periode, agencyId: null },
      headers: { "Content-Type": "application/json" },
    });
    expect([201, 409]).toContain(res.status());
    if (res.status() === 201) {
      const body = await res.json();
      expect(body.data.periode).toBe(periode);
      expect(body.data.status).toBe("CALCULATED");
    }
  });
});
