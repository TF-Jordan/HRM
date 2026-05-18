import { test, expect } from "@playwright/test";

async function login(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByLabel(/Adresse e-mail/i).fill("admin@hrcore.local");
  await page.getByLabel(/Mot de passe/i).fill("Admin@HRCore2025!");
  await page.getByRole("button", { name: /Se connecter/i }).click();
  await page.waitForURL(/\/dashboard$/);
}

async function getMyEmployeeId(page: import("@playwright/test").Page): Promise<string> {
  const res = await page.request.get("/api/hrm/me/employee");
  const body = await res.json();
  return body.data.id as string;
}

test.describe("Phase 9 — Médecin du travail", () => {
  test("medical page renders with employee selector", async ({ page }) => {
    await login(page);
    await page.goto("/medical");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("Suivi médical");
    await expect(page.getByText(/Sélectionnez un employé/i)).toBeVisible();
  });

  test("create medical visit end-to-end via BFF", async ({ page }) => {
    await login(page);
    const employeeId = await getMyEmployeeId(page);
    const res = await page.request.post("/api/hrm/medical/visits", {
      data: {
        employeeId,
        dateVisite: "2026-06-01",
        medecin: "Dr Mballa",
        resultatAptitude: "APTE",
        restrictions: null,
        prochaineEcheance: "2027-06-01",
        certificatFileId: null,
      },
      headers: { "Content-Type": "application/json" },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.data.employeeId).toBe(employeeId);
    expect(body.data.resultatAptitude).toBe("APTE");
  });

  test("create medical certificate end-to-end via BFF", async ({ page }) => {
    await login(page);
    const employeeId = await getMyEmployeeId(page);
    const res = await page.request.post("/api/hrm/medical/certificates", {
      data: {
        employeeId,
        typeCertificat: "VISITE_PERIODIQUE",
        dateEmission: "2026-06-01",
        dateExpiration: "2027-06-01",
        statut: "ACTIF",
        fichierId: null,
      },
      headers: { "Content-Type": "application/json" },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.data.typeCertificat).toBe("VISITE_PERIODIQUE");
    expect(body.data.statut).toBe("ACTIF");
  });

  test("visit dialog opens after selecting employee", async ({ page }) => {
    await login(page);
    await page.goto("/medical");
    await page.waitForLoadState("networkidle");
    await page.getByRole("combobox").first().click();
    await page.getByRole("option").first().click();
    await page.waitForLoadState("networkidle");
    await page.getByRole("button", { name: /Nouvelle visite/i }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await expect(page.getByLabel("Médecin")).toBeVisible();
  });
});
