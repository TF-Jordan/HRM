package yowyob.comops.api.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import yowyob.comops.api.file.adapter.out.persistence.InMemoryStoredFileRepository;
import yowyob.comops.api.file.domain.model.StoredFile;
import yowyob.comops.api.product.adapter.out.persistence.InMemoryProductCategoryRepository;
import yowyob.comops.api.product.adapter.out.persistence.InMemoryProductRepository;
import yowyob.comops.api.product.adapter.out.persistence.InMemoryProductStructureRepository;
import yowyob.comops.api.product.domain.model.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductStructureApplicationServiceTest {

    @Test
    void managesRichProductStructures() {
        InMemoryProductRepository productRepository = new InMemoryProductRepository();
        InMemoryProductCategoryRepository categoryRepository = new InMemoryProductCategoryRepository();
        InMemoryProductStructureRepository structureRepository = new InMemoryProductStructureRepository();
        InMemoryStoredFileRepository storedFileRepository = new InMemoryStoredFileRepository();
        ProductStructureApplicationService service = new ProductStructureApplicationService(structureRepository,
                productRepository, categoryRepository, storedFileRepository);

        UUID tenantId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID uploadedByUserId = UUID.randomUUID();
        Product product = Product.create(tenantId, organizationId, "SKU-01", "Coffee", "DRINK", "BEV", "DEFAULT",
                "123", "Desc", BigDecimal.valueOf(1000), "XAF", "ACTIVE");
        productRepository.save(product).block();
        StoredFile storedFile = StoredFile.create(tenantId, organizationId, uploadedByUserId, "front.png", "image/png",
                1024, "/files/front.png");
        storedFileRepository.save(storedFile).block();
        var category = categoryRepository.save(
                yowyob.comops.api.product.domain.model.ProductCategory.create(tenantId, organizationId, "BEV",
                        "Beverages", null, "Beverage family")).block();

        var translation = service.upsertCategoryTranslation(tenantId, category.id(), "fr", "Boissons", "Rayon boissons")
                .block();
        var spec = service.upsertProductSpec(tenantId, product.id(), BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN,
                BigDecimal.TEN, "Coffee beans").block();
        var variant = service.createVariant(tenantId, product.id(), "SKU-01-RED", "999", "Red pack", false, "ACTIVE")
                .block();
        service.addVariantAttribute(tenantId, variant.id(), "color", "red").block();
        service.defineVariantPrice(tenantId, variant.id(), "SALE", BigDecimal.valueOf(1200), "XAF",
                Instant.parse("2026-01-01T00:00:00Z")).block();
        service.createBatch(tenantId, product.id(), "LOT-001", LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-12-31"), 50).block();
        service.createMediaAsset(tenantId, "PRODUCT", product.id(), storedFile.id(), "image/png", 0, "Front pack")
                .block();

        var effectivePrice = service.resolveEffectiveVariantPrice(tenantId, variant.id(), "SALE",
                Instant.parse("2026-02-01T00:00:00Z")).block();

        assertThat(translation.locale()).isEqualTo("fr");
        assertThat(spec.materials()).isEqualTo("Coffee beans");
        assertThat(service.listVariantAttributes(tenantId, variant.id()).collectList().block()).hasSize(1);
        assertThat(effectivePrice.amount()).isEqualByComparingTo("1200");
        assertThat(service.listBatches(tenantId, product.id()).collectList().block()).hasSize(1);
        assertThat(service.listMediaAssets(tenantId, "PRODUCT", product.id()).collectList().block()).hasSize(1);
    }
}
