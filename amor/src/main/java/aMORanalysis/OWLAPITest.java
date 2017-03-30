package aMORanalysis;

import com.clarkparsia.pellet.owlapi.PelletReasonerFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

/**
 *
 * @author matteo
 */
public class OWLAPITest {
    
    /**
     * if CACHED_REASONER is true testQueriesOnOntologies uses the same ontology reference foreach query 
     * and it does a first query not considering and not saving its results, then begins the effective simulation
     * 
     * if CACHED_REASONER is false testQueriesOnOntologies loads a new ontology reference foreach query
     */
    public static final boolean CACHED_REASONER = true;
    
    public static final String ONTOLOGIES_DIRECTORY = "files/benchmarks/LUBM-references/generated";
    public static final String RESULTS_PATH = "files/benchmarks/OWL-API-Results";
    
    public static final String QUERY1_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X " +
                            "WHERE " +
                            "{?X rdf:type ub:GraduateStudent . " +
                            "?X ub:takesCourse " +
                            "<http://www.Department";
    public static final String QUERY1_2 = ".edu/GraduateCourse0>}";
    public static final String QUERY2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X ?Y ?Z " +
                            "WHERE " +
                            "{?X rdf:type ub:GraduateStudent . " +
                            "?Y rdf:type ub:University . " +
                            "?Z rdf:type ub:Department . " +
                            "?X ub:memberOf ?Z . " +
                            "?Z ub:subOrganizationOf ?Y . " +
                            "?X ub:undergraduateDegreeFrom ?Y}";
    public static final String QUERY3_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X " +
                            "WHERE " +
                            "{?X rdf:type ub:Publication . " +
                            "?X ub:publicationAuthor " +
                            "<http://www.Department";
    public static final String QUERY3_2 = ".edu/AssistantProfessor0>}";
    public static final String QUERY4_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X ?Y1 ?Y2 ?Y3 " +
                            "WHERE " +
                            "{?X rdf:type ub:Professor . " +
                            "?X ub:worksFor <http://www.Department";
    public static final String QUERY4_2 = ".edu> . " +
                            "?X ub:name ?Y1 . " +
                            "?X ub:emailAddress ?Y2 . " +
                            "?X ub:telephone ?Y3}";
    public static final String QUERY5_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X " +
                            "WHERE " +
                            "{?X rdf:type ub:Person . " +
                            "?X ub:memberOf <http://www.Department";
    public static final String QUERY5_2 = ".edu>}";
    public static final String QUERY6 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X WHERE {?X rdf:type ub:Student}";
    public static final String QUERY7_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X ?Y " +
                            "WHERE " +
                            "{?X rdf:type ub:Student . " +
                            "?Y rdf:type ub:Course . " +
                            "?X ub:takesCourse ?Y . " +
                            "<http://www.Department";
    public static final String QUERY7_2 = ".edu/AssociateProfessor0> " +
                            "ub:teacherOf ?Y}";
    public static final String QUERY8_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X ?Y ?Z " +
                            "WHERE " +
                            "{?X rdf:type ub:Student . " +
                            "?Y rdf:type ub:Department . " +
                            "?X ub:memberOf ?Y . " +
                            "?Y ub:subOrganizationOf <http://www.University";
    public static final String QUERY8_2 = ".edu> . " +
                            "?X ub:emailAddress ?Z}";
    public static final String QUERY9 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X ?Y ?Z " +
                            "WHERE " +
                            "{?X rdf:type ub:Student . " +
                            "?Y rdf:type ub:Faculty . " +
                            "?Z rdf:type ub:Course . " +
                            "?X ub:advisor ?Y . " +
                            "?Y ub:teacherOf ?Z . " +
                            "?X ub:takesCourse ?Z}";
    public static final String QUERY10_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X " +
                            "WHERE " +
                            "{?X rdf:type ub:Student ." +
                            "?X ub:takesCourse " +
                            "<http://www.Department";
    public static final String QUERY10_2 = ".edu/GraduateCourse0>}";
    public static final String QUERY11_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X " +
                            "WHERE " +
                            "{?X rdf:type ub:ResearchGroup . " +
                            "?X ub:subOrganizationOf <http://www.University";
    public static final String QUERY11_2 = ".edu>}";
    public static final String QUERY12_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X ?Y " +
                            "WHERE " +
                            "{?X rdf:type ub:Chair . " +
                            "?Y rdf:type ub:Department . " +
                            "?X ub:worksFor ?Y . " +
                            "?Y ub:subOrganizationOf <http://www.University";
    public static final String QUERY12_2 = ".edu>}";
    public static final String QUERY13_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X " +
                            "WHERE " +
                            "{?X rdf:type ub:Person . " +
                            "<http://www.University";
    public static final String QUERY13_2 = ".edu> ub:hasAlumnus ?X}";
    public static final String QUERY14 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X " +
                            "WHERE {?X rdf:type ub:UndergraduateStudent}";
    
