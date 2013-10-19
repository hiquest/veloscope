package org.veloscope.utils;

import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veloscope.annotations.DefaultOrder;
import org.veloscope.annotations.QueryField;
import org.veloscope.annotations.grants.ListFilter;
import org.veloscope.exceptions.InvalidConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourcePrepare {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcePrepare.class);

    public static List<Order> prepareDefaultOrders(Class resourceClass) {
        List<Order> defaultOrders = new ArrayList<Order>();
        if (resourceClass.isAnnotationPresent(DefaultOrder.class)) {
            DefaultOrder filter = (DefaultOrder) resourceClass.getAnnotation(DefaultOrder.class);
            String orderType = filter.order();
            String field = filter.by();
            if (!orderType.equalsIgnoreCase("asc") && !orderType.equalsIgnoreCase("desc")) {
                throw new InvalidConfiguration("Invalid order type: " + orderType);
            }
            defaultOrders.add(orderType.equalsIgnoreCase("desc") ? Order.desc(field) : Order.asc(field));
        }
        return defaultOrders;
    }

    public static List<String> prepareAllowedFilters(Class resourceClass) {
        List<String> allowedFilters = new ArrayList<String>();
        if (resourceClass.isAnnotationPresent(ListFilter.class)) {
            ListFilter filter = (ListFilter) resourceClass.getAnnotation(ListFilter.class);
            allowedFilters = Arrays.asList(filter.by().split("[,\\s]+"));
        }

        LOG.debug("Filters prepared: " + allowedFilters.size());
        return allowedFilters;
    }

    public static List<String> prepareQueryFields(Class resourceClass) {
        List<String> out = new ArrayList<String>();

        if (resourceClass.isAnnotationPresent(QueryField.class)) {
            QueryField filter = (QueryField) resourceClass.getAnnotation(QueryField.class);
            out = Arrays.asList(filter.by().split("[,\\s]+"));
        }

        return out;
    }


}
