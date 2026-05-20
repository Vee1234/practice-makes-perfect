package uk.ac.qmul.digitalid.adapter.in.portal;

import uk.ac.qmul.digitalid.application.service.consumption.EvaluationOutcome;
import uk.ac.qmul.digitalid.application.service.consumption.VerificationPolicy;
import uk.ac.qmul.digitalid.domain.DigitalId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CompositeEligibilityPolicy implements EligibilityRule, VerificationPolicy {

    private final LocalDate checkDate;
    private final List<EligibilityRule> rules;

    public CompositeEligibilityPolicy(LocalDate checkDate, List<EligibilityRule> rules) {
        this.checkDate = Objects.requireNonNull(checkDate, "checkDate is required");
        this.rules     = List.copyOf(Objects.requireNonNull(rules, "rules is required"));
    }

    @Override
    public Optional<String> evaluate(EligibilityContext context) {
        List<String> failures = collectFailures(context);
        return failures.isEmpty() ? Optional.empty() : Optional.of(String.join(",", failures));
    }

    @Override
    public EvaluationOutcome evaluate(DigitalId identity) {
        List<String> failures = collectFailures(new EligibilityContext(identity, checkDate));
        return new EvaluationOutcome(failures.isEmpty(), List.copyOf(failures));
    }

    private List<String> collectFailures(EligibilityContext context) {
        List<String> failures = new ArrayList<>();
        for (EligibilityRule rule : rules) {
            rule.evaluate(context).ifPresent(failures::add);
        }
        return failures;
    }
}
