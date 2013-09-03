package org.veloscope.resource;

public abstract class UserEntity implements EntityInterface {

    public abstract String getEmail();           // email, usually unique
    public abstract String getPassword();        // encrypted password
    public abstract String getName();            // display name, like John Clark

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UserEntity)) {
            return false;
        }

        UserEntity otherUser = (UserEntity) obj;
        if (this.getId() == null || otherUser.getId() == null) {
            return false;
        }

        return this.getId().equals(otherUser.getId());
    }
}
