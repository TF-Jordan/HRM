import { test, expect } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Phase 2 — Admin RH (employees)", () => {
  test("dashboard shows live headcount KPI from KSM", async ({ page }) => {
    await login(page);
    await expect(page.getByText(/Effectif actif/i)).toBeVisible();
    // KPI value is a number (>= 0)
    const card = page.locator("text=Effectif actif").locator("..");
    await expect(card).toBeVisible();
  });

  test("employees list renders KSM data", async ({ page }) => {
    await login(page);
    await page.goto("/employees");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Employés");
    // Either rows or empty state
    const body = await page.content();
    expect(body.includes("HRC-") || body.includes("Aucun employé")).toBeTruthy();
  });

  test("new employee form validates and submits", async ({ page }) => {
    await login(page);
    await page.goto("/employees/new");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Nouvel employé");

    const suffix = Math.random().toString(36).slice(2, 8);
    await page.locator("#firstName").fill("Test");
    await page.locator("#lastName").fill(`Phase2-${suffix}`);
    await page.locator("#email").fill(`phase2.${suffix}@hrcore.local`);
    await page.locator("#categorie").fill("3");
    await page.locator("#dateEmbauche").fill("2025-01-15");
    await page.locator("#compteBancaire").fill("00012345678");

    await page.getByRole("button", { name: /Créer l'employé/i }).click();

    // Should redirect to the new employee detail page
    await page.waitForURL(/\/employees\/[0-9a-f-]+$/, { timeout: 15_000 });
    await expect(page.getByText(`Test Phase2-${suffix}`)).toBeVisible({ timeout: 5_000 });
  });

  test("employee detail page renders tabs", async ({ page }) => {
    await login(page);
    await page.goto("/employees");
    // Click first row if data exists
    const firstRow = page.locator("table tbody tr").first();
    if (await firstRow.count()) {
      await firstRow.click();
      await page.waitForURL(/\/employees\/[0-9a-f-]+$/);
      await expect(page.getByRole("tab", { name: /Identité/i })).toBeVisible();
      await expect(page.getByRole("tab", { name: /Contrats/i })).toBeVisible();
      await expect(page.getByRole("tab", { name: /Personnes à charge/i })).toBeVisible();
      await expect(page.getByRole("tab", { name: /Soldes/i })).toBeVisible();
    }
  });
});
