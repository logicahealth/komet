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

import java.util.Date;
import java.util.UUID;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.LookupService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.provider.workflow.BPMNInfo;
import sh.isaac.provider.workflow.WorkflowProvider;
import sh.isaac.provider.workflow.model.WorkflowContentStore;
import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.DefinitionDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.EndWorkflowType;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.ProcessStatus;
import sh.isaac.provider.workflow.model.contents.ProcessHistory;

//~--- classes ----------------------------------------------------------------

/**
 * Contains methods necessary to start, launch, cancel, or conclude a workflow
 * process
 *
 *
 * {@link WorkflowContentStore} {@link WorkflowProvider}
 * {@link BPMNInfo}.
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
@Service
@Singleton
public class WorkflowProcessInitializerConcluder {
   /** The workflow provider. */
   private final WorkflowProvider workflowProvider_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new workflow process initializer concluder.
    */

   // for HK2
   private WorkflowProcessInitializerConcluder() {
      this.workflowProvider_ = LookupService.get()
            .getService(WorkflowProvider.class);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Creates a new workflow process instance. In turn, a new entry is added to the ProcessDetails content store. The process status defaults as
    * DEFINED.
    *
    * Used by users when creating a new process
    *
    * @param definitionId The definition for which the process should be based on
    * @param userId The user whom is creating the new process
    * @param name The name of the new process
    * @param description The description of the new process
    * @return The process id which is in turn the key to the Process Detail's entry
    * @throws Exception the exception
    */
   public UUID createWorkflowProcess(UUID definitionId, UUID userId, String name, String description)
            throws Exception {
      if ((name == null) || name.isEmpty() || (description == null) || description.isEmpty()) {
         throw new Exception("Name and Description must be filled out when creating a process");
      }

      // TODO: Do we actually want this prevention measure?

      /*
       * for (ProcessDetail detail : processDetailStore.getAllEntries()) { if
       * (detail.getName().equalsIgnoreCase(name)) { throw new
       * Exception("Process names must be unique"); } }
       */

      // Create Process Details with "DEFINED"
      final ProcessDetail details = new ProcessDetail(definitionId,
                                                      userId,
                                                      new Date().getTime(),
                                                      ProcessStatus.DEFINED,
                                                      name,
                                                      description);
      final UUID processId = this.workflowProvider_.getProcessDetailStore()
                                                   .add(details);

      // Add Process History with START_STATE-AUTOMATED-EDIT_STATE
      // At some point, need to handle the case where multiple startActions
      // may be defined for single DefinitionId. For now, verify only one and
      // use it
      if (this.workflowProvider_.getBPMNInfo()
                                .getDefinitionStartActionMap()
                                .get(definitionId)
                                .size() != 1) {
         throw new Exception(
             "Currently only able to handle single startAction within a definition. This definition found: " +
             this.workflowProvider_.getBPMNInfo().getDefinitionStartActionMap().get(definitionId).size());
      }

      final AvailableAction startAdvancement = this.workflowProvider_.getBPMNInfo()
                                                                     .getDefinitionStartActionMap()
                                                                     .get(definitionId)
                                                                     .iterator()
                                                                     .next();
      final ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                             userId,
                                                             new Date().getTime(),
                                                             startAdvancement.getInitialState(),
                                                             startAdvancement.getAction(),
                                                             startAdvancement.getOutcomeState(),
                                                             "",
                                                             1);

      this.workflowProvider_.getProcessHistoryStore()
                            .add(advanceEntry);
      return processId;
   }

   /**
    * Ends a workflow instance either via concluding it or canceling it. In doing so, the ProcessDetails is updated accordingly, another Process
    * History entry is added showing the advancement, and in the case of "CANCEL" request, any editing changes previously associated with the
    * instance are reverted.
    *
    * @param processId The process being ended
    * @param actionToProcess The AvailableAction the user requested
    * @param userId The user ending the workflow
    * @param comment The user added comment associated with the advancement
    * @param endType The type of END-ADVANCEMENT associated with the selected
    * action (Canceled or Concluded)
    * @param editCoordinate the edit coordinate
    * @throws Exception Thrown if the process doesn't exist or an attempt is made to a) cancel or conclude a process which isn't active, b) conclude a process where
    * the process is not LAUNCHED, or c) conclude a process where the outcome state isn't a concluded state according to the definition
    */
   public void endWorkflowProcess(UUID processId,
                                  AvailableAction actionToProcess,
                                  UUID userId,
                                  String comment,
                                  EndWorkflowType endType,
                                  EditCoordinate editCoordinate)
            throws Exception {
      final ProcessDetail  entry = this.workflowProvider_.getProcessDetailStore()
                                                         .get(processId);
      final ProcessHistory hx    = this.workflowProvider_.getWorkflowAccessor()
                                                         .getProcessHistory(processId)
                                                         .last();

      if (entry == null) {
         throw new Exception("Cannot cancel nor conclude a workflow that hasn't been defined yet");
      } else if (!entry.isActive()) {
         throw new Exception("Cannot end a workflow that is not active.  Current status: " + entry.getStatus());
      } else if (endType == EndWorkflowType.CONCLUDED) {
         if (entry.getStatus() != ProcessStatus.LAUNCHED) {
            throw new Exception("Cannot conclude workflow that is in the following state: " + entry.getStatus());
         } else {
            if (!this.workflowProvider_.getBPMNInfo()
                                       .isConcludedState(hx.getOutcomeState())) {
               final DefinitionDetail defEntry = this.workflowProvider_.getDefinitionDetailStore()
                                                                       .get(entry.getDefinitionId());

               throw new Exception("Cannot perform Conclude action on the definition: " + defEntry.getName() +
                                   " version: " + defEntry.getVersion() + " when the workflow state is: " +
                                   hx.getOutcomeState());
            }
         }
      }

      // Request is valid, update process details
      entry.setOwnerId(BPMNInfo.UNOWNED_PROCESS);
      entry.setTimeCanceledOrConcluded(new Date().getTime());

      if (endType.equals(EndWorkflowType.CANCELED)) {
         entry.setStatus(ProcessStatus.CANCELED);
      } else if (endType.equals(EndWorkflowType.CONCLUDED)) {
         entry.setStatus(ProcessStatus.CONCLUDED);
      }

      this.workflowProvider_.getProcessDetailStore()
                            .put(processId, entry);

      // Add to process's history
      final ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                             userId,
                                                             new Date().getTime(),
                                                             actionToProcess.getInitialState(),
                                                             actionToProcess.getAction(),
                                                             actionToProcess.getOutcomeState(),
                                                             comment,
                                                             hx.getHistorySequence() + 1);

      this.workflowProvider_.getProcessHistoryStore()
                            .add(advanceEntry);

      // if a cancel has been requested, revert all changes associated with the workflow
      if (endType.equals(EndWorkflowType.CANCELED)) {
         this.workflowProvider_.getWorkflowUpdater()
                               .revertChanges(entry.getComponentToInitialEditMap()
                                     .keySet(), processId, editCoordinate);
      }
   }

   /**
    * Launch a process as long as a) the process is in a defined state and b) there are components associated with the process. By launching it, the
    * ProcessDetails has its status updated to LAUNCHED and the time launched gets an entry of <NOW>.
    *
    * Used when a process is advanced and it is in the edit state and is has a
    * DEFINED status.
    *
    * @param processId
    * the process being launched
    *
    * @throws Exception
    * Thrown if a) process doesn't exist, b) process exists but is not in the DEFINED status, or c) no components are associated with the process
    */
   public void launchProcess(UUID processId)
            throws Exception {
      final ProcessDetail entry = this.workflowProvider_.getProcessDetailStore()
                                                        .get(processId);

      if (entry == null) {
         throw new Exception("Cannot launch workflow that hasn't been defined first");
      } else if (entry.getStatus() != ProcessStatus.DEFINED) {
         throw new Exception("Only processes that have a DEFINED status may be launched");
      } else if (entry.getComponentToInitialEditMap()
                      .keySet()
                      .isEmpty()) {
         throw new Exception("Workflow can only be launched when the workflow contains components to work on");
      }

      // Update Process Details with "LAUNCHED"
      entry.setOwnerId(BPMNInfo.UNOWNED_PROCESS);
      entry.setStatus(ProcessStatus.LAUNCHED);
      entry.setTimeLaunched(new Date().getTime());
      this.workflowProvider_.getProcessDetailStore()
                            .put(processId, entry);
   }
}

