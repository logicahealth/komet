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
   // This hack is only visible for test hacking at the moment - this will be redone in the future when we handle multiple bpmn files
   public static String BPMN_PATH = "/sh/isaac/provider/workflow/VetzWorkflowV1.0.bpmn2";

   //~--- fields --------------------------------------------------------------

   private final Logger logger = LogManager.getLogger();

   /**
    * * Workflow-based Data Store containing the available actions based on role and
    * initial state. Initialized during the importing of a BPMN2 file (containing
    * the definition) and static from then on.
    */
   private WorkflowContentStore<AvailableAction> availableActionContentStore_;

   /**
    * Workflow-based Data Store containing the details associated with a given
    * project. Initialized during the importing of a BPMN2 file (containing the
    * definition) and static from then on.
    */
   private WorkflowContentStore<DefinitionDetail> definitionDetailContentStore_;

   /**
    * Workflow-based Data Store containing the workflow process instance entries.
    * Initialized by user during process creation and updated by users thereafter.
    */
   private WorkflowContentStore<ProcessDetail> processDetailContentStore_;

   /**
    * Workflow-based Data Store containing the process instance historical entries.
    * Updated each time a user advances workflow.
    */
   private WorkflowContentStore<ProcessHistory> processHistoryContentStore_;

   /**
    * Workflow-based Data Store containing the workflow User Role entries.
    * Initialized during reading of WF Definition only and static from then on.
    */
   private UserRoleService userRoleContentStore_;
   private BPMNInfo        bpmnInfo_;

   //~--- constant enums ------------------------------------------------------

   private enum WorkflowContentStoreType {
      AVAILABLE_ACTION,
      DEFINITION_DETAIL,
      HISTORICAL_WORKFLOW,
      PROCESS_DEFINITION
   }

   //~--- constructors --------------------------------------------------------

   // For HK2 only
   private WorkflowProvider() {
      logger.debug("Starting up the Workflow Provider");
      reCacheStoreRefs();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * @see sh.isaac.api.OchreCache#reset()
    */
   @Override
   public void reset() {
      logger.info("Clearing cache due to metastore shutdown");
      availableActionContentStore_  = null;
      definitionDetailContentStore_ = null;
      processDetailContentStore_    = null;
      processHistoryContentStore_   = null;
      userRoleContentStore_         = null;
      bpmnInfo_                     = null;
   }

   private synchronized void reCacheStoreRefs() {
      logger.info("Getting storage refs from metastore " + this);
      availableActionContentStore_ = new WorkflowContentStore<AvailableAction>(Get.metaContentService().<UUID,
            byte[]>openStore(WorkflowContentStoreType.AVAILABLE_ACTION.toString()),
            (bytes) -> (bytes == null) ? null
                                       : new AvailableAction(bytes));
      definitionDetailContentStore_ = new WorkflowContentStore<DefinitionDetail>(Get.metaContentService().<UUID,
            byte[]>openStore(WorkflowContentStoreType.DEFINITION_DETAIL.toString()),
            (bytes) -> (bytes == null) ? null
                                       : new DefinitionDetail(bytes));
      processDetailContentStore_ = new WorkflowContentStore<ProcessDetail>(Get.metaContentService().<UUID,
            byte[]>openStore(WorkflowContentStoreType.PROCESS_DEFINITION.toString()),
            (bytes) -> (bytes == null) ? null
                                       : new ProcessDetail(bytes));
      processHistoryContentStore_ = new WorkflowContentStore<ProcessHistory>(Get.metaContentService().<UUID,
            byte[]>openStore(WorkflowContentStoreType.HISTORICAL_WORKFLOW.toString()),
            (bytes) -> (bytes == null) ? null
                                       : new ProcessHistory(bytes));
      userRoleContentStore_ = LookupService.getService(UserRoleService.class);

      // this needs rework to load 1 (or more) BPMN2 Files from the classpath
      if (BPMN_PATH != null)  // Null is to support a test case where it doesn't want the file loaded by default
      {
         bpmnInfo_ = new Bpmn2FileImporter(BPMN_PATH, this).getBPMNInfo();
      }
   }

   @PreDestroy
   private void shutdown() {
      logger.debug("Shutting down the Workflow Provider");

      // This is a noop, the metacontent store properly shuts itself down
   }

   //~--- get methods ---------------------------------------------------------

   public WorkflowContentStore<AvailableAction> getAvailableActionStore() {
      if (availableActionContentStore_ == null) {
         reCacheStoreRefs();
      }

      return availableActionContentStore_;
   }

   public BPMNInfo getBPMNInfo() {
      if (bpmnInfo_ == null) {
         reCacheStoreRefs();
      }

      return bpmnInfo_;
   }

   public WorkflowContentStore<DefinitionDetail> getDefinitionDetailStore() {
      if (definitionDetailContentStore_ == null) {
         reCacheStoreRefs();
      }

      return definitionDetailContentStore_;
   }

   public WorkflowContentStore<ProcessDetail> getProcessDetailStore() {
      if (processDetailContentStore_ == null) {
         reCacheStoreRefs();
      }

      return processDetailContentStore_;
   }

   public WorkflowContentStore<ProcessHistory> getProcessHistoryStore() {
      if (processHistoryContentStore_ == null) {
         reCacheStoreRefs();
      }

      return processHistoryContentStore_;
   }

   public UserRoleService getUserRoleStore() {
      if (userRoleContentStore_ == null) {
         reCacheStoreRefs();
      }

      return userRoleContentStore_;
   }

   public WorkflowAccessor getWorkflowAccessor() {
      return LookupService.get()
                          .getService(WorkflowAccessor.class);
   }

   public WorkflowProcessInitializerConcluder getWorkflowProcessInitializerConcluder() {
      return LookupService.get()
                          .getService(WorkflowProcessInitializerConcluder.class);
   }

   public WorkflowUpdater getWorkflowUpdater() {
      return LookupService.get()
                          .getService(WorkflowUpdater.class);
   }
}

