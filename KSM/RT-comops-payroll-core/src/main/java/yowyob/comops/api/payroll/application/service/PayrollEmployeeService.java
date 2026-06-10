package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.application.port.in.ManagePayrollEmployeeUseCase;
import yowyob.comops.api.payroll.application.port.out.PayrollDataSourceRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollDataSourceRepository.Source;
import yowyob.comops.api.payroll.application.port.out.PayrollEmployeeRepository;
import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;
import yowyob.comops.api.payroll.domain.model.PayrollEmployee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * CRUD + CSV import over payroll-owned employee records (standalone payroll mode).
 *
 * The CSV import is the primary on-ramp: header-driven (column order free), comma or
 * semicolon separated, upserting by matricule so re-imports refresh rather than duplicate.
 * A successful import flips the organization's payroll data source to {@code LOCAL} so the
 * next run reads these rows instead of hrm-core.
 */
@Service
@Profile("!test-memory")
public class PayrollEmployeeService implements ManagePayrollEmployeeUseCase {

    /** Recognised CSV headers (lower-cased); aliases map common French spellings. */
    private static final Map<String, String> HEADER_ALIASES = Map.ofEntries(
            Map.entry("matricule", "matricule"),
            Map.entry("displayname", "displayName"),
            Map.entry("nom", "displayName"),
            Map.entry("nomcomplet", "displayName"),
            Map.entry("email", "email"),
            Map.entry("numcnps", "socialSecurityNo"),
            Map.entry("cnps", "socialSecurityNo"),
            Map.entry("socialsecurityno", "socialSecurityNo"),
            Map.entry("categorie", "categorie"),
            Map.entry("echelon", "echelon"),
            Map.entry("departement", "departmentCode"),
            Map.entry("departmentcode", "departmentCode"),
            Map.entry("dateembauche", "hireDate"),
            Map.entry("hiredate", "hireDate"),
            Map.entry("situationmatrimoniale", "maritalStatus"),
            Map.entry("maritalstatus", "maritalStatus"),
            Map.entry("enfants", "dependentChildren"),
            Map.entry("dependentchildren", "dependentChildren"),
            Map.entry("salairebase", "baseSalary"),
            Map.entry("basesalary", "baseSalary"),
            Map.entry("avantagesnature", "benefitsInKind"),
            Map.entry("benefitsinkind", "benefitsInKind"),
            Map.entry("poste", "position"),
            Map.entry("position", "position"),
            Map.entry("modepaiement", "paymentChannel"),
            Map.entry("paymentchannel", "paymentChannel"),
            Map.entry("compte", "accountRef"),
            Map.entry("accountref", "accountRef"));

    private final PayrollEmployeeRepository employeeRepository;
    private final PayrollDataSourceRepository dataSourceRepository;

    public PayrollEmployeeService(PayrollEmployeeRepository employeeRepository,
                                  PayrollDataSourceRepository dataSourceRepository) {
        this.employeeRepository = employeeRepository;
        this.dataSourceRepository = dataSourceRepository;
    }

    // ------------------------------------------------------------------ CRUD

