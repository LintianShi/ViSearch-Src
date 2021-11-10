package validation;

import arbitration.VisibilityType;
import history.HBGNode;
import history.HappenBeforeGraph;
import arbitration.Linearization;
import arbitration.LinVisibility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import util.Pair;

import java.io.Serializable;
import java.util.*;

public class SearchState implements Serializable, Comparable<SearchState> {
    public static transient HappenBeforeGraph happenBeforeGraph;
    private Linearization linearization;
    private LinVisibility visibility;
    private transient Set<HBGNode> visibleNodes = null;
    private transient ManualRecurse manualRecurse = null;
    public static VisibilityType visibilityType;

    public SearchState() {
        this.linearization = new Linearization();
        this.visibility = new LinVisibility();
    }

    public SearchState(Linearization linearization) {
        this.linearization = linearization;
        this.visibility = new LinVisibility();
    }

    public SearchState(Linearization linearization,LinVisibility visibility) {
        this.linearization = linearization;
        this.visibility = visibility;
    }

    public boolean isComplete() {
        return happenBeforeGraph.size() == linearization.size() && happenBeforeGraph.size() == visibility.size();
    }

    public List<SearchState> linExtent() {
        List<Linearization> newLins = linearization.extendLin();
        List<SearchState> newStates = new ArrayList<>();
        for (int i = 0; i < newLins.size(); i++) {
            SearchState newState = new SearchState(newLins.get(i), (LinVisibility) visibility.clone());
            newStates.add(newState);
        }
        return newStates;
    }

    public List<HBGNode> nextVisibility() {
        if (manualRecurse == null) {
            visibleNodes = getVisibleNodes();
            List<HBGNode> candidateNodes = getCandinateNodes(visibleNodes);
            this.manualRecurse = new ManualRecurse(candidateNodes);
        }
        List<HBGNode> subset = null;
        if ((subset = manualRecurse.enumerate()) != null) {
            Set<HBGNode> vis = new HashSet<>(visibleNodes);
            if (visibilityType == VisibilityType.CAUSAL || visibilityType == VisibilityType.PEER) {
                vis.addAll(closure(subset));
                //vis = closure(vis);
            } else {
                vis.addAll(subset);
            }
            visibility.updateNodeVisibility(linearization.getLast(), vis);
        }
        return subset;
    }

    private Set<HBGNode> closure(Collection<HBGNode> nodes) {
        if (visibilityType == VisibilityType.CAUSAL) {
            Set<HBGNode> result = new HashSet<>();
            for (HBGNode node : nodes) {
                result.addAll(visibility.getNodeVisibility(node));
            }
            return result;
        } else if (visibilityType == VisibilityType.PEER) {
            Set<HBGNode> result = new HashSet<>();
            for (HBGNode node : nodes) {
                result.addAll(node.getAllPrevs());
            }
            return result;
        } else {
            return new HashSet<>();
        }
    }


    public void pruneVisibility(List<HBGNode> vis) {
        manualRecurse.prune(vis);
    }

    private Set<HBGNode> getVisibleNodes() {
        Set<HBGNode> visibleNodes = new HashSet<>();
        if (visibilityType == VisibilityType.COMPLETE) {
            for (HBGNode node : linearization) {
                visibleNodes.add(node);
            }
        }  else if (visibilityType == VisibilityType.MONOTONIC || visibilityType == VisibilityType.PEER || visibilityType == VisibilityType.CAUSAL) {
            HBGNode node = linearization.getLast();
            List<HBGNode> prevs = node.getAllPrevs();
            for (HBGNode prev : prevs) {
                visibleNodes.addAll(visibility.getNodeVisibility(prev));
            }
            visibleNodes.addAll(prevs);
            visibleNodes.add(node);
        } else if (visibilityType == VisibilityType.BASIC) {
            HBGNode node = linearization.getLast();
            List<HBGNode> prevs = node.getAllPrevs();
            visibleNodes.addAll(prevs);
            visibleNodes.add(node);
        } else if (visibilityType == VisibilityType.WEAK) {
            HBGNode node = linearization.getLast();
            visibleNodes.add(node);
        }
        return visibleNodes;
    }

    private List<HBGNode> getCandinateNodes(Set<HBGNode> visibleNodes) {
        List<HBGNode> candidate = new ArrayList<>();
        if (visibilityType == VisibilityType.COMPLETE) {
            ;
        } else if (visibilityType == VisibilityType.WEAK) {
            for (int i = 0; i < linearization.size() - 1; i++) {
                candidate.add(linearization.get(i));
            }
        } else {
            for (HBGNode node : linearization) {
                if (!visibleNodes.contains(node)) {
                    candidate.add(node);
                }
            }
        }
        return candidate;
    }

    public Linearization getLinearization() {
        return linearization;
    }

    public LinVisibility getVisibility() {
        return visibility;
    }

    public List<Pair> extractHBRelation() {
        List<Pair> hbs = new ArrayList<>();
        for (int i = 1; i < linearization.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (linearization.get(j).getThreadId() != linearization.get(i).getThreadId()) {
                    hbs.add(new Pair(linearization.get(j).getId(), linearization.get(i).getId()));
                }
            }
        }
        return hbs;
    }

    public int compareTo(SearchState o) {
        if (linearization.size() > o.linearization.size()) {
            return 1;
        } else if (linearization.size() == o.linearization.size()) {
            return 0;
        } else {
            return -1;
        }
    }

    public int size() {
        return linearization.size();
    }

    public int getQueryOperationSize() {
        return linearization.getQueryOperationSize();
    }

    public String toString() {
        return linearization.toString() + " | " + visibility.toString();
    }

}
