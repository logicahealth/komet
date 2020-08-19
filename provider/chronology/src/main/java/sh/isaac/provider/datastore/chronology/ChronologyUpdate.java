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
package sh.isaac.provider.datastore.chronology;

import java.util.List;
import java.util.OptionalInt;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.dag.Node;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.model.logic.IsomorphicResultsFromPathHash;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.model.taxonomy.TaxonomyFlag;
import sh.isaac.model.taxonomy.TaxonomyRecord;
import sh.isaac.model.taxonomy.TaxonomyRecordPrimitive;
import sh.isaac.provider.datastore.taxonomy.TaxonomyProvider;

/**
 *
 * @author kec
 *
 */
@Service
@Singleton
public class ChronologyUpdate implements StaticIsaacCache {

    private static final Logger LOG = LogManager.getLogger();
    private static int INFERRED_ASSEMBLAGE_NID;
    private static int ISA_NID;
    private static int CHILD_OF_NID;
    private static int ROLE_GROUP_NID;
    private static IdentifierService IDENTIFIER_SERVICE;
    private static TaxonomyProvider TAXONOMY_SERVICE;

    private static boolean verboseDebugEnabled = false;

    private ChronologyUpdate() {
        //For HK2 only
    }

    private static void initCheck() {
        if (TAXONOMY_SERVICE == null) {
            INFERRED_ASSEMBLAGE_NID = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid();
            CHILD_OF_NID = TermAux.CHILD_OF.getNid();
            ISA_NID = TermAux.IS_A.getNid();
            ROLE_GROUP_NID = TermAux.ROLE_GROUP.getNid();
            IDENTIFIER_SERVICE = Get.identifierService();
            TAXONOMY_SERVICE = Get.service(TaxonomyProvider.class);
            verboseDebugEnabled = Get.configurationService().isVerboseDebugEnabled();
        }
    }

    //~--- methods -------------------------------------------------------------
    public static void handleStatusUpdate(ConceptChronology conceptChronology) {
        initCheck();
        TaxonomyRecord taxonomyRecord = new TaxonomyRecord();

        for (int stampSequence : conceptChronology.getVersionStampSequences()) {
            taxonomyRecord.addStampRecord(
                    conceptChronology.getNid(),
                    conceptChronology.getNid(),
                    stampSequence,
                    TaxonomyFlag.CONCEPT_STATUS.bits);
        }

        int[] record = taxonomyRecord.pack();
        //TaxonomyRecord.validate(record);
        TAXONOMY_SERVICE.accumulateAndGetTaxonomyData(
                conceptChronology.getAssemblageNid(),
                conceptChronology.getNid(),
                record,
                ChronologyUpdate::merge);
    }

    public static void handleTaxonomyUpdate(SemanticChronology logicGraphChronology) {
        initCheck();
        int referencedComponentNid = logicGraphChronology.getReferencedComponentNid();

        OptionalInt optionalConceptAssemblageNid = IDENTIFIER_SERVICE.getAssemblageNid(referencedComponentNid);
        int conceptAssemblageNid;
        if (optionalConceptAssemblageNid.isPresent()) {
            conceptAssemblageNid = optionalConceptAssemblageNid.getAsInt();
        } else {
            // TODO, remove this hack and figure out why some components are missing their assemblage. 
            conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();
        }
        final List<Graph<LogicGraphVersion>> versionGraphList = logicGraphChronology.getVersionGraphList();

        TaxonomyFlag taxonomyFlags;

        if (logicGraphChronology.getAssemblageNid() == INFERRED_ASSEMBLAGE_NID) {
            taxonomyFlags = TaxonomyFlag.INFERRED;
        } else {
            taxonomyFlags = TaxonomyFlag.STATED;
        }


        TaxonomyRecord taxonomyRecordForConcept = new TaxonomyRecord();

        for (Graph<LogicGraphVersion> versionGraph : versionGraphList) {
            // this is the CPU hog...
            // TODO: this unnecessarily processes old versions. Should check for old version with
            // inferred/stated and stamp, and skip if it already exists. 
            processVersionNode(referencedComponentNid, versionGraph.getRoot(), taxonomyRecordForConcept, taxonomyFlags);
        }
        int[] start = taxonomyRecordForConcept.pack();
        //start = start.clone();
        //TaxonomyRecord.validate(start);
        //int[] begin = origin_DestinationTaxonomyRecord_Map.get(logicGraphChronology.getReferencedComponentNid());
        //begin = begin.clone();
        int[] result = TAXONOMY_SERVICE.accumulateAndGetTaxonomyData(
                conceptAssemblageNid,
                logicGraphChronology.getReferencedComponentNid(),
                start, ChronologyUpdate::merge);
        //result = result.clone();
        if (start.length > result.length) {
//            TaxonomyRecord taxonomyRecordResult = new TaxonomyRecord(result);
            LOG.error("Accumulate shrank");
//         origin_DestinationTaxonomyRecord_Map.put(logicGraphChronology.getReferencedComponentNid(), begin);
//         int[] result2 = origin_DestinationTaxonomyRecord_Map.accumulateAndGet(
//               logicGraphChronology.getReferencedComponentNid(),
//               start, ChronologyUpdate::merge);
//         if (Arrays.equals(result, result2)) {
//            LOG.error("Results are equal. ");
//         } else {
//            LOG.error("Results are not equal. ");
//         }

        } else if (result.length == start.length) {
            LOG.error("Did not grow processing taxonomy update for {}", logicGraphChronology);
        }
    }

