package uk.ac.qmul.digitalid.domain;

import java.util.Objects;

public final class OperationResult<T> {
    private final T payload;
    private final DomainError error;

    private OperationResult(T payload, DomainError error) {
        this.payload = payload;
        this.error = error;
    }

    public static <T> OperationResult<T> success(T payload) {
        return new OperationResult<>(payload, null);
    }

    public static <T> OperationResult<T> failure(DomainError error) {
        return new OperationResult<>(null, Objects.requireNonNull(error));
    }

    public boolean isSuccess() {
        return error == null;
    }

    public T getPayload() {
        return payload;
    }

    public DomainError getError() {
        return error;
    }
}

