package datatype;

import history.Invocation;
import traceprocessing.Record;
import validation.OperationTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RedisRpq extends AbstractDataType {
    private ArrayList<RpqElement> data = new ArrayList<>();
    private HashMap<Integer, RpqElement> map = new HashMap<>();

    public AbstractDataType createInstance() {
        return new RedisRpq();
    }

    public String getOperationType(String methodName) {
        if (operationTypes == null) {
            operationTypes = new OperationTypes();
            operationTypes.setOperationType("rem", "UPDATE");
            operationTypes.setOperationType("add", "UPDATE");
            operationTypes.setOperationType("incrby", "UPDATE");
            operationTypes.setOperationType("max", "QUERY");
            operationTypes.setOperationType("score", "QUERY");
            return operationTypes.getOperationType(methodName);
        } else {
            return operationTypes.getOperationType(methodName);
        }
    }

    protected boolean isRelated(Invocation src, Invocation dest) {
        if (src.getOperationType().equals("UPDATE")) {
            return false;
        } else if (src.getOperationType().equals("QUERY")) {
            if (src.getId() == dest.getId()) {
                return true;
            }
            if (src.getMethodName().equals("score")) {
                Integer ele = (Integer) src.getArguments().get(0);
                if (dest.getOperationType().equals("UPDATE") && dest.getArguments().get(0).equals(ele)) {
                    return true;
                } else {
                    return false;
                }
            } else if (src.getMethodName().equals("max")) {
                if (src.getRetValue().equals("null")) {
                    return false;
                }
                Integer ele = Integer.parseInt(src.getRetValue().split(" ")[0]);
                if (dest.getOperationType().equals("UPDATE") && dest.getArguments().get(0).equals(ele)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public Invocation generateInvocation(Record record) {
        Invocation invocation = new Invocation();
        invocation.setRetValue(record.getRetValue());
        invocation.setMethodName(record.getOperationName());
        invocation.setOperationType(getOperationType(record.getOperationName()));

        if (record.getOperationName().equals("add")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
            invocation.addArguments(Integer.parseInt(record.getArgument(1)));
        } else if (record.getOperationName().equals("rem")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("incrby")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
            invocation.addArguments(Integer.parseInt(record.getArgument(1)));
        } else if (record.getOperationName().equals("max")) {
            String ret = record.getRetValue();
            if (!ret.equals("null")) {
                invocation.setRetValue(ret.split(" ")[1]);
            }
        } else if (record.getOperationName().equals("score")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else {
            System.out.println("Unknown operation");
        }

        return invocation;
    }

    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public void reset() {
        map = new HashMap<>();
        data = new ArrayList<>();
    }
    public void print() {;}

    private void shiftUp(int s) {
        int j = s, i = (j - 1) / 2;
        RpqElement temp = data.get(j);
        while (j > 0)
        {
            if (data.get(i).getVal().doubleValue() >= temp.getVal().doubleValue())
                break;
            else
                {
                    data.set(j, data.get(i));
                    data.get(j).setIndex(j);
                    j = i;
                    i = (i - 1) / 2;
                }
        }
        temp.setIndex(j);
        data.set(j, temp);
    }

    private void shiftDown(int s)
    {
        int i = s, j = 2 * i + 1, tail = data.size() - 1;
        RpqElement temp;
        if (i < data.size()) {
            temp = data.get(i);
        } else {
            return;
        }
        while (j <= tail)
        {
            if (j < tail && data.get(j).getVal().doubleValue() <= data.get(j + 1).getVal().doubleValue())
                j++;
            if (temp.getVal().doubleValue() >= data.get(j).getVal().doubleValue())
                break;
        else
            {
                data.set(i, data.get(j));
                data.get(i).setIndex(i);
                i = j;
                j = i * 2 + 1;
            }
        }
        temp.setIndex(i);
        data.set(i, temp);
    }

    private void add(int k, Integer v)
    {
        if (!map.containsKey(k)) {
            RpqElement element = new RpqElement(k, v);
            map.put(k, element);
            data.add(element);
            shiftUp(data.size() - 1);
        } else {
            RpqElement element = map.get(k);
            if (element.getTemp()) {
                inc(k, v);
                element.setTemp(false);
            }
        }
    }

    private void rem(int k)
    {
        if (map.containsKey(k))
        {
            int i = map.get(k).getIndex();
            map.remove(k);
            data.set(i, data.get(data.size() - 1));
            data.remove(data.size() - 1);
            shiftDown(i);
        }
    }

    private void inc(int k, int i)
    {
        if (i == 0)
            return;
        if (map.containsKey(k))
        {
            map.get(k).inc(i);
            if (i > 0)
                shiftUp(map.get(k).getIndex());
            else
                shiftDown(map.get(k).getIndex());
        } else {
            RpqElement element = new RpqElement(k, i);
            element.setTemp(true);
            map.put(k, element);
            data.add(element);
            shiftUp(data.size() - 1);
        }
    }

    private String max()
    {
        if (data.size() == 0) {
            //return "rwfzscore:" + "NONE";
            return "null";
        } else {
            RpqElement max = data.get(0);
            Integer val = max.getVal();
            //return "rwfzmax:" + Integer.toString(max.getEle()) + ":" + val.stripTrailingZeros().toPlainString();
            return  val.toString();
        }
    }

    private String score(Integer k) {
        if (data.size() == 0 || !map.containsKey(k)) {
            //return "rwfzscore:" + Integer.toString(k) + ":" + "NONE";
            return "null";
        } else {
            Integer val = map.get(k).getVal();
            //return "rwfzscore:" + Integer.toString(k) + ":" + val.stripTrailingZeros().toPlainString();
            return val.toString();
        }
    }

    public String add(Invocation invocation) {
        Integer k = (Integer) invocation.getArguments().get(0);
        Integer i = (Integer) invocation.getArguments().get(1);
        add(k, i);
        return "null";
        //return invocation.getMethodName() + ":" + Integer.toString(k) + ":" + v.toString();
    }

    public String rem(Invocation invocation) {
        Integer k = (Integer) invocation.getArguments().get(0);
        rem(k);
        return "null";
        //return invocation.getMethodName() + ":" + Integer.toString(k);
    }

    public String incrby(Invocation invocation) {
        Integer k = (Integer) invocation.getArguments().get(0);
        Integer i = (Integer) invocation.getArguments().get(1);
        inc(k, i);
        return "null";
        //return invocation.getMethodName() + ":" + Integer.toString(k) + ":" + i.toString();
    }

    public String score(Invocation invocation) {
        Integer k = (Integer) invocation.getArguments().get(0);
        return score(k);
        //return invocation.getMethodName();
    }

    public String max(Invocation invocation) {
        return max();
    }
}

class RpqElement {
    private Integer ele;
    private Integer val;
    private Integer index;
    private Boolean temp = false;

    public RpqElement(Integer ele, Integer val) {
        this.ele = ele;
        this.val = val;
    }

    public Integer getVal() {
        return val;
    }

    public Integer getEle() {
        return ele;
    }

    public void setEle(Integer ele) {
        this.ele = ele;
    }

    public void setVal(Integer val) {
        this.val = val;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Boolean getTemp() {
        return temp;
    }

    public void setTemp(Boolean temp) {
        this.temp = temp;
    }

    public void inc(Integer i) {
        val = val + i;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ele, val, index);
    }
}
