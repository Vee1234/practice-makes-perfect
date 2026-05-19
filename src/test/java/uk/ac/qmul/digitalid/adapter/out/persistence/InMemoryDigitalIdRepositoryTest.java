package uk.ac.qmul.digitalid.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.qmul.digitalid.domain.DigitalId;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.LegalName;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDigitalIdRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final DigitalIdNumber ID = DigitalIdNumber.of("DID-000001");

    private InMemoryDigitalIdRepository repository;
    private DigitalId digitalId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDigitalIdRepository();
        digitalId = DigitalId.create(ID, new LegalName("Jane Doe"), LocalDate.of(1990, 1, 1), NOW);
    }

    @Test
    void shouldFindById_whenDigitalIdHasBeenSaved() {
        repository.save(digitalId);

        Optional<DigitalId> result = repository.findById(ID);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(ID);
    }

    @Test
    void shouldReturnEmpty_whenDigitalIdNotFound() {
        Optional<DigitalId> result = repository.findById(DigitalIdNumber.of("DID-000099"));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrue_whenDigitalIdExists() {
        repository.save(digitalId);

        assertThat(repository.exists(ID)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenDigitalIdDoesNotExist() {
        assertThat(repository.exists(DigitalIdNumber.of("DID-000099"))).isFalse();
    }
}
