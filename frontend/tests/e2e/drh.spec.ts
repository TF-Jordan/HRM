import { test, expect } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Phase 8 — DRH (trainings, skills, budgets, analytics)", () => {
  test("trainings page renders with create button", async ({ page }) => {
    await login(page);
    await page.goto("/trainings");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Formations");
    await expect(page.getByRole("button", { name: /Planifier une formation/i })).toBeVisible();
  });

  test("plan training end-to-end via BFF", async ({ page }) => {
    await login(page);
    const suffix = Math.random().toString(36).slice(2, 8);
    const res = await page.request.post("/api/hrm/trainings", {
      data: {
        intitule: `Formation Spring Boot ${suffix}`,
        organisme: "OpenClassrooms",
        dateDebut: "2026-06-01",
        dateFin: "2026-06-03",
        cout: "750000",
        nbPlaces: 10,
        lieu: "Yaoundé",
      },
      headers: { "Content-Type": "application/json" },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.data.intitule).toBe(`Formation Spring Boot ${suffix}`);
    expect(body.data.status).toBe("PLANNED");
  });

  test("plan training dialog opens", async ({ page }) => {
    await login(page);
    await page.goto("/trainings");
    await page.waitForLoadState("networkidle");
    await page.getByRole("button", { name: /Planifier une formation/i }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await expect(page.getByLabel("Intitulé")).toBeVisible();
  });

  test("training-budgets page renders with year filter", async ({ page }) => {
    await login(page);
    await page.goto("/training-budgets");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Budget formation");
    await expect(page.getByRole("button", { name: /Nouveau budget/i })).toBeVisible();
  });

  test("create training budget end-to-end via BFF", async ({ page }) => {
    await login(page);
    const annee = 2027 + Math.floor(Math.random() * 50);
    const res = await page.request.post("/api/hrm/training-budgets", {
      data: { annee, montantAlloue: "5000000" },
      headers: { "Content-Type": "application/json" },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.data.annee).toBe(annee);
    expect(Number(body.data.montantAlloue)).toBe(5000000);
  });

  test("skills page renders with catalogue tab", async ({ page }) => {
    await login(page);
    await page.goto("/skills");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Compétences");
    await expect(page.getByRole("tab", { name: /Catalogue/i })).toBeVisible();
    await expect(page.getByRole("tab", { name: /Évaluations employés/i })).toBeVisible();
  });

  test("create skill end-to-end via BFF", async ({ page }) => {
    await login(page);
    const suffix = Math.random().toString(36).slice(2, 8);
    const res = await page.request.post("/api/hrm/skills", {
      data: {
        name: `TypeScript ${suffix}`,
        categorie: "Technique",
        description: "Langage typé",
      },
      headers: { "Content-Type": "application/json" },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.data.name).toBe(`TypeScript ${suffix}`);
  });

  test("analytics page renders with KPI cards", async ({ page }) => {
    await login(page);
    await page.goto("/analytics");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Analytics RH");
    await expect(page.getByRole("button", { name: /Nouveau snapshot/i })).toBeVisible();
  });

  test("create KPI snapshot end-to-end via BFF", async ({ page }) => {
    await login(page);
    const month = String(Math.floor(Math.random() * 12) + 1).padStart(2, "0");
    const year = 2030 + Math.floor(Math.random() * 50);
    const res = await page.request.post("/api/hrm/kpi", {
      data: {
        periode: `${year}-${month}`,
        effectifTotal: 120,
        effectifActif: 115,
        tauxTurnover: "0.05",
        tauxAbsenteisme: "0.03",
        masseSalariale: "85000000",
        couvertureCompetences: "0.78",
      },
      headers: { "Content-Type": "application/json" },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.data.periode).toBe(`${year}-${month}`);
    expect(body.data.effectifTotal).toBe(120);
  });
});
