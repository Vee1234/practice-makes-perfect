package uk.ac.qmul.digitalid.domain;


public record DigitalIdNumber(String value) {
    public static DigitalIdNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DigitalIdNumber must not be blank");
        }
        if (!value.equals(value.toUpperCase())) {
            throw new IllegalArgumentException("DigitalIdNumber must be uppercase");
        }
        if (!value.startsWith("DID-")) {
            throw new IllegalArgumentException("DigitalIdNumber must have prefix 'DID-'");
        }
        if (value.length() != 10) {
            throw new IllegalArgumentException("DigitalIdNumber must be length 10");
        }
        return new DigitalIdNumber(value);
    }
}
