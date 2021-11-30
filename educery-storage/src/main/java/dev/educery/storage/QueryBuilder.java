package dev.educery.storage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static dev.educery.utils.Utils.*;

/**
 * Builds queries and count queries.
 *
 * <h4>QueryBuilder Responsibilities:</h4>
 * <ul>
 * <li>knows either query text or a query name</li>
 * <li>knows either count text or a count name</li>
 * <li>knows query parameter values</li>
 * <li>builds a query from its values</li>
 * </ul>
 *
 * <h4>Client Responsibilities:</h4>
 * <ul>
 * <li>properly constructs a QueryBuilder</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class QueryBuilder {

    private final HashMap<String, Object> values = new HashMap<>();

    /**
     * @param queryName a query name
     * @return a new QueryBuilder
     */
    public static QueryBuilder withQueryName(String queryName) {
        QueryBuilder result = new QueryBuilder();
        return result.withValue(QueryName, queryName);
    }

    /**
     * @param queryText a query text
     * @return a new QueryBuilder
     */
    public static QueryBuilder withQueryText(String queryText) {
        return new QueryBuilder().withValue(QueryText, queryText); }

    /**
     * @param countName a count name
     * @return this QueryBuilder with a count
     */
    public QueryBuilder withCountNamed(String countName) { return this.withValue(CountName, countName); }

    /**
     * @param countText contains the text of a count query
     * @return this QueryBuilder with a count
     */
    public QueryBuilder withCountText(String countText) { return this.withValue(CountText, countText); }

    /**
     * @param valueName a value name
     * @param namedValue a named value
     * @return this QueryBuilder with a named value
     */
    public QueryBuilder withValue(String valueName, Object namedValue) {
        this.values.put(valueName, namedValue); return this; }

    /**
     * @param namedValues some named values
     * @return this QueryBuilder with named values
     */
    public QueryBuilder withValues(Map<String, Object> namedValues) {
        this.values.putAll(namedValues); return this; }

    public boolean hasNamedQuery() { return this.values.containsKey(QueryName); }
    public boolean hasNamedCount() { return this.values.containsKey(QueryName); }
    public boolean hasTextQuery() { return this.values.containsKey(QueryText); }
    public boolean hasTextCount() { return this.values.containsKey(QueryText); }

    public String getQueryName() { return (String) this.values.get(QueryName); }
    public String getQueryText() { return (String) this.values.get(QueryText); }

    public String getCountName() { return (String) this.values.get(CountName); }
    public String getCountText() { return (String) this.values.get(CountText); }

    /**
     * Builds a Query.
     * @param manager a query factory
     * @return a new Query
     */
    public Query buildQuery(EntityManager manager) {
        if (hasNamedQuery()) {
            return queryWithValues(manager.createNamedQuery(getQueryName()));
        }

        if (hasTextQuery()) {
            return queryWithValues(manager.createQuery(getQueryText()));
        }

        return null;
    }

    /**
     * Builds a count Query.
     * @param manager a query factory
     * @return a new count Query
     */
    public Query buildCount(EntityManager manager) {
        if (hasNamedCount()) {
            return queryWithValues(manager.createNamedQuery(getCountName()));
        }

        if (hasTextCount()) {
            return queryWithValues(manager.createQuery(getCountText()));
        }

        return null;
    }

    static final String QueryName = "queryName";
    static final String QueryText = "queryText";
    static final String CountName = "countName";
    static final String CountText = "countText";
    static final String[] ReservedKeys = {QueryName, QueryText, CountName, CountText};
    static final List<String> Reservations = wrap(ReservedKeys);
    private Query queryWithValues(Query query) {
        for (String valueName : this.values.keySet()) {
            if (!Reservations.contains(valueName)) {
                query.setParameter(valueName, this.values.get(valueName));
            }
        }
        return query;
    }

} // QueryBuilder
