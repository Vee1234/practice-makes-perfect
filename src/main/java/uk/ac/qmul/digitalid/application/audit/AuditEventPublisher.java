package uk.ac.qmul.digitalid.application.audit;

import uk.ac.qmul.digitalid.application.port.out.AuditSink;

import java.util.ArrayList;
import java.util.List;

public final class AuditEventPublisher {

    private final List<AuditSink> observers = new ArrayList<>();

    public void attach(AuditSink observer) {
        observers.add(observer);
    }

    public void detach(AuditSink observer) {
        observers.remove(observer);
    }

    public void notifyObservers(AuditEvent event) {
        for (AuditSink observer : observers) {
            observer.record(event);
        }
    }
}