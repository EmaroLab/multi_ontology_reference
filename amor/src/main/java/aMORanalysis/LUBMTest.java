package aMORanalysis;

/**
 * LUBMTest class manages 
 * @author matteo
 */


import com.clarkparsia.pellet.owlapi.PelletReasoner;
import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import java.util.List;
import java.util.Map;

import it.emarolab.amor.owlInterface.OWLLibrary;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.jena.PelletInfGraph;

public class LUBMTest {
    
    public static final String REASONER_FACTORY = OWLLibrary.REASONER_QUALIFIER_PELLET;
    public static final Integer COMMAND = OWLReferencesContainer.COMMAND_LOAD_FILE;
    public static final Boolean BUFFERING_OWLMANIPULATOR = false;
    
    private static final Long SPARQL_TIMEOUT = -1L;
    
    //private static Logger logger = new Logger( OWLRefEnquirerExample.class, false);//Logger.LoggerFlag.getLogOWLEnquirer());
    
    public static final String CSV_FILE_HEADER = "timestamp[ms];uniID;depID;queryID;time[ns];Complexity;resultSize;resultMapToString";
    public static final String CSV_FILE_HEADER_EXTENDED = "timestamp[ms];uniID;depID;queryID;"
                                                        + "time1[ns];time2[ns];time3[ns];time4[ns];time5[ns];"
                                                        + "time6[ns];time7[ns];time8[ns];time9[ns];time10[ns];"
                                                        + "average[ns];Complexity;resultSize;resultMapToString";
    public static final String DELIMITER = ";";
    public static final String NEW_LINE_SEPARATOR = "\n";
    
    
    
    public LUBMTest(){}
    
