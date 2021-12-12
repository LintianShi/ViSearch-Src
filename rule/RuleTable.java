package rule;

import checking.VisearchChecker;
import datatype.DataTypeFactory;
import history.HappenBeforeGraph;
import history.Linearization;
import com.google.common.collect.HashMultimap;
import history.HBGNode;
import traceprocessing.MyRawTraceProcessor;
import validation.HBGPreprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

public class RuleTable {
    private HashMultimap<HBGNode, HBGNode> linRules;
    private HashMultimap<HBGNode, HBGNode> visRules;

    public RuleTable(HashMultimap<HBGNode, HBGNode> rules) {
        this.linRules = rules;
    }

    public boolean linearizationFilter(Linearization linearization, HBGNode node) {
        Collection<HBGNode> mustBefore = linRules.get(node);
        for (HBGNode n : mustBefore) {
            if (!linearization.contains(n)) {
                return false;
            }
        }
        return true;
    }

    public int size() {
        return linRules.size();
    }

    public static void main(String[] args) throws Exception {

        File baseFile = new File("D:\\set311_with_size\\result");
        String dataType = "set";
        if (baseFile.isFile() || !baseFile.exists()) {
            throw new FileNotFoundException();
        }
        File[] files = baseFile.listFiles();
        int i = 0;
        long a = 0;
        long b = 0;
        for (File file : files) {
            VisearchChecker checker = new VisearchChecker("set", 1, false);
            VisearchChecker checker1 = new VisearchChecker("set", 1, true);
            i++;
            if (i == 10000) {
                break;
            }
//            if (!file.toString().equals("D:\\set311_with_size\\result\\set311_default_5_3_15_1634985181583.trc")) {
//                continue;
//            }
//            System.out.println(checker.measureVisibility(file.toString()));
//            System.out.println(checker1.measureVisibility(file.toString()));

            checker1.measureVisibility(file.toString());
//            System.out.println(file.toString());
            if (checker1.isStateFilter) {
                checker.measureVisibility(file.toString());
                System.out.println(checker.getAverageState() + "," + checker1.getAverageState());
            }

        }
    }
}
