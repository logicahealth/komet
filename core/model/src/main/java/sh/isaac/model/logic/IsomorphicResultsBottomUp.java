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
      this.referenceExpression.depthFirstVisit(null, this.referenceExpression.getRoot(), this.referenceVisitData, 0);
      this.comparisonVisitData = new TreeNodeVisitData(comparisonExpression.getNodeCount());
      this.comparisonExpression.depthFirstVisit(null, comparisonExpression.getRoot(), this.comparisonVisitData, 0);
      this.referenceExpressionToMergedNodeIdMap = new int[referenceExpression.getNodeCount()];
      Arrays.fill(this.referenceExpressionToMergedNodeIdMap, -1);
      this.comparisonExpressionToReferenceNodeIdMap = new int[comparisonExpression.getNodeCount()];
      Arrays.fill(this.comparisonExpressionToReferenceNodeIdMap, -1);
      this.isomorphicSolution = isomorphicAnalysis();

      for (int referenceNodeId = 0; referenceNodeId < this.isomorphicSolution.solution.length; referenceNodeId++) {
         if (this.isomorphicSolution.solution[referenceNodeId] > -1) {
            this.comparisonExpressionToReferenceNodeIdMap[this.isomorphicSolution.solution[referenceNodeId]] =
               referenceNodeId;
         }
      }

      this.isomorphicExpression = new LogicalExpressionOchreImpl(this.referenceExpression,
            this.isomorphicSolution.solution);
      this.referenceVisitData.getNodeIdsForDepth(3).stream().forEach((nodeId) -> {
                                         this.referenceRelationshipNodesMap.put(
                                         new RelationshipKey(nodeId, this.referenceExpression), nodeId);
                                      });
      this.comparisonVisitData.getNodeIdsForDepth(3).stream().forEach((nodeId) -> {
                                          this.comparisonRelationshipNodesMap.put(
                                          new RelationshipKey(nodeId, this.comparisonExpression), nodeId);
                                       });
      computeAdditions();
      computeDeletions();

      final int[] identityMap = new int[this.referenceExpression.getNodeCount()];

      for (int i = 0; i < identityMap.length; i++) {
         identityMap[i] = i;
      }

      this.mergedExpression = new LogicalExpressionOchreImpl(this.referenceExpression,
            identityMap,
            this.referenceExpressionToMergedNodeIdMap);

      // make a node mapping from comparison expression to the merged expression
      final int[] comparisonToMergedMap = new int[comparisonExpression.getNodeCount()];

      Arrays.fill(comparisonToMergedMap, -1);

      for (int referenceNodeId = 0; referenceNodeId < this.isomorphicSolution.solution.length; referenceNodeId++) {
         if (this.isomorphicSolution.solution[referenceNodeId] >= 0) {
            comparisonToMergedMap[this.isomorphicSolution.solution[referenceNodeId]] = referenceNodeId;
         }
      }

      // Add the deletions
      getDeletedRelationshipRoots().forEach((deletionRoot) -> {
         // deleted relationships roots come from the comparison expression.
               final int rootToAddParentSequence =
                  this.referenceExpressionToMergedNodeIdMap[this.comparisonExpressionToReferenceNodeIdMap[this.comparisonVisitData.getPredecessorSequence(deletionRoot.getNodeIndex())]];

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

      for (final Map.Entry<Integer, SortedSet<IsomorphicSearchBottomUpNode>> entry: possibleSolutionMap.entrySet()) {
         possibleSolutions = generatePossibleSolutionsForNode(entry.getKey(), entry.getValue(), possibleSolutions);
      }

      if (possibleSolutions.isEmpty()) {
         return incomingPossibleSolutions;
      }

      final HashMap<Integer, HashSet<IsomorphicSolution>> scoreSolutionMap = new HashMap<>();
      int                                           maxScore         = 0;

      for (final IsomorphicSolution solution: possibleSolutions) {
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
      final StringBuilder builder = new StringBuilder();

      builder.append("Isomorphic Analysis for:")
             .append(Get.conceptDescriptionText(this.referenceExpression.conceptSequence))
             .append("\n     ")
             .append(Get.identifierService()
                        .getUuidPrimordialFromConceptId(this.referenceExpression.conceptSequence))
             .append("\n\n");
      builder.append("Reference expression:\n\n ");
      builder.append(this.referenceExpression.toString("r"));
      builder.append("\nComparison expression:\n\n ");
      builder.append(this.comparisonExpression.toString("c"));

      if (this.isomorphicExpression != null) {
         builder.append("\nIsomorphic expression:\n\n ");
         builder.append(this.isomorphicExpression.toString("i"));
      }

      if (this.isomorphicSolution != null) {
         builder.append("\nIsomorphic solution: \n");

         String formatString = "[%2d";
         String nullString   = " ∅ ";

         if (this.isomorphicSolution.getSolution().length < 10) {
            formatString = "[%d";
            nullString   = " ∅ ";
         }

         if (this.isomorphicSolution.getSolution().length > 99) {
            formatString = "[%3d";
            nullString   = " ∅ ";
         }

         for (int i = 0; i < this.isomorphicSolution.getSolution().length; i++) {
            builder.append("  ");
            builder.append(String.format(formatString, i));
            builder.append("r] ➞ ");

            if (this.isomorphicSolution.getSolution()[i] == -1) {
               builder.append(nullString);
            } else {
               builder.append(String.format(formatString, this.isomorphicSolution.getSolution()[i]));
            }

            if (this.isomorphicSolution.getSolution()[i] < 0) {
               builder.append("\n");
            } else if (i != this.isomorphicSolution.getSolution()[i]) {
               builder.append("c]* ");
               builder.append(this.referenceExpression.getNode(i)
                                                 .toString("r"));
               builder.append("\n");
            } else {
               builder.append("c]  ");
               builder.append(this.referenceExpression.getNode(i)
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

         if (this.mergedExpression != null) {
            builder.append(this.mergedExpression.toString("m"));
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
      final LogicNode[] descendents           = rootToAdd.getDescendents();
      int         mergedExpressionIndex = this.mergedExpression.getNodeCount();
      final int[]       additionSolution      = new int[originExpression.getNodeCount()];

      Arrays.fill(additionSolution, -1);
      additionSolution[rootToAdd.getNodeIndex()] = mergedExpressionIndex++;

      for (final LogicNode descendent: descendents) {
         additionSolution[descendent.getNodeIndex()] = mergedExpressionIndex++;
      }

      final LogicNode[] addedNodes = this.mergedExpression.addNodes(originExpression,
                                                              additionSolution,
                                                              rootToAdd.getNodeIndex());

      // Need convert rootToAddParentSequence from originExpression nodeId to mergedExpression nodeId.
      this.mergedExpression.getNode(rootToAddParentSequence)
                           .addChildren(addedNodes[0]);

      // TODO make sure all children are added.
   }

   private void computeAdditions() {
      final SequenceSet<?> nodesInSolution    = new SequenceSet<>();
      final SequenceSet<?> nodesNotInSolution = new SequenceSet<>();

      for (int i = 0; i < this.isomorphicSolution.getSolution().length; i++) {
         if (this.isomorphicSolution.getSolution()[i] >= 0) {
            nodesInSolution.add(i);
         } else {
            nodesNotInSolution.add(i);
         }
      }

      nodesNotInSolution.stream().forEach((additionNode) -> {
                                    int additionRoot = additionNode;

                                    while (
                                       nodesNotInSolution.contains(
                                           this.referenceVisitData.getPredecessorSequence(additionRoot))) {
                                       additionRoot = this.referenceVisitData.getPredecessorSequence(additionRoot);
                                    }

                                    this.referenceAdditionRoots.add(additionRoot);
                                 });
   }

   private void computeDeletions() {
      final SequenceSet<?> comparisonNodesInSolution = new SequenceSet<>();

      Arrays.stream(this.isomorphicSolution.getSolution()).forEach((nodeId) -> {
                        if (nodeId >= 0) {
                           comparisonNodesInSolution.add(nodeId);
                        }
                     });

      final SequenceSet<?> comparisonNodesNotInSolution = new SequenceSet<>();

      IntStream.range(0, this.comparisonVisitData.getNodesVisited())
               .forEach((nodeId) -> {
                           if (!comparisonNodesInSolution.contains(nodeId)) {
                              comparisonNodesNotInSolution.add(nodeId);
                           }
                        });
      comparisonNodesNotInSolution.stream().forEach((deletedNode) -> {
               int deletedRoot = deletedNode;

               while (comparisonNodesNotInSolution.contains(this.comparisonVisitData.getPredecessorSequence(deletedRoot))) {
                  deletedRoot = this.comparisonVisitData.getPredecessorSequence(deletedRoot);
               }

               this.comparisonDeletionRoots.add(deletedRoot);
            });
   }

   private Set<IsomorphicSolution> generatePossibleSolutionsForNode(int solutionNodeId,
         Set<IsomorphicSearchBottomUpNode> incomingPossibleNodes,
         Set<IsomorphicSolution> possibleSolutions) {
      // Using a set to eliminate duplicate solutions.
      final Set<IsomorphicSolution> outgoingPossibleNodes = new HashSet<>();

      possibleSolutions.forEach((incomingPossibleSolution) -> {
                                   incomingPossibleNodes.forEach((isomorphicSearchNode) -> {
                  if (this.comparisonExpression.getNode(isomorphicSearchNode.nodeId)
                                          .equals(this.referenceExpression.getNode(solutionNodeId))) {
                     final int[] generatedPossibleSolution = new int[incomingPossibleSolution.getSolution().length];

                     System.arraycopy(incomingPossibleSolution.getSolution(),
                                      0,
                                      generatedPossibleSolution,
                                      0,
                                      generatedPossibleSolution.length);
                     generatedPossibleSolution[solutionNodeId] = isomorphicSearchNode.nodeId;

                     final IsomorphicSolution isomorphicSolution = new IsomorphicSolution(generatedPossibleSolution,
                                                                                    this.referenceVisitData,
                                                                                    this.comparisonVisitData);

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
      final TreeSet<IsomorphicSearchBottomUpNode> comparisonSearchNodeSet = new TreeSet<>();

      for (int i = 0; i < this.comparisonVisitData.getNodesVisited(); i++) {
         final LogicNode   logicNode = this.comparisonExpression.getNode(i);
         final LogicNode[] children  = logicNode.getChildren();

         if (children.length == 0) {
            comparisonSearchNodeSet.add(new IsomorphicSearchBottomUpNode(logicNode.getNodeSemantic(),
                  this.comparisonVisitData.getConceptsReferencedAtNodeOrAbove(i),
                  -1,
                  i));
         } else {
            for (final LogicNode child: children) {
               comparisonSearchNodeSet.add(new IsomorphicSearchBottomUpNode(logicNode.getNodeSemantic(),
                     this.comparisonVisitData.getConceptsReferencedAtNodeOrAbove(i),
                     child.getNodeIndex(),
                     i));
            }
         }
      }

      final SequenceSet<?>          nodesProcessed    = new SequenceSet<>();
      final Set<IsomorphicSolution> possibleSolutions = new HashSet<>();
      final int[]                   seedSolution      = new int[this.referenceExpression.getNodeCount()];

      Arrays.fill(seedSolution, -1);
      seedSolution[this.referenceExpression.getRoot().getNodeIndex()] = this.comparisonExpression.getRoot()
            .getNodeIndex();
      nodesProcessed.add(this.referenceExpression.getRoot()
            .getNodeIndex());

      // Test for second level matches... Need to do so to make intermediate logicNodes (necessary set/sufficient set)
      // are included in the solution, even if there are no matching leaf logicNodes.
      this.referenceExpression.getRoot().getChildStream().forEach((referenceRootChild) -> {
                                     this.comparisonExpression.getRoot().getChildStream().forEach((comparisonRootChild) -> {
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
      possibleSolutions.add(new IsomorphicSolution(seedSolution, this.referenceVisitData, this.comparisonVisitData));

      final Map<Integer, SortedSet<IsomorphicSearchBottomUpNode>> possibleMatches = new TreeMap<>();
      SequenceSet<?>                                        nodesToTry      = this.referenceVisitData.getLeafNodes();

      while (!nodesToTry.isEmpty()) {
         possibleMatches.clear();

         final SequenceSet<?> nextSetToTry = new SequenceSet<>();

         nodesToTry.stream().forEach((referenceNodeId) -> {
                               final int predecessorSequence = this.referenceVisitData.getPredecessorSequence(
                                                            referenceNodeId);  // only add if the node matches. ?

                               if (predecessorSequence >= 0) {
                                  if (!nodesProcessed.contains(predecessorSequence)) {
                                     nextSetToTry.add(this.referenceVisitData.getPredecessorSequence(referenceNodeId));
                                     nodesProcessed.add(predecessorSequence);
                                  }
                               }

                               final LogicNode referenceLogicNode = this.referenceExpression.getNode(referenceNodeId);

                               if (referenceLogicNode.getChildren().length == 0) {
                                  final IsomorphicSearchBottomUpNode from =
                                     new IsomorphicSearchBottomUpNode(referenceLogicNode.getNodeSemantic(),
                                                                      this.referenceVisitData.getConceptsReferencedAtNodeOrAbove(
                                                                         referenceNodeId),
                                                                      -1,
                                                                      Integer.MIN_VALUE);
                                  final IsomorphicSearchBottomUpNode to =
                                     new IsomorphicSearchBottomUpNode(referenceLogicNode.getNodeSemantic(),
                                                                      this.referenceVisitData.getConceptsReferencedAtNodeOrAbove(
                                                                         referenceNodeId),
                                                                      -1,
                                                                      Integer.MAX_VALUE);
                                  final SortedSet<IsomorphicSearchBottomUpNode> searchNodesForReferenceNode =
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
                                  for (final LogicNode child: referenceLogicNode.getChildren()) {
                                     possibleSolutions.stream().map((possibleSolution) -> {
                                                   final IsomorphicSearchBottomUpNode from =
                                                      new IsomorphicSearchBottomUpNode(
                                                         referenceLogicNode.getNodeSemantic(),
                                                               this.referenceVisitData.getConceptsReferencedAtNodeOrAbove(
                                                                  referenceNodeId),
                                                               possibleSolution.getSolution()[child.getNodeIndex()],
                                                               Integer.MIN_VALUE);
                                                   final IsomorphicSearchBottomUpNode to =
                                                      new IsomorphicSearchBottomUpNode(
                                                         referenceLogicNode.getNodeSemantic(),
                                                               this.referenceVisitData.getConceptsReferencedAtNodeOrAbove(
                                                                  referenceNodeId),
                                                               possibleSolution.getSolution()[child.getNodeIndex()],
                                                               Integer.MAX_VALUE);
                                                   final SortedSet<IsomorphicSearchBottomUpNode> searchNodesForReferenceNode =
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
         final Set<IsomorphicSolution> tempPossibleSolutions = new HashSet<>();

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

      for (final int solutionArrayValue: solution) {
         if (solutionArrayValue >= 0) {
            score++;
         }
      }

      return score;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public final Stream<LogicNode> getAddedRelationshipRoots() {
      final TreeSet<RelationshipKey> addedRelationshipRoots = new TreeSet<>(this.referenceRelationshipNodesMap.keySet());

      addedRelationshipRoots.removeAll(this.comparisonRelationshipNodesMap.keySet());
      return addedRelationshipRoots.stream()
                                   .map((
                                   RelationshipKey key) -> this.referenceExpression.getNode(
                                       this.referenceRelationshipNodesMap.get(key)));
   }

   @Override
   public Stream<LogicNode> getAdditionalNodeRoots() {
      return this.referenceAdditionRoots.stream()
                                   .mapToObj((nodeId) -> this.referenceExpression.getNode(nodeId));
   }

   @Override
   public LogicalExpressionOchreImpl getComparisonExpression() {
      return this.comparisonExpression;
   }

   @Override
   public Stream<LogicNode> getDeletedNodeRoots() {
      return this.comparisonDeletionRoots.stream()
                                    .mapToObj((nodeId) -> this.comparisonExpression.getNode(nodeId));
   }

   @Override
   public final Stream<LogicNode> getDeletedRelationshipRoots() {
      final TreeSet<RelationshipKey> deletedRelationshipRoots = new TreeSet<>(this.comparisonRelationshipNodesMap.keySet());

      deletedRelationshipRoots.removeAll(this.referenceRelationshipNodesMap.keySet());
      return deletedRelationshipRoots.stream()
                                     .map((
                                     RelationshipKey key) -> this.comparisonExpression.getNode(
                                         this.comparisonRelationshipNodesMap.get(key)));
   }

   @Override
   public LogicalExpression getIsomorphicExpression() {
      return this.isomorphicExpression;
   }

   @Override
   public LogicalExpression getMergedExpression() {
      return this.mergedExpression;
   }

   @Override
   public LogicalExpressionOchreImpl getReferenceExpression() {
      return this.referenceExpression;
   }

   @Override
   public Stream<LogicNode> getSharedRelationshipRoots() {
      final TreeSet<RelationshipKey> sharedRelationshipRoots = new TreeSet<>(this.referenceRelationshipNodesMap.keySet());

      sharedRelationshipRoots.retainAll(this.comparisonRelationshipNodesMap.keySet());
      return sharedRelationshipRoots.stream()
                                    .map((
                                    RelationshipKey key) -> this.referenceExpression.getNode(
                                        this.referenceRelationshipNodesMap.get(key)));
   }
}

