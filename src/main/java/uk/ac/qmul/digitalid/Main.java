package uk.ac.qmul.digitalid;

import uk.ac.qmul.digitalid.adapter.in.console.ConsoleCommand;
import uk.ac.qmul.digitalid.adapter.in.console.ConsoleCommandRunner;
import uk.ac.qmul.digitalid.adapter.in.portal.*;
import uk.ac.qmul.digitalid.adapter.out.audit.InMemoryAuditSink;
import uk.ac.qmul.digitalid.adapter.out.persistence.InMemoryDigitalIdRepository;
import uk.ac.qmul.digitalid.application.audit.AuditEventPublisher;
import uk.ac.qmul.digitalid.application.auth.AuthorisationService;
import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.application.port.in.ChangeStatusPort;
import uk.ac.qmul.digitalid.application.port.in.CreateIdentityPort;
import uk.ac.qmul.digitalid.application.service.audit.AuditQueryService;
import uk.ac.qmul.digitalid.application.service.consumption.DigitalIdConsumptionService;
import uk.ac.qmul.digitalid.application.service.management.ChangeStatusCommand;
import uk.ac.qmul.digitalid.application.service.management.DigitalIdManagementService;
import uk.ac.qmul.digitalid.application.service.management.IdentityCreateCommand;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;
import uk.ac.qmul.digitalid.domain.IdentityStatus;
import uk.ac.qmul.digitalid.domain.LegalName;
import uk.ac.qmul.digitalid.domain.OperationResult;

