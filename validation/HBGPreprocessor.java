package validation;

import com.google.common.collect.Multimap;
import history.VisibilityType;
import com.google.common.collect.HashMultimap;
import datatype.AbstractDataType;
import datatype.DataTypeFactory;
import history.HBGNode;
import history.HappenBeforeGraph;
import rule.RuleTable;
import traceprocessing.MyRawTraceProcessor;
import util.IntPair;
import util.NodePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class HBGPreprocessor {
    private List<NodePair> extractCommonHBRelation(List<Set<NodePair>> hbs) {
        List<NodePair> results = new ArrayList<>();
        HashMap<NodePair, Integer> map = new HashMap<>();
        for (Set<NodePair> list : hbs) {
            for (NodePair hb : list) {
                if (!map.containsKey(hb)) {
                    map.put(hb, 1);
                } else {
                    int count = map.get(hb);
                    map.put(hb, count + 1);
                }

            }
        }

        for (NodePair hb : map.keySet()) {
            if (map.get(hb) == hbs.size()) {
                results.add(hb);
            }
        }
        return results;
    }

    private void extractLinRules(List<SearchState> states, HashMultimap<HBGNode, HBGNode> linRules) {
        List<Set<NodePair>> hbs = new ArrayList<>();
        for (SearchState state : states) {
            hbs.add(state.extractHBRelation());
        }
        List<NodePair> commonHBs = extractCommonHBRelation(hbs);
        for (NodePair pair : commonHBs) {
//            System.out.printf("Lin: %s -> %s\n", pair.left.toString(), pair.right);
            linRules.put(pair.right, pair.left);
        }
//        System.out.println();
    }

    private void addVisRule(NodePair vis, Set<NodePair> hbs, HashMultimap<NodePair, NodePair> visRules, HashSet<NodePair> set) {
        if (!visRules.containsKey(vis)) {
            visRules.putAll(vis, hbs);
        } else {
            List<NodePair> removeHb = new ArrayList<>();
            for (NodePair hb : visRules.get(vis)) {
                if (!hbs.contains(hb)) {
                    removeHb.add(hb);
                }
            }
            for (NodePair r : removeHb) {
                visRules.remove(vis, r);
            }
        }
    }

    private HashMultimap<NodePair, NodePair> checkVisRules(HBGNode visNode, HashMultimap<NodePair, NodePair> visRules, List<SearchState> states) {
        if (visRules.isEmpty()) {
            return visRules;
        }
        for (NodePair key : visRules.keySet()) {
            for (NodePair value : visRules.get(key)) {
                
            }
        }
        for (SearchState state : states) {
            Set<NodePair> hbs = state.extractHBRelation();
            Set<NodePair> vis = state.extractVisRelation(visNode);
            List<NodePair> removeKeys = new LinkedList<>();
            for (NodePair key : visRules.keySet()) {
                if (!vis.contains(key)) {
                    Set<NodePair> values = visRules.get(key);
                    boolean flag = false;
                    for (NodePair value : values) {
                        if (!hbs.contains(value)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        continue;
                    }
                    removeKeys.add(key);
                }
            }
            for (NodePair key : removeKeys) {
                visRules.removeAll(key);
            }
        }

        return visRules;
    }

    private HashMultimap<NodePair, NodePair> extractVisRules(HBGNode visNode, List<SearchState> states) {
        HashSet<NodePair> set = new HashSet<>();
        HashMultimap<NodePair, NodePair> visRules = HashMultimap.create();
        for (SearchState state : states) {
            Set<NodePair> hbs = state.extractHBRelation();
            Set<NodePair> vis = state.extractVisRelation(visNode);
            for (NodePair v : vis) {
                addVisRule(v, hbs, visRules, set);
            }
        }
        return checkVisRules(visNode, visRules, states);
    }


    public RuleTable preprocess(HappenBeforeGraph happenBeforeGraph, String dataType, VisibilityType visibilityType) {
        AbstractDataType adt = new DataTypeFactory().getDataType(dataType);
        HashMultimap<HBGNode, HBGNode> linRules = HashMultimap.create();
        for (HBGNode node : happenBeforeGraph) {
            if (adt.isReadCluster(node.getInvocation())) {
//                System.out.println(node.toString());
                List<List<HBGNode>> relatedNodes = adt.getRelatedOperations(node, happenBeforeGraph);
//                System.out.println(relatedNodes.toString());

                HappenBeforeGraph subHBGraph = new HappenBeforeGraph(relatedNodes);
                if (subHBGraph.size() <= 1) {
                    continue;
                }

                SearchConfiguration configuration = new SearchConfiguration.Builder()
                                                            .setAdt(dataType)
                                                            .setFindAllAbstractExecution(true)
                                                            .setEnablePrickOperation(false)
                                                            .setVisibilityType(visibilityType)
                                                            .setEnableOutputSchedule(false)
                                                            .setEnableIncompatibleRelation(false)
                                                            .build();
                MinimalVisSearch subSearch = new MinimalVisSearch(configuration);
                subSearch.init(subHBGraph);
                subSearch.checkConsistency();
                extractLinRules(subSearch.getResults(), linRules);
//                HashMultimap<NodePair, NodePair> visRules = extractVisRules(node, subSearch.getResults());
//                if (!visRules.isEmpty()) {
//                    for (List<HBGNode> list : relatedNodes) {
//                        System.out.println(list);
//                    }
//                    System.out.println("Vis:");
//                    for (NodePair pair : visRules.keySet()) {
//                        System.out.println(pair.toString() + "=" + visRules.get(pair).toString());
//                    }
//                    System.out.println("%-");
//                }

            }
        }
        RuleTable ruleTable = new RuleTable(linRules);
        return ruleTable;
    }

    public static void main(String args[]) throws Exception {
//        Multimap<Integer, Integer> map = HashMultimap.create();
//        map.put(1, 1);
//        map.put(1, 2);
//        map.remove(1, 2);
//        map.remove(1, 1);
//        System.out.println(map.toString());
        File baseFile = new File("D:\\set311_with_size\\result");
        String dataType = "set";
        if (baseFile.isFile() || !baseFile.exists()) {
            throw new FileNotFoundException();
        }
        File[] files = baseFile.listFiles();
        int i = 0;
        for (File file : files) {
            i++;
            if (i == 1000) {
                return;
            }

            System.out.println(file.toString());
            MyRawTraceProcessor rp = new MyRawTraceProcessor();
            HappenBeforeGraph happenBeforeGraph = rp.generateProgram(file.toString(), new DataTypeFactory().getDataType(dataType)).generateHappenBeforeGraph();
//            new HBGPreprocessor().preprocess(happenBeforeGraph, dataType);
        }
    }
}

