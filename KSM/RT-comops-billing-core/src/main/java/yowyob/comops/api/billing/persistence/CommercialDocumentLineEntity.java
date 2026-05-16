package yowyob.comops.api.billing.persistence;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("billing.commercial_document_line")
public record CommercialDocumentLineEntity(
        @Id
        UUID id,
        @Column("document_id")
        UUID documentId,
        @Column("line_index")
        Integer lineIndex,
        @Column("product_id")
        UUID productId,
        @Column("quantity")
        BigDecimal quantity,
        @Column("unit_price")
        BigDecimal unitPrice) {
}
