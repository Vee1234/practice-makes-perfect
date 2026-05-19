package uk.ac.qmul.digitalid.application.auth;

import java.util.Objects;

public final class ActorContext {

    private final String actorId;
    private final ActorRole role;

    public ActorContext(String actorId, ActorRole role) {
        Objects.requireNonNull(actorId, "actorId is required");
        Objects.requireNonNull(role, "role is required");
        this.actorId = actorId;
        this.role = role;
    }

    public String actorId() { return actorId; }
    public ActorRole role() { return role; }
}