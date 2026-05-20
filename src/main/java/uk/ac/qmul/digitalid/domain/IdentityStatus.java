package uk.ac.qmul.digitalid.domain;

public enum IdentityStatus {
    ACTIVE {
        @Override
        public boolean canTransitionTo(IdentityStatus target) {
            return target == SUSPENDED || target == REVOKED || target == EXPIRED;
        }
    },
    SUSPENDED {
        @Override
        public boolean canTransitionTo(IdentityStatus target) {
            return target == ACTIVE || target == REVOKED || target == EXPIRED;
        }
    },
    REVOKED {
        @Override
        public boolean canTransitionTo(IdentityStatus target) {
            return false;
        }
    },
    EXPIRED {
        @Override
        public boolean canTransitionTo(IdentityStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(IdentityStatus target);
}
