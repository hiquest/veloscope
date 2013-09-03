package org.veloscope.checks;

import org.veloscope.annotations.grants.Delete;
import org.veloscope.annotations.grants.Get;
import org.veloscope.resource.Resource;

import java.util.ArrayList;
import java.util.List;

public class PrepareChecks {
    public static List<Check> forGet(Class<? extends Resource> resourceClass) {
        List<Check> checks = new ArrayList<Check>();
        if (!resourceClass.isAnnotationPresent(Get.class)) {
            checks.add(AlwaysForbid.it());
        } else {
            Get grantGet = resourceClass.getAnnotation(Get.class);
            checks.addAll(Checks.buildAll(grantGet.onlyIf()));
        }

        return checks;
    }

    public static List<Check> forDelete(Class<? extends Resource> resourceClass) {
        List<Check> checks = new ArrayList<Check>();
        if (!resourceClass.isAnnotationPresent(Delete.class)) {
            checks.add(AlwaysForbid.it());
        } else {
            Delete grantDelete = resourceClass.getAnnotation(Delete.class);
            checks.addAll(Checks.buildAll(grantDelete.onlyIf()));
        }

        return checks;
    }
}
