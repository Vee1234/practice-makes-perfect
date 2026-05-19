package uk.ac.qmul.digitalid.adapter.in.portal;

import java.util.Optional;

/**
 * A single composable eligibility condition. Returns an empty Optional on pass,
 * or the blocking reason code on fail. Designed to be combined via CompositeEligibilityPolicy.
 */
@FunctionalInterface
public interface EligibilityRule {
    Optional<String> evaluate(EligibilityContext context);
}
