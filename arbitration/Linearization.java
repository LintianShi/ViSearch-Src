package arbitration;

import history.HBGNode;
import history.HappenBeforeGraph;

import java.io.Serializable;
import java.util.*;

public class Linearization implements Serializable, Iterable<HBGNode> {
    private List<HBGNode> lin = new ArrayList<>();
    private List<HBGNode> front = new ArrayList<>();

    public Linearization() {
        ;
    }



    public void add(HBGNode node) {
        lin.add(node);
    }

    public String getRetValueTrace(int index) {
        ArrayList<String> retTrace = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            retTrace.add(lin.get(i).getInvocation().getRetValue());
        }
        return  retTrace.toString();
    }

    public boolean contains(HBGNode node) {
        return lin.contains(node);
    }

    public boolean contains(Integer id) {
        for (HBGNode node : lin) {
            if (node.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public void addFront(List<HBGNode> nodes) {
        front.addAll(nodes);
    }

    public HBGNode getLast() {
        return lin.get(lin.size() - 1);
    }

    public HBGNode get(int index) {
        return lin.get(index);
    }

    public int size() {
        return lin.size();
    }

    public int getQueryOperationSize() {
        int sz = 0;
        for (HBGNode node : lin) {
            if (node.getInvocation().getOperationType().equals("QUERY")) {
                sz++;
            }
        }
        return sz;
    }

    public Iterator<HBGNode> iterator() {
        return lin.iterator();
    }

    public Linearization prefix(int index) {
        if (index < 0 && index >= lin.size()) {
            return null;
        } else {
            Linearization sub = new Linearization();
            for (int i = 0; i <= index; i++) {
                sub.add(lin.get(i));
            }
            return sub;
        }
    }

    public Linearization prefix(HBGNode node) {
        for (int i = 0; i < lin.size(); i++) {
            if (node.equals(lin.get(i))) {
                return prefix(i);
            }
        }
        return null;
    }

    public List<Linearization> extendLin() {
        List<Linearization> extentLins = new ArrayList<>();
        for (int i = 0; i < front.size(); i++) {
            HBGNode node = front.get(i);
            if (node == null) {
                continue;
            }
            Linearization linearization = (Linearization) this.clone();
            linearization.add(node);
            linearization.front.set(i, node.getNext());
            extentLins.add(linearization);
        }
        return extentLins;
    }

    public String toString() {
        ArrayList<String> list = new ArrayList<>();
        for (HBGNode node : lin) {
            list.add(node.toString());
        }
        return list.toString();
    }

    @Override
    public Object clone() {
        Linearization newLin = new Linearization();
        newLin.lin = new ArrayList<>(this.lin);
        newLin.front = new ArrayList<>(this.front);
        return newLin;
    }
}
