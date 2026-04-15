package yowyob.comops.api.tp.domain.model;

public record ThirdPartyStatistics(
        long totalCount,
        long activeCount,
        long inactiveCount,
        long prospectCount,
        long convertedCount,
        long withBankAccountCount) {
}
