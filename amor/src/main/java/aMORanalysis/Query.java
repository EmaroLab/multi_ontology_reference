package aMORanalysis;

import it.emarolab.amor.owlInterface.OWLReferences;
import java.util.List;
import java.util.Map;

/**
 *
 * @author matteo
 */

/**
 * A Query object has two parameters: 
 * 1. an int that represents a query ID
 * 2. a String that represent the query itself in SPARQL language
 */

public class Query {
    private static final Long SPARQL_TIMEOUT = -1L;
    
    private final int queryID;
    private final String query;
    private OWLReferences ontoRef;
    
    public Query() {
        this.query = null;
        this.queryID = 0;
    }
    
    public Query(int queryID, String query, OWLReferences ontoRef) {
        this.queryID = queryID;
        this.query = query;
        this.ontoRef = ontoRef;
    }
    
    public Query(int queryID, String query) {
        this.queryID = queryID;
        this.query = query;
    }
    
    public int getQueryID() {
        return queryID;
    }
    
    public String getQuery() {
        return query;
    }
}