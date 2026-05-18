package uk.ac.qmul.digitalid.domain;

import java.util.Objects;

public record DomainError(ErrorCode code, String message) {
    public DomainError {
        Objects.requireNonNull(code);
        Objects.requireNonNull(message);
    }
}

