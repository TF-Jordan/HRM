package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.common.domain.model.PlatformServiceCode;
import yowyob.comops.api.file.application.service.DocumentHubApplicationService;
import yowyob.comops.api.inventory.application.port.out.InventorySessionRepository;
import yowyob.comops.api.inventory.application.port.out.ProductTransformationRepository;
import yowyob.comops.api.inventory.application.port.out.StockMovementRepository;
import yowyob.comops.api.inventory.application.port.out.WarehouseTransferRepository;
import yowyob.comops.api.inventory.domain.model.InventorySession;
import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import yowyob.comops.api.inventory.domain.model.StockMovement;
import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OpeningHoursExceptionRepository;
import yowyob.comops.api.organization.application.port.out.OpeningHoursRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.application.port.out.PhysicalSpaceRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationServiceSubscriptionRepository;
import yowyob.comops.api.organization.application.port.out.PointOfInterestLinkRepository;
import yowyob.comops.api.organization.application.port.out.PointOfInterestRepository;
import yowyob.comops.api.organization.domain.model.Agency;
import yowyob.comops.api.organization.domain.model.OpeningHoursExceptionRule;
import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import yowyob.comops.api.organization.domain.model.Organization;
import yowyob.comops.api.organization.domain.model.OrganizationServiceSubscription;
import yowyob.comops.api.organization.domain.model.PhysicalSpace;
import yowyob.comops.api.organization.domain.model.PointOfInterest;
import yowyob.comops.api.organization.domain.model.PointOfInterestLink;
import yowyob.comops.api.product.application.port.out.ProductRepository;
import yowyob.comops.api.product.domain.model.Product;
import yowyob.comops.api.resource.application.port.in.ListResourceAssignmentsUseCase;
import yowyob.comops.api.resource.application.port.in.ListResourceReservationsUseCase;
import yowyob.comops.api.resource.application.service.AssetPortfolioApplicationService;
import yowyob.comops.api.resource.domain.model.ResourceAssignment;
import yowyob.comops.api.resource.domain.model.ResourceReservation;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OperationalWorkspaceApplicationService {

    private static final String AGENCY_TARGET_TYPE = "AGENCY";
    private static final String PHYSICAL_SPACE_TARGET_TYPE = "PHYSICAL_SPACE";
    private static final String WAREHOUSE_TYPE = "WAREHOUSE";

    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;
    private final PhysicalSpaceRepository physicalSpaceRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final OpeningHoursExceptionRepository openingHoursExceptionRepository;
    private final PointOfInterestRepository pointOfInterestRepository;
    private final PointOfInterestLinkRepository pointOfInterestLinkRepository;
    private final OrganizationServiceSubscriptionRepository organizationServiceSubscriptionRepository;
    private final ProductRepository productRepository;
    private final InventorySessionRepository inventorySessionRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductTransformationRepository productTransformationRepository;
    private final WarehouseTransferRepository warehouseTransferRepository;
    private final ListResourceAssignmentsUseCase listResourceAssignmentsUseCase;
    private final ListResourceReservationsUseCase listResourceReservationsUseCase;
    private final AssetPortfolioApplicationService assetPortfolioApplicationService;
    private final DocumentHubApplicationService documentHubApplicationService;

    public OperationalWorkspaceApplicationService(
            OrganizationRepository organizationRepository,
            AgencyRepository agencyRepository,
            PhysicalSpaceRepository physicalSpaceRepository,
            OpeningHoursRepository openingHoursRepository,
            OpeningHoursExceptionRepository openingHoursExceptionRepository,
            PointOfInterestRepository pointOfInterestRepository,
            PointOfInterestLinkRepository pointOfInterestLinkRepository,
            OrganizationServiceSubscriptionRepository organizationServiceSubscriptionRepository,
            ProductRepository productRepository,
            InventorySessionRepository inventorySessionRepository,
            StockMovementRepository stockMovementRepository,
            ProductTransformationRepository productTransformationRepository,
            WarehouseTransferRepository warehouseTransferRepository,
            ListResourceAssignmentsUseCase listResourceAssignmentsUseCase,
            ListResourceReservationsUseCase listResourceReservationsUseCase,
            AssetPortfolioApplicationService assetPortfolioApplicationService,
            DocumentHubApplicationService documentHubApplicationService) {
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
        this.physicalSpaceRepository = physicalSpaceRepository;
        this.openingHoursRepository = openingHoursRepository;
        this.openingHoursExceptionRepository = openingHoursExceptionRepository;
        this.pointOfInterestRepository = pointOfInterestRepository;
        this.pointOfInterestLinkRepository = pointOfInterestLinkRepository;
        this.organizationServiceSubscriptionRepository = organizationServiceSubscriptionRepository;
        this.productRepository = productRepository;
        this.inventorySessionRepository = inventorySessionRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productTransformationRepository = productTransformationRepository;
        this.warehouseTransferRepository = warehouseTransferRepository;
        this.listResourceAssignmentsUseCase = listResourceAssignmentsUseCase;
        this.listResourceReservationsUseCase = listResourceReservationsUseCase;
        this.assetPortfolioApplicationService = assetPortfolioApplicationService;
        this.documentHubApplicationService = documentHubApplicationService;
    }

    public Mono<OperationalSiteView> agencyOperationalSite(UUID tenantId, UUID organizationId, UUID agencyId) {
        return validateAgencyScope(tenantId, organizationId, agencyId, false)
                .flatMap(agency -> buildOperationalSite(tenantId, organizationId, agency));
    }

    public Mono<OperationalSiteView> warehouseOperationalSite(UUID tenantId, UUID warehouseId) {
        return ensureWarehouse(tenantId, warehouseId)
                .flatMap(warehouse -> buildOperationalSite(tenantId, warehouse.organizationId(), warehouse));
    }

    public Mono<GeneralizedInventoryView> organizationInventory(UUID tenantId, UUID organizationId) {
        return validateOrganizationScope(tenantId, organizationId)
                .flatMap(organization -> buildInventory(tenantId, organization, null));
    }

    public Mono<GeneralizedInventoryView> agencyInventory(UUID tenantId, UUID organizationId, UUID agencyId) {
        return validateAgencyScope(tenantId, organizationId, agencyId, false)
                .flatMap(agency -> validateOrganizationScope(tenantId, organizationId)
                        .flatMap(organization -> buildInventory(tenantId, organization, agency)));
    }

    public Mono<GeneralizedInventoryView> warehouseInventory(UUID tenantId, UUID warehouseId) {
        return ensureWarehouse(tenantId, warehouseId)
                .flatMap(warehouse -> validateOrganizationScope(tenantId, warehouse.organizationId())
                        .flatMap(organization -> buildInventory(tenantId, organization, warehouse)));
    }

    public Mono<ServiceWorkspaceView> serviceWorkspace(UUID tenantId, UUID organizationId, String workspaceCode) {
        ServiceWorkspaceBlueprint blueprint = ServiceWorkspaceBlueprint.from(workspaceCode);
        return validateOrganizationScope(tenantId, organizationId)
                .flatMap(organization -> Mono.zip(
                        agencyRepository.findByOrganizationId(tenantId, organizationId).collectList(),
                        organizationServiceCodes(tenantId, organizationId),
                        physicalSpaceRepository.findByOrganizationId(tenantId, organizationId).collectList(),
                        assetPortfolioApplicationService.organizationPortfolio(tenantId, organizationId),
                        documentHubApplicationService.overview(tenantId, organizationId),
                        organizationInventory(tenantId, organizationId))
                        .map(tuple -> toWorkspaceView(blueprint, organization, tuple.getT1(), tuple.getT2(),
                                tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6())));
    }

    private Mono<OperationalSiteView> buildOperationalSite(UUID tenantId, UUID organizationId, Agency agency) {
        return Mono.zip(
                openingHoursRepository.findByAgencyId(tenantId, organizationId, agency.id())
                        .sort(Comparator.comparing(OpeningHoursRule::dayOfWeek))
                        .map(this::toOpeningHoursView)
                        .collectList(),
                openingHoursExceptionRepository.findFutureByAgencyId(tenantId, organizationId, agency.id(),
                                LocalDate.now())
                        .sort(Comparator.comparing(OpeningHoursExceptionRule::exceptionDate))
                        .map(this::toOpeningHoursExceptionView)
                        .collectList(),
                buildPointOfInterestViews(tenantId, organizationId, agency.id()),
                buildPhysicalLayout(tenantId, organizationId, agency.id()),
                assetPortfolioApplicationService.agencyPortfolio(tenantId, organizationId, agency.id()),
                documentHubApplicationService.listByTarget(tenantId, AGENCY_TARGET_TYPE, agency.id())
                        .collectList()
                        .map(this::toSiteDocumentView),
                buildInventory(validateOrganizationScope(tenantId, organizationId), tenantId, organizationId, agency))
                .map(tuple -> {
                    List<String> capabilities = buildSiteCapabilities(agency, tuple.getT4(), tuple.getT5(), tuple.getT6(),
                            tuple.getT7());
                    return new OperationalSiteView(
                            WAREHOUSE_TYPE.equalsIgnoreCase(agency.agencyType()) ? WAREHOUSE_TYPE : "AGENCY",
                            agency.id(),
                            organizationId,
                            agency.id(),
                            agency.agencyType(),
                            agency.code(),
                            agency.name(),
                            agency.city(),
                            agency.country(),
                            agency.location(),
                            agency.active(),
                            WAREHOUSE_TYPE.equalsIgnoreCase(agency.agencyType()),
                            tuple.getT1(),
                            tuple.getT2(),
                            tuple.getT3(),
                            tuple.getT4(),
                            tuple.getT5(),
                            tuple.getT6(),
                            tuple.getT7(),
                            capabilities);
                });
    }

    private Mono<GeneralizedInventoryView> buildInventory(Mono<Organization> organizationMono, UUID tenantId,
            UUID organizationId, Agency scopeAgency) {
        return organizationMono.flatMap(organization -> buildInventory(tenantId, organization, scopeAgency));
    }

    private Mono<GeneralizedInventoryView> buildInventory(UUID tenantId, Organization organization, Agency scopeAgency) {
        UUID organizationId = organization.id();
        Mono<List<Product>> productsMono = productRepository.findByOrganizationId(tenantId, organizationId).collectList();
        Mono<List<InventorySession>> sessionsMono = inventorySessionRepository.findByOrganization(tenantId, organizationId)
                .filter(session -> inAgencyScope(scopeAgency, session.agencyId()))
                .collectList();
        Mono<List<StockMovement>> movementsMono = scopeAgency == null
                ? stockMovementRepository.findByOrganization(tenantId, organizationId).collectList()
                : stockMovementRepository.findByAgency(tenantId, organizationId, scopeAgency.id()).collectList();
        Mono<List<ProductTransformation>> transformationsMono = listTransformations(tenantId, organizationId, scopeAgency);
        Mono<List<WarehouseTransfer>> transfersMono = warehouseTransferRepository.findByOrganization(tenantId, organizationId)
                .filter(transfer -> inTransferScope(scopeAgency, transfer))
                .collectList();
        Mono<List<PhysicalSpace>> spacesMono = scopeAgency == null
                ? physicalSpaceRepository.findByOrganizationId(tenantId, organizationId).collectList()
                : physicalSpaceRepository.findByAgencyId(tenantId, organizationId, scopeAgency.id()).collectList();
        Mono<AssetPortfolioApplicationService.AssetPortfolioView> assetsMono = scopeAgency == null
                ? assetPortfolioApplicationService.organizationPortfolio(tenantId, organizationId)
                : assetPortfolioApplicationService.agencyPortfolio(tenantId, organizationId, scopeAgency.id());
        Mono<Integer> documentCountMono = scopeAgency == null
                ? documentHubApplicationService.overview(tenantId, organizationId)
                        .map(DocumentHubApplicationService.DocumentHubOverview::totalDocuments)
                : documentHubApplicationService.listByTarget(tenantId, AGENCY_TARGET_TYPE, scopeAgency.id())
                        .count()
                        .map(Long::intValue);

        return Mono.zip(productsMono, sessionsMono, movementsMono, transformationsMono, transfersMono, spacesMono,
                        assetsMono,
                        documentCountMono)
                .map(tuple -> toInventoryView(scopeAgency, organizationId, tuple.getT1(), tuple.getT2(), tuple.getT3(),
                        tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8()));
    }

    private GeneralizedInventoryView toInventoryView(Agency scopeAgency, UUID organizationId, List<Product> products,
            List<InventorySession> sessions, List<StockMovement> movements, List<ProductTransformation> transformations,
            List<WarehouseTransfer> transfers, List<PhysicalSpace> physicalSpaces,
            AssetPortfolioApplicationService.AssetPortfolioView assetPortfolio,
            int documentCount) {
        Map<UUID, Product> productsById = new LinkedHashMap<>();
        products.forEach(product -> productsById.put(product.id(), product));

        Set<UUID> scopedProductIds = new LinkedHashSet<>();
        Map<String, Long> movementCountsByType = new LinkedHashMap<>();
        Map<UUID, BigDecimal> positions = new LinkedHashMap<>();
        int validatedMovementCount = 0;
        int draftMovementCount = 0;
        BigDecimal netQuantity = BigDecimal.ZERO;
        for (StockMovement movement : movements) {
            scopedProductIds.add(movement.productId());
            movementCountsByType.merge(movement.movementType(), 1L, Long::sum);
            if (movement.validated()) {
                validatedMovementCount++;
                netQuantity = netQuantity.add(movement.signedQuantity());
                positions.merge(movement.productId(), movement.signedQuantity(), BigDecimal::add);
            } else {
                draftMovementCount++;
            }
        }

        sessions.forEach(session -> scopedProductIds.add(session.productId()));
        transformations.forEach(transformation -> {
            scopedProductIds.add(transformation.sourceProductId());
            scopedProductIds.add(transformation.targetProductId());
        });
        transfers.forEach(transfer -> scopedProductIds.add(transfer.productId()));

        int validatedInventorySessions = (int) sessions.stream()
                .filter(session -> "VALIDATED".equals(session.status()))
                .count();
        int draftInventorySessions = sessions.size() - validatedInventorySessions;
        int validatedTransformations = (int) transformations.stream()
                .filter(ProductTransformation::validated)
                .count();
        int draftTransformations = transformations.size() - validatedTransformations;
        int completedTransfers = (int) transfers.stream()
                .filter(WarehouseTransfer::completed)
                .count();
        int requestedTransfers = transfers.size() - completedTransfers;

        List<ProductPositionView> productPositions = positions.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> {
                    Product product = productsById.get(entry.getKey());
                    return new ProductPositionView(entry.getKey(),
                            product == null ? null : product.sku(),
                            product == null ? null : product.name(),
                            product == null ? null : product.variantLabel(),
                            entry.getValue());
                })
                .toList();

        return new GeneralizedInventoryView(
                scopeAgency == null ? "ORGANIZATION" : (WAREHOUSE_TYPE.equalsIgnoreCase(scopeAgency.agencyType())
                        ? WAREHOUSE_TYPE : "AGENCY"),
                scopeAgency == null ? organizationId : scopeAgency.id(),
                organizationId,
                scopeAgency == null ? null : scopeAgency.id(),
                products.size(),
                (int) products.stream().filter(Product::active).count(),
                scopedProductIds.size(),
                validatedMovementCount,
                draftMovementCount,
                netQuantity,
                sessions.size(),
                validatedInventorySessions,
                draftInventorySessions,
                transformations.size(),
                validatedTransformations,
                draftTransformations,
                transfers.size(),
                requestedTransfers,
                completedTransfers,
                physicalSpaces.size(),
                (int) physicalSpaces.stream().filter(PhysicalSpace::active).count(),
                assetPortfolio.totalResources(),
                assetPortfolio.assignedResources(),
                assetPortfolio.reservedResources(),
                assetPortfolio.openMaintenanceCount(),
                documentCount,
                movementCountsByType,
                productPositions);
    }

    private Mono<List<ProductTransformation>> listTransformations(UUID tenantId, UUID organizationId, Agency scopeAgency) {
        if (scopeAgency != null) {
            return productTransformationRepository.findByAgency(tenantId, organizationId, scopeAgency.id())
                    .collectList();
        }
        return agencyRepository.findByOrganizationId(tenantId, organizationId)
                .flatMap(agency -> productTransformationRepository.findByAgency(tenantId, organizationId, agency.id()))
                .collectList();
    }

    private Mono<List<PointOfInterestView>> buildPointOfInterestViews(UUID tenantId, UUID organizationId, UUID agencyId) {
        Mono<Map<UUID, PointOfInterest>> poiMapMono = pointOfInterestRepository.findByOrganizationId(tenantId, organizationId)
                .collectMap(PointOfInterest::id);
        Mono<List<PointOfInterest>> sitePointsMono = pointOfInterestRepository.findByAgencyId(tenantId, organizationId, agencyId)
                .collectList();
        Mono<List<PointOfInterestLink>> linkedPointsMono = pointOfInterestLinkRepository.findByAgencyId(tenantId, agencyId)
                .collectList();
        return Mono.zip(poiMapMono, sitePointsMono, linkedPointsMono)
                .map(tuple -> {
                    Map<UUID, PointOfInterest> poiById = tuple.getT1();
                    List<PointOfInterestView> views = new ArrayList<>();
                    for (PointOfInterest pointOfInterest : tuple.getT2()) {
                        views.add(toPointOfInterestView(pointOfInterest, null));
                    }
                    for (PointOfInterestLink link : tuple.getT3()) {
                        PointOfInterest pointOfInterest = poiById.get(link.pointOfInterestId());
                        if (pointOfInterest != null) {
                            views.add(toPointOfInterestView(pointOfInterest, link));
                        }
                    }
                    views.sort(Comparator.comparing(PointOfInterestView::name, String.CASE_INSENSITIVE_ORDER));
                    return views;
                });
    }

    private Mono<PhysicalLayoutView> buildPhysicalLayout(UUID tenantId, UUID organizationId, UUID agencyId) {
        return physicalSpaceRepository.findByAgencyId(tenantId, organizationId, agencyId)
                .collectList()
                .flatMap(spaces -> {
                    if (spaces.isEmpty()) {
                        return Mono.just(new PhysicalLayoutView(0, 0, 0, List.of()));
                    }
                    return Flux.fromIterable(spaces)
                            .flatMap(space -> Mono.zip(
                                            listResourceAssignmentsUseCase
                                                    .listAssignments(tenantId, PHYSICAL_SPACE_TARGET_TYPE, space.id())
                                                    .filter(assignment -> "ACTIVE".equals(assignment.status()))
                                                    .count(),
                                            listResourceReservationsUseCase
                                                    .listReservations(tenantId, PHYSICAL_SPACE_TARGET_TYPE, space.id())
                                                    .filter(reservation -> !"RELEASED".equals(reservation.status()))
                                                    .count(),
                                            documentHubApplicationService
                                                    .listByTarget(tenantId, PHYSICAL_SPACE_TARGET_TYPE, space.id())
                                                    .count())
                                    .map(tuple -> new PhysicalSpaceNodeSeed(space, tuple.getT1(), tuple.getT2(),
                                            tuple.getT3().intValue())))
                            .collectList()
                            .map(this::toPhysicalLayoutView);
                });
    }

    private ServiceWorkspaceView toWorkspaceView(ServiceWorkspaceBlueprint blueprint, Organization organization,
            List<Agency> agencies, List<String> subscribedServiceCodes, List<PhysicalSpace> physicalSpaces,
            AssetPortfolioApplicationService.AssetPortfolioView assetPortfolio,
            DocumentHubApplicationService.DocumentHubOverview documentHubOverview,
            GeneralizedInventoryView generalizedInventoryView) {
        List<Agency> activeAgencies = agencies.stream().filter(Agency::active).toList();
        long activeWarehouseCount = activeAgencies.stream()
                .filter(this::isWarehouse)
                .count();
        long activeOperationalAgencyCount = activeAgencies.stream()
                .filter(agency -> !isWarehouse(agency))
                .count();

        Set<String> subscribed = new LinkedHashSet<>(subscribedServiceCodes);
        List<String> missingServices = blueprint.requiredServiceCodes().stream()
                .filter(required -> !subscribed.contains(required))
                .toList();

        List<String> missingCapabilities = new ArrayList<>();
        boolean servicesReady = missingServices.isEmpty();
        boolean agenciesReady = switch (blueprint.workspaceCode()) {
            case "CASHIER", "BILLING" -> activeOperationalAgencyCount > 0;
            case "BANKING" -> !activeAgencies.isEmpty();
            case "ACCOUNTING" -> !activeAgencies.isEmpty();
            default -> !activeAgencies.isEmpty();
        };
        if (!agenciesReady) {
            missingCapabilities.add("NO_ACTIVE_AGENCY");
        }

        boolean warehousesReady = !blueprint.requiresWarehouse() || activeWarehouseCount > 0;
        if (!warehousesReady) {
            missingCapabilities.add("NO_WAREHOUSE");
        }

        boolean productsReady = !blueprint.requiresCatalog() || generalizedInventoryView.activeCatalogProductCount() > 0;
        if (!productsReady) {
            missingCapabilities.add("NO_PRODUCT_CATALOG");
        }

        boolean assetsReady = !blueprint.requiresAssets() || assetPortfolio.totalResources() > 0;
        if (!assetsReady) {
            missingCapabilities.add("NO_ASSET_BASELINE");
        }

        int activePhysicalSpaceCount = (int) physicalSpaces.stream().filter(PhysicalSpace::active).count();
        boolean physicalLayoutReady = !blueprint.requiresPhysicalLayout() || activePhysicalSpaceCount > 0;
        if (!physicalLayoutReady) {
            missingCapabilities.add("NO_PHYSICAL_LAYOUT");
        }

        boolean documentsReady = documentHubOverview.totalDocuments() > 0;
        if (!documentsReady) {
            missingCapabilities.add("NO_DOCUMENT_BASELINE");
        }

        missingServices.stream()
                .map(serviceCode -> "MISSING_SERVICE_" + serviceCode)
                .forEach(missingCapabilities::add);

        boolean ready = organization.isActive() && servicesReady && agenciesReady && warehousesReady
                && productsReady && assetsReady && physicalLayoutReady;

        return new ServiceWorkspaceView(
                blueprint.workspaceCode(),
                blueprint.displayName(),
                blueprint.description(),
                organization.id(),
                blueprint.requiredServiceCodes(),
                subscribedServiceCodes,
                missingServices,
                activeAgencies.size(),
                (int) activeOperationalAgencyCount,
                (int) activeWarehouseCount,
                new WorkspaceAssetSummary(assetPortfolio.totalResources(), assetPortfolio.assignedResources(),
                        assetPortfolio.reservedResources(), assetPortfolio.openMaintenanceCount()),
                new WorkspaceInventorySummary(generalizedInventoryView.catalogProductCount(),
                        generalizedInventoryView.activeCatalogProductCount(),
                        generalizedInventoryView.scopedProductCount(),
                        generalizedInventoryView.validatedStockMovementCount(),
                        generalizedInventoryView.inventorySessionCount(),
                        generalizedInventoryView.warehouseTransferCount()),
                new WorkspacePhysicalLayoutSummary(physicalSpaces.size(), activePhysicalSpaceCount,
                        (int) physicalSpaces.stream().filter(space -> space.parentSpaceId() == null).count()),
                new WorkspaceDocumentSummary(documentHubOverview.totalDocuments(), documentHubOverview.countsByTargetType(),
                        documentHubOverview.countsByCategory()),
                new WorkspaceReadiness(organization.isActive(), servicesReady, agenciesReady, warehousesReady,
                        productsReady, assetsReady, physicalLayoutReady, documentsReady, ready, missingCapabilities));
    }

    private Mono<List<String>> organizationServiceCodes(UUID tenantId, UUID organizationId) {
        return organizationServiceSubscriptionRepository.findByOrganizationId(tenantId, organizationId)
                .map(OrganizationServiceSubscription::serviceCode)
                .collectList()
                .map(codes -> {
                    Set<String> effective = new LinkedHashSet<>(PlatformServiceCode.mandatoryCodes());
                    effective.addAll(codes);
                    return PlatformServiceCode.orderCodes(effective);
                });
    }

    private List<String> buildSiteCapabilities(Agency agency,
            PhysicalLayoutView physicalLayout,
            AssetPortfolioApplicationService.AssetPortfolioView assetPortfolio,
            SiteDocumentView siteDocumentView,
            GeneralizedInventoryView generalizedInventoryView) {
        List<String> capabilities = new ArrayList<>();
        capabilities.add("OPERATIONS");
        if (physicalLayout.totalSpaces() > 0) {
            capabilities.add("PHYSICAL_LAYOUT");
        }
        if (assetPortfolio.totalResources() > 0) {
            capabilities.add("ASSET_MANAGEMENT");
        }
        if (generalizedInventoryView.validatedStockMovementCount() > 0
                || generalizedInventoryView.inventorySessionCount() > 0
                || generalizedInventoryView.warehouseTransferCount() > 0) {
            capabilities.add("GENERALIZED_INVENTORY");
        }
        if (siteDocumentView.totalDocuments() > 0) {
            capabilities.add("DOCUMENT_HUB");
        }
        if (isWarehouse(agency)) {
            capabilities.add("WAREHOUSE_OPERATIONS");
        }
        if (assetPortfolio.totalResources() > 0 && generalizedInventoryView.scopedProductCount() > 0) {
            capabilities.add("SITE_READINESS");
        }
        return capabilities;
    }

    private SiteDocumentView toSiteDocumentView(List<DocumentHubApplicationService.DocumentLinkView> links) {
        Map<String, Long> countsByCategory = new LinkedHashMap<>();
        for (DocumentHubApplicationService.DocumentLinkView link : links) {
            countsByCategory.merge(link.documentCategory(), 1L, Long::sum);
        }
        return new SiteDocumentView(links.size(), countsByCategory);
    }

    private PointOfInterestView toPointOfInterestView(PointOfInterest pointOfInterest, PointOfInterestLink link) {
        return new PointOfInterestView(pointOfInterest.id(), pointOfInterest.name(), pointOfInterest.poiType(),
                pointOfInterest.latitude(), pointOfInterest.longitude(),
                link == null ? null : link.distanceMeters(),
                link == null ? null : link.description());
    }

    private PhysicalLayoutView toPhysicalLayoutView(List<PhysicalSpaceNodeSeed> seeds) {
        Map<UUID, List<PhysicalSpaceNodeSeed>> childrenByParent = new LinkedHashMap<>();
        for (PhysicalSpaceNodeSeed seed : seeds) {
            childrenByParent.computeIfAbsent(seed.space().parentSpaceId(), ignored -> new ArrayList<>()).add(seed);
        }
        childrenByParent.values().forEach(children -> children.sort(Comparator
                .comparing((PhysicalSpaceNodeSeed seed) -> seed.space().levelNumber() == null ? Integer.MAX_VALUE
                        : seed.space().levelNumber())
                .thenComparing(seed -> seed.space().code(), String.CASE_INSENSITIVE_ORDER)));
        return new PhysicalLayoutView(seeds.size(),
                (int) seeds.stream().filter(seed -> seed.space().active()).count(),
                childrenByParent.getOrDefault(null, List.of()).size(),
                buildPhysicalLayoutNodes(childrenByParent, null));
    }

    private List<PhysicalSpaceNodeView> buildPhysicalLayoutNodes(Map<UUID, List<PhysicalSpaceNodeSeed>> childrenByParent,
            UUID parentId) {
        List<PhysicalSpaceNodeSeed> children = childrenByParent.getOrDefault(parentId, List.of());
        List<PhysicalSpaceNodeView> views = new ArrayList<>(children.size());
        for (PhysicalSpaceNodeSeed child : children) {
            PhysicalSpace space = child.space();
            views.add(new PhysicalSpaceNodeView(space.id(), space.parentSpaceId(), space.code(), space.name(),
                    space.spaceType(), space.description(), space.levelNumber(), space.capacity(), space.active(),
                    child.assignmentCount(), child.reservationCount(), child.documentCount(),
                    buildPhysicalLayoutNodes(childrenByParent, space.id())));
        }
        return views;
    }

    private OpeningHoursView toOpeningHoursView(OpeningHoursRule rule) {
        return new OpeningHoursView(rule.dayOfWeek(), rule.opensAt() == null ? null : rule.opensAt().toString(),
                rule.closesAt() == null ? null : rule.closesAt().toString(), rule.closed());
    }

    private OpeningHoursExceptionView toOpeningHoursExceptionView(OpeningHoursExceptionRule rule) {
        return new OpeningHoursExceptionView(rule.exceptionDate(), rule.label(),
                rule.opensAt() == null ? null : rule.opensAt().toString(),
                rule.closesAt() == null ? null : rule.closesAt().toString(), rule.closed());
    }

    private Mono<Organization> validateOrganizationScope(UUID tenantId, UUID organizationId) {
        return organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("organization not found")));
    }

    private Mono<Agency> validateAgencyScope(UUID tenantId, UUID organizationId, UUID agencyId, boolean warehouseOnly) {
        return validateOrganizationScope(tenantId, organizationId)
                .then(agencyRepository.findById(tenantId, agencyId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("agency not found")))
                        .flatMap(agency -> {
                            if (!agency.organizationId().equals(organizationId)) {
                                return Mono.error(new IllegalArgumentException("agency does not belong to the organization"));
                            }
                            if (warehouseOnly && !isWarehouse(agency)) {
                                return Mono.error(new IllegalArgumentException("agency is not a warehouse"));
                            }
                            return Mono.just(agency);
                        }));
    }

    private Mono<Agency> ensureWarehouse(UUID tenantId, UUID warehouseId) {
        return agencyRepository.findById(tenantId, warehouseId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("warehouse not found")))
                .flatMap(agency -> isWarehouse(agency)
                        ? Mono.just(agency)
                        : Mono.error(new IllegalArgumentException("agency is not a warehouse")));
    }

    private boolean isWarehouse(Agency agency) {
        return WAREHOUSE_TYPE.equalsIgnoreCase(agency.agencyType());
    }

    private boolean inAgencyScope(Agency scopeAgency, UUID agencyId) {
        return scopeAgency == null || scopeAgency.id().equals(agencyId);
    }

    private boolean inTransferScope(Agency scopeAgency, WarehouseTransfer transfer) {
        return scopeAgency == null
                || scopeAgency.id().equals(transfer.sourceAgencyId())
                || scopeAgency.id().equals(transfer.targetAgencyId());
    }

    public record OperationalSiteView(
            String scopeType,
            UUID scopeId,
            UUID organizationId,
            UUID agencyId,
            String agencyType,
            String code,
            String name,
            String city,
            String country,
            String location,
            boolean active,
            boolean warehouse,
            List<OpeningHoursView> openingHours,
            List<OpeningHoursExceptionView> upcomingExceptions,
            List<PointOfInterestView> pointsOfInterest,
            PhysicalLayoutView physicalLayout,
            AssetPortfolioApplicationService.AssetPortfolioView assetPortfolio,
            SiteDocumentView documents,
            GeneralizedInventoryView inventory,
            List<String> capabilities) {
    }

    public record OpeningHoursView(DayOfWeek dayOfWeek, String opensAt, String closesAt, boolean closed) {
    }

    public record OpeningHoursExceptionView(LocalDate exceptionDate, String label, String opensAt, String closesAt,
            boolean closed) {
    }

    public record PointOfInterestView(UUID id, String name, String poiType, Double latitude, Double longitude,
            Integer distanceMeters, String linkDescription) {
    }

    public record PhysicalLayoutView(int totalSpaces, int activeSpaces, int rootSpaceCount,
            List<PhysicalSpaceNodeView> tree) {
    }

    public record PhysicalSpaceNodeView(UUID id, UUID parentSpaceId, String code, String name, String spaceType,
            String description, Integer levelNumber, Integer capacity, boolean active, long assignedResources,
            long reservedResources, int documentCount, List<PhysicalSpaceNodeView> children) {
    }

    public record SiteDocumentView(int totalDocuments, Map<String, Long> countsByCategory) {
    }

    public record GeneralizedInventoryView(
            String scopeType,
            UUID scopeId,
            UUID organizationId,
            UUID agencyId,
            int catalogProductCount,
            int activeCatalogProductCount,
            int scopedProductCount,
            int validatedStockMovementCount,
            int draftStockMovementCount,
            BigDecimal netQuantity,
            int inventorySessionCount,
            int validatedInventorySessionCount,
            int draftInventorySessionCount,
            int transformationCount,
            int validatedTransformationCount,
            int draftTransformationCount,
            int warehouseTransferCount,
            int requestedWarehouseTransferCount,
            int completedWarehouseTransferCount,
            int physicalSpaceCount,
            int activePhysicalSpaceCount,
            long resourceCount,
            long assignedResourceCount,
            long reservedResourceCount,
            long openMaintenanceCount,
            int documentCount,
            Map<String, Long> stockMovementsByType,
            List<ProductPositionView> productPositions) {
    }

    public record ProductPositionView(UUID productId, String sku, String name, String variantLabel,
            BigDecimal onHandQuantity) {
    }

    public record ServiceWorkspaceView(
            String workspaceCode,
            String displayName,
            String description,
            UUID organizationId,
            List<String> requiredServiceCodes,
            List<String> subscribedServiceCodes,
            List<String> missingServiceCodes,
            int activeAgencyCount,
            int activeOperationalAgencyCount,
            int activeWarehouseCount,
            WorkspaceAssetSummary assets,
            WorkspaceInventorySummary inventory,
            WorkspacePhysicalLayoutSummary physicalLayout,
            WorkspaceDocumentSummary documents,
            WorkspaceReadiness readiness) {
    }

    public record WorkspaceAssetSummary(long totalResources, long assignedResources, long reservedResources,
            long openMaintenanceCount) {
    }

    public record WorkspaceInventorySummary(int catalogProductCount, int activeCatalogProductCount,
            int scopedProductCount, int validatedStockMovementCount, int inventorySessionCount,
            int warehouseTransferCount) {
    }

    public record WorkspacePhysicalLayoutSummary(int totalSpaces, int activeSpaces, int rootSpaceCount) {
    }

    public record WorkspaceDocumentSummary(int totalDocuments, Map<String, Long> countsByTargetType,
            Map<String, Long> countsByCategory) {
    }

    public record WorkspaceReadiness(boolean organizationReady, boolean servicesReady, boolean agenciesReady,
            boolean warehousesReady, boolean productsReady, boolean assetsReady, boolean physicalLayoutReady,
            boolean documentsReady, boolean ready,
            List<String> missingCapabilities) {
    }

    private record PhysicalSpaceNodeSeed(PhysicalSpace space, long assignmentCount, long reservationCount,
            int documentCount) {
    }

    private enum ServiceWorkspaceBlueprint {
        BILLING("Billing", "Commercial billing workflow workspace.",
                List.of("ORGANIZATION", "COMMERCIAL", "PRODUCT", "SALES", "ACCOUNTING", "SETTINGS"),
                true, false, false, false, Set.of("BILLING", "BILLING_SERVICE", "FACTURATION")),
        BANKING("Banking", "Treasury and banking workspace.",
                List.of("ORGANIZATION", "COMMERCIAL", "TREASURY", "ACCOUNTING", "SETTINGS"),
                false, false, false, true, Set.of("BANKING", "BANK", "TRESORERIE")),
        ACCOUNTING("Accounting", "Accounting close, reporting and compliance workspace.",
                List.of("ORGANIZATION", "COMMERCIAL", "ACCOUNTING", "TREASURY", "SETTINGS"),
                false, false, false, false, Set.of("ACCOUNTING", "COMPTABILITE")),
        CASHIER("Cashier", "Cashier, checkout and drawer workspace.",
                List.of("ORGANIZATION", "COMMERCIAL", "PRODUCT", "INVENTORY", "SALES", "TREASURY", "RESOURCE",
                        "SETTINGS"),
                true, false, true, true, Set.of("CASHIER", "CAISSE", "POS"));

        private final String displayName;
        private final String description;
        private final List<String> requiredServiceCodes;
        private final boolean requiresCatalog;
        private final boolean requiresWarehouse;
        private final boolean requiresAssets;
        private final boolean requiresPhysicalLayout;
        private final Set<String> aliases;

        ServiceWorkspaceBlueprint(String displayName, String description, List<String> requiredServiceCodes,
                boolean requiresCatalog, boolean requiresWarehouse, boolean requiresAssets,
                boolean requiresPhysicalLayout, Set<String> aliases) {
            this.displayName = displayName;
            this.description = description;
            this.requiredServiceCodes = requiredServiceCodes;
            this.requiresCatalog = requiresCatalog;
            this.requiresWarehouse = requiresWarehouse;
            this.requiresAssets = requiresAssets;
            this.requiresPhysicalLayout = requiresPhysicalLayout;
            this.aliases = aliases;
        }

        static ServiceWorkspaceBlueprint from(String rawCode) {
            String normalized = normalize(rawCode);
            for (ServiceWorkspaceBlueprint blueprint : values()) {
                if (blueprint.name().equals(normalized) || blueprint.aliases.contains(normalized)) {
                    return blueprint;
                }
            }
            throw new IllegalArgumentException("unknown service workspace: " + rawCode);
        }

        String workspaceCode() {
            return name();
        }

        String displayName() {
            return displayName;
        }

        String description() {
            return description;
        }

        List<String> requiredServiceCodes() {
            return requiredServiceCodes;
        }

        boolean requiresCatalog() {
            return requiresCatalog;
        }

        boolean requiresWarehouse() {
            return requiresWarehouse;
        }

        boolean requiresAssets() {
            return requiresAssets;
        }

        boolean requiresPhysicalLayout() {
            return requiresPhysicalLayout;
        }

        private static String normalize(String rawCode) {
            if (rawCode == null || rawCode.isBlank()) {
                throw new IllegalArgumentException("workspaceCode is required");
            }
            return rawCode.trim()
                    .replace('-', '_')
                    .replace(' ', '_')
                    .toUpperCase(Locale.ROOT);
        }
    }
}
