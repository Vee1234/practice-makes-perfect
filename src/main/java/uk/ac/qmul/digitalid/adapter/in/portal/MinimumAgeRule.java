package uk.ac.qmul.digitalid.adapter.in.portal;

import java.time.Period;
import java.util.Optional;

public final class MinimumAgeRule implements EligibilityRule {

    public static final int MINIMUM_AGE_YEARS = 17;

    @Override
    public Optional<String> evaluate(EligibilityContext context) {
        int age = Period.between(context.getIdentity().getDateOfBirth(), context.getCheckDate()).getYears();
        return age >= MINIMUM_AGE_YEARS ? Optional.empty() : Optional.of("MINIMUM_AGE_NOT_MET");
    }
}
