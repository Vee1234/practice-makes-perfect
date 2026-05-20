package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.domain.WelfareBand;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class WelfareBandEligibilityRule implements EligibilityRule {

    private final Set<WelfareBand> eligibleBands;

    public WelfareBandEligibilityRule(Set<WelfareBand> eligibleBands) {
        this.eligibleBands = Set.copyOf(Objects.requireNonNull(eligibleBands, "eligibleBands is required"));
    }

    @Override
    public Optional<String> evaluate(EligibilityContext context) {
        WelfareBand band = context.getIdentity().getWelfareBand();
        if (band == null) {
            return Optional.of("WELFARE_BAND_NOT_SET");
        }
        return eligibleBands.contains(band)
                ? Optional.empty()
                : Optional.of("BAND_NOT_ELIGIBLE");
    }
}
