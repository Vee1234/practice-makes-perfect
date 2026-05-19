package uk.ac.qmul.digitalid.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class DigitalId {
    private final DigitalIdNumber id;
    private final LegalName currentLegalName;
    private final LocalDate dateOfBirth;
    private final IdentityStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private DigitalId(DigitalIdNumber id, LegalName name, LocalDate dob, IdentityStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.currentLegalName = Objects.requireNonNull(name);
        this.dateOfBirth = Objects.requireNonNull(dob);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static DigitalId create(DigitalIdNumber id, LegalName name, LocalDate dob, Instant now) {
        return new DigitalId(id, name, dob, IdentityStatus.ACTIVE, now, now);
    }

    public IdentityStatus status() {
        return status;
    }

    public DigitalId changeId(DigitalIdNumber newId) {
        throw new UnsupportedOperationException("DigitalIdNumber is immutable");
    }

    public DigitalId changeDateOfBirth(LocalDate newDob) {
        throw new UnsupportedOperationException("Date of birth is immutable");
    }
}
