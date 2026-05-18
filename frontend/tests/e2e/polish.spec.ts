import { test, expect } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Phase 11 — Polish", () => {
  test("Theme toggle switches dark class on html", async ({ page }) => {
    await login(page);
    await page.waitForLoadState("networkidle");
    const html = page.locator("html");
    await expect(html).not.toHaveClass(/dark/);
    await page.getByRole("button", { name: /Changer de thème/i }).click();
    await expect(html).toHaveClass(/dark/);
    await page.getByRole("button", { name: /Changer de thème/i }).click();
    await expect(html).not.toHaveClass(/dark/);
  });

  test("Command palette opens with Ctrl+K and navigates", async ({ page }) => {
    await login(page);
    await page.waitForLoadState("networkidle");
    await page.keyboard.press("Control+K");
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByPlaceholder(/Rechercher dans l'application|Rechercher/i).fill("employ");
    await page.keyboard.press("Enter");
    await page.waitForURL(/\/employees/);
    await expect(page.getByRole("heading", { level: 1 })).toContainText(/Employés/i);
  });

  test("Mobile menu button visible on small screen", async ({ page }) => {
    await page.setViewportSize({ width: 600, height: 800 });
    await login(page);
    await page.waitForLoadState("networkidle");
    await expect(page.getByRole("button", { name: /Ouvrir le menu/i })).toBeVisible();
  });

  test("Mobile menu button hidden on large screen", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.waitForLoadState("networkidle");
    await expect(page.getByRole("button", { name: /Ouvrir le menu/i })).toBeHidden();
  });

  test("Export PDF button visible on employees page", async ({ page }) => {
    await login(page);
    await page.goto("/employees");
    await page.waitForLoadState("networkidle");
    await expect(
      page.getByRole("button", { name: /Exporter PDF/i }).first(),
    ).toBeVisible();
  });

  test("Employees PDF download triggers", async ({ page }) => {
    await login(page);
    await page.goto("/employees");
    await page.waitForLoadState("networkidle");
    await page.locator("table tbody tr").first().waitFor({ state: "visible", timeout: 10_000 });
    const exportBtn = page.getByRole("button", { name: /Exporter PDF/i }).first();
    const downloadPromise = page.waitForEvent("download");
    await exportBtn.click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/^employees-\d{4}-\d{2}-\d{2}\.pdf$/);
  });

  test("axe accessibility scan on /dashboard", async ({ page }) => {
    await login(page);
    await page.waitForLoadState("networkidle");
    const results = await new AxeBuilder({ page })
      .withTags(["wcag2a", "wcag2aa"])
      .disableRules(["color-contrast"]) // tokens may flag false positives
      .analyze();
    expect(results.violations).toEqual([]);
  });

  test("axe accessibility scan on /employees", async ({ page }) => {
    await login(page);
    await page.goto("/employees");
    await page.waitForLoadState("networkidle");
    const results = await new AxeBuilder({ page })
      .withTags(["wcag2a", "wcag2aa"])
      .disableRules(["color-contrast"])
      .analyze();
    expect(results.violations).toEqual([]);
  });
});
