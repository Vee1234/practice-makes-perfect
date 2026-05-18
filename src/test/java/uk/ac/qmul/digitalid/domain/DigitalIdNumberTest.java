package uk.ac.qmul.digitalid.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DigitalIdNumberTest {
    @Test
    void acceptsValidId() {
        assertDoesNotThrow(() -> DigitalIdNumber.of("DID-000001"));
    }

    @Test
    void rejectsBlank() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> DigitalIdNumber.of(" "));
        assertTrue(ex.getMessage().toLowerCase().contains("blank"));
    }

    @Test
    void rejectsLowercase() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> DigitalIdNumber.of("did-000001"));
        assertTrue(ex.getMessage().toLowerCase().contains("uppercase"));
    }

    @Test
    void rejectsWrongPrefix() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> DigitalIdNumber.of("XID-000001"));
        assertTrue(ex.getMessage().toLowerCase().contains("prefix"));
    }

    @Test
    void rejectsWrongLength() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> DigitalIdNumber.of("DID-00001"));
        assertTrue(ex.getMessage().toLowerCase().contains("length"));
    }
}

