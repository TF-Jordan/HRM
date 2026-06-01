package yowyob.comops.api.treasury.domain;

public final class DuplicateInvoiceSettlementNumberException extends RuntimeException {

    public DuplicateInvoiceSettlementNumberException(String settlementNumber) {
        super("Invoice settlement number already exists: " + settlementNumber);
    }
}
