package gov.vha.isaac.ochre.model.logic;

import gov.vha.isaac.ochre.api.tree.TreeNodeVisitData;
import org.apache.mahout.math.function.IntObjectProcedure;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.roaringbitmap.RoaringBitmap;

import java.util.Arrays;

/**
 * Created by kec on 7/19/15.
 */
public class IsomorphicSolution implements Comparable<IsomorphicSolution> {
    final int hashcode;
    final int[] solution;
    int score = -1;
    boolean legal = true;


    public IsomorphicSolution(int[] solution, TreeNodeVisitData referenceTreeVisitData,
                              TreeNodeVisitData comparisonTreeVisitData) {
        this.solution = solution;
        this.hashcode = Arrays.hashCode(solution);
        score(referenceTreeVisitData, comparisonTreeVisitData);
     }

    @Override
    public int hashCode() {
        return hashcode;
    }

    public int getScore() {
        return score;
    }

    public boolean isLegal() {
        return legal;
    }

    public int[] getSolution() {
        return solution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IsomorphicSolution that = (IsomorphicSolution) o;

        if (hashcode != that.hashcode) return false;
        if (score != that.score) return false;
        return Arrays.equals(solution, that.solution);
    }

    @Override
    public int compareTo(IsomorphicSolution o) {
        int comparison = Integer.compare(score, o.score);
        if (comparison != 0) {
            return comparison;
        }
        comparison = Integer.compare(hashcode, o.hashcode);
        if (comparison != 0) {
            return comparison;
        }
        return compare(solution, o.solution);
    }

    int compare(int[] o1, int[] o2) {
        for (int i = 0; i < o1.length; i++) {
            if (o1[i] != o2[i]) {
                return (o1[i] < o2[i]) ? -1 : ((o1[i] == o2[i]) ? 0 : 1);
            }
        }
        return 0;
    }

    final void score(TreeNodeVisitData referenceTreeVisitData, TreeNodeVisitData comparisonTreeVisitData) {
        OpenIntHashSet parentNodeIds = new OpenIntHashSet(solution.length);
        OpenIntHashSet usedNodeIds = new OpenIntHashSet(solution.length);
        OpenIntObjectHashMap<OpenIntHashSet> siblingGroupToNodeSequenceMap = new OpenIntObjectHashMap<>();
        int sum = 0;
        // give a bonus point ever time a common parent is used in the solution.
        int bonus = 0;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] >= 0) {
                sum++;
                if (usedNodeIds.contains(solution[i])) {
                    legal = false;
                    score = -1;
                    return;
                } else {
                    usedNodeIds.add(solution[i]);
                }
                int referenceParentNodeId = referenceTreeVisitData.getPredecessorSequence(i);
                int siblingGroup = referenceTreeVisitData.getSiblingGroupForSequence(i);
                OpenIntHashSet nodesInSiblingGroup = siblingGroupToNodeSequenceMap.get(siblingGroup);
                if (nodesInSiblingGroup == null) {
                    nodesInSiblingGroup = new OpenIntHashSet();
                    siblingGroupToNodeSequenceMap.put(siblingGroup, nodesInSiblingGroup);
                }
                nodesInSiblingGroup.add(i);
                if (referenceParentNodeId >= 0) {
                    if (parentNodeIds.contains(referenceParentNodeId)) {
                        bonus++;
                    } else {
                        parentNodeIds.add(referenceParentNodeId);
                    }
                }
            }
        }
        // For all nodes corresponding to a sibling group in the reference expression, the nodes in the
        // comparison expression must all be in the same sibling group in the comparison expression
        for (int siblingGroup: siblingGroupToNodeSequenceMap.keys().elements()) {
            OpenIntHashSet groupMembers = siblingGroupToNodeSequenceMap.get(siblingGroup);
            int comparisonSiblingGroup = -1;
            for (int groupMember: groupMembers.keys().elements()) {
                if (comparisonSiblingGroup == -1) {
                    comparisonSiblingGroup = comparisonTreeVisitData.getSiblingGroupForSequence(solution[groupMember]);
                } else {
                    if (comparisonSiblingGroup != comparisonTreeVisitData.getSiblingGroupForSequence(solution[groupMember])) {
                        legal = false;
                        score = -2;
                        return;
                    }
                }
            }
        }

        score =  sum + bonus;
    }

    @Override
    public String toString() {
        return "solution{" +
                 legal +
                "  s:" + score +
                ", " + Arrays.toString(solution) +
                '}';
    }
}
