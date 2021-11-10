package datatype;

import history.Invocation;
import traceprocessing.Record;
import validation.OperationTypes;

public class RiakCounter extends AbstractDataType {
    private Integer data;

    public String getOperationType(String methodName) {
        if (operationTypes == null) {
            operationTypes = new OperationTypes();
            operationTypes.setOperationType("inc", "UPDATE");
            operationTypes.setOperationType("dec", "UPDATE");
            operationTypes.setOperationType("get", "QUERY");
            return operationTypes.getOperationType(methodName);
        } else {
            return operationTypes.getOperationType(methodName);
        }
    }

    protected boolean isRelated(Invocation src, Invocation dest) {
        return true;
    }

    public Invocation generateInvocation(Record record) {
        Invocation invocation = new Invocation();
        invocation.setRetValue(record.getRetValue());
        invocation.setMethodName(record.getOperationName());
        invocation.setOperationType(getOperationType(record.getOperationName()));

        if (record.getOperationName().equals("inc")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("dec")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("get")) {
            ;
        } else {
            System.out.println("Unknown operation");
        }
        return invocation;
    }

    public String inc(Invocation invocation) {
        Integer value = (Integer) invocation.getArguments().get(0);
        data += value;
        return null;
    }

    public String dec(Invocation invocation) {
        Integer value = (Integer) invocation.getArguments().get(0);
        data -= value;
        return "null";
    }

    public String get(Invocation invocation) {
        return Integer.toString(data);
    }

    public void reset() {
        data = 0;
    }

    public void print() {
        System.out.println(data);
    }

    public int hashCode() {
        return data;
    }

    public AbstractDataType createInstance() {
        return new RiakCounter();
    }
}
