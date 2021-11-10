package validation;

import datatype.AbstractDataType;
import history.*;
import arbitration.*;

import java.util.*;

public class Validation {
    public static boolean crdtExecute(AbstractDataType adt, SearchState searchState) {
        Linearization lin = searchState.getLinearization();
        LinVisibility visibility = searchState.getVisibility();
        try {
            HBGNode lastNode = lin.getLast();
            if (lastNode.getInvocation().getOperationType().equals("UPDATE")) {
                return true;
            } else if (lastNode.getInvocation().getOperationType().equals("QUERY")) {
                Set<HBGNode> vis = visibility.getNodeVisibility(lastNode);
                for (int i = 0; i < lin.size() - 1; i++) {
                    HBGNode node = lin.get(i);
                    if (node.getInvocation().getOperationType().equals("UPDATE") && vis.contains(node)) {
                        adt.invoke(node.getInvocation());
                    }
                }
                String ret = adt.invoke(lastNode.getInvocation());
                adt.reset();
                if (lastNode.getInvocation().getRetValue().equals(ret)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
