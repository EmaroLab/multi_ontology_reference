package aMORanalysis;

import it.emarolab.amor.owlDebugger.Logger;
import java.io.File;

/**
 *
 * @author matteo
 */

public class Test {
    /**
     * the path where results will be saved
     */
    public static final String RESULTS_PATH = "files/benchmarks/Results";
    /**
     * the path which contains ontologies to test
     */
    public static final String ONTOLOGIES_DIRECTORY = "files/benchmarks/LUBM-references/generated";
    
    /**
     * if AVERAGE_EXTENDED_VERSION is true each query is executed 10 times, 
     * each execution time and the average time are written on csv file
     * 
     * N.B. you may also change TIME_CSV_ID in queriesChart.py file if you want to plot charts
     */
    public static final Boolean AVERAGE_EXTENDED_VERSION = true;
    
    /**
     * this method generates the results directory and the timestamp directory, which contains all results
     * of a test, if they don't exist and return the path of the timestamp directory 
     * @return 
     */
    public static String resultsDirectoryManager() {
        Long timestamp = System.currentTimeMillis();
        File resultsDir = new File(RESULTS_PATH);
        File timestampDir = new File(RESULTS_PATH + "/" + timestamp);

        if (!resultsDir.exists())
            resultsDir.mkdir();
        if(!timestampDir.exists())
            timestampDir.mkdir();
        
        return timestampDir.getAbsolutePath();
    }
    
    /**
     * This method creates an empty directory which will contain charts
     * @param file 
     */
    public static void createChartsDirectory(File file) {
        for(File fileEntry : file.listFiles()) {
            File chartsDir = new File(fileEntry.getAbsolutePath() + "/Charts");
            if(!chartsDir.exists())
                chartsDir.mkdir();
        }
    }
    
    public static void main(String[] args){
        
        Logger.setPrintOnConsole(false);
        final File folder = new File(ONTOLOGIES_DIRECTORY);
        String resultsDir = resultsDirectoryManager();
        new aMORTest().testQueriesOnOntologies(folder, resultsDir);
        new OWLAPITest().testQueriesOnOntologies(folder, resultsDir);
        
        createChartsDirectory(new File(resultsDir));
        }
}
