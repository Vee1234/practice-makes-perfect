package uk.ac.qmul.digitalid.domain;

import java.util.Objects;

public record LegalName(String value) {
    public LegalName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("LegalName must not be blank");
        }
    }
}

