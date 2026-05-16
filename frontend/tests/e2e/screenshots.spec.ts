import { test } from "@playwright/test";

test.describe("Visual snapshots (Phase 0 + 1)", () => {
  test("FR login full page", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto("/login");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/login-fr.png", fullPage: true });
  });

  test("EN login full page", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto("/en/login");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/login-en.png", fullPage: true });
  });

  test("FR dashboard authenticated", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto("/login");
    await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
    await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
    await page.getByRole("button", { name: /Se connecter/i }).click();
    await page.waitForURL(/\/dashboard$/);
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/dashboard-fr.png", fullPage: true });
  });
});
