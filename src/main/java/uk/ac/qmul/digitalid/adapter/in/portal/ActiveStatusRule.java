package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.domain.IdentityStatus;

import java.util.Optional;

public final class ActiveStatusRule implements EligibilityRule {

    @Override
    public Optional<String> evaluate(EligibilityContext context) {
        boolean active = context.getIdentity().getStatus() == IdentityStatus.ACTIVE;
        return active ? Optional.empty() : Optional.of("INACTIVE_STATUS");
    }
}
