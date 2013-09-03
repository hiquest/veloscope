package org.veloscope.checks;

import org.veloscope.resource.UserEntity;

public interface Check {
    public boolean check(UserEntity me, Object object);
}
