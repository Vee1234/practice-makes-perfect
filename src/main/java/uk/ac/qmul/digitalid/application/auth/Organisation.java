package uk.ac.qmul.digitalid.application.auth;

import java.util.Objects;

public final class Organisation {

    private final String organisationId;
    private final OrganisationRole role;

    public Organisation(String organisationId, OrganisationRole role) {
        Objects.requireNonNull(organisationId, "organisationId is required");
        Objects.requireNonNull(role, "role is required");
        this.organisationId = organisationId;
        this.role = role;
    }

    public String getOrganisationId() { return organisationId; }
    public OrganisationRole getRole() { return role; }
}
