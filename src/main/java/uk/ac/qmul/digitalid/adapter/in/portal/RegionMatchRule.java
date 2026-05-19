package uk.ac.qmul.digitalid.adapter.in.portal;

import java.util.Objects;
import java.util.Optional;

public final class RegionMatchRule implements EligibilityRule {

    private final String submittedRegion;

    public RegionMatchRule(String submittedRegion) {
        this.submittedRegion = Objects.requireNonNull(submittedRegion, "submittedRegion is required");
    }

    @Override
    public Optional<String> evaluate(EligibilityContext context) {
        String registeredRegion = context.getIdentity().getResidentialRegion();
        if (registeredRegion == null) {
            return Optional.of("REGION_NOT_SET");
        }
        return registeredRegion.equalsIgnoreCase(submittedRegion)
                ? Optional.empty()
                : Optional.of("REGION_MISMATCH");
    }
}