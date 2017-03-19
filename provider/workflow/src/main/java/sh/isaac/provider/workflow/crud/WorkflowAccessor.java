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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.UserRole;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.model.sememe.DynamicSememeUsageDescriptionImpl;
import sh.isaac.provider.workflow.BPMNInfo;
import sh.isaac.provider.workflow.WorkflowProvider;
import sh.isaac.provider.workflow.model.WorkflowContentStore;
import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.DefinitionDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.ProcessStatus;
import sh.isaac.provider.workflow.model.contents.ProcessHistory;
import sh.isaac.provider.workflow.model.contents.ProcessHistory.ProcessHistoryComparator;
import sh.isaac.utility.Frills;

//~--- classes ----------------------------------------------------------------

/**
 * Contains methods necessary to perform workflow-based accessing
 *
 * {@link WorkflowContentStore} {@link WorkflowProvider}
 * {@link BPMNInfo}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
@Service
@Singleton
public class WorkflowAccessor {
   private WorkflowProvider workflowProvider_;

   //~--- constructors --------------------------------------------------------

   // for HK2
   private WorkflowAccessor() {
      workflowProvider_ = LookupService.get()
                                       .getService(WorkflowProvider.class);
   }

   //~--- methods -------------------------------------------------------------

   private String formatStringAssociationInformation(String value, String target) {
      // Association: <Source Component> : <Target Component>
      return String.format("Association: %s : %s", value, target);
   }

   private String formatStringConceptInformation(int nid, StampCoordinate stampCoord, LanguageCoordinate langCoord) {
      // Concept: <Concept FSN>
      return String.format("Concept: %s",
                           Frills.getConceptSnapshot(nid, stampCoord, langCoord)
                                 .get()
                                 .getFullySpecifiedDescription()
                                 .get()
                                 .value()
                                 .getText());
   }

   private String formatStringDescriptionInformation(LatestVersion<DescriptionSememe> descSem) {
      // Description: <Desctipion Text>
      return String.format("Description: %s", descSem.value()
            .getText());
   }

   private String formatStringMapInformation(String value, String target) {
      // Map: <MapSet FSN>-<Source Code> : <Target Code>
      return String.format("Map: %s : %s", value, target);
   }

   private String formatStringValueInformation(String value) {
      // Value: <Value Text>
      return String.format("Value: %s", value);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Map the process history to each process for which the user's roles
    * enable them to advance workflow based on the process's current state.
    * Only active processes can be advanced thus only those processes with such
    * a status are returned.
    *
    * Used to determine which processes to list when the user selects the
    * "Author Workflows" link
    *
    * @param definitionId
    *            The definition being examined
    * @param userId
    *            The user for whom relevant processes are being determined
    *
    * @return The map of advanceable processes to their Process History
    */
   public Map<ProcessDetail, SortedSet<ProcessHistory>> getAdvanceableProcessInformation(UUID definitionId,
         UUID userId) {
      Map<ProcessDetail, SortedSet<ProcessHistory>> processInformation = new HashMap<>();

      // Get User Roles
      Map<String, Set<AvailableAction>> actionsByInitialState = getUserAvailableActionsByInitialState(definitionId,
                                                                                                      userId);

      // For each ActiveProcesses, see if its current state is "applicable
      // current state" and if
      for (ProcessDetail process: workflowProvider_.getProcessDetailStore()
            .values()) {
         if (process.isActive() && process.getDefinitionId().equals(definitionId)) {
            SortedSet<ProcessHistory> hx = getProcessHistory(process.getId());

            if (actionsByInitialState.containsKey(hx.last()
                  .getOutcomeState())) {
               processInformation.put(process, hx);
            }
         }
      }

      return processInformation;
   }

   /**
    * Examines the definition to see if the component is in an active workflow.
    * An active workflow is a workflow in either DEFINED or LAUNCHED process
    * status.
    *
    * Used to ensure that a concept or sememe doesn't belong to two active
    * processes simultaneously as that is not allowed at this point. If a
    * person attempts to do so, they should get a warning that the commit will
    * not be added to a workflow.
    *
    * @param definitionId
    *            The key the the Definition Detail entry
    * @param compNid
    *            The component to be investigated
    *
    * @return True if the component is in an active workflow.
    */
   public boolean isComponentInActiveWorkflow(UUID definitionId, int compNid) {
      for (ProcessDetail proc: workflowProvider_.getProcessDetailStore()
            .values()) {
         if (proc.getDefinitionId().equals(definitionId) &&
               proc.isActive() &&
               proc.getComponentToInitialEditMap().keySet().contains(compNid)) {
            return true;
         }
      }

      return false;
   }

   public boolean isComponentInProcess(UUID processId, int componentNid) {
      ProcessDetail process = getProcessDetails(processId);

      return (process != null) ? process.getComponentToInitialEditMap()
                                        .containsKey(componentNid)
                               : false;
   }

   /**
    * Return a String formatted for component modifications. Includes Concept, Description, Map, Association, or Value.
    *
    *  @param nid int - Component id
    *  @param stampCoord StampCoordinate
    *  @param langCoord LanguageCoordinate
    *
    *  @exception Exception
    */
   private String getComponentModification(int nid,
         StampCoordinate stampCoord,
         LanguageCoordinate langCoord)
            throws Exception {
      ObjectChronologyType oct = Get.identifierService()
                                    .getChronologyTypeForNid(nid);

      if (oct == ObjectChronologyType.CONCEPT) {
         return formatStringConceptInformation(nid, stampCoord, langCoord);
      } else if (oct == ObjectChronologyType.SEMEME) {
         SememeChronology<? extends SememeVersion<?>> sememe = Get.sememeService()
                                                                  .getSememe(nid);

         switch (sememe.getSememeType()) {
         case DESCRIPTION:
            LatestVersion<DescriptionSememe> descSem =
               (LatestVersion<DescriptionSememe>) ((SememeChronology) sememe).getLatestVersion(LogicGraphSememe.class,
                                                                                               stampCoord)
                                                                             .get();

            return formatStringDescriptionInformation(descSem);

         case DYNAMIC:
            LatestVersion<DynamicSememe> dynSem =
               (LatestVersion<DynamicSememe>) ((SememeChronology) sememe).getLatestVersion(LogicGraphSememe.class,
                                                                                           stampCoord)
                                                                         .get();
            int                                            assemblageSeq = dynSem.value()
                                                                                 .getAssemblageSequence();
            ConceptChronology<? extends ConceptVersion<?>> conChron = Get.conceptService()
                                                                         .getConcept(assemblageSeq);
            String                                         target        = null;
            String                                         value         = null;
            DynamicSememeUsageDescription sememeDefinition               = DynamicSememeUsageDescriptionImpl.read(nid);

            for (DynamicSememeColumnInfo info: sememeDefinition.getColumnInfo()) {
               if (info.getColumnDescriptionConcept()
                       .equals(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_VALUE
                             .getUUID())) {
                  value = info.getDefaultColumnValue()
                              .dataToString();
               } else if (info.getColumnDescriptionConcept()
                              .equals(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT
                                    .getUUID())) {
                  target = info.getDefaultColumnValue()
                               .dataToString();
               }
            }

            if (Frills.isMapping(sememe)) {
               return formatStringMapInformation(value, target);
            } else if (Frills.isAssociation(sememe)) {
               return formatStringAssociationInformation(value, target);
            } else {
               return formatStringValueInformation(value);
            }
         default:
            throw new Exception("Unsupported Sememe Type: " + sememe.getSememeType());
         }
      } else {
         throw new Exception("Unsupported Object Chronology Type: " + oct);
      }
   }

   /**
    * Return an ArrayList of Strings formatted for component modifications for a given processId.
    * Includes Concept, Description, Map, Association, or Value.
    *
    *  @param processId UUID - identifier of the process.
    *  @return ArrayList<String> - collection of formatted string of component modifications.
    *
    *  @exception Exception
    */
   public ArrayList<String> getComponentModifications(UUID processId)
            throws Exception {
      // get process detail for process
      ProcessDetail     processDetail               = getProcessDetails(processId);
      ArrayList<String> componentModificationString = new ArrayList<>();

      for (Map.Entry<Integer, Stamp> entry: processDetail.getComponentToInitialEditMap()
            .entrySet()) {
         try {
            componentModificationString.add(getComponentModification(entry.getKey(),

            // Verify these two parameters.
            (StampCoordinate) entry.getValue(), (LanguageCoordinate) entry.getValue()));
         } catch (Exception ex) {
            throw ex;
         }
      }

      return componentModificationString;
   }

   /**
    * Gets the Definition Detail entry for the specified definition key
    *
    * Used to access all information associated with a given Workflow
    * Definition.
    *
    * @param definitionId
    *            The key the the Definition Detail entry
    *
    * @return The definition details entry requested
    */
   public DefinitionDetail getDefinitionDetails(UUID definitionId) {
      return workflowProvider_.getDefinitionDetailStore()
                              .get(definitionId);
   }

   /**
    * Returns last Process History entry associated with the process id.
    * Used to identify the history of a given workflow process (i.e. an
    * instance of a definition)
    *
    * @param processId
    *            The key the the Process Detail entry
    *
    * @return the sorted history of the process.
    */
   public ProcessHistory getLastProcessHistory(UUID processId) {
      for (ProcessDetail process: workflowProvider_.getProcessDetailStore()
            .values()) {
         if (process.getId()
                    .compareTo(processId) == 0) {
            SortedSet<ProcessHistory> hx = getProcessHistory(process.getId());

            return hx.last();
         }
      }

      return null;
   }

   /**
    * Gets the Process Detail entry for the specified process key
    *
    * Used to access all information associated with a given workflow process
    * (i.e. an instance of a definition).
    *
    * @param processId
    *            The key the the Process Detail entry
    *
    * @return The process details entry requested.  If none exists, return null
    */
   public ProcessDetail getProcessDetails(UUID processId) {
      return workflowProvider_.getProcessDetailStore()
                              .get(processId);
   }

   /**
    * Returns all Process History entries associated with the process id. This
    * contains all the advancements made during the given process. History is
    * sorted by advancement time.
    *
    * Used to identify the history of a given workflow process (i.e. an
    * instance of a definition)
    *
    * @param processId
    *            The key the the Process Detail entry
    *
    * @return the sorted history of the process.
    */
   public SortedSet<ProcessHistory> getProcessHistory(UUID processId) {
      SortedSet<ProcessHistory> allHistoryForProcess = new TreeSet<>(new ProcessHistoryComparator());

      for (ProcessHistory hx: workflowProvider_.getProcessHistoryStore()
            .values()) {
         if (hx.getProcessId()
               .equals(processId)) {
            allHistoryForProcess.add(hx);
         }
      }

      return allHistoryForProcess;
   }

   /**
    * Request a list of workflow processes.  Can request any or all workflow statuses of DEFINED, LAUNCHED, CANCELED or CONCLUDED.
    *
    * @param definitionId
    *            The workflow definition (type) being examined.
    * @param status
    *            A list of statuses to include.
    *
    * @return The list of processes filtered by the status provided.
    */
   public ArrayList<ProcessDetail> getProcessInformation(UUID definitionId,
         UUID userId,
         ArrayList<ProcessStatus> status) {
      ArrayList<ProcessDetail> processes = new ArrayList<>();

      // Get User Roles

      /*
       * Map<String, Set<AvailableAction>> actionsByInitialState = getUserAvailableActionsByInitialState(
       *               definitionId, userId);
       */

      // For each process, see if its current state is "applicable current state"
      for (ProcessDetail process: workflowProvider_.getProcessDetailStore()
            .values()) {
         if (process.getDefinitionId().equals(definitionId) && status.contains(process.getStatus())) {
            processes.add(process);
         }
      }

      return processes;
   }

   /**
    * Returns the of available actions a user has roles based on the
    * definition's possible initial-states
    *
    * Used to support the getAdvanceableProcessInformation() and
    * getUserPermissibleActionsForProcess()
    *
    * @param definitionId
    *            The definition being examined
    * @param userId
    *            The user is being examined
    *
    * @return The set of all Available Actions for each initial state for which
    *         the user can advance workflow.
    */
   private Map<String, Set<AvailableAction>> getUserAvailableActionsByInitialState(UUID definitionId, UUID userId) {
      Map<String, Set<AvailableAction>> applicableActions = new HashMap<>();

      // Get User Roles
      Set<UserRole> userRoles = workflowProvider_.getUserRoleStore()
                                                 .getUserRoles(userId);

      // Get Map of available actions (by initialState) that can be executed
      // based on userRoles
      for (AvailableAction action: workflowProvider_.getAvailableActionStore()
            .values()) {
         if (action.getDefinitionId().equals(definitionId) && userRoles.contains(action.getRole())) {
            if (!applicableActions.containsKey(action.getInitialState())) {
               applicableActions.put(action.getInitialState(), new HashSet<AvailableAction>());
            }

            applicableActions.get(action.getInitialState())
                             .add(action);
         }
      }

      return applicableActions;
   }

   /**
    * Identifies the set of Available Actions containing actions which the user
    * may take on a given process
    *
    * Used to determine which actions populate the Transition Workflow picklist
    *
    * @param processId
    *            The process being examined
    * @param userId
    *            The user for whom available actions are being identified
    *
    * @return A set of AvailableActions defining the actions a user can take on
    *         the process
    */
   public Set<AvailableAction> getUserPermissibleActionsForProcess(UUID processId, UUID userId) {
      ProcessDetail processDetail = getProcessDetails(processId);

      if (processDetail != null) {
         ProcessHistory processLatest = getProcessHistory(processId).last();
         Map<String, Set<AvailableAction>> actionsByInitialState =
            getUserAvailableActionsByInitialState(processDetail.getDefinitionId(),
                                                  userId);

         if (actionsByInitialState.containsKey(processLatest.getOutcomeState())) {
            return actionsByInitialState.get(processLatest.getOutcomeState());
         }
      }

      return new HashSet<>();
   }

   /**
    * Identify the version of the component prior to workflow process being launched
    *
    * @param processId
    *            The process being examined
    * @param compNid
    *            The component to be investigated
    * @return The version of the component prior to it entering into workflow. If no version is found, the chronology was created within this workflow process
    *
    * @throws Exception
    */
   public StampedVersion getVersionPriorToWorkflow(UUID processId, int compNid) {
      ProcessDetail proc = getProcessDetails(processId);

      if (!proc.getComponentToInitialEditMap()
               .keySet()
               .contains(compNid)) {
         return null;
      }

      long                timeLaunched = proc.getTimeCreated();
      ObjectChronology<?> objChron;

      if (Get.identifierService()
             .getChronologyTypeForNid(compNid) == ObjectChronologyType.CONCEPT) {
         objChron = Get.conceptService()
                       .getConcept(compNid);
      } else if (Get.identifierService()
                    .getChronologyTypeForNid(compNid) == ObjectChronologyType.SEMEME) {
         objChron = Get.sememeService()
                       .getSememe(compNid);
      } else {
         throw new RuntimeException("Cannot reconcile NID with Identifier Service for nid: " + compNid);
      }

      OfInt stampSequencesItr = objChron.getVersionStampSequences()
                                        .iterator();
      int   stampSeq          = -1;
      long  stampTime         = 0;

      while (stampSequencesItr.hasNext() && (stampTime < timeLaunched)) {
         int  currentStampSeq  = stampSequencesItr.next();
         long currentStampTime = Get.stampService()
                                    .getTimeForStamp(currentStampSeq);

         if (currentStampTime < timeLaunched) {
            stampTime = currentStampTime;
            stampSeq  = currentStampSeq;
         }
      }

      for (StampedVersion version: objChron.getVersionList()) {
         if (version.getStampSequence() == stampSeq) {
            return version;
         }
      }

      return null;
   }

   public <T extends StampedVersion> T getVersionPriorToWorkflow(Class<T> versionClazz,
         UUID processId,
         int compNid)
            throws Exception {
      StampedVersion version = getVersionPriorToWorkflow(processId, compNid);

      return versionClazz.cast(version);
   }
}

