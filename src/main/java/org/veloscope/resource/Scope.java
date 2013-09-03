package org.veloscope.resource;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class Scope<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Scope.class);

    private EntityManager entityManager;
    private Class<T> clazz;
    private List<Criterion> restrictions = new ArrayList<Criterion>();
    private List<Order> orders = new ArrayList<Order>();

    private Scope(EntityManager entityManager, Class<T> clazz) {
        this.entityManager = entityManager;
        this.clazz = clazz;
    }

    public List<T> list(Integer page, Integer perPage) {
        Criteria c = buildCriteria();
        c.setFirstResult(page * perPage);
        c.setMaxResults(perPage);
        return (List<T>) c.list();
    }

    private Criteria buildCriteria() {
        Session hibernateSession = (Session) entityManager.getDelegate();
        Criteria c = hibernateSession.createCriteria(this.clazz);
        for (Criterion ct : restrictions) {
            c.add(ct);
        }
        for (Order o : orders) {
            c.addOrder(o);
        }
        return c;
    }

    public List<T> list() {
        Criteria c = buildCriteria();
        return (List<T>) c.list();
    }

    public T unique() {
        Criteria c = buildCriteria();
        return (T) c.uniqueResult();
    }

    public T first() {
        Criteria c = buildCriteria();
        return (T) c.uniqueResult();
    }

    public Long count() {
        Criteria c = buildCriteria();
        c.setProjection(Projections.rowCount());
        return (Long) c.uniqueResult();
    }

    public static <T> Scope<T> build(EntityManager entityManager, Class<T> clazz) {
        return new Scope<T>(entityManager, clazz);
    }

    public Scope<T> rest(Criterion... restrictions) {
        for (Criterion c : restrictions) {
            this.restrictions.add(c);
        }
        return this;
    }

    public Scope<T> by(String name, String value) {
        // todo temp hack, should be fixed
        if (name.endsWith("id") || name.endsWith("Id")) {
            Long val = Long.parseLong(value);
            return rest(Restrictions.eq(name, val));
        }

        return rest(Restrictions.eq(name, value));
    }

    public Scope<T> by(String name, Long value) {
        return rest(Restrictions.eq(name, value));
    }

    public Scope<T> by(String name, Object value) {
        return rest(Restrictions.eq(name, value));
    }

    public Scope<T> orders(Order... orders) {
        for (Order o : orders) {
            this.orders.add(o);
        }
        return this;
    }
}
