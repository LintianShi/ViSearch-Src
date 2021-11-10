package checking;

import arbitration.VisibilityType;
import datatype.DataTypeFactory;
import history.HappenBeforeGraph;
import traceprocessing.RiakTraceProcessor;
import validation.*;
import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.LinkedList;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VisearchChecker {
    private String adt;
    private MinimalVisSearch vfs;

    public VisearchChecker(String adt) {
        this.adt = adt;
    }

    public boolean normalCheck(String input, SearchConfiguration configuration, boolean enablePreprocess) {
        HappenBeforeGraph happenBeforeGraph = load(input);
        if (enablePreprocess) {
            preprocess(happenBeforeGraph);
        }
        this.vfs = new MinimalVisSearch(configuration);
        this.vfs.init(happenBeforeGraph);
        return this.vfs.checkConsistency();
    }

    public boolean multiThreadCheck(String input, SearchConfiguration configuration, boolean enablePreprocess) {
        HappenBeforeGraph happenBeforeGraph = load(input);
        if (enablePreprocess) {
            preprocess(happenBeforeGraph);
        }
        SearchConfiguration subConfiguration = new SearchConfiguration.Builder()
                                                                .setVisibilityType(configuration.getVisibilityType())
                                                                .setAdt(configuration.getAdt())
                                                                .setEnableIncompatibleRelation(false)
                                                                .setEnableOutputSchedule(false)
                                                                .setEnablePrickOperation(false)
                                                                .setFindAllAbstractExecution(false)
                                                                .setVisibilityLimit(-1)
                                                                .setQueueLimit(20)
                                                                .setSearchMode(1)
                                                                .setSearchLimit(-1)
                                                                .build();
        MinimalVisSearch subVfs = new MinimalVisSearch(subConfiguration);
        subVfs.init(happenBeforeGraph);
        boolean result = subVfs.checkConsistency();
        List<SearchState> states = subVfs.getAllSearchState();
        if (states.size() == 0) {
            return result;
        }

        MultiThreadSearch multiThreadSearch = new MultiThreadSearch(happenBeforeGraph, configuration);
        return multiThreadSearch.startSearch(states);
    }

    protected void preprocess(HappenBeforeGraph happenBeforeGraph) {
        new HBGPreprocessor().preprocess(happenBeforeGraph, new DataTypeFactory().getDataType(adt));
    }

    protected HappenBeforeGraph load(String filename) {
        RiakTraceProcessor rp = new RiakTraceProcessor();
        HappenBeforeGraph happenBeforeGraph = rp.generateProgram(filename,  new DataTypeFactory().getDataType(adt)).generateHappenBeforeGraph();
        return happenBeforeGraph;
    }

    protected synchronized void outputResult(String filename, List<SearchState> results) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
            oos.writeObject(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readResult(String filename) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
            List<SearchState> results = (List<SearchState>) ois.readObject();
            System.out.println(results.get(0).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testDataSet(String filepath, boolean enableMulti, VisibilityType visibilityType) throws Exception {
        File baseFile = new File(filepath);
        if (baseFile.isFile() || !baseFile.exists()) {
            throw new FileNotFoundException();
        }
        File[] files = baseFile.listFiles();
        int i = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (File file : files) {
            i++;
            if (i % 1000 == 0) {
                System.out.println(i);
            }
            Boolean result = testTrace(file.toString(), enableMulti, visibilityType);
            if (!result) {
                System.out.println(file.toString() + ":" + result);
            }
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public void testDataSet(List<String> dataset, boolean enableMulti, VisibilityType visibilityType) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (String file : dataset) {
            Boolean result = testTrace(file, enableMulti, visibilityType);
            System.out.println(file + ":" + result);
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public void measureDataSet(List<String> dataset) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (String file : dataset) {
            String result = measureVisibility(file);
            System.out.println(file + ":" + result);
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public void measureDataSet(String filepath) throws Exception {
        File baseFile = new File(filepath);
        if (baseFile.isFile() || !baseFile.exists()) {
            throw new FileNotFoundException();
        }
        File[] files = baseFile.listFiles();
        int i = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (File file : files) {
            String result = measureVisibility(file.toString());
            if (!result.equals("COMPLETE")) {
                System.out.println(i + ":" + file + ":" + result);
            }
            i++;
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public boolean testTrace(String filename, boolean enableMulti, VisibilityType visibilityType) throws Exception {
        SearchConfiguration configuration = new SearchConfiguration.Builder()
                .setAdt(adt)
                .setEnableIncompatibleRelation(false)
                .setEnablePrickOperation(false)
                .setEnableOutputSchedule(false)
                .setVisibilityType(visibilityType)
                .setFindAllAbstractExecution(false)
                .build();
        Boolean result;
        if (enableMulti) {
            result = multiThreadCheck(filename, configuration, false);
        } else {
            result = normalCheck(filename, configuration, false);
        }
        return result;
    }

    public List<String> filter(String filename) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
        List<String> result = new LinkedList<String>();
        String str = null;
        while ((str = br.readLine()) != null) {
            if (str.endsWith(":false"))
                result.add(str.substring(0, str.lastIndexOf(':')));
        }
        return result;
    }

    public String measureVisibility(String filename) throws Exception {
        for (int i = 0; i < 6; i++) {
            boolean result = testTrace(filename, true, VisibilityType.values()[i]);
            if (result) {
                return VisibilityType.values()[i].name();
            }
        }
        return "undefined";
    }

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers
                .newFor("vs")
                .build()
                .defaultHelp(true)
                .description(
                        "ViSearch: A measurement framework for replicated data type on Vis-Ar weak consistency");
        parser.addArgument("-t", "--type").help(". Data type for checking")
                .type(String.class)
                .dest("type");
        parser.addArgument("-f", "--filepath").help(". File path to check")
                .type(String.class)
                .dest("filepath");
        parser.addArgument("-v", "--vis").help(". Visibility Level")
                .type(String.class)
                .dest("vis")
                .setDefault("complete");
        parser.addArgument("--measure").help(". Enable measure")
                .dest("measure")
                .action(storeTrue());
        parser.addArgument("--dataset").help(". Checking for data set")
                .dest("dataset")
                .action(storeTrue());
        Namespace res;
        try {
            res = parser.parseArgs(args);
            System.out.println(res);
            String dataType = res.getString("type");
            String filepath = res.getString("filepath");
            VisearchChecker checker = new VisearchChecker(dataType);
            if (res.getBoolean("dataset")) {
                if (res.getBoolean("measure")) {
                    checker.measureDataSet(filepath);
                } else {
                    checker.testDataSet(filepath, true, VisibilityType.getVisibility(res.getString("vis")));
                }
            } else {
                if (res.getBoolean("measure")) {
                    checker.measureVisibility(filepath);
                } else {
                    checker.testTrace(filepath, true, VisibilityType.getVisibility(res.getString("vis")));
                }
            }
            System.out.println(res);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
    }
}
