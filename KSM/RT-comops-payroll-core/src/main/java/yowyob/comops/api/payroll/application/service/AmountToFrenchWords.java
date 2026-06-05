package yowyob.comops.api.payroll.application.service;

import java.math.BigDecimal;

/**
 * Converts an integer amount to French words — required on a legal payslip, where the net pay
 * must also appear "en toutes lettres" (Cameroon Labour Code, art. 68). Handles French specifics:
 * 17–19 (dix-sept…), 70–79 (soixante-dix…), 80 (quatre-vingts), 90–99 (quatre-vingt-dix…),
 * the "et un/et onze" liaison, and the invariable "mille".
 */
public final class AmountToFrenchWords {

    private AmountToFrenchWords() {}

    private static final String[] UNITS = {
            "zéro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix",
            "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf"
    };

    /** Renders an amount with its currency label, e.g. "trois cent mille francs CFA". */
    public static String moneyInWords(BigDecimal amount, String currencyLabel) {
        long value = amount == null ? 0L : amount.setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
        String words = words(value);
        String capitalised = Character.toUpperCase(words.charAt(0)) + words.substring(1);
        return capitalised + " " + currencyLabel;
    }

    public static String words(long n) {
        if (n < 0) {
            return "moins " + words(-n);
        }
        if (n == 0) {
            return "zéro";
        }
        long milliards = n / 1_000_000_000L;
        long millions = (n / 1_000_000L) % 1000;
        long thousands = (n / 1000L) % 1000;
        long units = n % 1000;

        StringBuilder sb = new StringBuilder();
        appendScale(sb, milliards, "milliard", "milliards");
        appendScale(sb, millions, "million", "millions");
        appendThousands(sb, thousands);
        if (units > 0) {
            append(sb, threeDigits(units));
        }
        return sb.toString().trim();
    }

    private static void appendScale(StringBuilder sb, long n, String singular, String plural) {
        if (n == 0) {
            return;
        }
        append(sb, threeDigits(n));
        append(sb, n > 1 ? plural : singular);
    }

    private static void appendThousands(StringBuilder sb, long thousands) {
        if (thousands == 0) {
            return;
        }
        if (thousands > 1) {
            append(sb, threeDigits(thousands));
        }
        append(sb, "mille"); // invariable, and "un mille" → "mille"
    }

    private static String threeDigits(long n) {
        StringBuilder sb = new StringBuilder();
        long hundreds = n / 100;
        long rest = n % 100;
        if (hundreds == 1) {
            sb.append("cent");
        } else if (hundreds > 1) {
            sb.append(UNITS[(int) hundreds]).append(" cent");
            if (rest == 0) {
                sb.append('s'); // deux cents
            }
        }
        if (rest > 0) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(tens(rest));
        }
        return sb.toString();
    }

    private static String tens(long n) {
        if (n < 20) {
            return UNITS[(int) n];
        }
        long ten = n / 10;
        long unit = n % 10;
        return switch ((int) ten) {
            case 2, 3, 4, 5, 6 -> tensWord(ten) + liaison(unit);
            case 7 -> "soixante" + seventyLiaison(unit);
            case 8 -> unit == 0 ? "quatre-vingts" : "quatre-vingt-" + UNITS[(int) unit];
            case 9 -> "quatre-vingt-" + UNITS[(int) (10 + unit)];
            default -> UNITS[(int) n];
        };
    }

    private static String tensWord(long ten) {
        return switch ((int) ten) {
            case 2 -> "vingt";
            case 3 -> "trente";
            case 4 -> "quarante";
            case 5 -> "cinquante";
            case 6 -> "soixante";
            default -> "";
        };
    }

    /** For 20–69: "et un" only for the unit 1; otherwise "-<unit>"; nothing for 0. */
    private static String liaison(long unit) {
        if (unit == 0) return "";
        if (unit == 1) return " et un";
        return "-" + UNITS[(int) unit];
    }

    /** For 70–79: 71 → "et onze", 70/72–79 → "-dix", "-douze"… */
    private static String seventyLiaison(long unit) {
        if (unit == 1) return " et onze";
        return "-" + UNITS[(int) (10 + unit)];
    }

    private static void append(StringBuilder sb, String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(token);
    }
}
