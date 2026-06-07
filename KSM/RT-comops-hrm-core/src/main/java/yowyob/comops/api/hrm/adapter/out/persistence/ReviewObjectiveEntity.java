package yowyob.comops.api.hrm.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Review objective row. The primary key is assigned by the domain at creation
 * time, so Spring Data R2DBC cannot infer insert-vs-update from a null id. We
 * therefore carry an explicit, non-persisted {@code newEntity} flag that the
 * adapter sets from an existence check: {@code true} forces an INSERT (create),
 * {@code false} forces an UPDATE (evaluate). Rows materialised from the database
 * use the {@link PersistenceCreator} constructor and are never "new".
 */
@Table(name = "hrm_review_objective")
public class ReviewObjectiveEntity implements Persistable<UUID> {

    @Id
    private final UUID id;
    private final UUID tenantId;
    private final UUID reviewId;
    private final String description;
    private final BigDecimal poids;
    private final BigDecimal noteAtteinte;
    private final String commentaire;

    @Transient
    private final boolean newEntity;

    @PersistenceCreator
    public ReviewObjectiveEntity(UUID id, UUID tenantId, UUID reviewId, String description,
                                 BigDecimal poids, BigDecimal noteAtteinte, String commentaire) {
        this(id, tenantId, reviewId, description, poids, noteAtteinte, commentaire, false);
    }

    private ReviewObjectiveEntity(UUID id, UUID tenantId, UUID reviewId, String description,
                                  BigDecimal poids, BigDecimal noteAtteinte, String commentaire,
                                  boolean newEntity) {
        this.id = id;
        this.tenantId = tenantId;
        this.reviewId = reviewId;
        this.description = description;
        this.poids = poids;
        this.noteAtteinte = noteAtteinte;
        this.commentaire = commentaire;
        this.newEntity = newEntity;
    }

    /** Build an entity destined for persistence, controlling the insert/update strategy. */
    public static ReviewObjectiveEntity of(UUID id, UUID tenantId, UUID reviewId, String description,
                                           BigDecimal poids, BigDecimal noteAtteinte, String commentaire,
                                           boolean newEntity) {
        return new ReviewObjectiveEntity(id, tenantId, reviewId, description, poids, noteAtteinte,
                commentaire, newEntity);
    }

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public UUID reviewId() { return reviewId; }
    public String description() { return description; }
    public BigDecimal poids() { return poids; }
    public BigDecimal noteAtteinte() { return noteAtteinte; }
    public String commentaire() { return commentaire; }

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return newEntity; }
}
