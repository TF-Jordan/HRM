package yowyob.comops.api.product.domain;

public final class DuplicateBatchLotNumberException extends RuntimeException {

    public DuplicateBatchLotNumberException(String lotNumber) {
        super("Batch lot number already exists: " + lotNumber);
    }
}
