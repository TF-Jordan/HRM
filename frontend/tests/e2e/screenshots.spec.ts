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
    await page.locator("text=/HRC-[0-9]+/").first().waitFor({ state: "visible" });
    await page.screenshot({ path: "tests/e2e/screenshots/employee-detail.png", fullPage: true });
  });

  test("FR my leaves (Phase 3)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/leaves/my");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/my-leaves.png", fullPage: true });
  });

  test("FR my loans (Phase 3)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/loans/my");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/my-loans.png", fullPage: true });
  });

  test("FR my expenses (Phase 3)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/expenses/my");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/my-expenses.png", fullPage: true });
  });

  test("FR my profile (Phase 3)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/me/profile");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/my-profile.png", fullPage: true });
  });

  test("FR pending leaves (Phase 4)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/leaves/pending");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/pending-leaves.png", fullPage: true });
  });

  test("FR mission orders (Phase 4)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/mission-orders");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/mission-orders.png", fullPage: true });
  });

  test("FR reviews (Phase 4)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/reviews");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/reviews.png", fullPage: true });
  });

  test("FR expenses approve (Phase 4)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/expenses/approve");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/expenses-approve.png", fullPage: true });
  });

  test("FR payroll runs (Phase 5)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/payroll");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/payroll-runs.png", fullPage: true });
  });

  test("FR payroll run detail (Phase 5)", async ({ page, request }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    const runs = await page.request.get("/api/hrm/payroll/runs");
    const data = await runs.json();
    const valid = data.data?.find((r: { nbEmployes: number }) => r.nbEmployes > 0);
    if (valid) {
      await page.goto(`/payroll/runs/${valid.id}`);
      await page.waitForLoadState("networkidle");
      await page.screenshot({ path: "tests/e2e/screenshots/payroll-run-detail.png", fullPage: true });
    }
  });

  test("FR approve loans (Phase 5)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/loans/approve");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/approve-loans.png", fullPage: true });
  });

  test("FR declarations (Phase 6)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/declarations");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/declarations.png", fullPage: true });
  });

  test("FR recruitment offers (Phase 7)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/recruitment");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/recruitment-offers.png", fullPage: true });
  });

  test("FR recruitment kanban (Phase 7)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/recruitment");
    await page.getByRole("tab", { name: /Candidatures/i }).click();
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/recruitment-kanban.png", fullPage: true });
  });

  test("FR trainings list (Phase 8)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/trainings");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/trainings.png", fullPage: true });
  });

  test("FR training budgets (Phase 8)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/training-budgets");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/training-budgets.png", fullPage: true });
  });

  test("FR skills catalogue (Phase 8)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/skills");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/skills.png", fullPage: true });
  });

  test("FR analytics (Phase 8)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/analytics");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/analytics.png", fullPage: true });
  });

  test("FR medical empty (Phase 9)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/medical");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/medical.png", fullPage: true });
  });

  test("FR medical with visits (Phase 9)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/medical");
    await page.waitForLoadState("networkidle");
    await page.getByRole("combobox").first().click();
    await page.getByRole("option").first().click();
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/medical-visits.png", fullPage: true });
  });

  test("FR analytics with comparison (Phase 10)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    // Seed 2 snapshots if needed
    for (let i = 0; i < 2; i++) {
      const periode = `${2070 + Math.floor(Math.random() * 30)}-${String(i + 1).padStart(2, "0")}`;
      await page.request.post("/api/hrm/kpi", {
        data: {
          periode,
          effectifTotal: 150 + i * 5,
          effectifActif: 140 + i * 5,
          tauxTurnover: i === 0 ? "0.04" : "0.06",
          tauxAbsenteisme: i === 0 ? "0.025" : "0.03",
          masseSalariale: i === 0 ? "75000000" : "80000000",
          couvertureCompetences: i === 0 ? "0.72" : "0.78",
        },
        headers: { "Content-Type": "application/json" },
      });
    }
    await page.goto("/analytics");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/analytics-comparison.png", fullPage: true });
  });

  test("FR employees with export CSV (Phase 10)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.goto("/employees");
    await page.waitForLoadState("networkidle");
    await page.screenshot({ path: "tests/e2e/screenshots/employees-with-export.png", fullPage: true });
  });

  test("FR dashboard dark mode (Phase 11)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.waitForLoadState("networkidle");
    await page.getByRole("button", { name: /Changer de thème/i }).click();
    await page.waitForTimeout(150);
    await page.screenshot({ path: "tests/e2e/screenshots/dashboard-dark.png", fullPage: true });
  });

  test("FR command palette open (Phase 11)", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await page.waitForLoadState("networkidle");
    await page.keyboard.press("Control+K");
    await page.waitForTimeout(200);
    await page.screenshot({ path: "tests/e2e/screenshots/command-palette.png", fullPage: false });
  });

  test("FR mobile sidebar drawer (Phase 11)", async ({ page }) => {
    await page.setViewportSize({ width: 600, height: 900 });
    await login(page);
    await page.waitForLoadState("networkidle");
    await page.getByRole("button", { name: /Ouvrir le menu/i }).click();
    await page.waitForTimeout(200);
    await page.screenshot({ path: "tests/e2e/screenshots/mobile-sidebar.png", fullPage: true });
  });
});
