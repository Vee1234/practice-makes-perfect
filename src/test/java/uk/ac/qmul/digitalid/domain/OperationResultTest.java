package uk.ac.qmul.digitalid.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OperationResultTest {
    @Test
    void successBehavesPredictably() {
        OperationResult<String> result = OperationResult.success("ok");
        assertTrue(result.isSuccess());
        assertEquals("ok", result.payload());
        assertNull(result.error());
    }

    @Test
    void failureBehavesPredictably() {
        DomainError error = new DomainError(ErrorCode.INVALID_ID, "bad id");
        OperationResult<String> result = OperationResult.failure(error);
        assertFalse(result.isSuccess());
        assertNull(result.payload());
        assertEquals(error, result.error());
    }
}

