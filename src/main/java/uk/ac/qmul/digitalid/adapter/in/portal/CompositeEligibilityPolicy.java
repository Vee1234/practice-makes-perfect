package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.VerificationPolicy;
import uk.ac.qmul.digitalid.domain.DigitalId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Composite + Strategy: evaluates an ordered list of EligibilityRules, collects all failing
 * reason codes, and adapts the combined outcome to the VerificationPolicy interface so it
 * plugs into the shared consumption service without modification.
 */
public final class CompositeEligibilityPolicy implements EligibilityRule, VerificationPolicy {

    private final LocalDate checkDate;
    private final List<EligibilityRule> rules;
    private List<String> failingReasonCodes = List.of();

    public CompositeEligibilityPolicy(LocalDate checkDate, List<EligibilityRule> rules) {
        this.checkDate = Objects.requireNonNull(checkDate, "checkDate is required");
        this.rules     = List.copyOf(Objects.requireNonNull(rules, "rules is required"));
    }

    @Override
    public Optional<String> evaluate(EligibilityContext context) {
        List<String> failures = collectFailures(context);
        this.failingReasonCodes = List.copyOf(failures);
        return failures.isEmpty() ? Optional.empty() : Optional.of(String.join(",", failures));
    }

    @Override
    public boolean evaluate(DigitalId identity) {
        evaluate(new EligibilityContext(identity, checkDate));
        return failingReasonCodes.isEmpty();
    }

    public List<String> getFailingReasonCodes() {
        return failingReasonCodes;
    }

    private List<String> collectFailures(EligibilityContext context) {
        List<String> failures = new ArrayList<>();
        for (EligibilityRule rule : rules) {
            rule.evaluate(context).ifPresent(failures::add);
        }
        return failures;
    }
}