    @Override
    public Flux<PayrollEmployee> list(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> employeeRepository.findByOrganization(ctx.tenantId(), organizationId));
    }

    @Override
    public Mono<PayrollEmployee> get(UUID payrollEmployeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> require(ctx, payrollEmployeeId));
    }

    @Override
    public Mono<PayrollEmployee> create(UpsertPayrollEmployeeCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                employeeRepository
                        .findByMatricule(ctx.tenantId(), command.organizationId(), command.matricule())
                        .flatMap(existing -> Mono.<PayrollEmployee>error(new IllegalStateException(
                                "An employee with matricule " + command.matricule() + " already exists")))
                        .switchIfEmpty(Mono.defer(() ->
                                employeeRepository.save(fromCommand(ctx, command)))));
    }

    @Override
    public Mono<PayrollEmployee> update(UUID payrollEmployeeId, UpsertPayrollEmployeeCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                require(ctx, payrollEmployeeId)
                        .map(existing -> existing.updateFrom(fromCommand(ctx, command)))
                        .flatMap(employeeRepository::save));
    }

    @Override
    public Mono<PayrollEmployee> deactivate(UUID payrollEmployeeId, LocalDate departureDate) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                require(ctx, payrollEmployeeId)
                        .map(e -> e.deactivate(departureDate))
                        .flatMap(employeeRepository::save));
    }

    // ------------------------------------------------------------ data source

    @Override
    public Mono<Source> getDataSource(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> dataSourceRepository.get(ctx.tenantId(), organizationId));
    }

    @Override
    public Mono<Void> setDataSource(UUID organizationId, Source source) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> dataSourceRepository.set(ctx.tenantId(), organizationId, source));
    }

    // ------------------------------------------------------------- CSV import

    @Override
    public Mono<CsvImportReport> importCsv(UUID organizationId, String csv) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                doImport(ctx, organizationId, csv));
    }

    private Mono<CsvImportReport> doImport(TenantContext ctx, UUID organizationId, String csv) {
        if (csv == null || csv.isBlank()) {
            return Mono.error(new IllegalArgumentException("CSV content is empty"));
        }
        List<String> lines = csv.lines().filter(l -> !l.isBlank()).toList();
        if (lines.size() < 2) {
            return Mono.error(new IllegalArgumentException("CSV must contain a header and at least one row"));
        }
        char sep = lines.get(0).chars().filter(c -> c == ';').count()
                > lines.get(0).chars().filter(c -> c == ',').count() ? ';' : ',';
        Map<String, Integer> columns = parseHeader(lines.get(0), sep);
        if (!columns.containsKey("matricule") || !columns.containsKey("displayName")
                || !columns.containsKey("hireDate") || !columns.containsKey("baseSalary")) {
            return Mono.error(new IllegalArgumentException(
                    "CSV header must contain at least: matricule, nom (displayName), dateEmbauche (hireDate), salaireBase (baseSalary)"));
        }

        List<CsvRowError> errors = new ArrayList<>();
        List<UpsertPayrollEmployeeCommand> commands = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNo = i + 1;
            try {
                commands.add(parseRow(organizationId, splitCsvLine(lines.get(i), sep), columns, lineNo));
            } catch (RuntimeException ex) {
                errors.add(new CsvRowError(lineNo, safeCell(lines.get(i), sep, columns.get("matricule")),
                        ex.getMessage()));
            }
        }

        int[] created = {0};
        int[] updated = {0};
        return Flux.fromIterable(commands)
                .concatMap(cmd -> employeeRepository
                        .findByMatricule(ctx.tenantId(), organizationId, cmd.matricule())
                        .flatMap(existing -> {
                            updated[0]++;
                            return employeeRepository.save(existing.updateFrom(fromCommand(ctx, cmd)));
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            created[0]++;
                            return employeeRepository.save(fromCommand(ctx, cmd));
                        }))
                        .onErrorResume(ex -> {
                            errors.add(new CsvRowError(-1, cmd.matricule(), ex.getMessage()));
                            return Mono.empty();
                        }))
                .then(Mono.defer(() -> created[0] + updated[0] > 0
                        ? dataSourceRepository.set(ctx.tenantId(), organizationId, Source.LOCAL)
                        : Mono.empty()))
                .then(Mono.fromSupplier(() -> new CsvImportReport(
                        lines.size() - 1, created[0], updated[0], List.copyOf(errors))));
    }

    private Map<String, Integer> parseHeader(String headerLine, char sep) {
        String[] cells = splitCsvLine(headerLine, sep);
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < cells.length; i++) {
            String key = cells[i].strip().toLowerCase(Locale.ROOT)
                    .replace("_", "").replace(" ", "").replace("é", "e").replace("è", "e");
            String canonical = HEADER_ALIASES.get(key);
            if (canonical != null) {
                columns.put(canonical, i);
            }
        }
        return columns;
    }

    private UpsertPayrollEmployeeCommand parseRow(UUID organizationId, String[] cells,
                                                  Map<String, Integer> columns, int lineNo) {
        String matricule = requiredCell(cells, columns, "matricule", lineNo);
        String displayName = requiredCell(cells, columns, "displayName", lineNo);
        LocalDate hireDate = parseDate(requiredCell(cells, columns, "hireDate", lineNo), lineNo);
        BigDecimal baseSalary = parseAmount(requiredCell(cells, columns, "baseSalary", lineNo), lineNo);

        return new UpsertPayrollEmployeeCommand(
                organizationId,
                null,
                matricule,
                displayName,
                cell(cells, columns, "email"),
                cell(cells, columns, "socialSecurityNo"),
                parseIntOrDefault(cell(cells, columns, "categorie"), 1),
                cell(cells, columns, "echelon"),
                cell(cells, columns, "departmentCode"),
                hireDate,
                cell(cells, columns, "maritalStatus"),
                parseIntOrDefault(cell(cells, columns, "dependentChildren"), 0),
                baseSalary,
                parseAmountOrZero(cell(cells, columns, "benefitsInKind")),
                cell(cells, columns, "position"),
                cell(cells, columns, "paymentChannel"),
                cell(cells, columns, "accountRef"));
    }

    // ------------------------------------------------------------- low level

    private PayrollEmployee fromCommand(TenantContext ctx, UpsertPayrollEmployeeCommand c) {
        return PayrollEmployee.create(ctx.tenantId(), c.organizationId(), c.agencyId(),
                c.matricule(), c.displayName(), blankToNull(c.email()),
                blankToNull(c.socialSecurityNo()),
                c.categorie() == null ? 1 : c.categorie(), blankToNull(c.echelon()),
                blankToNull(c.departmentCode()), c.hireDate(),
                parseMarital(c.maritalStatus()),
                c.dependentChildren() == null ? 0 : c.dependentChildren(),
                c.baseSalary(), c.benefitsInKind(), blankToNull(c.position()),
                parseChannel(c.paymentChannel()), blankToNull(c.accountRef()));
    }

    private Mono<PayrollEmployee> require(TenantContext ctx, UUID id) {
        return employeeRepository.findById(ctx.tenantId(), id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Payroll employee not found")));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static MaritalStatus parseMarital(String value) {
        if (value == null || value.isBlank()) return MaritalStatus.SINGLE;
        return switch (value.strip().toUpperCase(Locale.ROOT)) {
            case "MARRIED", "MARIE", "MARIÉ", "MARIEE", "MARIÉE" -> MaritalStatus.MARRIED;
            case "DIVORCED", "DIVORCE", "DIVORCÉ", "DIVORCEE", "DIVORCÉE" -> MaritalStatus.DIVORCED;
            case "WIDOWED", "VEUF", "VEUVE" -> MaritalStatus.WIDOWED;
            default -> MaritalStatus.SINGLE;
        };
    }

    private static PaymentChannel parseChannel(String value) {
        if (value == null || value.isBlank()) return PaymentChannel.CASH;
        String v = value.strip().toUpperCase(Locale.ROOT).replace(" ", "_").replace("-", "_");
        return switch (v) {
            case "BANK_TRANSFER", "VIREMENT", "BANQUE" -> PaymentChannel.BANK_TRANSFER;
            case "MTN_MOBILE_MONEY", "MTN" -> PaymentChannel.MTN_MOBILE_MONEY;
            case "ORANGE_MONEY", "ORANGE" -> PaymentChannel.ORANGE_MONEY;
            default -> PaymentChannel.CASH;
        };
    }

    private static LocalDate parseDate(String value, int lineNo) {
        try {
            return LocalDate.parse(value.strip());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "line " + lineNo + ": invalid date '" + value + "' (expected YYYY-MM-DD)");
        }
    }

    private static BigDecimal parseAmount(String value, int lineNo) {
        try {
            return new BigDecimal(value.strip().replace(" ", "").replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("line " + lineNo + ": invalid amount '" + value + "'");
        }
    }

    private static BigDecimal parseAmountOrZero(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(value.strip().replace(" ", "").replace(",", "."));
    }

    private static Integer parseIntOrDefault(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static String requiredCell(String[] cells, Map<String, Integer> columns,
                                       String column, int lineNo) {
        String value = cell(cells, columns, column);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("line " + lineNo + ": missing required column '" + column + "'");
        }
        return value.strip();
    }

    private static String cell(String[] cells, Map<String, Integer> columns, String column) {
        Integer idx = columns.get(column);
        if (idx == null || idx >= cells.length) return null;
        String value = cells[idx].strip();
        return value.isEmpty() ? null : value;
    }

    private static String safeCell(String line, char sep, Integer idx) {
        if (idx == null) return "?";
        String[] cells = splitCsvLine(line, sep);
        return idx < cells.length ? cells[idx].strip() : "?";
    }

    /** Splits a CSV line honouring double-quoted cells (quotes may wrap the separator). */
    private static String[] splitCsvLine(String line, char sep) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == sep && !inQuotes) {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cells.add(current.toString());
        return cells.toArray(new String[0]);
    }
}
