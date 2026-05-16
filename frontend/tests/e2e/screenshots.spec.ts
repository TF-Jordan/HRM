import { test } from "@playwright/test";

test.describe("Visual snapshots (Phase 0)", () => {
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
});
