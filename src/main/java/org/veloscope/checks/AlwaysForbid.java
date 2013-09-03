package org.veloscope.checks;

import org.veloscope.resource.UserEntity;

public class AlwaysForbid implements Check {

    private static final AlwaysForbid instance = new AlwaysForbid();

    private AlwaysForbid() { }

    @Override
    public boolean check(UserEntity me, Object object) {
        return false;
    }

    public static AlwaysForbid it() {
        return instance;
    }
}
