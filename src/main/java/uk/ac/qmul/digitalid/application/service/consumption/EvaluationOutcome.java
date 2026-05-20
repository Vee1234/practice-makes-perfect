package uk.ac.qmul.digitalid.application.service.consumption;

import java.util.List;

public record EvaluationOutcome(boolean passed, List<String> failingReasonCodes) {}
