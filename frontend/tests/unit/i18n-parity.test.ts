import { describe, expect, it } from "vitest";
import { readdirSync, readFileSync } from "node:fs";
import { join } from "node:path";

const MESSAGES_DIR = join(__dirname, "..", "..", "src", "i18n", "messages");

function flattenKeys(obj: unknown, prefix = ""): string[] {
  if (typeof obj !== "object" || obj === null) return [prefix.slice(0, -1)];
  const keys: string[] = [];
  for (const [k, v] of Object.entries(obj as Record<string, unknown>)) {
    if (typeof v === "object" && v !== null) {
      keys.push(...flattenKeys(v, `${prefix}${k}.`));
    } else {
      keys.push(`${prefix}${k}`);
    }
  }
  return keys.sort();
}

function loadAllKeys(locale: string): Map<string, string[]> {
  const dir = join(MESSAGES_DIR, locale);
  const files = readdirSync(dir).filter((f) => f.endsWith(".json"));
  const map = new Map<string, string[]>();
  for (const file of files) {
    const content = JSON.parse(readFileSync(join(dir, file), "utf-8"));
    map.set(file, flattenKeys(content));
  }
  return map;
}

describe("i18n parity FR↔EN", () => {
  it("every fr/*.json file exists in en/", () => {
    const frFiles = readdirSync(join(MESSAGES_DIR, "fr"));
    const enFiles = readdirSync(join(MESSAGES_DIR, "en"));
    expect(enFiles.sort()).toEqual(frFiles.sort());
  });

  it("every key in fr is present in en (and vice versa) for each namespace", () => {
    const fr = loadAllKeys("fr");
    const en = loadAllKeys("en");
    for (const [file, frKeys] of fr) {
      const enKeys = en.get(file) ?? [];
      const missingInEn = frKeys.filter((k) => !enKeys.includes(k));
      const missingInFr = enKeys.filter((k) => !frKeys.includes(k));
      expect({ file, missingInEn, missingInFr }).toEqual({
        file,
        missingInEn: [],
        missingInFr: [],
      });
    }
  });
});
