package yowyob.comops.api.bootstrap;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {                "spring.r2dbc.url=r2dbc:postgresql://localhost:54329/iwm",
                "spring.r2dbc.username=iwm",
                "spring.r2dbc.password=iwm",
                "spring.liquibase.url=jdbc:postgresql://localhost:54329/iwm",
                "spring.liquibase.user=iwm",
                "spring.liquibase.password=iwm",
                "spring.liquibase.change-log=classpath:db/changelog/contracts/accounting-treasury-contract.yaml",
                "spring.liquibase.drop-first=false",
                "iwm.outbox.delivery.type=recording",
                "iwm.outbox.consumers.mode=inline"
        })
@ActiveProfiles("r2dbc")
@EnabledIfSystemProperty(named = "iwm.tests.r2dbc.enabled", matches = "true")
class AccountingTreasuryContractTests extends AbstractContractR2dbcIntegrationTest {

    private static final String TENANT_ID = "22000000-0000-0000-0000-000000000001";
    private static final String USER_ID = "22000000-0000-0000-0000-000000000011";
    private static final String ORGANIZATION_ID = "22000000-0000-0000-0000-000000000110";
    private static final String INVOICE_ID = "22000000-0000-0000-0000-000000000114";
    private static final String BANK_ACCOUNT_ID = "22000000-0000-0000-0000-000000000116";

    @Test
    void accountingAndTreasuryContractIsStableOnSeededDataset() {
        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).get()
                .uri("/api/accounting/invoices/{invoiceId}", INVOICE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("DRAFT")
                .jsonPath("$.data.lines.length()").isEqualTo(1)
                .jsonPath("$.data.outstandingAmount").isEqualTo(50);

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).post()
                .uri("/api/accounting/invoices/{invoiceId}/post", INVOICE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("POSTED");

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).post()
                .uri("/api/treasury/bank-accounts/invoice-settlements")
                .bodyValue(Map.of(
                        "organizationId", ORGANIZATION_ID,
                        "bankAccountId", BANK_ACCOUNT_ID,
                        "invoiceId", INVOICE_ID,
                        "paymentMethod", "BANK_TRANSFER",
                        "amount", 50.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.settlementNumber").isEqualTo("SET-C-0001")
                .jsonPath("$.data.status").isEqualTo("REGISTERED");

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).get()
                .uri("/api/accounting/invoices/{invoiceId}", INVOICE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.paymentStatus").isEqualTo("PAID")
                .jsonPath("$.data.settledAmount").isEqualTo(50)
                .jsonPath("$.data.outstandingAmount").isEqualTo(0);

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).get()
                .uri(uriBuilder -> uriBuilder.path("/api/treasury/bank-accounts/invoice-settlements")
                        .queryParam("organizationId", ORGANIZATION_ID)
                        .queryParam("invoiceId", INVOICE_ID)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].settlementNumber").isEqualTo("SET-C-0001");
    }
}
