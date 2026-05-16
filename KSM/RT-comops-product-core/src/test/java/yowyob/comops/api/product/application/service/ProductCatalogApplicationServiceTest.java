package yowyob.comops.api.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import yowyob.comops.api.product.adapter.out.persistence.InMemoryProductCategoryRepository;
import yowyob.comops.api.product.adapter.out.persistence.InMemoryProductPriceRepository;
import yowyob.comops.api.product.adapter.out.persistence.InMemoryProductRepository;
import yowyob.comops.api.product.domain.model.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductCatalogApplicationServiceTest {

    @Test
    void createsCategoriesAndResolvesEffectivePrice() {
        InMemoryProductRepository productRepository = new InMemoryProductRepository();
        ProductCatalogApplicationService service = new ProductCatalogApplicationService(
                new InMemoryProductCategoryRepository(), new InMemoryProductPriceRepository(), productRepository);

        UUID tenantId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        Product product = Product.create(tenantId, organizationId, "SKU-01", "Product", "FAMILY", "FOOD", "DEFAULT",
                "123", "Desc", BigDecimal.valueOf(1000), "XAF", "ACTIVE");
        productRepository.save(product).block();

        var category = service.createCategory(tenantId, organizationId, "FOOD", "Food", null, "Food catalog").block();
        service.definePrice(tenantId, product.id(), "SELLING", BigDecimal.valueOf(1200), "XAF",
                Instant.parse("2026-01-01T00:00:00Z")).block();
        service.definePrice(tenantId, product.id(), "SELLING", BigDecimal.valueOf(1500), "XAF",
                Instant.parse("2026-03-01T00:00:00Z")).block();

        var effective = service.resolveEffectivePrice(tenantId, product.id(), "SELLING",
                Instant.parse("2026-04-01T00:00:00Z")).block();

        assertThat(category.code()).isEqualTo("FOOD");
        assertThat(effective.amount()).isEqualByComparingTo("1500");
        assertThat(service.listPrices(tenantId, product.id()).collectList().block()).hasSize(2);
    }
}
