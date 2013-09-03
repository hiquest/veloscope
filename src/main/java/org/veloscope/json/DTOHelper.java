package org.veloscope.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class DTOHelper {
    private static final Logger LOG = LoggerFactory.getLogger(DTOHelper.class);

    public static <T, V extends DataTransferObject<T>> List<V> fromList(List<T> list, Class<V> clazz) {
        List<V> out = new ArrayList<V>();

        for (T t: list) {
            try {
                V v = clazz.newInstance();
                v.fill(t);
                out.add(v);
            } catch (InstantiationException e) {
                e.printStackTrace();
                LOG.error("Error!", e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                LOG.error("Error!", e);
            }
        }

        return out;
    }

    public static <T, V extends DataTransferObject<T>> V fromEntity(T entity, Class<V> clazz) {
        if (entity == null) {
            return null;
        }

        V v = null;
        try {
            v = clazz.newInstance();
            v.fill(entity);
        } catch (InstantiationException e) {
            e.printStackTrace();
            LOG.error("Error!", e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            LOG.error("Error!", e);
        }

        return v;
    }

}
