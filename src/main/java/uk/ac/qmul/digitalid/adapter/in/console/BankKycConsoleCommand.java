package uk.ac.qmul.digitalid.adapter.in.console;

import uk.ac.qmul.digitalid.adapter.in.portal.BankKycResponse;
import uk.ac.qmul.digitalid.adapter.in.portal.BankPortal;
import uk.ac.qmul.digitalid.domain.DigitalIdNumber;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

public final class BankKycConsoleCommand implements ConsoleCommand {

    private final BankPortal portal;

    public BankKycConsoleCommand(BankPortal portal) {
        this.portal = portal;
    }

    @Override
    public String getDescription() { return "Run basic KYC check"; }

    @Override
    public void execute(Scanner in, PrintStream out) {
        Optional<DigitalIdNumber> id = ConsoleInput.readField(in, out, "Digital ID number", DigitalIdNumber::of);
        if (id.isEmpty()) return;

        BankKycResponse response = portal.checkBasicKyc(id.get());
        if (response.getReasonCodes().contains("NOT_FOUND")) { out.println("Identity not found."); return; }
        out.println("Valid now:     " + response.isValidNow());
        out.println("Name:          " + response.getName());
        out.println("Date of birth: " + response.getDateOfBirth());
        out.println("KYC decision:  " + response.getKycDecision());
        out.println("Reason codes:  " + response.getReasonCodes());
    }
}
