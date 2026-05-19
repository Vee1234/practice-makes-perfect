package uk.ac.qmul.digitalid.adapter.out.persistence;

import uk.ac.qmul.digitalid.application.port.out.DigitalIdRepository;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryDigitalIdRepository implements DigitalIdRepository {

    private final Map<DigitalIdNumber, DigitalId> store = new HashMap<>();

    @Override
    public void save(DigitalId digitalId) {
        store.put(digitalId.id(), digitalId);
    }

    @Override
    public Optional<DigitalId> findById(DigitalIdNumber id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean exists(DigitalIdNumber id) {
        return store.containsKey(id);
    }
}