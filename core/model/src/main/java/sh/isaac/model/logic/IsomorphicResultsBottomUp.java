/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.model.logic;

//~--- JDK imports ------------------------------------------------------------

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.collections.SequenceSet;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.tree.TreeNodeVisitData;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class IsomorphicResultsBottomUp
         implements IsomorphicResults {
   /**
    * Nodes that are relationship roots in the referenceExpression.
    */
   private final Map<RelationshipKey, Integer> referenceRelationshipNodesMap = new TreeMap<>();

   /**
    * Nodes that are relationship roots in the comparisonExpression.
    */
   private final Map<RelationshipKey, Integer> comparisonRelationshipNodesMap = new TreeMap<>();
   SequenceSet<?>                              comparisonDeletionRoots        = new SequenceSet<>();
   SequenceSet<?>                              referenceAdditionRoots         = new SequenceSet<>();
   LogicalExpressionOchreImpl                  comparisonExpression;
   LogicalExpressionOchreImpl                  referenceExpression;
   LogicalExpressionOchreImpl                  isomorphicExpression;
   LogicalExpressionOchreImpl                  mergedExpression;

   /*
    * isomorphicSolution is a mapping from logicNodes in the referenceExpression to logicNodes
    * in the comparisonExpression. The index of the isomorphicSolution is the nodeId
    * in the referenceExpression, the value of the array at that index is the
    * nodeId in the comparisonExpression: isomorphicSolution[nodeIdInReference] == nodeIdInComparison
    * If the nodeIdInComparison == -1, then there is no corresponding node in the
    * comparisonExpression as part of the isomorphicSolution.
    */
   IsomorphicSolution isomorphicSolution;
   TreeNodeVisitData  referenceVisitData;
   TreeNodeVisitData  comparisonVisitData;
   int[]              referenceExpressionToMergedNodeIdMap;
   int[]              comparisonExpressionToReferenceNodeIdMap;

   //~--- constructors --------------------------------------------------------

   public IsomorphicResultsBottomUp(LogicalExpression referenceExpression, LogicalExpression comparisonExpression) {
      this.referenceExpression  = (LogicalExpressionOchreImpl) referenceExpression;
      this.comparisonExpression = (LogicalExpressionOchreImpl) comparisonExpression;
      this.referenceVisitData   = new TreeNodeVisitData(referenceExpression.getNodeCount());
      this.referenceExpression.depthFirstVisit(null, this.referenceExpression.getRoot(), referenceVisitData, 0);
      this.comparisonVisitData = new TreeNodeVisitData(comparisonExpression.getNodeCount());
      this.comparisonExpression.depthFirstVisit(null, comparisonExpression.getRoot(), comparisonVisitData, 0);
      this.referenceExpressionToMergedNodeIdMap = new int[referenceExpression.getNodeCount()];
      Arrays.fill(referenceExpressionToMergedNodeIdMap, -1);
      this.comparisonExpressionToReferenceNodeIdMap = new int[comparisonExpression.getNodeCount()];
      Arrays.fill(comparisonExpressionToReferenceNodeIdMap, -1);
      this.isomorphicSolution = isomorphicAnalysis();

      for (int referenceNodeId = 0; referenceNodeId < isomorphicSolution.solution.length; referenceNodeId++) {
         if (this.isomorphicSolution.solution[referenceNodeId] > -1) {
            this.comparisonExpressionToReferenceNodeIdMap[this.isomorphicSolution.solution[referenceNodeId]] =
               referenceNodeId;
         }
      }

      this.isomorphicExpression = new LogicalExpressionOchreImpl(this.referenceExpression,
            this.isomorphicSolution.solution);
      this.referenceVisitData.getNodeIdsForDepth(3).stream().forEach((nodeId) -> {
                                         referenceRelationshipNodesMap.put(
                                         new RelationshipKey(nodeId, this.referenceExpression), nodeId);
                                      });
      this.comparisonVisitData.getNodeIdsForDepth(3).stream().forEach((nodeId) -> {
                                          comparisonRelationshipNodesMap.put(
                                          new RelationshipKey(nodeId, this.comparisonExpression), nodeId);
                                       });
      computeAdditions();
      computeDeletions();

      int[] identityMap = new int[this.referenceExpression.getNodeCount()];

      for (int i = 0; i < identityMap.length; i++) {
         identityMap[i] = i;
      }

      this.mergedExpression = new LogicalExpressionOchreImpl(this.referenceExpression,
            identityMap,
            referenceExpressionToMergedNodeIdMap);

      // make a node mapping from comparison expression to the merged expression
      int[] comparisonToMergedMap = new int[comparisonExpression.getNodeCount()];

      Arrays.fill(comparisonToMergedMap, -1);

      for (int referenceNodeId = 0; referenceNodeId < this.isomorphicSolution.solution.length; referenceNodeId++) {
         if (this.isomorphicSolution.solution[referenceNodeId] >= 0) {
            comparisonToMergedMap[this.isomorphicSolution.solution[referenceNodeId]] = referenceNodeId;
         }
      }

      // Add the deletions
      getDeletedRelationshipRoots().forEach((deletionRoot) -> {
         // deleted relationships roots come from the comparison expression.
               int rootToAddParentSequence =
                  referenceExpressionToMergedNodeIdMap[this.comparisonExpressionToReferenceNodeIdMap[comparisonVisitData.getPredecessorSequence(deletionRoot.getNodeIndex())]];

               addFragment(deletionRoot, this.comparisonExpression, rootToAddParentSequence);
            });
   }

   //~--- methods -------------------------------------------------------------

   /**
    *
    * @param incomingPossibleSolutions the incoming set of solutions, to seed
    * the generation for this depth
    * @param possibleSolutionMap The set of possible logicNodes to consider for the
    * next depth of the tree.
    * @return A set of possible solutions
    *
    */
   public Set<IsomorphicSolution> generatePossibleSolutions(Set<IsomorphicSolution> incomingPossibleSolutions,
         Map<Integer, SortedSet<IsomorphicSearchBottomUpNode>> possibleSolutionMap) {
      Set<IsomorphicSolution> possibleSolutions = new HashSet<>();

      possibleSolutions.addAll(incomingPossibleSolutions);

      for (Map.Entry<Integer, SortedSet<IsomorphicSearchBottomUpNode>> entry: possibleSolutionMap.entrySet()) {
         possibleSolutions = generatePossibleSolutionsForNode(entry.getKey(), entry.getValue(), possibleSolutions);
      }

      if (possibleSolutions.isEmpty()) {
         return incomingPossibleSolutions;
      }

      HashMap<Integer, HashSet<IsomorphicSolution>> scoreSolutionMap = new HashMap<>();
      int                                           maxScore         = 0;

      for (IsomorphicSolution solution: possibleSolutions) {
         if (solution.getScore() >= maxScore) {
            maxScore = solution.getScore();

            HashSet<IsomorphicSolution> maxScoreSet = scoreSolutionMap.get(maxScore);

            if (maxScoreSet == null) {
               maxScoreSet = new HashSet<>();
               scoreSolutionMap.put(maxScore, maxScoreSet);
            }

            maxScoreSet.add(solution);
         }
      }

      return scoreSolutionMap.get(maxScore);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();

      builder.append("Isomorphic Analysis for:")
             .append(Get.conceptDescriptionText(referenceExpression.conceptSequence))
             .append("\n     ")
             .append(Get.identifierService()
                        .getUuidPrimordialFromConceptId(referenceExpression.conceptSequence))
             .append("\n\n");
      builder.append("Reference expression:\n\n ");
      builder.append(referenceExpression.toString("r"));
      builder.append("\nComparison expression:\n\n ");
      builder.append(comparisonExpression.toString("c"));

      if (isomorphicExpression != null) {
         builder.append("\nIsomorphic expression:\n\n ");
         builder.append(isomorphicExpression.toString("i"));
      }

      if (isomorphicSolution != null) {
         builder.append("\nIsomorphic solution: \n");

         String formatString = "[%2d";
         String nullString   = " ∅ ";

         if (isomorphicSolution.getSolution().length < 10) {
            formatString = "[%d";
            nullString   = " ∅ ";
         }

         if (isomorphicSolution.getSolution().length > 99) {
            formatString = "[%3d";
            nullString   = " ∅ ";
         }

         for (int i = 0; i < isomorphicSolution.getSolution().length; i++) {
            builder.append("  ");
            builder.append(String.format(formatString, i));
            builder.append("r] ➞ ");

            if (isomorphicSolution.getSolution()[i] == -1) {
               builder.append(nullString);
            } else {
               builder.append(String.format(formatString, isomorphicSolution.getSolution()[i]));
            }

            if (isomorphicSolution.getSolution()[i] < 0) {
               builder.append("\n");
            } else if (i != isomorphicSolution.getSolution()[i]) {
               builder.append("c]* ");
               builder.append(referenceExpression.getNode(i)
                                                 .toString("r"));
               builder.append("\n");
            } else {
               builder.append("c]  ");
               builder.append(referenceExpression.getNode(i)
                                                 .toString("r"));
               builder.append("\n");
            }
         }

         builder.append("\nAdditions: \n\n");
         getAdditionalNodeRoots().forEach((additionRoot) -> {
                                             builder.append("  ")
                                                   .append(additionRoot.fragmentToString("r"));
                                             builder.append("\n");
                                          });
         builder.append("\nDeletions: \n\n");
         getDeletedNodeRoots().forEach((deletionRoot) -> {
                                          builder.append("  ")
                                                .append(deletionRoot.fragmentToString("c"));
                                          builder.append("\n");
                                       });
         builder.append("\nShared relationship roots: \n\n");
         getSharedRelationshipRoots().forEach((sharedRelRoot) -> {
                  builder.append("  ")
                         .append(sharedRelRoot.fragmentToString());
                  builder.append("\n");
               });
         builder.append("\nNew relationship roots: \n\n");
         getAddedRelationshipRoots().forEach((addedRelRoot) -> {
                  builder.append("  ")
                         .append(addedRelRoot.fragmentToString());
                  builder.append("\n");
               });
         builder.append("\nDeleted relationship roots: \n\n");
         getDeletedRelationshipRoots().forEach((deletedRelRoot) -> {
                  builder.append("  ")
                         .append(deletedRelRoot.fragmentToString());
                  builder.append("\n");
               });
         builder.append("\nMerged expression: \n\n");

         if (mergedExpression != null) {
            builder.append(mergedExpression.toString("m"));
         } else {
            builder.append("null");
         }

         builder.append("\n");
      }

      return builder.toString();
   }

   private void addFragment(LogicNode rootToAdd,
                            LogicalExpressionOchreImpl originExpression,
                            int rootToAddParentSequence) {
      LogicNode[] descendents           = rootToAdd.getDescendents();
      int         mergedExpressionIndex = this.mergedExpression.getNodeCount();
      int[]       additionSolution      = new int[originExpression.getNodeCount()];

      Arrays.fill(additionSolution, -1);
      additionSolution[rootToAdd.getNodeIndex()] = mergedExpressionIndex++;

      for (LogicNode descendent: descendents) {
         additionSolution[descendent.getNodeIndex()] = mergedExpressionIndex++;
      }

      LogicNode[] addedNodes = this.mergedExpression.addNodes(originExpression,
                                                              additionSolution,
                                                              rootToAdd.getNodeIndex());

      // Need convert rootToAddParentSequence from originExpression nodeId to mergedExpression nodeId.
      this.mergedExpression.getNode(rootToAddParentSequence)
                           .addChildren(addedNodes[0]);

      // TODO make sure all children are added.
   }

   private void computeAdditions() {
      SequenceSet<?> nodesInSolution    = new SequenceSet<>();
      SequenceSet<?> nodesNotInSolution = new SequenceSet<>();

      for (int i = 0; i < isomorphicSolution.getSolution().length; i++) {
         if (isomorphicSolution.getSolution()[i] >= 0) {
            nodesInSolution.add(i);
         } else {
            nodesNotInSolution.add(i);
         }
      }

      nodesNotInSolution.stream().forEach((additionNode) -> {
                                    int additionRoot = additionNode;

                                    while (
                                       nodesNotInSolution.contains(
                                           referenceVisitData.getPredecessorSequence(additionRoot))) {
                                       additionRoot = referenceVisitData.getPredecessorSequence(additionRoot);
                                    }

                                    referenceAdditionRoots.add(additionRoot);
                                 });
   }

   private void computeDeletions() {
      SequenceSet<?> comparisonNodesInSolution = new SequenceSet<>();

      Arrays.stream(isomorphicSolution.getSolution()).forEach((nodeId) -> {
                        if (nodeId >= 0) {
                           comparisonNodesInSolution.add(nodeId);
                        }
                     });

      SequenceSet<?> comparisonNodesNotInSolution = new SequenceSet<>();

      IntStream.range(0, comparisonVisitData.getNodesVisited())
               .forEach((nodeId) -> {
                           if (!comparisonNodesInSolution.contains(nodeId)) {
                              comparisonNodesNotInSolution.add(nodeId);
                           }
                        });
      comparisonNodesNotInSolution.stream().forEach((deletedNode) -> {
               int deletedRoot = deletedNode;

               while (comparisonNodesNotInSolution.contains(comparisonVisitData.getPredecessorSequence(deletedRoot))) {
                  deletedRoot = comparisonVisitData.getPredecessorSequence(deletedRoot);
               }

               comparisonDeletionRoots.add(deletedRoot);
            });
   }

   private Set<IsomorphicSolution> generatePossibleSolutionsForNode(int solutionNodeId,
         Set<IsomorphicSearchBottomUpNode> incomingPossibleNodes,
         Set<IsomorphicSolution> possibleSolutions) {
      // Using a set to eliminate duplicate solutions.
      Set<IsomorphicSolution> outgoingPossibleNodes = new HashSet<>();

      possibleSolutions.forEach((incomingPossibleSolution) -> {
                                   incomingPossibleNodes.forEach((isomorphicSearchNode) -> {
                  if (comparisonExpression.getNode(isomorphicSearchNode.nodeId)
                                          .equals(referenceExpression.getNode(solutionNodeId))) {
                     int[] generatedPossibleSolution = new int[incomingPossibleSolution.getSolution().length];

                     System.arraycopy(incomingPossibleSolution.getSolution(),
                                      0,
                                      generatedPossibleSolution,
                                      0,
                                      generatedPossibleSolution.length);
                     generatedPossibleSolution[solutionNodeId] = isomorphicSearchNode.nodeId;

                     IsomorphicSolution isomorphicSolution = new IsomorphicSolution(generatedPossibleSolution,
                                                                                    referenceVisitData,
                                                                                    comparisonVisitData);

                     if (isomorphicSolution.legal) {
                        outgoingPossibleNodes.add(isomorphicSolution);
                     }
                  }
               });
                                });

      if (outgoingPossibleNodes.isEmpty()) {
         outgoingPossibleNodes.addAll(possibleSolutions);
      }

      return outgoingPossibleNodes;
   }

   // ? score based on number or leafs included, with higher score for smaller number of intermediate logicNodes.
   private IsomorphicSolution isomorphicAnalysis() {
      TreeSet<IsomorphicSearchBottomUpNode> comparisonSearchNodeSet = new TreeSet<>();

      for (int i = 0; i < comparisonVisitData.getNodesVisited(); i++) {
         LogicNode   logicNode = comparisonExpression.getNode(i);
         LogicNode[] children  = logicNode.getChildren();

         if (children.length == 0) {
            comparisonSearchNodeSet.add(new IsomorphicSearchBottomUpNode(logicNode.getNodeSemantic(),
                  comparisonVisitData.getConceptsReferencedAtNodeOrAbove(i),
                  -1,
                  i));
         } else {
            for (LogicNode child: children) {
               comparisonSearchNodeSet.add(new IsomorphicSearchBottomUpNode(logicNode.getNodeSemantic(),
                     comparisonVisitData.getConceptsReferencedAtNodeOrAbove(i),
                     child.getNodeIndex(),
                     i));
            }
         }
      }

      SequenceSet<?>          nodesProcessed    = new SequenceSet<>();
      Set<IsomorphicSolution> possibleSolutions = new HashSet<>();
      int[]                   seedSolution      = new int[referenceExpression.getNodeCount()];

      Arrays.fill(seedSolution, -1);
      seedSolution[referenceExpression.getRoot().getNodeIndex()] = comparisonExpression.getRoot()
            .getNodeIndex();
      nodesProcessed.add(referenceExpression.getRoot()
            .getNodeIndex());

      // Test for second level matches... Need to do so to make intermediate logicNodes (necessary set/sufficient set)
      // are included in the solution, even if there are no matching leaf logicNodes.
      referenceExpression.getRoot().getChildStream().forEach((referenceRootChild) -> {
                                     comparisonExpression.getRoot().getChildStream().forEach((comparisonRootChild) -> {
                                        // Necessary/sufficient set logicNodes.
                  if (referenceRootChild.equals(comparisonRootChild)) {
                     seedSolution[referenceRootChild.getNodeIndex()] = comparisonRootChild.getNodeIndex();
                     nodesProcessed.add(referenceRootChild.getNodeIndex());

                     // And node below Necessary/sufficient set logicNodes
                     referenceRootChild.getChildStream().forEach((referenceAndNode) -> {
                                                   assert referenceAndNode.getNodeSemantic() == NodeSemantic.AND:
                                                         "Expecting reference AND, Found node semantic instead: " +
                                                         referenceAndNode.getNodeSemantic();
                                                   comparisonRootChild.getChildStream().forEach((comparisonAndNode) -> {
                           assert comparisonAndNode.getNodeSemantic() == NodeSemantic.AND:
                                  "Expecting comparison AND, Found node semantic instead: " +
                                  comparisonAndNode.getNodeSemantic();
                           nodesProcessed.add(referenceAndNode.getNodeIndex());
                           seedSolution[referenceAndNode.getNodeIndex()] = comparisonAndNode.getNodeIndex();
                        });
                                                });
                  }
               });
                                  });
      possibleSolutions.add(new IsomorphicSolution(seedSolution, referenceVisitData, comparisonVisitData));

      Map<Integer, SortedSet<IsomorphicSearchBottomUpNode>> possibleMatches = new TreeMap<>();
      SequenceSet<?>                                        nodesToTry      = referenceVisitData.getLeafNodes();

      while (!nodesToTry.isEmpty()) {
         possibleMatches.clear();

         SequenceSet<?> nextSetToTry = new SequenceSet<>();

         nodesToTry.stream().forEach((referenceNodeId) -> {
                               int predecessorSequence = referenceVisitData.getPredecessorSequence(
                                                            referenceNodeId);  // only add if the node matches. ?

                               if (predecessorSequence >= 0) {
                                  if (!nodesProcessed.contains(predecessorSequence)) {
                                     nextSetToTry.add(referenceVisitData.getPredecessorSequence(referenceNodeId));
                                     nodesProcessed.add(predecessorSequence);
                                  }
                               }

                               LogicNode referenceLogicNode = referenceExpression.getNode(referenceNodeId);

                               if (referenceLogicNode.getChildren().length == 0) {
                                  IsomorphicSearchBottomUpNode from =
                                     new IsomorphicSearchBottomUpNode(referenceLogicNode.getNodeSemantic(),
                                                                      referenceVisitData.getConceptsReferencedAtNodeOrAbove(
                                                                         referenceNodeId),
                                                                      -1,
                                                                      Integer.MIN_VALUE);
                                  IsomorphicSearchBottomUpNode to =
                                     new IsomorphicSearchBottomUpNode(referenceLogicNode.getNodeSemantic(),
                                                                      referenceVisitData.getConceptsReferencedAtNodeOrAbove(
                                                                         referenceNodeId),
                                                                      -1,
                                                                      Integer.MAX_VALUE);
                                  SortedSet<IsomorphicSearchBottomUpNode> searchNodesForReferenceNode =
                                     comparisonSearchNodeSet.subSet(from,
                                                                    to);

                                  if (!searchNodesForReferenceNode.isEmpty()) {
                                     if (!possibleMatches.containsKey(referenceNodeId)) {
                                        possibleMatches.put(referenceNodeId, new TreeSet<>());
                                     }

                                     possibleMatches.get(referenceNodeId)
                                                    .addAll(searchNodesForReferenceNode);
                                  }
                               } else {
                                  for (LogicNode child: referenceLogicNode.getChildren()) {
                                     possibleSolutions.stream().map((possibleSolution) -> {
                                                   IsomorphicSearchBottomUpNode from =
                                                      new IsomorphicSearchBottomUpNode(
                                                         referenceLogicNode.getNodeSemantic(),
                                                               referenceVisitData.getConceptsReferencedAtNodeOrAbove(
                                                                  referenceNodeId),
                                                               possibleSolution.getSolution()[child.getNodeIndex()],
                                                               Integer.MIN_VALUE);
                                                   IsomorphicSearchBottomUpNode to =
                                                      new IsomorphicSearchBottomUpNode(
                                                         referenceLogicNode.getNodeSemantic(),
                                                               referenceVisitData.getConceptsReferencedAtNodeOrAbove(
                                                                  referenceNodeId),
                                                               possibleSolution.getSolution()[child.getNodeIndex()],
                                                               Integer.MAX_VALUE);
                                                   SortedSet<IsomorphicSearchBottomUpNode> searchNodesForReferenceNode =
                                                      comparisonSearchNodeSet.subSet(from,
                                                                                     to);

                                                   return searchNodesForReferenceNode;
                                                }).filter((searchNodesForReferenceNode) -> (!searchNodesForReferenceNode.isEmpty())).forEach((searchNodesForReferenceNode) -> {
                                                       if (!possibleMatches.containsKey(referenceNodeId)) {
                                                          possibleMatches.put(referenceNodeId, new TreeSet<>());
                                                       }

                                                       possibleMatches.get(referenceNodeId)
                                                             .addAll(searchNodesForReferenceNode);
                                                    });
                                  }
                               }

                               nodesProcessed.add(referenceNodeId);
                            });

         // Introducing tempPossibleSolutions secondary to limitation with lambdas, requiring a final object...
         Set<IsomorphicSolution> tempPossibleSolutions = new HashSet<>();

         tempPossibleSolutions.addAll(possibleSolutions);
         possibleSolutions.clear();
         possibleSolutions.addAll(generatePossibleSolutions(tempPossibleSolutions, possibleMatches));
         nodesToTry = nextSetToTry;
      }

      return possibleSolutions.stream()
                              .max((IsomorphicSolution o1,
                                    IsomorphicSolution o2) -> Integer.compare(o1.getScore(), o2.getScore()))
                              .get();
   }

   /**
    * Scoring algorithm to determine if it is possible that a
    * isomorphicSolution based on the possibleSolution may score >= the current
    * maximum isomorphicSolution. Used to trim the search space of unnecessary
    * permutations.
    */
   private int scoreSolution(int[] solution) {
      int score = 0;

      for (int solutionArrayValue: solution) {
         if (solutionArrayValue >= 0) {
            score++;
         }
      }

      return score;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public final Stream<LogicNode> getAddedRelationshipRoots() {
      TreeSet<RelationshipKey> addedRelationshipRoots = new TreeSet<>(referenceRelationshipNodesMap.keySet());

      addedRelationshipRoots.removeAll(comparisonRelationshipNodesMap.keySet());
      return addedRelationshipRoots.stream()
                                   .map((
                                   RelationshipKey key) -> referenceExpression.getNode(
                                       referenceRelationshipNodesMap.get(key)));
   }

   @Override
   public Stream<LogicNode> getAdditionalNodeRoots() {
      return referenceAdditionRoots.stream()
                                   .mapToObj((nodeId) -> referenceExpression.getNode(nodeId));
   }

   @Override
   public LogicalExpressionOchreImpl getComparisonExpression() {
      return comparisonExpression;
   }

   @Override
   public Stream<LogicNode> getDeletedNodeRoots() {
      return comparisonDeletionRoots.stream()
                                    .mapToObj((nodeId) -> comparisonExpression.getNode(nodeId));
   }

   @Override
   public final Stream<LogicNode> getDeletedRelationshipRoots() {
      TreeSet<RelationshipKey> deletedRelationshipRoots = new TreeSet<>(comparisonRelationshipNodesMap.keySet());

      deletedRelationshipRoots.removeAll(referenceRelationshipNodesMap.keySet());
      return deletedRelationshipRoots.stream()
                                     .map((
                                     RelationshipKey key) -> comparisonExpression.getNode(
                                         comparisonRelationshipNodesMap.get(key)));
   }

   @Override
   public LogicalExpression getIsomorphicExpression() {
      return isomorphicExpression;
   }

   @Override
   public LogicalExpression getMergedExpression() {
      return this.mergedExpression;
   }

   @Override
   public LogicalExpressionOchreImpl getReferenceExpression() {
      return referenceExpression;
   }

   @Override
   public Stream<LogicNode> getSharedRelationshipRoots() {
      TreeSet<RelationshipKey> sharedRelationshipRoots = new TreeSet<>(referenceRelationshipNodesMap.keySet());

      sharedRelationshipRoots.retainAll(comparisonRelationshipNodesMap.keySet());
      return sharedRelationshipRoots.stream()
                                    .map((
                                    RelationshipKey key) -> referenceExpression.getNode(
                                        referenceRelationshipNodesMap.get(key)));
   }
}

