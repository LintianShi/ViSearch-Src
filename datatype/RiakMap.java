package datatype;

import history.HBGNode;
import history.HappenBeforeGraph;
import history.Invocation;
import traceprocessing.Record;
import validation.OperationTypes;

import java.util.*;

public class RiakMap extends AbstractDataType {
    private HashMap<Integer, Integer> data = new HashMap<>();

    public String getOperationType(String methodName) {
        if (operationTypes == null) {
            operationTypes = new OperationTypes();
            operationTypes.setOperationType("put", "UPDATE");
            operationTypes.setOperationType("get", "QUERY");
            operationTypes.setOperationType("containsValue", "QUERY");
            operationTypes.setOperationType("size", "QUERY");
            return operationTypes.getOperationType(methodName);
        } else {
            return operationTypes.getOperationType(methodName);
        }
    }

//    public List<List<HBGNode>> getRelatedOperations(HBGNode node, HappenBeforeGraph happenBeforeGraph) {
//        List<List<HBGNode>> lists = new ArrayList<>();
//        Set<Integer> keys = new HashSet<>();
//        for (HBGNode startNode : happenBeforeGraph.getStartNodes()) {
//            List<HBGNode> tempList = new ArrayList<>();
//            HBGNode temp = startNode;
//            while (happenBeforeGraph.getPo(temp) != null) {
//                if (this.isRelated(node.getInvocation(), temp.getInvocation())) {
//                    if (node.getInvocation().getMethodName().equals("containsValue")) {
//                        keys.add((Integer) temp.getInvocation().getArguments().get(0));
//                    } else {
//                        tempList.add(temp.clone());
//                    }
//                }
//                temp = happenBeforeGraph.getPo(temp);
//            }
//            if (tempList.size() > 0) {
//                lists.add(tempList);
//            }
//        }
//        if (!node.getInvocation().getMethodName().equals("containsValue")) {
//            return lists;
//        }
//        for (HBGNode startNode : happenBeforeGraph.getStartNodes()) {
//            List<HBGNode> tempList = new ArrayList<>();
//            HBGNode temp = startNode;
//            while (happenBeforeGraph.getPo(temp) != null) {
//                if (isRelated(node.getInvocation(), temp.getInvocation(), keys)) {
//                    tempList.add(temp.clone());
//                }
//                temp = happenBeforeGraph.getPo(temp);
//            }
//            if (tempList.size() > 0) {
//                lists.add(tempList);
//            }
//        }
//        System.out.println(lists.toString());
//        return lists;
//    }

    private boolean isRelated(Invocation src, Invocation dest, Set<Integer> keys) {
        if (!src.getMethodName().equals("containsValue")) {
            return false;
        } else {
            if (src.getId() == dest.getId()) {
                return true;
            } else if (dest.getMethodName().equals("put") && keys.contains((Integer) dest.getArguments().get(0))) {
                return true;
            }
        }
        return false;
    }

    protected boolean isRelated(Invocation src, Invocation dest) {
        if (src.getOperationType().equals("UPDATE")) {
            return false;
        } else if (src.getOperationType().equals("QUERY")) {
            if (src.getMethodName().equals("get")) {
                if (src.getId() == dest.getId()) {
                    return true;
                }
                Integer key = (Integer) src.getArguments().get(0);
                if (dest.getMethodName().equals("put") && dest.getArguments().get(0).equals(key)) {
                    return true;
                } else {
                    return false;
                }
            } else if (src.getMethodName().equals("containsValue")) {
                Integer value = (Integer) src.getArguments().get(0);
                if (dest.getOperationType().equals("UPDATE") && dest.getArguments().get(1).equals(value)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public Invocation generateInvocation(Record record) {
        Invocation invocation = new Invocation();
        invocation.setRetValue(record.getRetValue());
        invocation.setMethodName(record.getOperationName());
        invocation.setOperationType(getOperationType(record.getOperationName()));

        if (record.getOperationName().equals("put")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
            invocation.addArguments(Integer.parseInt(record.getArgument(1)));
        } else if (record.getOperationName().equals("get")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("containsValue")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("size")) {
            ;
        } else {
            System.out.println("Unknown operation");
        }
        return invocation;
    }

    public void reset() {
        data = new HashMap<>();
    }

    public void print() {
        System.out.println(data.toString());
    }

    public int hashCode() {
        return data.hashCode();
    }

    public AbstractDataType createInstance() {
        return new RiakMap();
    }

    public String put(Invocation invocation) {
        Integer key = (Integer) invocation.getArguments().get(0);
        Integer value = (Integer) invocation.getArguments().get(1);
        data.put(key, value);
        return "null";
    }

    public String get(Invocation invocation) {
        Integer key = (Integer) invocation.getArguments().get(0);
        Integer value = data.get(key);
        if (value != null) {
            return Integer.toString(value);
        } else {
            return "null";
        }
    }

    public String containsValue(Invocation invocation) {
        Integer value = (Integer) invocation.getArguments().get(0);
        if (data.containsValue(value)) {
            return "true";
        } else {
            return "false";
        }
    }

    public String size(Invocation invocation) {
        return Integer.toString(data.size());
    }
}
