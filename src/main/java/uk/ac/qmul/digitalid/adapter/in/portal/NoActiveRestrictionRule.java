package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.domain.RestrictionType;

import java.util.Objects;
import java.util.Optional;

public final class NoActiveRestrictionRule implements EligibilityRule {

    private final RestrictionType restrictionType;

    public NoActiveRestrictionRule(RestrictionType restrictionType) {
        this.restrictionType = Objects.requireNonNull(restrictionType, "restrictionType is required");
    }

    @Override
    public Optional<String> evaluate(EligibilityContext context) {
        boolean blocked = context.getIdentity().getRestrictions().stream()
                .anyMatch(r -> r.getType() == restrictionType
                        && r.isActiveOn(context.getCheckDate()));
        return blocked ? Optional.of(restrictionType.name() + "_ACTIVE") : Optional.empty();
    }
}
