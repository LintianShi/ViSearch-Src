package validation;

import arbitration.VisibilityType;
import datatype.AbstractDataType;
import datatype.DataTypeFactory;
import history.HBGNode;
import history.HappenBeforeGraph;
import traceprocessing.RiakTraceProcessor;
import util.Pair;
import util.PairOfPair;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HBGPreprocessor {
    private List<Pair> extractCommonHBRelation(List<List<Pair>> hbs) {
        List<Pair> results = new ArrayList<>();
        HashMap<Pair, Integer> map = new HashMap<>();
        for (List<Pair> list : hbs) {
            for (Pair hb : list) {
                if (!map.containsKey(hb)) {
                    map.put(hb, 1);
                } else {
                    int count = map.get(hb);
                    map.put(hb, count + 1);
                }

            }
        }

        for (Pair hb : map.keySet()) {
            if (map.get(hb) == hbs.size()) {
                results.add(hb);
            }
        }
        return results;
    }

//    private List<PairOfPair> removeCommonRelations(List<PairOfPair> incompatibleRelations, List<Pair> commonRelations) {
//        List<PairOfPair> cleanIncompatibleRelations = new ArrayList<>();
//        for (PairOfPair pairOfPair : incompatibleRelations) {
//            boolean flag = true;
//            for (Pair common : commonRelations) {
//                Pair reversePair = common.getReversal();
//                if (pairOfPair.getLeft().equals(reversePair) || pairOfPair.getRight().equals(reversePair)) {
//                    flag = false;
//                    break;
//                }
//            }
//            if (flag) {
//                cleanIncompatibleRelations.add(pairOfPair);
//            }
//        }
//        return cleanIncompatibleRelations;
//        return incompatibleRelations;
//    }

//    private List<PairOfPair> extractCooccurrenceHBRelation(List<List<Pair>> hbs) {
//        HashMap<PairOfPair, Integer> map = new HashMap<>();
//        for (List<Pair> list : hbs) {
//            for (int i = 0; i < list.size(); i++) {
//                for (int j = i + 1; j < list.size(); j++) {
//                    PairOfPair pairOfPair = new PairOfPair(list.get(i), list.get(j));
//                    if (!map.containsKey(pairOfPair)) {
//                        map.put(pairOfPair, 1);
//                    } else {
//                        int count = map.get(pairOfPair);
//                        map.put(pairOfPair, count + 1);
//                    }
//                }
//            }
//        }
//
//        List<PairOfPair> cooccurrencePairs = new ArrayList<>();
//        for (PairOfPair pairOfPair : map.keySet()) {
//            if (map.get(pairOfPair) + map.getOrDefault(pairOfPair.getNegative(), 0) == hbs.size()) {
//                cooccurrencePairs.add(pairOfPair);
//            }
//        }
//
//        return cooccurrencePairs;
//    }


    public void preprocess(HappenBeforeGraph happenBeforeGraph, AbstractDataType adt) {
        List<PairOfPair> coccurrenceRelations = new ArrayList<>();
        for (HBGNode node : happenBeforeGraph) {
            if (adt.isReadCluster(node.getInvocation())) {
//                System.out.println(node.toString());
                List<List<HBGNode>> relatedNodes = adt.getRelatedOperations(node, happenBeforeGraph);
//                System.out.println(relatedNodes.toString());

                HappenBeforeGraph subHBGraph = new HappenBeforeGraph(relatedNodes);
//                System.out.println("Sub graph size: " + subHBGraph.size());
//                if (subHBGraph.size() > 5) {
//                    continue;
//                }

                SearchConfiguration configuration = new SearchConfiguration.Builder()
                                                            .setAdt("set")
                                                            .setFindAllAbstractExecution(true)
                                                            .setEnablePrickOperation(false)
                                                            .setVisibilityType(VisibilityType.CAUSAL)
                                                            .setEnableOutputSchedule(false)
                                                            .setEnableIncompatibleRelation(false)
                                                            .build();
                MinimalVisSearch subSearch = new MinimalVisSearch(configuration);
                subSearch.init(subHBGraph);
                subSearch.checkConsistency();
                if (subSearch.getResults().size() == 0) {
                    System.out.println("no abstract execution");
                    continue;
                }

                List<List<Pair>> hbs = new ArrayList<>();
                for (SearchState state : subSearch.getResults()) {
                    hbs.add(state.extractHBRelation());
                }

                List<Pair> commonHBs = extractCommonHBRelation(hbs);
                for (Pair pair : commonHBs) {
                    System.out.printf("%s -> %s\n", happenBeforeGraph.get(pair.left).toString(), happenBeforeGraph.get(pair.right).toString());
                }
//                addHBRelations(happenBeforeGraph, commonHBs);
//
//                List<PairOfPair> subCoccurrenceRelations = removeCommonRelations(extractCooccurrenceHBRelation(hbs), commonHBs);
//                System.out.printf("Common: %d, Co-occurrence: %d\n", commonHBs.size(), subCoccurrenceRelations.size());
//                for (PairOfPair pairOfPair : subCoccurrenceRelations) {
//                    System.out.printf("<%s -> %s> --- <%s ->%s>\n", happenBeforeGraph.get(pairOfPair.getLeft().getLeft()).toString(), happenBeforeGraph.get(pairOfPair.getLeft().getRight()).toString(), happenBeforeGraph.get(pairOfPair.getRight().getLeft()).toString(), happenBeforeGraph.get(pairOfPair.getRight().getRight()).toString());
//                }
//               coccurrenceRelations.addAll(subCoccurrenceRelations);
//
//                System.out.println("Common relation size: " + commonHBs.size());
//                System.out.println("Co-occurrence relation size: " + subCoccurrenceRelations.size());
            }
        }

//        Multimap<ImmutablePair<Integer, Integer>, ImmutablePair<Integer, Integer>> ruleTable = generateRuleTable(incompatibleRelations);
//        happenBeforeGraph.setRuleTable(ruleTable);
    }

    public static void main(String args[]) throws Exception {
        File baseFile = new File("D:\\set311_with_size\\result");
        AbstractDataType adt = new DataTypeFactory().getDataType("set");
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
            RiakTraceProcessor rp = new RiakTraceProcessor();
            HappenBeforeGraph happenBeforeGraph = rp.generateProgram(file.toString(), adt).generateHappenBeforeGraph();
            new HBGPreprocessor().preprocess(happenBeforeGraph, adt);
        }
    }
}

