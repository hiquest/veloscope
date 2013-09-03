package org.veloscope.resource;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veloscope.annotations.DefaultOrder;
import org.veloscope.annotations.FakeDelete;
import org.veloscope.annotations.grants.ListFilter;
import org.veloscope.checks.Check;
import org.veloscope.checks.PrepareChecks;
import org.veloscope.exceptions.InvalidConfiguration;
import org.veloscope.json.DTOHelper;
import org.veloscope.json.DataTransferObject;
import org.veloscope.security.SecurityHelper;
import org.veloscope.utils.Reflection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * Resource.
 *
 * @param <T>
 */
public class Resource<T extends EntityInterface> {

    private static final Logger LOG = LoggerFactory.getLogger(Resource.class);

    @PersistenceContext
    protected EntityManager entityManager;
    private Class<T> cl;
    private Class<? extends DataTransferObject<T>> defaultDTOClass;
    private List<String> allowedFilters;
    private List<Order> defaultOrders;
    private Map<String, List<Check>> checks = new HashMap<String, List<Check>>();
    private boolean fakeDelete = false;

    public Resource() {
        Class<? extends Resource> myClass = this.getClass();

        prepareDefaultOrders();
        this.checks.put("get", PrepareChecks.forGet(myClass));
        this.checks.put("delete", PrepareChecks.forDelete(myClass));
        prepareAllowedFilters();
        fakeDelete = myClass.isAnnotationPresent(FakeDelete.class);
    }

    public Resource(Class cl) {
        this();
        this.cl = cl;
    }

    public Resource(Class cl, Class<? extends DataTransferObject<T>> defaultDTOClass) {
        this();
        this.cl = cl;
        this.defaultDTOClass = defaultDTOClass;
    }

    public Resource(EntityManager entityManager, Class<T> cl, Class<? extends DataTransferObject<T>> defaultDTOClass) {
        this();
        this.entityManager = entityManager;
        this.cl = cl;
        this.defaultDTOClass = defaultDTOClass;
    }

    private void prepareDefaultOrders() {
        defaultOrders = new ArrayList<Order>();
        Class resourceClass = this.getClass();
        if (resourceClass.isAnnotationPresent(DefaultOrder.class)) {
            DefaultOrder filter = (DefaultOrder) resourceClass.getAnnotation(DefaultOrder.class);
            String orderType = filter.order();
            String field = filter.by();
            if (!orderType.equalsIgnoreCase("asc") && !orderType.equalsIgnoreCase("desc")) {
                throw new InvalidConfiguration("Invalid order type: " + orderType);
            }
            defaultOrders.add( orderType.equalsIgnoreCase("desc") ? Order.desc(field) : Order.asc(field) );
        }
    }

    private void prepareAllowedFilters() {
        allowedFilters = new ArrayList<String>();
        Class resourceClass = this.getClass();
        if (resourceClass.isAnnotationPresent(ListFilter.class)) {
            ListFilter filter = (ListFilter) resourceClass.getAnnotation(ListFilter.class);
            this.allowedFilters = Arrays.asList(filter.by().split("[,\\s]+"));
        }

        LOG.debug("Filters prepared: " + allowedFilters.size());
    }


    //--------- DAO ----------------
    public Criteria createCriteria() {
        Session hibernateSession = (Session) entityManager.getDelegate();
        return hibernateSession.createCriteria(cl);
    }

    public Query getNamedQuery(String str) {
        Session hibernateSession = (Session) entityManager.getDelegate();
        return hibernateSession.getNamedQuery(str);
    }

    public Query createQuery(String str) {
        Session hibernateSession = (Session) entityManager.getDelegate();
        return hibernateSession.createQuery(str);
    }

    public T findById(Long id) {
        return entityManager.find(cl, id);
    }

    public void merge(T obj) {
        if (obj.getId() == null) {
            throw new IllegalStateException("Object doesn't have an id and therefore can not be merged");
        }

        entityManager.merge(obj);
        entityManager.flush();
    }

    public Long save(T obj) {
        entityManager.persist(obj);
        entityManager.flush();
        return obj.getId();
    }

    public void saveOrMerge(T obj) {
        if (obj.getId() == null) {
            save(obj);
        } else {
            merge(obj);
        }
    }

    public void delete(T obj) {
        if (this.fakeDelete) {
            Method m = Reflection.findMethod(obj, "setDeleted");
            if (m == null) {
                throw new InvalidConfiguration("setDeleted method is not present in entity class but resource is annotated with @FakeDelete");
            }
            try {
                m.invoke(obj, Boolean.TRUE);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new InvalidConfiguration(e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new InvalidConfiguration(e);
            }
            entityManager.merge(obj);
        } else {
            entityManager.remove(obj);
        }
    }

    // ------------- PERMISSIONS ----------------
    public boolean canFilter(String field) {
        LOG.debug("Check can filter: " + field);
        return this.allowedFilters.contains(field);
    }

    public boolean canFilter(Collection<String> fields) {
        for (String field : fields) {
            if (!canFilter(field)) {
                return false;
            }
        }
        return true;
    }

    public boolean canDelete() {
        return can("delete");
    }

    public boolean can(String action) {
        return can(action, null);
    }

    public boolean can(String action, EntityInterface object) {
        List<Check> chs = checks.get(action);
        if (chs == null) {
            throw new InvalidParameterException("No such action registered: " + action);
        }

        for (Check check : chs) {
            if (!check.check(SecurityHelper.me(), object)) {
                return false;
            }
        }
        return true;
    }

    // -------------- SCOPES ---------------------
    public Scope<T> buildScope() {
        Scope<T> out = Scope.build(entityManager, cl);
        if (fakeDelete) {
            out = out.rest(Restrictions.eq("deleted", Boolean.FALSE));
        }

        return out.rest(defaultScope()).orders(defaultOrders());
    }

    private Order[] defaultOrders() {
        return defaultOrders.toArray(new Order[defaultOrders.size()]);
    }

    public Criterion[] defaultScope() {
        return new Criterion[]{};
    }

    public List<T> list(Integer page, Integer perPage) {
        return buildScope().list(page, perPage);
    }

    public List<T> list() {
        return buildScope().list();
    }

    public T first() {
        return buildScope().first();
    }

    // --------------- JSON ----------------------
    public DataTransferObject<T> toJSON(T obj) {
        if (defaultDTOClass == null) {
            throw new InvalidParameterException("defaultDTOClass is not set!");
        }
        return toJSON(obj, defaultDTOClass);
    }

    public DataTransferObject<T> toJSON(T obj, Class<? extends DataTransferObject<T>> dtoClass) {
        return DTOHelper.fromEntity(obj, dtoClass);
    }

    public List<DataTransferObject<T>> toJSON(List<T> out) {
        if (defaultDTOClass == null) {
            throw new InvalidParameterException("defaultDTOClass is not set!");
        }
        return toJSON(out, defaultDTOClass);
    }

    public List<DataTransferObject<T>> toJSON(List<T> out, Class dtoClass) {
        return DTOHelper.fromList(out, dtoClass);
    }
}
