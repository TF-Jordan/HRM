package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.EmployeePersonalInfoRepository;
import yowyob.comops.api.hrm.domain.model.EmployeePersonalInfo;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class EmployeePersonalInfoR2dbcRepositoryAdapter implements EmployeePersonalInfoRepository {

    private final EmployeePersonalInfoSpringDataRepository repository;

    public EmployeePersonalInfoR2dbcRepositoryAdapter(EmployeePersonalInfoSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<EmployeePersonalInfo> save(EmployeePersonalInfo info) {
        return repository.save(toEntity(info)).map(this::toDomain);
    }

    @Override
    public Mono<EmployeePersonalInfo> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    private EmployeePersonalInfoEntity toEntity(EmployeePersonalInfo i) {
        return new EmployeePersonalInfoEntity(
                i.id(), i.tenantId(), i.createdAt(), i.updatedAt(), i.employeeId(),
                i.lieuNaissance(), i.situationMatrimoniale(), i.typePiece(), i.numeroPiece(),
                i.dateEmissionPiece(), i.niuFiscal(), i.permisConduire(), i.languesParlees(),
                i.emailPersonnel(), i.telephoneDomicile(), i.whatsapp(),
                i.adressePostale(), i.adresseDomicile(), i.ville(), i.region(), i.codePostal());
    }

    private EmployeePersonalInfo toDomain(EmployeePersonalInfoEntity e) {
        return EmployeePersonalInfo.rehydrate(
                e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.employeeId(),
                e.lieuNaissance(), e.situationMatrimoniale(), e.typePiece(), e.numeroPiece(),
                e.dateEmissionPiece(), e.niuFiscal(), e.permisConduire(), e.languesParlees(),
                e.emailPersonnel(), e.telephoneDomicile(), e.whatsapp(),
                e.adressePostale(), e.adresseDomicile(), e.ville(), e.region(), e.codePostal());
    }
}
