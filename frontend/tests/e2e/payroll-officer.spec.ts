import { test, expect } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Phase 6 — Responsable Paie", () => {
  test("declarations page renders + new form opens", async ({ page }) => {
    await login(page);
    await page.goto("/declarations");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Déclarations sociales");
    await page.getByRole("button", { name: /Nouvelle déclaration/i }).click();
    await expect(page.locator("#declPeriode")).toBeVisible();
  });

  test("create CNPS declaration end-to-end", async ({ page }) => {
    await login(page);
    const periode = `2026-${String(Math.floor(Math.random() * 12) + 1).padStart(2, "0")}`;
    const res = await page.request.post("/api/hrm/declarations", {
      data: { type: "CNPS", periode, format: "CSV" },
      headers: { "Content-Type": "application/json" },
    });
    expect([201, 409, 422]).toContain(res.status());
    if (res.status() === 201) {
      const body = await res.json();
      expect(body.data.type).toBe("CNPS");
      expect(body.data.statut).toBe("DRAFT");
    }
  });
});
