package org.veloscope.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class Reflection {

    private static final Logger LOG = LoggerFactory.getLogger(Reflection.class);

    public static Method findMethod(Object obj, String methodName) {
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}
