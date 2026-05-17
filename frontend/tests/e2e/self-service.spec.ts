import { test, expect } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Phase 3 — Employee self-service", () => {
  test("my leaves page renders with employee data", async ({ page }) => {
    await login(page);
    await page.goto("/leaves/my");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Mes congés");
  });

  test("my loans page renders with employee data", async ({ page }) => {
    await login(page);
    await page.goto("/loans/my");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Mes avances");
  });

  test("my expenses page renders with employee data", async ({ page }) => {
    await login(page);
    await page.goto("/expenses/my");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Mes notes de frais");
  });

  test("my profile shows session + employee + permissions", async ({ page }) => {
    await login(page);
    await page.goto("/me/profile");
    await expect(page.getByText(/admin@hrcore\.local/).first()).toBeVisible();
    await expect(page.getByText(/hrm:/).first()).toBeVisible({ timeout: 10_000 });
  });

  test("leave dialog opens with all fields", async ({ page }) => {
    await login(page);
    await page.goto("/leaves/my");
    await page.getByRole("button", { name: /Nouvelle demande/i }).click();
    await expect(page.locator("#dateDebut")).toBeVisible();
    await expect(page.locator("#dateFin")).toBeVisible();
    await expect(page.locator("#motif")).toBeVisible();
    await expect(page.locator("#leaveType")).toBeVisible();
  });
});
