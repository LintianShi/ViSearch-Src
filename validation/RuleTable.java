package validation;

import arbitration.Linearization;
import com.google.common.collect.HashMultimap;
import history.HBGNode;

import java.util.Collection;

public class RuleTable {
    private HashMultimap<HBGNode, HBGNode> linRules;

    public RuleTable(HashMultimap<HBGNode, HBGNode> rules) {
        this.linRules = rules;
    }

    public boolean linearizationFilter(Linearization linearization, HBGNode node) {
        Collection<HBGNode> mustBefore = linRules.get(node);
        for (HBGNode n : mustBefore) {
            if (linearization.contains(n)) {
                return false;
            }
        }
        return true;
    }
}
