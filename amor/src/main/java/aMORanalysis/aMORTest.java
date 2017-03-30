package aMORanalysis;

/**
 *
 * @author matteo
 */

import it.emarolab.amor.owlDebugger.Logger;
import java.util.List;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;
import it.emarolab.amor.owlInterface.OWLReferences;

public class aMORTest {
    
    /**
     * if CACHED_REASONER is true testQueriesOnOntologies uses the same ontology reference foreach query 
     * and it does a first query not considering and not saving its results, then begins the effective simulation
     * 
     * if CACHED_REASONER is false testQueriesOnOntologies loads a new ontology reference foreach query
     */
    public static final boolean CACHED_REASONER = true;
    public static final Boolean BUFFERING_REASONER = false; 
    
    public static final String ONTOLOGIES_DIRECTORY = "files/benchmarks/LUBM-references/generated";

    public static final String RESULTS_PATH = "files/benchmarks/aMOR-Results";
    
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
    
    
    /**
     *
     * this method iterates through files in the ontologies folder,
     *for each file generates a list of queries and calls lubmTest.csvFileManager()
     * @param folder
     * @param resultsDir 
     */
    public static void testQueriesOnOntologies(final File folder, String resultsDir) {
        
        Long timestamp = System.currentTimeMillis();// / 1000;
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                testQueriesOnOntologies(fileEntry, resultsDir);
            } else {
                String ontology_file_path = fileEntry.getAbsolutePath();
                String ontology_iri_path = "file:" + fileEntry.getAbsolutePath(); 
                        //"http://www.cs.ox.ac.uk/isg/tools/RDFox/2014/AAAI/input/UOBM/owl/UOBM.owl";
                        
                
                File ontoFile = new File(ontology_file_path);
                List ids = getIdsFromFileName(getFileNameFromPath(ontoFile));
                try{
                    String refName = ids.get(0).toString() + "_" + ids.get(1).toString();

                    OWLReferences ontoRef = null;
                    if(CACHED_REASONER){
                        ontoRef = OWLReferencesContainer.newOWLReferenceFromFileWithPellet(refName, ontology_file_path, ontology_iri_path, BUFFERING_REASONER);
                        /**
                         * Next 3 lines prevent a wrong estimation of the first query execution time
                         */ 
                        aMORQuery uselessQuery = new aMORQuery(9,QUERY9);
                        uselessQuery.setOWLReferences( ontoRef);
                        LUBMTest lubmTest = new LUBMTest();
                        String uselessLine = lubmTest.getCsvLineFromQuery(uselessQuery);
                        
                        ontoRef.synchronizeReasoner();
                    }
                    
                    List<aMORQuery> queriesList = new ArrayList();

                    aMORQuery query1 = new aMORQuery(1,QUERY1_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY1_2);
                    aMORQuery query2 = new aMORQuery(2,QUERY2);
                    aMORQuery query3 = new aMORQuery(3,QUERY3_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY3_2);
                    aMORQuery query4 = new aMORQuery(4,QUERY4_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY4_2);
                    aMORQuery query5 = new aMORQuery(5,QUERY5_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY5_2);
                    aMORQuery query6 = new aMORQuery(6,QUERY6);
                    aMORQuery query7 = new aMORQuery(7,QUERY7_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY7_2);
                    aMORQuery query8 = new aMORQuery(8,QUERY8_1 + ids.get(0).toString() + QUERY8_2);
                    aMORQuery query9 = new aMORQuery(9,QUERY9);
                    aMORQuery query10 = new aMORQuery(10,QUERY10_1 + ids.get(1).toString() + ".University" + ids.get(0).toString() + QUERY10_2);
                    aMORQuery query11 = new aMORQuery(11,QUERY11_1 + ids.get(0).toString() + QUERY11_2);
                    aMORQuery query12 = new aMORQuery(12,QUERY12_1 + ids.get(0).toString() + QUERY12_2);
                    aMORQuery query13 = new aMORQuery(13,QUERY13_1 + ids.get(0).toString() + QUERY13_2);
                    aMORQuery query14 = new aMORQuery(14,QUERY14);
                    
                    
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
                    
                    for(aMORQuery q : queriesList){
                        OWLReferences ontTmp;
                        if(ontoRef == null) {  // = (CACHED_REASONER == false)
                            OWLReferencesContainer.getOWLReferencesKeys().remove(refName);
                            ontTmp = OWLReferencesContainer.newOWLReferenceFromFileWithPellet(refName, ontology_file_path, ontology_iri_path, BUFFERING_REASONER);
                            q.setOWLReferences(ontTmp);
                        } else q.setOWLReferences(ontoRef);
                    }
                       
                    File ontologyResultsDir = new File( resultsDir + "/" 
                            + getResultsFileNameFromIds(Integer.parseInt(ids.get(0).toString()), Integer.parseInt(ids.get(1).toString())));


                    if(!ontologyResultsDir.exists()) 
                        ontologyResultsDir.mkdir();

                    new LUBMTest().csvFileManager(queriesList, ontologyResultsDir.toString() 
                            + "/aMOR-" + getResultsFileNameFromIds(Integer.parseInt(ids.get(0).toString()), Integer.parseInt(ids.get(1).toString())));

                    queriesList.clear();
                } catch( Exception e){
                    e.printStackTrace();
                    System.err.println( "Cannot manage file: " + ontology_file_path);
                }
            }
        }
    }
    
    /**
     * This method returns the string corresponding to the name of the results file of a test.
     * It takes in input the university id and the department id taken by the tested ontology file 
     * @param uniId
     * @param depId
     * @return 
     */
    public static String getResultsFileNameFromIds(int uniId, int depId) {
        String fileName = "uni" + uniId + "-dep" + depId ;
        return fileName;
    }
    
    /**
     * This method takes a string corresponding to an ontology file name and returns
     * a list containing university and department ids
     * @param fileName
     * @return 
     */
    public static List<String> getIdsFromFileName(String fileName) {
        fileName = fileName.replaceAll("[^0-9]+", " ");
        return Arrays.asList(fileName.trim().split(" "));
    }
    
    /**
     * This method takes a java File and returns its name
     * @param file
     * @return 
     */
    public static String getFileNameFromPath(File file) {
        return file.getName();
    }
    
    public static String getFileNameFromPath(String file) {
        File newFile = new File(file);
        return getFileNameFromPath(newFile);
    }
    
    
    public static void main(String[] args){
        
        Logger.setPrintOnConsole(false);
        final File folder = new File(ONTOLOGIES_DIRECTORY);
        String resultsDir = new Test().resultsDirectoryManager();
        testQueriesOnOntologies(folder, resultsDir);
        
    }
}