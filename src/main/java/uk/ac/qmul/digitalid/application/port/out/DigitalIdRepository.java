package uk.ac.qmul.digitalid.application.port.out;

import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.util.Optional;

public interface DigitalIdRepository {
    void save(DigitalId digitalId);
    Optional<DigitalId> findById(DigitalIdNumber id);
    boolean exists(DigitalIdNumber id);
}