    /**
     * aMOR version
     * 
     * this method takes an aMORQuery and executes it writing the execution time
     * with other values in a string that will be written in the csv file
     * 
     * There are two ways to do that depending on the value of AVERAGE_EXTENDED_VERSION flag
     * if it is true the query is executed 10 times
     * @param query
     * @return 
     */
    public String getCsvLineFromQuery(aMORQuery query){
        String csvLine = null;
        List<Map<String, String>> result = null;
        if(!Test.AVERAGE_EXTENDED_VERSION){
            Long startTime = System.nanoTime();
            result = query.ask();
            Long endTime = System.nanoTime();
            Long time = endTime - startTime;
            //logger.addDebugString("SPARQL results: " + result);
            
            csvLine = String.valueOf(System.currentTimeMillis()) + DELIMITER 
                    + aMORTest.getIdsFromFileName( query.getOWLReferences().getReferenceName()).get(0) //this.ontoRef.getReferenceName()).get(0)
                    + DELIMITER + aMORTest.getIdsFromFileName( query.getOWLReferences().getReferenceName()).get(1) //this.ontoRef.getReferenceName()).get(1)
                    + DELIMITER + query.getQueryID()
                    + DELIMITER + String.valueOf(time) + DELIMITER + query.getOWLReferences().getOWLOntology().getAxiomCount()
                    + DELIMITER + result.size()
                    + DELIMITER + result.toString();
            
        }
        else {
            Long timeSum = 0L;
            csvLine = String.valueOf(System.currentTimeMillis()) + DELIMITER
                    + aMORTest.getIdsFromFileName(query.getOWLReferences().getReferenceName()).get(1)
                    + DELIMITER + aMORTest.getIdsFromFileName(query.getOWLReferences().getReferenceName()).get(0)
                    + DELIMITER + query.getQueryID();
            for(int i = 0; i < 10; i++) {
                Long startTime = System.nanoTime();
                result = query.ask();// this.ontoRef.sparql2Msg(query.getQuery(), SPARQL_TIMEOUT);
                Long endTime = System.nanoTime();
                Long time = endTime - startTime;
                timeSum += time;
                csvLine = csvLine.concat(DELIMITER + String.valueOf(time));
            }
            csvLine = csvLine.concat(DELIMITER + timeSum/10 + DELIMITER + query.getOWLReferences().getOWLOntology().getAxiomCount() + DELIMITER + result.size() + DELIMITER + result.toString());
        }
            return csvLine;
    }
    
    
    /**
     * OWL API version
     * 
     * this method takes a query and the csv file path (it is needed to retreive ids of university and department
     * on which the query is executed)
     * 
     * Execution time and other values will be written in a string corresponding to a csv line
     * 
     * There are two ways to do that depending on the value of AVERAGE_EXTENDED_VERSION flag
     * if it is true the query is executed 10 times
     * 
     * @param query
     * @param csvFilePath
     * @return 
     */
    public String getCsvLineFromQuery(Query query, String csvFilePath) {
        String csvLine = null;

        if(!Test.AVERAGE_EXTENDED_VERSION) {
            Long startTime = System.nanoTime();
            KnowledgeBase kb = ((PelletReasoner) OWLAPITest.reasoner).getKB();
            PelletInfGraph graph = new org.mindswap.pellet.jena.PelletReasoner().bind(kb);
            InfModel model = ModelFactory.createInfModel(graph);
            // make the query
            QueryExecution qe = SparqlDLExecutionFactory.create(QueryFactory.create(query.getQuery()), model);
            String queryLog = qe.getQuery().toString() + System.getProperty("line.separator") + "[TimeOut:";
            if (SPARQL_TIMEOUT!= null) { // apply time out
		if (SPARQL_TIMEOUT > 0) {
                    qe.setTimeout(SPARQL_TIMEOUT);
                    queryLog += SPARQL_TIMEOUT + "ms].";
		} else queryLog += "NONE].";
            } else queryLog += "NONE].";
            ResultSet results = qe.execSelect();
            // store the results
            List<QuerySolution> solutions = new ArrayList<>();
            while (results.hasNext()) {
		QuerySolution r = results.next();
		solutions.add(r);
            }
            //logger.addDebugString("SPARQL query:" + System.getProperty("line.separator") + queryLog + System.getProperty("line.separator") + ResultSetFormatter.asText(results));
			
            Long endTime = System.nanoTime();
            Long time = endTime - startTime;
            csvLine = String.valueOf(System.currentTimeMillis()) + DELIMITER 
                    + aMORTest.getIdsFromFileName(aMORTest.getFileNameFromPath(csvFilePath)).get(0) //this.ontoRef.getReferenceName()).get(0)
                    + DELIMITER + aMORTest.getIdsFromFileName(aMORTest.getFileNameFromPath(csvFilePath)).get(1) //this.ontoRef.getReferenceName()).get(1)
                    + DELIMITER + query.getQueryID()
                    + DELIMITER + String.valueOf(time) + DELIMITER + OWLAPITest.ontology.getAxiomCount()
                    + DELIMITER + sparql2Msg(solutions).size()
                    + DELIMITER + sparql2Msg(solutions).toString();
            return csvLine;
        }
        else {
            Long timesum = 0L;
            List<QuerySolution> solutions = new ArrayList<>();
            csvLine = String.valueOf(System.currentTimeMillis()) + DELIMITER 
                    + aMORTest.getIdsFromFileName(aMORTest.getFileNameFromPath(csvFilePath)).get(0) //this.ontoRef.getReferenceName()).get(0)
                    + DELIMITER + aMORTest.getIdsFromFileName(aMORTest.getFileNameFromPath(csvFilePath)).get(1) //this.ontoRef.getReferenceName()).get(1)
                    + DELIMITER + query.getQueryID();
            for(int i = 0; i < 10; i++) {
                Long startTime = System.nanoTime();
                KnowledgeBase kb = ((PelletReasoner) OWLAPITest.reasoner).getKB();
                PelletInfGraph graph = new org.mindswap.pellet.jena.PelletReasoner().bind(kb);
                InfModel model = ModelFactory.createInfModel(graph);
                // make the query
                QueryExecution qe = SparqlDLExecutionFactory.create(QueryFactory.create(query.getQuery()), model);
                String queryLog = qe.getQuery().toString() + System.getProperty("line.separator") + "[TimeOut:";
                if (SPARQL_TIMEOUT!= null) { // apply time out
                    if (SPARQL_TIMEOUT > 0) {
                        qe.setTimeout(SPARQL_TIMEOUT); // TODO: it does not seems to work with pellet SELECT queries
                        queryLog += SPARQL_TIMEOUT + "ms].";
                    } else queryLog += "NONE].";
                } else queryLog += "NONE].";
                ResultSet results = qe.execSelect(); // TODO: it can do much more.... (ask query, evaluate constants, etc.)
                // store the results
                
                while (results.hasNext()) {
                    QuerySolution r = results.next();
                    solutions.add(r);
                }
                //logger.addDebugString("SPARQL query:" + System.getProperty("line.separator") + queryLog + System.getProperty("line.separator") + ResultSetFormatter.asText(results));
			
                Long endTime = System.nanoTime();
                Long time = endTime - startTime;
                timesum += time;
                csvLine = csvLine.concat(DELIMITER + String.valueOf(time));
            }
            csvLine = csvLine.concat(
                    DELIMITER + timesum/10 + 
                    DELIMITER + OWLAPITest.ontology.getAxiomCount() + 
                    DELIMITER + sparql2Msg(solutions).size() + 
                    DELIMITER + sparql2Msg(solutions).toString());
        }
        return csvLine;
    }
    
