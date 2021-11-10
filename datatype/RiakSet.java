package datatype;

import history.Invocation;
import traceprocessing.Record;
import validation.OperationTypes;

import java.util.HashSet;

public class RiakSet extends AbstractDataType {
    private HashSet<Integer> data = new HashSet<>();

    public String getOperationType(String methodName) {
        if (operationTypes == null) {
            operationTypes = new OperationTypes();
            operationTypes.setOperationType("remove", "UPDATE");
            operationTypes.setOperationType("add", "UPDATE");
            operationTypes.setOperationType("contains", "QUERY");
            operationTypes.setOperationType("size", "QUERY");
            return operationTypes.getOperationType(methodName);
        } else {
            return operationTypes.getOperationType(methodName);
        }
    }

    public boolean isReadCluster(Invocation invocation) {
        if (invocation.getMethodName().equals("contains")) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isRelated(Invocation src, Invocation dest) {
        if (src.getOperationType().equals("UPDATE")) {
            return false;
        } else if (src.getOperationType().equals("QUERY")) {
            if (src.getId() == dest.getId()) {
                return true;
            }
            Integer ele = (Integer) src.getArguments().get(0);
            if (dest.getOperationType().equals("UPDATE") && dest.getArguments().get(0).equals(ele)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void reset() {
        data = new HashSet<>();
    }

    public void print() {
        System.out.println(data.toString());
    }

    public int hashCode() {
        return data.hashCode();
    }

    public Invocation generateInvocation(Record record) {
        Invocation invocation = new Invocation();
        invocation.setRetValue(record.getRetValue());
        invocation.setMethodName(record.getOperationName());
        invocation.setOperationType(getOperationType(record.getOperationName()));

        if (record.getOperationName().equals("add")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("remove")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("contains")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("size")) {
           ;
        } else {
            System.out.println("Unknown operation");
        }
        return invocation;
    }

    public AbstractDataType createInstance() {
        return new RiakSet();
    }

    public String add(Invocation invocation) {
        Integer key = (Integer) invocation.getArguments().get(0);
        data.add(key);
        return "null";
    }

    public String remove(Invocation invocation) {
        Integer key = (Integer) invocation.getArguments().get(0);
        data.remove(key);
        return "null";
    }

    public String contains(Invocation invocation) {
        Integer key = (Integer) invocation.getArguments().get(0);
        if (data.contains(key)) {
            return "true";
        } else {
            return "false";
        }
    }

    public String size(Invocation invocation) {
        return Integer.toString(data.size());
    }
}
