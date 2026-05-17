import { test } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Visual snapshots", () => {
  test("FR login full page", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto("/login");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/login-fr.png", fullPage: true });
  });

  test("FR dashboard with KPIs", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/dashboard-fr.png", fullPage: true });
  });

  test("FR employees list", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/employees");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/employees-list.png", fullPage: true });
  });

  test("FR new employee form", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/employees/new");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/employees-new.png", fullPage: true });
  });

  test("FR employee detail 360", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/employees");
    await page.locator("table tbody tr").first().waitFor({ state: "visible" });
    await page.locator("table tbody tr").first().click();
    await page.waitForURL(/\/employees\/[0-9a-f-]+$/);
    // Wait for the matricule (mono font) to appear — proves data loaded
    await page.locator("text=/HRC-[0-9]+/").first().waitFor({ state: "visible" });
    await page.screenshot({ path: "tests/e2e/screenshots/employee-detail.png", fullPage: true });
  });
});