    private static int[] merge(int[] existing, int[] update) {
        if (existing == null || existing.length == 0) {
            return update;
        }
        if (update == null || update.length == 0) {
            return existing;
        }
        int[] newMethod = TaxonomyRecordPrimitive.merge(existing, update);

        return newMethod;
    }

    /**
     * Process new logic graph.
     *
     * @param firstVersion the first version
     * @param parentTaxonomyRecord the parent taxonomy record
     * @param taxonomyFlags the taxonomy flags
     */
    private static void processNewLogicGraph(LogicGraphVersion firstVersion,
            TaxonomyRecord parentTaxonomyRecord,
            TaxonomyFlag taxonomyFlags) {
        if (firstVersion.getCommitState() == CommitStates.COMMITTED) {
            final LogicalExpression expression = firstVersion.getLogicalExpression();
            LogicNode[] children = expression.getRoot().getChildren();
            boolean necessaryOnly = false;
            if (children.length > 1) {
                for (LogicNode child : children) {
                    if (child.getNodeSemantic() == NodeSemantic.NECESSARY_SET) {
                        necessaryOnly = true;
                    }
                }
            }

            for (LogicNode necessaryOrSufficientSet : children) {
                // if there is more than one set, only process necessary set...
                if (!necessaryOnly || necessaryOrSufficientSet.getNodeSemantic() == NodeSemantic.NECESSARY_SET) {
                    for (LogicNode andOrOrLogicNode : necessaryOrSufficientSet.getChildren()) {
                        for (LogicNode aLogicNode : andOrOrLogicNode.getChildren()) {
                            processRelationshipRoot(firstVersion.getReferencedComponentNid(),
                                    aLogicNode,
                                    parentTaxonomyRecord,
                                    taxonomyFlags,
                                    firstVersion.getStampSequence(),
                                    expression);
                        }
                    }
                }
            }
        }
    }

    /**
     * Process relationship root.
     *
     * @param logicNode the logical logic node
     * @param taxonomyRecordForConcept the parent taxonomy record
     * @param taxonomyFlags the taxonomy flags
     * @param stampSequence the stamp sequence
     * @param logicalExpression the comparison expression
     */
    private static void processRelationshipRoot(int conceptNid, LogicNode logicNode,
            TaxonomyRecord taxonomyRecordForConcept,
            TaxonomyFlag taxonomyFlags,
            int stampSequence,
            LogicalExpression logicalExpression) {
        switch (logicNode.getNodeSemantic()) {
            case CONCEPT:
                
                updateIsaRel(conceptNid,
                        ((ConceptNodeWithNids) logicNode).getConceptNid(),
                        taxonomyRecordForConcept,
                        taxonomyFlags,
                        stampSequence);
                break;

            case ROLE_SOME:
                updateSomeRole((RoleNodeSomeWithNids) logicNode, taxonomyRecordForConcept, taxonomyFlags, stampSequence);
                break;

            case FEATURE:

                // Features do not have taxonomy implications...
                break;

            case PROPERTY_PATTERN_IMPLICATION:
                // PROPERTY_PATTERN_IMPLICATION do not have direct taxonomy implications...
                break;

            default:
                throw new UnsupportedOperationException("at Can't handle: " + logicNode.getNodeSemantic());
        }
    }

