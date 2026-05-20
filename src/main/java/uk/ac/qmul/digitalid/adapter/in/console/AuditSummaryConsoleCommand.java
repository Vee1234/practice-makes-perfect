package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.adapter.in.portal.AuditEventSummary;
import uk.ac.qmul.digitalid.adapter.in.portal.AuditorPortal;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public final class AuditSummaryConsoleCommand implements ConsoleCommand {

    private final AuditorPortal portal;

    public AuditSummaryConsoleCommand(AuditorPortal portal) {
        this.portal = portal;
    }

    @Override
    public String getDescription() { return "View audit event summaries"; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        List<AuditEventSummary> summaries = portal.getEventSummaries();
        if (summaries.isEmpty()) { out.println("No audit events recorded."); return; }
        summaries.forEach(s -> out.printf("[%s] %s | actor=%-20s success=%-5b reason=%s%n",
                s.getTimestamp(), s.getEventType(), s.getActor(),
                s.isSuccess(), s.getReasonCode()));
    }
}
