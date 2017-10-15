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



package sh.isaac.provider.workflow.crud;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.UUID;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.provider.workflow.BPMNInfo;
import sh.isaac.provider.workflow.WorkflowProvider;
import sh.isaac.provider.workflow.model.WorkflowContentStore;
import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.ProcessDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.EndWorkflowType;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.ProcessStatus;
import sh.isaac.provider.workflow.model.contents.ProcessHistory;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.MutableComponentNidVersion;
import sh.isaac.api.component.semantic.version.MutableDescriptionVersion;
import sh.isaac.api.component.semantic.version.MutableDynamicVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableLongVersion;
import sh.isaac.api.component.semantic.version.MutableStringVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.MutableSemanticVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;

//~--- classes ----------------------------------------------------------------

/**
 * Contains methods necessary to update existing workflow content after
 * initialization aside from launching or ending them.
 *
 * {@link WorkflowContentStore} {@link WorkflowProvider}
 * {@link BPMNInfo}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
@Service
@Singleton
public class WorkflowUpdater {
   /** The workflow provider. */
   private final WorkflowProvider workflowProvider;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new workflow updater.
    */

   // For HK2
   private WorkflowUpdater() {
      this.workflowProvider = LookupService.get()
            .getService(WorkflowProvider.class);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Attempts to add components associated with a commit to a process. Can
    * only be done if the process and component are in the process state as
    * defined by addComponentToWorkflow. Does so for all concepts and sememes
    * in the commit record as well as the commit record's stamp sequence .
    *
    * Called by the REST implement commit() methods.
    *
    * @param processId
    * The process to which a commit record is being added
    * @param commitRecord
    * The commit record being associated with the process
    *
    * @throws Exception
    * Thrown if process doesn't exist,
    */
   public void addCommitRecordToWorkflow(UUID processId, Optional<CommitRecord> commitRecord)
            throws Exception {
      if (commitRecord.isPresent()) {
         final ProcessDetail detail = this.workflowProvider.getProcessDetailStore()
                                                            .get(processId);
         final OfInt conceptItr = Get.identifierService()
                                     .getConceptNidsForConceptSequences(commitRecord.get()
                                           .getConceptsInCommit()
                                           .parallelStream())
                                     .iterator();
         final OfInt sememeItr = Get.identifierService()
                                    .getSemanticNidsForSemanticSequences(commitRecord.get()
                                          .getSemanticSequencesInCommit()
                                          .parallelStream())
                                    .iterator();

         while (conceptItr.hasNext()) {
            final int conNid = conceptItr.next();

            if (isModifiableComponentInProcess(detail, conNid)) {
               addComponentToWorkflow(detail, conNid, commitRecord.get());
            } else {
               // TODO: Prevention strategy for when component not
               // deemed "addable" to WF
               throw new Exception("Concept may not be added to Workflow: " + conNid);
            }
         }

         while (sememeItr.hasNext()) {
            final int semNid = sememeItr.next();

            if (isModifiableComponentInProcess(detail, semNid)) {
               addComponentToWorkflow(detail, semNid, commitRecord.get());
            } else {
               // TODO: Prevention strategy for when component not
               // deemed "addable" to WF
               throw new Exception("Sememe may not be added to Workflow: " + semNid);
            }
         }
      }
   }

   /**
    * Associates a component with a process. If the comoponent has already been associated, nothing to do. Otherwise, add the component and the
    * timestamp of the edit to know the last version prior to editing
    *
    * Note: Made public to enable unit testing
    *
    * @param process The process to which a component/stamp pair is being added
    * @param compNid The component being added
    * @param commitRecord the commit record
    */
   public void addComponentToWorkflow(ProcessDetail process, int compNid, CommitRecord commitRecord) {
      if (!process.getComponentToInitialEditMap()
                  .keySet()
                  .contains(compNid)) {
         final int   stampSeq       = commitRecord.getStampsInCommit()
                                                  .getIntIterator()
                                                  .next();
         final State status         = Get.stampService()
                                         .getStatusForStamp(stampSeq);
         final long  time           = Get.stampService()
                                         .getTimeForStamp(stampSeq);
         final int   author         = Get.stampService()
                                         .getAuthorSequenceForStamp(stampSeq);
         final int   module         = Get.stampService()
                                         .getModuleSequenceForStamp(stampSeq);
         final int   path           = Get.stampService()
                                         .getPathSequenceForStamp(stampSeq);
         final Stamp componentStamp = new Stamp(status, time, author, module, path);

         process.getComponentToInitialEditMap()
                .put(compNid, componentStamp);
         this.workflowProvider.getProcessDetailStore()
                               .put(process.getId(), process);
      }
   }

   /**
    * Advance an existing process with the specified action. In doing so, the
    * user must add an advancement comment.
    *
    * Used by filling in the information prompted for after selecting a
    * Transition Workflow action.
    *
    * @param processId The process being advanced.
    * @param userId The user advancing the process.
    * @param actionRequested The advancement action the user requested.
    * @param comment The comment added by the user in advancing the process.
    * @param editCoordinate the edit coordinate
    * @return True if the advancement attempt was successful
    * @throws Exception Thrown if the requested action was to launch or end a process
    * and while updating the process accordingly, an execption
    * occurred
    */
   public boolean advanceWorkflow(UUID processId,
                                  UUID userId,
                                  String actionRequested,
                                  String comment,
                                  EditCoordinate editCoordinate)
            throws Exception {
      // Get User Permissible actions
      final Set<AvailableAction> userPermissableActions = this.workflowProvider.getWorkflowAccessor()
                                                                                .getUserPermissibleActionsForProcess(
                                                                                   processId,
                                                                                         userId);

      // Advance Workflow
      for (final AvailableAction action: userPermissableActions) {
         if (action.getAction()
                   .equals(actionRequested)) {
            final ProcessDetail process = this.workflowProvider.getProcessDetailStore()
                                                                .get(processId);

            // Update Process Details for launch, cancel, or conclude
            if (this.workflowProvider.getBPMNInfo()
                                      .getEndWorkflowTypeMap()
                                      .get(EndWorkflowType.CANCELED)
                                      .contains(action)) {
               // Request to cancel workflow
               this.workflowProvider.getWorkflowProcessInitializerConcluder()
                                     .endWorkflowProcess(processId,
                                           action,
                                           userId,
                                           comment,
                                           EndWorkflowType.CANCELED,
                                           editCoordinate);
            } else if (process.getStatus()
                              .equals(ProcessStatus.DEFINED)) {
               for (final AvailableAction startAction: this.workflowProvider.getBPMNInfo()
                     .getDefinitionStartActionMap()
                     .get(process.getDefinitionId())) {
                  if (startAction.getOutcomeState()
                                 .equals(action.getInitialState())) {
                     // Advancing request is to launch workflow
                     this.workflowProvider.getWorkflowProcessInitializerConcluder()
                                           .launchProcess(processId);
                     break;
                  }
               }
            } else if (this.workflowProvider.getBPMNInfo()
                                             .getEndWorkflowTypeMap()
                                             .get(EndWorkflowType.CONCLUDED)
                                             .contains(action)) {
               // Conclude Request made
               this.workflowProvider.getWorkflowProcessInitializerConcluder()
                                     .endWorkflowProcess(processId,
                                           action,
                                           userId,
                                           comment,
                                           EndWorkflowType.CONCLUDED,
                                           null);
            } else {
               // Generic Advancement.  Must still update Detail Store to automate releasing of instance
               final ProcessDetail entry = this.workflowProvider.getProcessDetailStore()
                                                                 .get(processId);

               entry.setOwnerId(BPMNInfo.UNOWNED_PROCESS);
               this.workflowProvider.getProcessDetailStore()
                                     .put(processId, entry);
            }

            // Add to process history
            final ProcessHistory hx = this.workflowProvider.getWorkflowAccessor()
                                                            .getProcessHistory(processId)
                                                            .last();
            final ProcessHistory entry = new ProcessHistory(processId,
                                                            userId,
                                                            new Date().getTime(),
                                                            action.getInitialState(),
                                                            action.getAction(),
                                                            action.getOutcomeState(),
                                                            comment,
                                                            hx.getHistorySequence() + 1);

            this.workflowProvider.getProcessHistoryStore()
                                  .add(entry);
            return true;
         }
      }

      return false;
   }

   /**
    * Removes a component from a process where the component had been
    * previously saved and associated with. In doing so, reverts the component
    * to its original state prior to the saves associated with the component.
    * The revert is performed by adding new versions to ensure that the
    * component attributes are identical prior to any modification associated
    * with the process. Note that nothing prevents future edits to be performed
    * upon the component associated with the same process.
    *
    * Used when component is removed from the process's component details panel
    *
    * @param processId            The process from which the component is to be removed
    * @param compNid            The component whose changes are to be reverted and removed
    *            from the process
    * @param editCoordinate the edit coordinate
    * @throws Exception             Thrown if the component has been found to not be currently
    *             associated with the process
    */
   public void removeComponentFromWorkflow(UUID processId,
         int compNid,
         EditCoordinate editCoordinate)
            throws Exception {
      final ProcessDetail detail = this.workflowProvider.getProcessDetailStore()
                                                         .get(processId);

      if (isModifiableComponentInProcess(detail, compNid)) {
         if (!detail.getComponentToInitialEditMap()
                    .keySet()
                    .contains(compNid)) {
            throw new Exception("Component " + compNid + " is not already in Workflow");
         }

         revertChanges(Arrays.asList(compNid), processId, editCoordinate);
         detail.getComponentToInitialEditMap()
               .remove(compNid);
         this.workflowProvider.getProcessDetailStore()
                               .put(processId, detail);
      } else {
         throw new Exception("Components may not be removed from Workflow: " + compNid);
      }
   }

   /**
    * Revert changes.
    *
    * @param compNidSet the comp nid set
    * @param processId the process id
    * @param editCoordinate the edit coordinate
    * @throws Exception the exception
    */
   protected void revertChanges(Collection<Integer> compNidSet,
                                UUID processId,
                                EditCoordinate editCoordinate)
            throws Exception {
      if (editCoordinate != null) {
         for (final Integer compNid: compNidSet) {
            final StampedVersion version = this.workflowProvider.getWorkflowAccessor()
                                                                 .getVersionPriorToWorkflow(processId, compNid);

            // add new version identical to version associated with
            // actualStampSeq
            if (Get.identifierService()
                   .getChronologyTypeForNid(compNid) == ObjectChronologyType.CONCEPT) {
               final ConceptChronology conceptChron = Get.conceptService()
                                                            .getConcept(compNid);

               if (version != null) {
                  // conceptChron = ((ConceptVersion) version).getChronology();
                  conceptChron.createMutableVersion(((ConceptVersion) version).getState(), editCoordinate);
               } else {
                  conceptChron.createMutableVersion(State.INACTIVE, editCoordinate);
               }

               Get.commitService()
                  .addUncommitted(conceptChron);
               Get.commitService()
                  .commit(Get.configurationService().getDefaultEditCoordinate(), "Reverting concept to how it was prior to workflow");
            } else if (Get.identifierService()
                          .getChronologyTypeForNid(compNid) == ObjectChronologyType.SEMANTIC) {
               final SemanticChronology semChron = Get.assemblageService()
                                                       .getSemanticChronology(compNid);

               if (version != null) {
                  MutableSemanticVersion createdVersion = semChron.createMutableVersion(((SemanticVersion) version).getState(),
                                                                                                    editCoordinate);

                  createdVersion = (MutableSemanticVersion) populateData(createdVersion, (SemanticVersion) version);
               } else {
                  final List<SemanticVersion> list        = ((SemanticChronology) semChron).getVersionList();
                  final SemanticVersion       lastVersion = list.toArray(new SemanticVersion[list.size()])[list.size() - 1];
                  SemanticVersion createdVersion =
                     ((SemanticChronology) semChron).createMutableVersion(State.INACTIVE,
                                                                        editCoordinate);

                  createdVersion = populateData(createdVersion, lastVersion);
               }

               Get.commitService()
                  .addUncommitted(semChron)
                  .get();
               Get.commitService()
                  .commit(Get.configurationService().getDefaultEditCoordinate(), "Reverting sememe to how it was prior to workflow")
                  .get();
            }
         }
      }
   }

   /**
    * Populate data.
    *
    * @param newVer the new ver
    * @param originalVersion the original version
    * @return the sememe version
    * @throws Exception the exception
    */
   private SemanticVersion populateData(SemanticVersion newVer, SemanticVersion originalVersion)
            throws Exception {
      switch (newVer.getChronology()
                    .getVersionType()) {
      case MEMBER:
         return newVer;

      case COMPONENT_NID:
         ((MutableComponentNidVersion) newVer).setComponentNid(((ComponentNidVersion) originalVersion).getComponentNid());
         return newVer;

      case DESCRIPTION:
         ((MutableDescriptionVersion) newVer).setText(((DescriptionVersion) originalVersion).getText());
         ((MutableDescriptionVersion) newVer).setDescriptionTypeConceptSequence(((DescriptionVersion) originalVersion).getDescriptionTypeConceptSequence());
         ((MutableDescriptionVersion) newVer).setCaseSignificanceConceptSequence(((DescriptionVersion) originalVersion).getCaseSignificanceConceptSequence());
         ((MutableDescriptionVersion) newVer).setLanguageConceptSequence(((DescriptionVersion) originalVersion).getLanguageConceptSequence());
         return newVer;

      case DYNAMIC:
         ((MutableDynamicVersion) newVer).setData(((DynamicVersion) originalVersion).getData());
         return newVer;

      case LONG:
         ((MutableLongVersion) newVer).setLongValue(((LongVersion) originalVersion).getLongValue());
         return newVer;

      case STRING:
         ((MutableStringVersion) newVer).setString(((StringVersion) originalVersion).getString());
         return newVer;

      /*
       * RelationshipVersionAdaptorImpl origRelVer =
       * (RelationshipVersionAdaptorImpl) originalVersion;
       * RelationshipAdaptorChronicleKeyImpl key = new
       * RelationshipAdaptorChronicleKeyImpl(
       * origRelVer.getOriginSequence(),
       * origRelVer.getDestinationSequence(),
       * origRelVer.getTypeSequence(), origRelVer.getGroup(),
       * origRelVer.getPremiseType(), origRelVer.getNodeSequence());
       *
       * return new RelationshipVersionAdaptorImpl(key, inactiveStampSeq);
       */
      case LOGIC_GRAPH:
         ((MutableLogicGraphVersion) newVer).setGraphData(((LogicGraphVersion) originalVersion).getGraphData());
         return newVer;

      case UNKNOWN:
         throw new UnsupportedOperationException();
      }

      return null;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Identifies if process is in an edit state. May only be done if either the
    * component is not in any workflow or if it is already in this process's
    * workflow AND one of the following: a) Process status is DEFINED or b)
    * process status is LAUNCHED while its latestHistory's Outcome is an
    * Editing state.
    *
    * Used by addCommitRecordToWorkflow() and removeComponentFromWorfklow() to
    * ensure that the process is in a valid state to be performing such an
    * action
    *
    * @param process
    * The process being investigated
    * @param compNid
    * The component to be added/removed
    *
    * @return True if the component can be added or removed from the process
    *
    * @throws Exception
    * Thrown if process doesn't exist,
    */
   private boolean isModifiableComponentInProcess(ProcessDetail process, int compNid)
            throws Exception {
      if (process == null) {
         throw new Exception("Cannot examine modification capability as the process doesn't exist");
      }

      final UUID processId = process.getId();

      // Check if in Case A. If not, throw exception
      if (this.workflowProvider.getWorkflowAccessor().isComponentInActiveWorkflow(process.getDefinitionId(),
            compNid) &&
            !process.getComponentToInitialEditMap().keySet().contains(compNid)) {
         // Can't do so because component is already in another active
         // workflow
         return false;
      }

      boolean canAddComponent = false;

      // Test Case B
      if (process.getStatus() == ProcessStatus.DEFINED) {
         canAddComponent = true;
      } else {
         // Test Case C
         if (process.getStatus() == ProcessStatus.LAUNCHED) {
            final ProcessHistory latestHx = this.workflowProvider.getWorkflowAccessor()
                                                                  .getProcessHistory(processId)
                                                                  .last();

            if (this.workflowProvider.getBPMNInfo()
                                      .isEditState(process.getDefinitionId(), latestHx.getOutcomeState())) {
               canAddComponent = true;
            }
         }
      }

      if (!canAddComponent) {
         if (!process.isActive()) {
            // Cannot do so because process is not active
            return false;
         } else {
            // Cannot do so because process is in LAUNCHED state yet the
            // workflow is not in an EDIT state
            return false;
         }
      }

      return true;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the process owner. When the owner equals BPMNInfo.UNOWNED_PROCESS,
    * means process not owned by anyone
    *
    * @param processId
    *            the process id to be updated
    * @param newOwner
    *            the new owner. If lock is being acquired, send userId. If
    *            being released, to BPMNInfo.UNOWNED_PROCESS
    */
   public void setProcessOwner(UUID processId, UUID newOwner) {
      final ProcessDetail process = this.workflowProvider.getProcessDetailStore()
                                                          .get(processId);

      process.setOwnerId(newOwner);
      this.workflowProvider.getProcessDetailStore()
                            .put(process.getId(), process);
   }
}

