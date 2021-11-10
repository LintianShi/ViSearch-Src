package datatype;

import history.HBGNode;
import history.HappenBeforeGraph;
import history.Invocation;
import traceprocessing.Record;
import validation.OperationTypes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataType {
    protected OperationTypes operationTypes = null;

    public final String invoke(Invocation invocation) throws Exception {
        String methodName = invocation.getMethodName();
        Class clazz = this.getClass();
        Method method = clazz.getDeclaredMethod(methodName, Invocation.class);
        method.setAccessible(true);
        return (String)method.invoke(this, invocation);
    }

    public List<List<HBGNode>> getRelatedOperations(HBGNode node, HappenBeforeGraph happenBeforeGraph) {
        List<List<HBGNode>> lists = new ArrayList<>();
        for (HBGNode startNode : happenBeforeGraph.getStartNodes()) {
            List<HBGNode> tempList = new ArrayList<>();
            HBGNode temp = startNode;
            while (temp != null) {
                if (this.isRelated(node.getInvocation(), temp.getInvocation())) {
                    tempList.add(temp);
                }
                if (temp.equals(node)) {
                    break;
                }
                temp = temp.getNext();
            }
            if (tempList.size() > 0) {
                lists.add(tempList);
            }
        }
        return lists;
    }

    public boolean isReadCluster(Invocation invocation) {
        if (invocation.getOperationType().equals("QUERY")) {
            return true;
        } else {
            return false;
        }
    }

    protected abstract boolean isRelated(Invocation src, Invocation dest);

    public abstract void reset();

    public abstract void print();

    public abstract int hashCode();

    public abstract Invocation generateInvocation(Record record);

    public abstract AbstractDataType createInstance();

    public abstract String getOperationType(String methodName);
}
