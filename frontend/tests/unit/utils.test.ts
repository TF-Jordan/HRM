import { describe, expect, it } from "vitest";
import { cn, initials } from "@/lib/utils";

describe("cn", () => {
  it("merges tailwind classes deterministically", () => {
    expect(cn("p-2", "p-4")).toBe("p-4");
  });

  it("filters falsy values", () => {
    expect(cn("a", false && "b", null, undefined, "c")).toBe("a c");
  });
});

describe("initials", () => {
  it("uses two first letters of two words", () => {
    expect(initials("Jane Doe")).toBe("JD");
  });

  it("falls back to ? when null", () => {
    expect(initials(null)).toBe("?");
  });

  it("upper-cases", () => {
    expect(initials("jordan ngom")).toBe("JN");
  });
});
