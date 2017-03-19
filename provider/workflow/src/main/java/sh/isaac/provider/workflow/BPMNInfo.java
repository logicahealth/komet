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



package sh.isaac.provider.workflow;

//~--- JDK imports ------------------------------------------------------------

import java.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.EndWorkflowType;

//~--- classes ----------------------------------------------------------------

/**
 * A class that stores various info about the BPMN workflow.
 *
 * {@link Bpmn2FileImporter} {@link WorkflowProvider}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class BPMNInfo {
   /** The Constant UNOWNED_PROCESS. */
   public static final UUID UNOWNED_PROCESS = new UUID(0, 0);

   /** A universal means of expressing a workflow time stamp. */
   final static public DateTimeFormatter workflowDateFormatter = DateTimeFormatter.ofPattern("hh:mm:ssa MM/dd/yy");

   //~--- fields --------------------------------------------------------------

   /** A map of available actions per type of ending workflow. */
   private final Map<EndWorkflowType, Set<AvailableAction>> endNodeTypeMap;

   /** A map of available actions per definition from which a workflow may be started. */
   private final Map<UUID, Set<AvailableAction>> definitionStartActionMap;

   /** The definition id. */
   private final UUID definitionId;

   /** A map of all states per definition from which a process may be edited. */
   private final Map<UUID, Set<String>> editStatesMap;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new BPMN info.
    *
    * @param definitionId the definition id
    * @param endNodeTypeMap the end node type map
    * @param definitionStartActionMap the definition start action map
    * @param editStatesMap the edit states map
    */
   protected BPMNInfo(UUID definitionId,
                      Map<EndWorkflowType, Set<AvailableAction>> endNodeTypeMap,
                      Map<UUID, Set<AvailableAction>> definitionStartActionMap,
                      Map<UUID, Set<String>> editStatesMap) {
      this.definitionId             = definitionId;
      this.endNodeTypeMap           = Collections.unmodifiableMap(endNodeTypeMap);
      this.definitionStartActionMap = Collections.unmodifiableMap(definitionStartActionMap);
      this.editStatesMap            = Collections.unmodifiableMap(editStatesMap);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Identifies if the state represents a concluded state by examining the
    * endNoeTypeMap's CONCLUDED list of AvaialbleActions.
    *
    * @param state            The state to test
    * @return true if the state is a Concluded state
    */
   public boolean isConcludedState(String state) {
      for (final AvailableAction action: this.endNodeTypeMap.get(EndWorkflowType.CONCLUDED)) {
         if (action.getInitialState()
                   .equals(state)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Gets the definition id.
    *
    * @return the definition id
    */
   public UUID getDefinitionId() {
      return this.definitionId;
   }

   /**
    * Retrieves the map of workflow definitions to the set of all available
    * actions start-process actions available.
    *
    * Used for populating Process History with the initial workflow action via
    * an automated role.
    *
    * @return the read-only map of available actions per type of ending workflow
    */
   public Map<UUID, Set<AvailableAction>> getDefinitionStartActionMap() {
      return this.definitionStartActionMap;
   }

   /**
    * Identifies if the state is an edit state by examining the editStatesMap.
    *
    * @param definitionId
    *            The definition which the state's process belongs
    * @param state
    *            The state to test
    *
    * @return true if the state is an Edit state
    */
   public boolean isEditState(UUID definitionId, String state) {
      return this.editStatesMap.get(definitionId)
                               .contains(state);
   }

   /**
    * Gets a map of all edit states available per workflow definition.
    *
    * Used to identify if the current state is an edit state. If it is,
    * modeling and mapping can occur and will be added to the process.
    *
    * @return the read-only set of edit states
    */
   public Map<UUID, Set<String>> getEditStatesMap() {
      return this.editStatesMap;
   }

   /**
    * Retrieves the map of end workflow types to the set of all available
    * actions causing a process to be ProcessStatus.CANCELED or
    * ProcessStatus.CONLCUDED.
    *
    * Used for programmatically identifying if a requested action requested is
    * concluding a process.
    *
    * @return the read-only map of available actions per type of ending workflow
    */
   public Map<EndWorkflowType, Set<AvailableAction>> getEndWorkflowTypeMap() {
      return this.endNodeTypeMap;
   }
}

