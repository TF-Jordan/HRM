package yowyob.comops.api.accounting.extension.web;

import java.util.List;
import java.util.UUID;

public record ThirdPartySummaryView(
        UUID id,
        String code,
        String name,
        String type,
        boolean enabled,
        String accountingAccount,
        List<String> accountingAccountNumbers) {
}
