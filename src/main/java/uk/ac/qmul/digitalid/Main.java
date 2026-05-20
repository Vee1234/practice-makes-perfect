package uk.ac.qmul.digitalid;

import uk.ac.qmul.digitalid.adapter.in.console.*;
import uk.ac.qmul.digitalid.adapter.in.portal.*;
import uk.ac.qmul.digitalid.adapter.out.audit.InMemoryAuditSink;
import uk.ac.qmul.digitalid.adapter.out.persistence.InMemoryDigitalIdRepository;
import uk.ac.qmul.digitalid.application.audit.AuditEventPublisher;
import uk.ac.qmul.digitalid.application.auth.AuthorisationService;
import uk.ac.qmul.digitalid.application.auth.Organisation;
import uk.ac.qmul.digitalid.application.auth.OrganisationRole;
import uk.ac.qmul.digitalid.application.service.audit.AuditQueryService;
import uk.ac.qmul.digitalid.application.service.consumption.DigitalIdConsumptionService;
import uk.ac.qmul.digitalid.application.service.management.DigitalIdManagementService;
import uk.ac.qmul.digitalid.domain.IdentityStatus;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Organisation CENTRAL_AUTHORITY =
            new Organisation("DTS-CA", OrganisationRole.CENTRAL_AUTHORITY);

    public static void main(String[] args) {

        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();
        InMemoryAuditSink auditSink = new InMemoryAuditSink();
        AuditEventPublisher auditPublisher = new AuditEventPublisher();
        auditPublisher.attach(auditSink);
        Clock clock = Clock.systemUTC();

        AuthorisationService authService = new AuthorisationService();
        DigitalIdManagementService managementService =
                new DigitalIdManagementService(repository, auditPublisher, authService, clock);
        DigitalIdConsumptionService consumptionService =
                new DigitalIdConsumptionService(repository, auditPublisher, clock);
        AuditQueryService auditQueryService = new AuditQueryService(auditSink, authService);

        EmployerPortal employerPortal       = new EmployerPortal(consumptionService, clock);
        BankPortal bankPortal               = new BankPortal(consumptionService, clock);
        DrivingLicencePortal licencePortal  = new DrivingLicencePortal(consumptionService, clock);
        WelfarePortal welfarePortal         = new WelfarePortal(consumptionService, clock);
        AuditorPortal auditorPortal         = new AuditorPortal(auditQueryService);

        Map<String, List<ConsoleCommand>> orgCommands = Map.of(
                "CENTRAL_AUTHORITY", List.of(
                        new CreateIdentityConsoleCommand(managementService, CENTRAL_AUTHORITY),
                        new ChangeStatusConsoleCommand(managementService, IdentityStatus.SUSPENDED, "Suspend a Digital ID",            "suspended", CENTRAL_AUTHORITY),
                        new ChangeStatusConsoleCommand(managementService, IdentityStatus.ACTIVE,    "Activate (un-suspend) a Digital ID", "activated", CENTRAL_AUTHORITY),
                        new ChangeStatusConsoleCommand(managementService, IdentityStatus.REVOKED,   "Revoke a Digital ID",               "revoked",   CENTRAL_AUTHORITY),
                        new AddRestrictionConsoleCommand(managementService, CENTRAL_AUTHORITY),
                        new SetWelfareBandConsoleCommand(managementService, CENTRAL_AUTHORITY),
                        new ViewIdentityConsoleCommand(managementService, CENTRAL_AUTHORITY)
                ),
                "EMPLOYER",              List.of(new EmployerVerifyConsoleCommand(employerPortal)),
                "BANK",                  List.of(new BankKycConsoleCommand(bankPortal)),
                "DRIVING_LICENCE_AUTHORITY", List.of(new DrivingLicenceConsoleCommand(licencePortal)),
                "WELFARE",               List.of(new WelfareConsoleCommand(welfarePortal)),
                "AUDITOR",               List.of(new AuditSummaryConsoleCommand(auditorPortal))
        );

        new ConsoleCommandRunner(orgCommands).run(new Scanner(System.in), System.out);
    }
}