import java.io.PrintStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Organisation CENTRAL_AUTHORITY =
            new Organisation("DTS-CA", OrganisationRole.CENTRAL_AUTHORITY);

    public static void main(String[] args) {

        // --- infrastructure ---
        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        AuditEventPublisher auditPublisher = new AuditEventPublisher();
        auditPublisher.attach(auditSink);
        Clock clock = Clock.systemUTC();

        // --- application services ---
        AuthorisationService authService = new AuthorisationService();
        DigitalIdManagementService managementService =
                new DigitalIdManagementService(repository, auditPublisher, authService, clock);
        DigitalIdConsumptionService consumptionService =
                new DigitalIdConsumptionService(repository, auditPublisher, clock);
        AuditQueryService auditQueryService = new AuditQueryService(auditSink, authService);

        // --- portals ---
        EmployerPortal employerPortal = new EmployerPortal(consumptionService, clock);
        BankPortal bankPortal = new BankPortal(consumptionService, clock);
        DrivingLicencePortal drivingLicencePortal = new DrivingLicencePortal(consumptionService, clock);
        WelfarePortal welfarePortal = new WelfarePortal(consumptionService, clock);
        AuditorPortal auditorPortal = new AuditorPortal(auditQueryService);

        // --- command map ---
        Map<String, List<ConsoleCommand>> orgCommands = Map.of(
                "CENTRAL_AUTHORITY", List.of(
                        createIdentityCommand(managementService),
                        suspendIdentityCommand(managementService)
                ),
                "EMPLOYER", List.of(
                        employerVerifyCommand(employerPortal)
                ),
                "BANK", List.of(
                        bankKycCommand(bankPortal)
                ),
                "DRIVING_LICENCE_AUTHORITY", List.of(
                        drivingLicenceCommand(drivingLicencePortal)
                ),
                "WELFARE", List.of(
                        welfareCommand(welfarePortal)
                ),
                "AUDITOR", List.of(
                        auditSummaryCommand(auditorPortal)
                )
        );

        new ConsoleCommandRunner(orgCommands).run(new Scanner(System.in), System.out);
    }

    // --- command definitions ---

    private static ConsoleCommand createIdentityCommand(CreateIdentityPort port) {
        return new ConsoleCommand() {
            public String getDescription() { return "Create a new Digital ID"; }

            public void execute(Scanner in, PrintStream out) {
                out.print("Digital ID number (e.g. DID-123456): ");
                String id = in.nextLine().trim();
                out.print("Full legal name: ");
                String name = in.nextLine().trim();
                out.print("Date of birth (YYYY-MM-DD): ");
                String dob = in.nextLine().trim();

                OperationResult<?> result = port.createIdentity(new IdentityCreateCommand(
                        DigitalIdNumber.of(id), new LegalName(name),
                        LocalDate.parse(dob), CENTRAL_AUTHORITY));

                if (result.isSuccess()) {
                    out.println("Identity created: " + id);
                } else {
                    out.println("Failed: " + result.getError().message());
                }
            }
        };
    }

    private static ConsoleCommand suspendIdentityCommand(ChangeStatusPort port) {
        return new ConsoleCommand() {
            public String getDescription() { return "Suspend a Digital ID"; }

            public void execute(Scanner in, PrintStream out) {
                out.print("Digital ID number: ");
                String id = in.nextLine().trim();

                OperationResult<?> result = port.changeStatus(new ChangeStatusCommand(
                        DigitalIdNumber.of(id), IdentityStatus.SUSPENDED, CENTRAL_AUTHORITY));

                if (result.isSuccess()) {
                    out.println("Identity suspended: " + id);
                } else {
                    out.println("Failed: " + result.getError().message());
                }
            }
        };
    }

    private static ConsoleCommand employerVerifyCommand(EmployerPortal portal) {
        return new ConsoleCommand() {
            public String getDescription() { return "Verify right to work"; }

            public void execute(Scanner in, PrintStream out) {
                out.print("Digital ID number: ");
                String id = in.nextLine().trim();

                RightToWorkResponse response = portal.verifyRightToWork(DigitalIdNumber.of(id));
                out.println("Valid now:   " + response.isValidNow());
                out.println("Reason code: " + response.getReasonCode());
                out.println("Checked at:  " + response.getCheckedAt());
            }
        };
    }

    private static ConsoleCommand bankKycCommand(BankPortal portal) {
        return new ConsoleCommand() {
            public String getDescription() { return "Run basic KYC check"; }

            public void execute(Scanner in, PrintStream out) {
                out.print("Digital ID number: ");
                String id = in.nextLine().trim();
                out.print("Submitted full name: ");
                String name = in.nextLine().trim();
                out.print("Submitted date of birth (YYYY-MM-DD): ");
                String dob = in.nextLine().trim();

                BankKycResponse response = portal.checkBasicKyc(new BankKycRequest(
                        DigitalIdNumber.of(id), new LegalName(name), LocalDate.parse(dob)));

                out.println("Valid now:     " + response.isValidNow());
                out.println("Name matched:  " + response.isNameMatched());
                out.println("DOB matched:   " + response.isDobMatched());
                out.println("KYC decision:  " + response.getKycDecision());
                out.println("Reason codes:  " + response.getReasonCodes());
            }
        };
    }

    private static ConsoleCommand drivingLicenceCommand(DrivingLicencePortal portal) {
        return new ConsoleCommand() {
            public String getDescription() { return "Check driving licence eligibility"; }

            public void execute(Scanner in, PrintStream out) {
                out.print("Digital ID number: ");
                String id = in.nextLine().trim();

                DrivingLicenceResponse response =
                        portal.checkLicenceEligibility(DigitalIdNumber.of(id), LocalDate.now());

                out.println("Eligible:         " + response.isEligible());
                out.println("Minimum age met:  " + response.isMinimumAgeMet());
                out.println("Restriction block:" + response.isRestrictionBlock());
                out.println("Reason codes:     " + response.getReasonCodes());
            }
        };
    }

    private static ConsoleCommand welfareCommand(WelfarePortal portal) {
        return new ConsoleCommand() {
            public String getDescription() { return "Check benefit eligibility"; }

            public void execute(Scanner in, PrintStream out) {
                out.print("Digital ID number: ");
                String id = in.nextLine().trim();
                out.print("Submitted residential region: ");
                String region = in.nextLine().trim();

                WelfareEligibilityResponse response =
                        portal.checkBenefitEligibility(DigitalIdNumber.of(id), region, LocalDate.now());

                out.println("Eligible:        " + response.isEligible());
                out.println("Region matched:  " + response.isRegionMatched());
                out.println("Band category:   " + response.getBandCategory());
                out.println("Reason codes:    " + response.getReasonCodes());
            }
        };
    }

    private static ConsoleCommand auditSummaryCommand(AuditorPortal portal) {
        return new ConsoleCommand() {
            public String getDescription() { return "View audit event summaries"; }

            public void execute(Scanner in, PrintStream out) {
                List<AuditEventSummary> summaries = portal.getEventSummaries();
                if (summaries.isEmpty()) {
                    out.println("No audit events recorded.");
                    return;
                }
                summaries.forEach(s -> out.printf("[%s] %s | actor=%-20s success=%-5b reason=%s%n",
                        s.getTimestamp(), s.getEventType(), s.getActor(),
                        s.isSuccess(), s.getReasonCode()));
            }
        };
    }
}
