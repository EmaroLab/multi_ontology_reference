/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aMORanalysis;

import it.emarolab.amor.owlInterface.OWLReferences;
import java.util.List;
import java.util.Map;

/**
 *
 * @author matteo
 */

/**
 * aMORQuery extends the Query class providing an ontology reference
 */

public class aMORQuery extends Query{
    
    private static final Long SPARQL_TIMEOUT = -1L;
    private OWLReferences ontoRef;
    
    public aMORQuery(int queryID, String query, OWLReferences ontoRef) {
        
        super(queryID, query);
        this.ontoRef = ontoRef;

    }
    
    public aMORQuery(int queryID, String query) {
        super(queryID, query);
    }
    
    
    public List<Map<String, String>> ask(){
        return ontoRef.sparql2Msg( getQuery(), SPARQL_TIMEOUT);
    }
    
    
    public void setOWLReferences( OWLReferences ontoRef){
        this.ontoRef = ontoRef;
    }
    
    
    public OWLReferences getOWLReferences(){
        return this.ontoRef;
    }
    
}