    /**
     * It is a copy of the methods implemented in aMOR, used to convert a List<QuerySolution>
     * in a List<Map<String,String>> to correctly represent data in the CSV file in the OWL-API version
     * @param results
     * @return 
     */
    public List< Map< String, String>> sparql2Msg( List< QuerySolution> results) {
        List< Map< String, String>> out = new ArrayList();
		if( results != null) // timeout
			for( QuerySolution q : results){
				Iterator<String> names = q.varNames();
				Map<String, String> item = new HashMap<>();
				while( names.hasNext()){
					String n = names.next();
					item.put( n, q.get( n).toString());
				}
				out.add( item);
			}
        return out;
    }
    
    /**
     * this method is used to write in a csv file a string
     * @param line
     * @param fileWriter 
     */
    public void writeCSVLine(String line, FileWriter fileWriter) {
        try {            
            fileWriter.append(line);
            fileWriter.append(NEW_LINE_SEPARATOR);
        }catch(Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
    }
    
    /**
     * this method takes a list of aMORQuery and calls getCsvLineFromQuery(aMORQuery) for each query contained
     * in it, each result is written on csv file from writeCsvLine(String, FileWriter)
     * @param queriesList
     * @param csvFilePath 
     */
    public void csvFileManager(List<aMORQuery> queriesList, String csvFilePath) {
        FileWriter  fileWriter = null;
        try {
            fileWriter = new FileWriter(csvFilePath + ".csv");
            if(!Test.AVERAGE_EXTENDED_VERSION) {
                fileWriter.append(CSV_FILE_HEADER);
                fileWriter.append(NEW_LINE_SEPARATOR);
            }
            else {
                fileWriter.append(CSV_FILE_HEADER_EXTENDED);
                fileWriter.append(NEW_LINE_SEPARATOR);
            }
            OWLReferences ontoRef = null;
            for(aMORQuery query : queriesList){ 
                writeCSVLine(getCsvLineFromQuery(query), fileWriter);
                 ontoRef = query.getOWLReferences(); // !!!!!!!!!!!
            }
            if( ontoRef != null)
                ontoRef.saveOntology(csvFilePath.replace("aMOR-","") + "-enquired.owl");
        }catch(Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();

        } finally {
            try {
                if( fileWriter != null){
                    fileWriter.flush();
                    fileWriter.close();
                } else System.err.println( "null file writer for " + csvFilePath);
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * this method is the OWL-API version of the method above, it take a list of Query and for each query calls
     * getCsvLineFromQuery(Query, String)
     * @param queriesList
     * @param csvFilePath 
     */
    public void OWLAPICsvFileManager(List<Query> queriesList, String csvFilePath) {
        FileWriter  fileWriter = null;
        try {
            fileWriter = new FileWriter(csvFilePath + ".csv");
            if(!Test.AVERAGE_EXTENDED_VERSION) {
                fileWriter.append(CSV_FILE_HEADER);
                fileWriter.append(NEW_LINE_SEPARATOR);
            }
            else {
                fileWriter.append(CSV_FILE_HEADER_EXTENDED);
                fileWriter.append(NEW_LINE_SEPARATOR);
            }
            for(Query query : queriesList){ 
                writeCSVLine(getCsvLineFromQuery(query, csvFilePath), fileWriter);
            }
        }catch(Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();

        } finally {
            try {
                if( fileWriter != null){
                    fileWriter.flush();
                    fileWriter.close();
                } else System.err.println( "null file writer for " + csvFilePath);
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }
    
}