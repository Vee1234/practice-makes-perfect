package uk.ac.qmul.digitalid.application.service.consumption;

import uk.ac.qmul.digitalid.domain.DigitalId;

import java.util.List;

public record VerificationOutcome(DigitalId identity, List<String> failingReasonCodes) {}
