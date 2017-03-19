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

import java.util.UUID;

import javax.annotation.PreDestroy;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.OchreCache;
import sh.isaac.api.UserRoleService;
import sh.isaac.provider.workflow.crud.WorkflowAccessor;
import sh.isaac.provider.workflow.crud.WorkflowProcessInitializerConcluder;
import sh.isaac.provider.workflow.crud.WorkflowUpdater;
import sh.isaac.provider.workflow.model.WorkflowContentStore;
import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.DefinitionDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail;
import sh.isaac.provider.workflow.model.contents.ProcessHistory;

//~--- classes ----------------------------------------------------------------

/**
 * {@link WorkflowProvider}
 *
 * This provider is how users get access to the Workflow implementation objects.
 * The data store for this implementation piggy-backs on top of the metacontent store - which comes
 * up before this class, and manages its own shutdown - so no shutdown / startup sequence
 * is required for this service.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class WorkflowProvider
         implements OchreCache {
   /** The bpmn path. */

   // This hack is only visible for test hacking at the moment - this will be redone in the future when we handle multiple bpmn files
   public static String BPMN_PATH = "/sh/isaac/provider/workflow/VetzWorkflowV1.0.bpmn2";

   //~--- fields --------------------------------------------------------------

   /** The logger. */
   private final Logger logger = LogManager.getLogger();

   /**
    * * Workflow-based Data Store containing the available actions based on role and
    * initial state. Initialized during the importing of a BPMN2 file (containing
    * the definition) and static from then on.
    */
   private WorkflowContentStore<AvailableAction> availableActionContentStore;

   /**
    * Workflow-based Data Store containing the details associated with a given
    * project. Initialized during the importing of a BPMN2 file (containing the
    * definition) and static from then on.
    */
   private WorkflowContentStore<DefinitionDetail> definitionDetailContentStore;

   /**
    * Workflow-based Data Store containing the workflow process instance entries.
    * Initialized by user during process creation and updated by users thereafter.
    */
   private WorkflowContentStore<ProcessDetail> processDetailContentStore;

   /**
    * Workflow-based Data Store containing the process instance historical entries.
    * Updated each time a user advances workflow.
    */
   private WorkflowContentStore<ProcessHistory> processHistoryContentStore;

   /**
    * Workflow-based Data Store containing the workflow User Role entries.
    * Initialized during reading of WF Definition only and static from then on.
    */
   private UserRoleService userRoleContentStore;

   /** The bpmn info. */
   private BPMNInfo bpmnInfo;

   //~--- constant enums ------------------------------------------------------

   /**
    * The Enum WorkflowContentStoreType.
    */
   private enum WorkflowContentStoreType {
      /** The available action. */
      AVAILABLE_ACTION,

      /** The definition detail. */
      DEFINITION_DETAIL,

      /** The historical workflow. */
      HISTORICAL_WORKFLOW,

      /** The process definition. */
      PROCESS_DEFINITION
   }

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new workflow provider.
    */

   // For HK2 only
   private WorkflowProvider() {
      this.logger.debug("Starting up the Workflow Provider");
      reCacheStoreRefs();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Reset.
    *
    * @see sh.isaac.api.OchreCache#reset()
    */
   @Override
   public void reset() {
      this.logger.info("Clearing cache due to metastore shutdown");
      this.availableActionContentStore  = null;
      this.definitionDetailContentStore = null;
      this.processDetailContentStore    = null;
      this.processHistoryContentStore   = null;
      this.userRoleContentStore         = null;
      this.bpmnInfo                     = null;
   }

   /**
    * Re cache store refs.
    */
   private synchronized void reCacheStoreRefs() {
      this.logger.info("Getting storage refs from metastore " + this);
      this.availableActionContentStore = new WorkflowContentStore<>(Get.metaContentService().<UUID,
            byte[]>openStore(WorkflowContentStoreType.AVAILABLE_ACTION.toString()),
            (bytes) -> (bytes == null) ? null
                                       : new AvailableAction(bytes));
      this.definitionDetailContentStore = new WorkflowContentStore<>(Get.metaContentService().<UUID,
            byte[]>openStore(WorkflowContentStoreType.DEFINITION_DETAIL.toString()),
            (bytes) -> (bytes == null) ? null
                                       : new DefinitionDetail(bytes));
      this.processDetailContentStore = new WorkflowContentStore<>(Get.metaContentService().<UUID,
            byte[]>openStore(WorkflowContentStoreType.PROCESS_DEFINITION.toString()),
            (bytes) -> (bytes == null) ? null
                                       : new ProcessDetail(bytes));
      this.processHistoryContentStore = new WorkflowContentStore<>(Get.metaContentService().<UUID,
            byte[]>openStore(WorkflowContentStoreType.HISTORICAL_WORKFLOW.toString()),
            (bytes) -> (bytes == null) ? null
                                       : new ProcessHistory(bytes));
      this.userRoleContentStore = LookupService.getService(UserRoleService.class);

      // this needs rework to load 1 (or more) BPMN2 Files from the classpath
      if (BPMN_PATH != null)  // Null is to support a test case where it doesn't want the file loaded by default
      {
         this.bpmnInfo = new Bpmn2FileImporter(BPMN_PATH, this).getBPMNInfo();
      }
   }

   /**
    * Shutdown.
    */
   @PreDestroy
   private void shutdown() {
      this.logger.debug("Shutting down the Workflow Provider");

      // This is a noop, the metacontent store properly shuts itself down
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the available action store.
    *
    * @return the available action store
    */
   public WorkflowContentStore<AvailableAction> getAvailableActionStore() {
      if (this.availableActionContentStore == null) {
         reCacheStoreRefs();
      }

      return this.availableActionContentStore;
   }

   /**
    * Gets the BPMN info.
    *
    * @return the BPMN info
    */
   public BPMNInfo getBPMNInfo() {
      if (this.bpmnInfo == null) {
         reCacheStoreRefs();
      }

      return this.bpmnInfo;
   }

   /**
    * Gets the definition detail store.
    *
    * @return the definition detail store
    */
   public WorkflowContentStore<DefinitionDetail> getDefinitionDetailStore() {
      if (this.definitionDetailContentStore == null) {
         reCacheStoreRefs();
      }

      return this.definitionDetailContentStore;
   }

   /**
    * Gets the process detail store.
    *
    * @return the process detail store
    */
   public WorkflowContentStore<ProcessDetail> getProcessDetailStore() {
      if (this.processDetailContentStore == null) {
         reCacheStoreRefs();
      }

      return this.processDetailContentStore;
   }

   /**
    * Gets the process history store.
    *
    * @return the process history store
    */
   public WorkflowContentStore<ProcessHistory> getProcessHistoryStore() {
      if (this.processHistoryContentStore == null) {
         reCacheStoreRefs();
      }

      return this.processHistoryContentStore;
   }

   /**
    * Gets the user role store.
    *
    * @return the user role store
    */
   public UserRoleService getUserRoleStore() {
      if (this.userRoleContentStore == null) {
         reCacheStoreRefs();
      }

      return this.userRoleContentStore;
   }

   /**
    * Gets the workflow accessor.
    *
    * @return the workflow accessor
    */
   public WorkflowAccessor getWorkflowAccessor() {
      return LookupService.get()
                          .getService(WorkflowAccessor.class);
   }

   /**
    * Gets the workflow process initializer concluder.
    *
    * @return the workflow process initializer concluder
    */
   public WorkflowProcessInitializerConcluder getWorkflowProcessInitializerConcluder() {
      return LookupService.get()
                          .getService(WorkflowProcessInitializerConcluder.class);
   }

   /**
    * Gets the workflow updater.
    *
    * @return the workflow updater
    */
   public WorkflowUpdater getWorkflowUpdater() {
      return LookupService.get()
                          .getService(WorkflowUpdater.class);
   }
}

