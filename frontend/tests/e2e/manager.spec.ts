import { test, expect } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

test.describe("Phase 4 — Manager", () => {
  test("pending leaves page renders", async ({ page }) => {
    await login(page);
    await page.goto("/leaves/pending");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Congés en attente");
  });

  test("mission orders page renders + form opens", async ({ page }) => {
    await login(page);
    await page.goto("/mission-orders");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Ordres de mission");
    await page.getByRole("button", { name: /Nouvel ordre/i }).click();
    await expect(page.locator("#destination")).toBeVisible();
  });

  test("expenses to approve page renders", async ({ page }) => {
    await login(page);
    await page.goto("/expenses/approve");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("approuver");
  });

  test("reviews page renders + form opens", async ({ page }) => {
    await login(page);
    await page.goto("/reviews");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Évaluations");
    await page.getByRole("button", { name: /Nouvelle évaluation/i }).click();
    await expect(page.locator("#reviewPeriode")).toBeVisible();
  });

  test("create mission order end-to-end", async ({ page }) => {
    await login(page);
    const meRes = await page.request.get("/api/hrm/me/employee");
    const me = await meRes.json();
    const employeeId = me.data.id;

    const res = await page.request.post("/api/hrm/mission-orders", {
      data: {
        employeeId,
        destination: "Douala",
        objet: "Audit comptable",
        dateDebut: "2026-09-01",
        dateFin: "2026-09-05",
        montantAvance: 150000,
        centreCout: "AUDIT",
      },
      headers: { "Content-Type": "application/json" },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.data.destination).toBe("Douala");
    expect(body.data.status).toBe("DRAFT");
  });
});
