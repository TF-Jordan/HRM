package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_imported_bank_statement_lines")
public record AccountingExtensionImportedBankStatementLinesEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("rows_json")
        String rowsJson) {
}