    public static OWLReasoner reasoner;
    public static OWLOntology ontology;
    
    public static void setOntology(String filePath) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager(); 
        ontology = manager.loadOntologyFromOntologyDocument(IRI.create(filePath));
        OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance(); 
        reasoner = reasonerFactory.createReasoner(ontology, new SimpleConfiguration());
        //System.out.println(manager.toString() + ontology.toString());
    }
    
    public static void testQueriesOnOntologies(final File folder, String resultsDir){
        Long timestamp = System.currentTimeMillis();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                testQueriesOnOntologies(fileEntry, resultsDir);
            } else {
                String ontology_file_path = fileEntry.getAbsolutePath();
                String ontology_iri_path = "file:" + fileEntry.getAbsolutePath();
                
                File ontoFile = new File(ontology_file_path);
                List ids = aMORTest.getIdsFromFileName(aMORTest.getFileNameFromPath(ontoFile));
                
                try{
                    setOntology(ontology_iri_path);
                    Query uselessQuery = new Query(9, QUERY9);
                    LUBMTest lubmTest = new LUBMTest();
                    String uselessLine = lubmTest.getCsvLineFromQuery(uselessQuery, ontology_file_path);
                    
                    List<Query> queriesList = new ArrayList();
                    
                    Query query1 = new  Query(1,QUERY1_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY1_2);
                    Query query2 = new  Query(2,QUERY2);
                    Query query3 = new  Query(3,QUERY3_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY3_2);
                    Query query4 = new  Query(4,QUERY4_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY4_2);
                    Query query5 = new  Query(5,QUERY5_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY5_2);
                    Query query6 = new  Query(6,QUERY6);
                    Query query7 = new  Query(7,QUERY7_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY7_2);
                    Query query8 = new  Query(8,QUERY8_1 + ids.get(0).toString() + QUERY8_2);
                    Query query9 = new  Query(9,QUERY9);
                    Query query10 = new Query(10,QUERY10_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY10_2);
                    Query query11 = new Query(11,QUERY11_1 + ids.get(0).toString() + QUERY11_2);
                    Query query12 = new Query(12,QUERY12_1 + ids.get(0).toString() + QUERY12_2);
                    Query query13 = new Query(13,QUERY13_1 + ids.get(0).toString() + QUERY13_2);
                    Query query14 = new Query(14,QUERY14);
                    
                    
                    queriesList.add(query1);
                    queriesList.add(query2);
                    queriesList.add(query3);
                    queriesList.add(query4);
                    queriesList.add(query5);
                    queriesList.add(query6);
                    queriesList.add(query7);
                    queriesList.add(query8);
                    queriesList.add(query9);
                    queriesList.add(query10);
                    queriesList.add(query11);
                    queriesList.add(query12);
                    queriesList.add(query13);
                    queriesList.add(query14);
                    
                    
                    File ontologyResultsDir = new File(resultsDir  + "/" 
                            + aMORTest.getResultsFileNameFromIds(Integer.parseInt(ids.get(0).toString()), Integer.parseInt(ids.get(1).toString())));

                    if(!ontologyResultsDir.exists()) 
                        ontologyResultsDir.mkdir();

                    new LUBMTest().OWLAPICsvFileManager(queriesList, ontologyResultsDir.toString() 
                            + "/OWLAPI-" + aMORTest.getResultsFileNameFromIds(Integer.parseInt(ids.get(0).toString()), Integer.parseInt(ids.get(1).toString())));

                    queriesList.clear();
                } catch( Exception e){
                    e.printStackTrace();
                    System.err.println( "Cannot manage file: " + ontology_file_path);
                }
            }
        }
    }
    
    public static void main(String[] args) throws OWLOntologyCreationException {
        final File folder = new File(ONTOLOGIES_DIRECTORY);
        testQueriesOnOntologies(folder, new Test().resultsDirectoryManager());
    }
}
