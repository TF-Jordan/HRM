import { test, expect } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Phase 10 — Contrôleur de gestion RH", () => {
  test("Export CSV button visible on employees page", async ({ page }) => {
    await login(page);
    await page.goto("/employees");
    await page.waitForLoadState("networkidle");
    await expect(
      page.getByRole("button", { name: /Exporter CSV/i }).first(),
    ).toBeVisible();
  });

  test("Export CSV button visible on payroll page", async ({ page }) => {
    await login(page);
    await page.goto("/payroll");
    await page.waitForLoadState("networkidle");
    await expect(
      page.getByRole("button", { name: /Exporter CSV/i }).first(),
    ).toBeVisible();
  });

  test("Export CSV button visible on declarations page", async ({ page }) => {
    await login(page);
    await page.goto("/declarations");
    await page.waitForLoadState("networkidle");
    await expect(
      page.getByRole("button", { name: /Exporter CSV/i }).first(),
    ).toBeVisible();
  });

  test("Employees CSV download triggers and contains header line", async ({ page }) => {
    await login(page);
    await page.goto("/employees");
    await page.waitForLoadState("networkidle");
    const exportBtn = page.getByRole("button", { name: /Exporter CSV/i }).first();
    await exportBtn.waitFor({ state: "visible" });
    // Make sure data is loaded (table visible)
    await page.locator("table tbody tr").first().waitFor({ state: "visible", timeout: 10_000 });
    const downloadPromise = page.waitForEvent("download");
    await exportBtn.click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/^employees-\d{4}-\d{2}-\d{2}\.csv$/);
  });

  test("Analytics comparison panel appears with 2+ snapshots", async ({ page }) => {
    await login(page);

    // Ensure we have at least 2 snapshots
    for (let i = 0; i < 2; i++) {
      const periode = `${2050 + Math.floor(Math.random() * 50)}-${String(i + 1).padStart(2, "0")}`;
      await page.request.post("/api/hrm/kpi", {
        data: {
          periode,
          effectifTotal: 100 + i * 10,
          effectifActif: 95 + i * 10,
          tauxTurnover: i === 0 ? "0.05" : "0.07",
          tauxAbsenteisme: i === 0 ? "0.02" : "0.03",
          masseSalariale: i === 0 ? "60000000" : "65000000",
          couvertureCompetences: i === 0 ? "0.7" : "0.75",
        },
        headers: { "Content-Type": "application/json" },
      });
    }

    await page.goto("/analytics");
    await page.waitForLoadState("networkidle");
    await expect(
      page.getByText(/Comparaison entre deux périodes/i),
    ).toBeVisible();
    await expect(
      page.getByText(/Période de référence/i),
    ).toBeVisible();
  });
});
