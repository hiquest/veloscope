package org.veloscope.checks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veloscope.exceptions.RuleIsNotSupported;
import org.veloscope.resource.UserEntity;
import org.veloscope.security.SecurityHelper;
import org.veloscope.utils.Reflection;
import org.veloscope.utils.Strings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Checks {

    private static final Logger LOG = LoggerFactory.getLogger(Checks.class);

    public static List<Check> buildAll(String rules) {
        List<Check> out = new ArrayList<Check>();
        if (Strings.empty(rules.trim())) {
            return out;
        }
        List<String> rulez = Arrays.asList(rules.trim().split("[&]"));
        for (String s: rulez) {
            out.add(build(s));
        }
        return out;
    }

    private static boolean valid(String field) {
       return Strings.notEmpty(field) && (field.startsWith("@") || field.equalsIgnoreCase("me"));
    }


    private static Object invokeMethod(Object obj, Method method) {
        try {
            return method.invoke(obj);
        } catch (IllegalAccessException e) {
            LOG.error("Error: " + e);
            throw new RuleIsNotSupported("IllegalAccessException: " + method.getName());
        } catch (InvocationTargetException e) {
            LOG.error("Error: " + e);
            throw new RuleIsNotSupported("InvocationTargetException: " + method.getName());
        }
    }

    private static Check build(final String s) {
        String rule = s.trim();
        if ("authorized".equalsIgnoreCase(rule)) {
            return new Check() {
                @Override
                public boolean check(UserEntity me, Object object) {
                    return SecurityHelper.amIAuthorized();
                }
            };
        } else if (rule.contains("=")) {
            String [] ss = rule.split("=");
            final String a = ss[0].trim();
            final String b = ss[1].trim();
            if (!valid(a) || !valid(b)) {
                throw new RuleIsNotSupported("Not parsable rule: " + rule);
            }

            return new Check() {

                private Object get(Object obj, String field) {
                    String methodName = "get" + Strings.capitalize(field);
                    Method getter = Reflection.findMethod(obj, methodName);
                    if (getter == null) {
                        throw new RuleIsNotSupported("Object has no method: " + methodName);
                    }
                    return invokeMethod(obj, getter);
                }

                private Object getValue(String field, UserEntity me, Object object) {
                    if (field.startsWith("@")) {
                        return get(object, field.substring(1));
                    } else if (field.equalsIgnoreCase("me")) {
                        return me;
                    } else {
                        throw new RuleIsNotSupported("Unknown field: " + field);
                    }
                }

                @Override
                public boolean check(UserEntity me, Object object) {
                    LOG.debug("Checking: " + s);

                    if (me == null || object == null) {
                        return false;
                    }

                    Object x = getValue(a, me, object);
                    Object y = getValue(b, me, object);

                    LOG.debug("x: " + x);
                    LOG.debug("y: " + y);

                    return x.equals(y);
                }
            };

        } else {
            // TODO other rules
            throw new RuleIsNotSupported("This rule is currently not supported: " + rule);
        }

    }
}
