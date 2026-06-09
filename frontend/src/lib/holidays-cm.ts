/**
 * Cameroon public holidays. Returns the holiday dates (YYYY-MM-DD) for a year.
 *
 * Fixed holidays per the labour code:
 *   01-01  Jour de l'An
 *   02-11  Fête de la Jeunesse
 *   05-01  Fête du Travail
 *   05-20  Fête Nationale
 *   08-15  Assomption
 *   10-01  Jour de la Réunification
 *   12-25  Noël
 *
 * Movable Christian holidays (Easter-based):
 *   Vendredi Saint, Lundi de Pâques, Ascension
 *
 * The 3 Islamic holidays (Aïd el-Fitr, Aïd el-Kébir, Mouloud) are set
 * annually by presidential decree and are NOT included here — they have to be
 * configured per tenant in settings-core when known.
 */

function pad(n: number): string {
  return n < 10 ? `0${n}` : String(n);
}

function format(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/**
 * Computus (Gauss algorithm) — Gregorian Easter Sunday for the given year.
 */
function easterSunday(year: number): Date {
  const a = year % 19;
  const b = Math.floor(year / 100);
  const c = year % 100;
  const d = Math.floor(b / 4);
  const e = b % 4;
  const f = Math.floor((b + 8) / 25);
  const g = Math.floor((b - f + 1) / 3);
  const h = (19 * a + b - d - g + 15) % 30;
  const i = Math.floor(c / 4);
  const k = c % 4;
  const l = (32 + 2 * e + 2 * i - h - k) % 7;
  const m = Math.floor((a + 11 * h + 22 * l) / 451);
  const month = Math.floor((h + l - 7 * m + 114) / 31); // 3 = March, 4 = April
  const day = ((h + l - 7 * m + 114) % 31) + 1;
  return new Date(Date.UTC(year, month - 1, day));
}

function addDays(date: Date, days: number): Date {
  const copy = new Date(date.getTime());
  copy.setUTCDate(copy.getUTCDate() + days);
  return copy;
}

export function holidaysCmForYear(year: number): Set<string> {
  const easter = easterSunday(year);
  const goodFriday = addDays(easter, -2);
  const easterMonday = addDays(easter, 1);
  const ascension = addDays(easter, 39);

  return new Set<string>([
    `${year}-01-01`,
    `${year}-02-11`,
    `${year}-05-01`,
    `${year}-05-20`,
    `${year}-08-15`,
    `${year}-10-01`,
    `${year}-12-25`,
    format(goodFriday),
    format(easterMonday),
    format(ascension),
  ]);
}

/**
 * Number of jours ouvrables (Mon–Sat) between two YYYY-MM-DD dates (inclusive),
 * excluding Sundays and Cameroon public holidays.
 *
 * This matches the Cameroon labour code definition: jours ouvrables = all days
 * except the weekly rest day (Sunday) and public holidays.
 */
export function workingDaysBetween(startIso: string, endIso: string): number {
  if (!startIso || !endIso) return 0;
  const start = new Date(startIso);
  const end = new Date(endIso);
  if (end < start) return 0;

  const holidayCache = new Map<number, Set<string>>();
  function holidaysFor(year: number): Set<string> {
    if (!holidayCache.has(year)) holidayCache.set(year, holidaysCmForYear(year));
    return holidayCache.get(year)!;
  }

  let count = 0;
  const cursor = new Date(start.getTime());
  while (cursor <= end) {
    const day = cursor.getUTCDay(); // 0 = Sun, 6 = Sat
    const iso = format(cursor);
    // Jours ouvrables: exclude only Sunday (day 0) and public holidays
    if (day !== 0 && !holidaysFor(cursor.getUTCFullYear()).has(iso)) {
      count += 1;
    }
    cursor.setUTCDate(cursor.getUTCDate() + 1);
  }
  return count;
}

export function isHolidayCm(iso: string): boolean {
  const year = Number(iso.slice(0, 4));
  return holidaysCmForYear(year).has(iso);
}
