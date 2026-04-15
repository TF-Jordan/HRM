package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.tp.domain.model.ThirdPartyStatistics;

public record ThirdPartyStatisticsResponse(
        long totalCount,
        long activeCount,
        long inactiveCount,
        long prospectCount,
        long convertedCount,
        long withBankAccountCount) {

    public static ThirdPartyStatisticsResponse from(ThirdPartyStatistics statistics) {
        return new ThirdPartyStatisticsResponse(statistics.totalCount(), statistics.activeCount(),
                statistics.inactiveCount(), statistics.prospectCount(), statistics.convertedCount(),
                statistics.withBankAccountCount());
    }
}
