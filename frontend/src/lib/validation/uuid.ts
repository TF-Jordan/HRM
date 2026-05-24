import { z } from "zod";

/**
 * Lenient UUID validator — accepts all hex-formatted UUIDs including
 * non-standard versions (e.g. KSM seed UUIDs like 00000001-0000-0000-0000-000000000003).
 *
 * Zod 4's z.uuid() enforces RFC 4122 version bits [1-8] which rejects
 * seed/synthetic UUIDs used by the KSM backend.
 */
export const uuidLike = z
  .string()
  .regex(
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i,
    "Invalid UUID",
  );