    static AtomicLong isomporphicTime = new AtomicLong(50);

    private static void processVersionNode(int conceptNid, Node<? extends LogicGraphVersion> node,
            TaxonomyRecord taxonomyRecordForConcept,
            TaxonomyFlag taxonomyFlags) {
        LogicGraphVersion logicGraphVersion = node.getData();
        if (node.getParent() == null) {
            processNewLogicGraph(logicGraphVersion, taxonomyRecordForConcept, taxonomyFlags);
        } else {
            try {
                final LogicalExpression comparisonExpression = node.getParent()
                        .getData()
                        .getLogicalExpression();
                final LogicalExpression referenceExpression = node.getData()
                        .getLogicalExpression();

                long startTime = System.currentTimeMillis();
                final IsomorphicResultsFromPathHash isomorphicResults = new IsomorphicResultsFromPathHash(
                        referenceExpression,
                        comparisonExpression);
                isomorphicResults.call();
                long elapsedTime = System.currentTimeMillis() - startTime;
                 if (verboseDebugEnabled && isomporphicTime.get() < elapsedTime) {
                    isomporphicTime.set(elapsedTime);
                    LOG.info("\n\n\nNew isomorphic record: " + elapsedTime + "\n" + isomorphicResults + "\n\n");

                    LOG.info("Reference expression for:  " + elapsedTime + "\n" + referenceExpression.toBuilder() + "\n\n");
                    LOG.info("Comparison expression for:  " + elapsedTime + "\n" + comparisonExpression.toBuilder() + "\n\n");
                }
                processLogicalExpression(conceptNid, node,
                        taxonomyRecordForConcept,
                        taxonomyFlags, referenceExpression, isomorphicResults);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        }
        for (Node<? extends LogicGraphVersion> childNode : node.getChildren()) {
            processVersionNode(conceptNid, childNode, taxonomyRecordForConcept, taxonomyFlags);
        }
    }

    /**
     * Process version node.
     *
     * @param node the node
     * @param taxonomyRecordForConcept the parent taxonomy record
     * @param taxonomyFlags the taxonomy flags
     */
    private static void processLogicalExpression(int conceptNid, Node<? extends LogicGraphVersion> node,
            TaxonomyRecord taxonomyRecordForConcept,
            TaxonomyFlag taxonomyFlags,
            LogicalExpression logicalExpression, IsomorphicResults isomorphicResults) {
                
        LogicNode[] children = node.getData().getLogicalExpression().getRoot().getChildren();
        boolean necessaryOnly = false;
        if (children.length > 1) {
            for (LogicNode child : children) {
                if (child.getNodeSemantic() == NodeSemantic.NECESSARY_SET) {
                    necessaryOnly = true;
                }
            }
        }

        TreeSet<LogicNode> addedRoots = new TreeSet<>();
        for (LogicNode relationshipRoot : isomorphicResults.getAddedRelationshipRoots()) {
            if (necessaryOnly && relationshipRoot.hasParentSemantic(NodeSemantic.SUFFICIENT_SET)) {
                continue;
            }
            addedRoots.add(relationshipRoot);
            final int stampSequence = node.getData()
                    .getStampSequence();

            processRelationshipRoot(conceptNid,
                    relationshipRoot,
                    taxonomyRecordForConcept,
                    taxonomyFlags,
                    stampSequence,
                    logicalExpression);
        }

        TreeSet<LogicNode> retainedRoots = new TreeSet<>();
        for (LogicNode relationshipRoot :isomorphicResults.getSharedRelationshipRoots()) {
            retainedRoots.add(relationshipRoot);
        }

        for (LogicNode relationshipRoot : isomorphicResults.getDeletedRelationshipRoots()) {
            if (addedRoots.contains(relationshipRoot) || retainedRoots.contains(relationshipRoot)) {
                continue;
            }

            final int activeStampSequence = node.getData()
                    .getStampSequence();
            final int stampSequence = Get.stampService()
                    .getRetiredStampSequence(activeStampSequence);

            processRelationshipRoot(conceptNid,
                    relationshipRoot,
                    taxonomyRecordForConcept,
                    taxonomyFlags,
                    stampSequence,
                    logicalExpression);
        }

    }


    /**
     * Update isa rel.
     *

     * @param originNid the origin nid
     * @param destinationNid the destination nid
     * @param taxonomyRecordForConcept the parent taxonomy record
     * @param taxonomyFlags the taxonomy flags
     * @param stampSequence the stamp sequence
     */
    private static void updateIsaRel(int originNid,
            int destinationNid,
            TaxonomyRecord taxonomyRecordForConcept,
            TaxonomyFlag taxonomyFlags,
            int stampSequence) {
        taxonomyRecordForConcept.addStampRecord(destinationNid, ISA_NID, stampSequence, taxonomyFlags.bits);

        TaxonomyRecord destinationTaxonomyRecord = new TaxonomyRecord();
        destinationTaxonomyRecord.addStampRecord(originNid, CHILD_OF_NID, stampSequence, taxonomyFlags.bits);

        OptionalInt optionalConceptAssemblageNid = IDENTIFIER_SERVICE.getAssemblageNid(originNid);
        int conceptAssemblageNid;
        if (optionalConceptAssemblageNid.isPresent()) {
            conceptAssemblageNid = optionalConceptAssemblageNid.getAsInt();
        } else {
            // TODO, remove this hack and figure out why some components are missing their assemblage. 
            conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();
        }
        int[] record = destinationTaxonomyRecord.pack();
        //TaxonomyRecord.validate(record);
        TAXONOMY_SERVICE.accumulateAndGetTaxonomyData(
                conceptAssemblageNid,
                destinationNid,
                record,
                ChronologyUpdate::merge);
    }

    /**
     * Update some role.
     *
     * @param someNode the some node
     * @param parentTaxonomyRecord the parent taxonomy record
     * @param taxonomyFlags the taxonomy flags
     * @param stampSequence the stamp sequence
     */
    private static void updateSomeRole(RoleNodeSomeWithNids someNode,
            TaxonomyRecord parentTaxonomyRecord,
            TaxonomyFlag taxonomyFlags,
            int stampSequence) {
        if (someNode.getTypeConceptNid() == ROLE_GROUP_NID) {
            final AndNode andNode = (AndNode) someNode.getOnlyChild();

            andNode.getChildStream()
                    .forEach(
                            (roleGroupSomeNode) -> {
                                if (roleGroupSomeNode instanceof RoleNodeSomeWithNids) {
                                    updateSomeRole(
                                            (RoleNodeSomeWithNids) roleGroupSomeNode,
                                            parentTaxonomyRecord,
                                            taxonomyFlags,
                                            stampSequence);
                                } else {
                                    // TODO [KEC] Dan put this here to stop a pile of errors....
                                    // one of the types coming back was a FeatureNodeWithSequences - not sure what to do with it.
                                }
                            });
        } else {
            if (someNode.getOnlyChild() instanceof ConceptNodeWithNids) {
                final ConceptNodeWithNids restrictionNode = (ConceptNodeWithNids) someNode.getOnlyChild();

                parentTaxonomyRecord.addStampRecord(
                        restrictionNode.getConceptNid(),
                        someNode.getTypeConceptNid(),
                        stampSequence,
                        taxonomyFlags.bits);
            } else {
                // TODO [KEC] dan put this here to stop a pile of errors. It was returning AndNode.  Not sure what to do with it
            }
        }
    }

    @Override
    public void reset() {
        LOG.info("Clearing ChronologyUpdate cache");
        INFERRED_ASSEMBLAGE_NID = 0;
        CHILD_OF_NID = 0;
        ISA_NID = 0;
        ROLE_GROUP_NID = 0;
        IDENTIFIER_SERVICE = null;
        TAXONOMY_SERVICE = null;
    }
}
