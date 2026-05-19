package uk.ac.qmul.digitalid.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class DigitalId {
    private final DigitalIdNumber id;
    private final LegalName currentLegalName;
    private final LocalDate dateOfBirth;
    private final IdentityStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Set<Restriction> restrictions;

    private DigitalId(DigitalIdNumber id, LegalName name, LocalDate dob, IdentityStatus status,
                      Instant createdAt, Instant updatedAt, Set<Restriction> restrictions) {
        this.id = Objects.requireNonNull(id);
        this.currentLegalName = Objects.requireNonNull(name);
        this.dateOfBirth = Objects.requireNonNull(dob);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.restrictions = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(restrictions)));
    }

    public static DigitalId create(DigitalIdNumber id, LegalName name, LocalDate dob, Instant now) {
        return new DigitalId(id, name, dob, IdentityStatus.ACTIVE, now, now, Set.of());
    }

    public DigitalIdNumber getId() {
        return id;
    }

    public IdentityStatus getStatus() {
        return status;
    }

    public DigitalId updateLegalName(LegalName newName, Instant updatedAt) {
        return new DigitalId(this.id, newName, this.dateOfBirth, this.status, this.createdAt, updatedAt, this.restrictions);
    }

    public OperationResult<DigitalId> changeStatus(IdentityStatus target, Instant updatedAt) {
        if (!this.status.canTransitionTo(target)) {
            return OperationResult.failure(new DomainError(
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    "Cannot transition from " + this.status + " to " + target));
        }
        return OperationResult.success(
                new DigitalId(this.id, this.currentLegalName, this.dateOfBirth, target, this.createdAt, updatedAt, this.restrictions));
    }

    public OperationResult<DigitalId> addRestriction(Restriction restriction, LocalDate asOf) {
        Objects.requireNonNull(restriction);
        Objects.requireNonNull(asOf);
        boolean alreadyActive = restrictions.stream()
                .anyMatch(r -> r.getType() == restriction.getType() && r.isActiveOn(asOf));
        if (alreadyActive) {
            return OperationResult.failure(new DomainError(
                    ErrorCode.INVALID_COMMAND,
                    "An active restriction of type " + restriction.getType() + " already exists"));
        }
        Set<Restriction> updated = new HashSet<>(this.restrictions);
        updated.add(restriction);
        return OperationResult.success(
                new DigitalId(this.id, this.currentLegalName, this.dateOfBirth, this.status, this.createdAt, this.updatedAt, updated));
    }

    public Set<Restriction> getRestrictions() {
        return restrictions;
    }

    public LegalName getCurrentLegalName() {
        return currentLegalName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
