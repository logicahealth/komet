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

import java.io.File;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.provider.workflow.WorkflowProvider;
import sh.isaac.provider.workflow.model.contents.ProcessDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.EndWorkflowType;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.ProcessStatus;
import sh.isaac.provider.workflow.model.contents.ProcessHistory;
import sh.isaac.provider.workflow.model.contents.ProcessHistory.ProcessHistoryComparator;
import sh.isaac.provider.workflow.user.RoleConfigurator;

//~--- classes ----------------------------------------------------------------

/**
 * Test the WorkflowProcessInitializerConcluder class
 *
 * {@link WorkflowProcessInitializerConcluder}.
 * {@link AbstractWorkflowProviderTestPackage}.
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class WorkflowProcessInitializerConcluderTest
        extends AbstractWorkflowProviderTestPackage {
   @Before
   public void beforeTest() {
      wp_.getProcessDetailStore()
         .clear();
      wp_.getProcessHistoryStore()
         .clear();
   }

   @AfterClass
   public static void tearDownClass()
            throws IOException {
      LookupService.shutdownSystem();
      RecursiveDelete.delete(new File("target/store"));
   }

   /**
    * Test cancelation of workflow process updates the process details as
    * expected and adds a process history entry as expected. Furthermore,
    * ensure that cannot cancel a process that has a) hasn't been created or b)
    * already been canceled
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testCancelWorkflowProcess()
            throws Exception {
      // Attempt to cancel a process that hasn't yet been created
      try {
         endWorkflowProcess(UUID.randomUUID(),
                            cancelAction,
                            RoleConfigurator.getFirstTestUser(),
                            CANCELED_WORKFLOW_COMMENT,
                            EndWorkflowType.CANCELED);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      final UUID processId = wp_.getWorkflowProcessInitializerConcluder()
                          .createWorkflowProcess(mainDefinitionId,
                                RoleConfigurator.getFirstTestUser(),
                                "Main Process Name",
                                "Main Process Description");

      Thread.sleep(1);
      addComponentsToProcess(processId, RoleConfigurator.getFirstTestUserSeq(), State.ACTIVE);
      executeSendForReviewAdvancement(processId);
      wp_.getWorkflowProcessInitializerConcluder()
         .launchProcess(processId);
      Thread.sleep(1);
      executeSendForApprovalAdvancement(processId);
      Thread.sleep(1);

      final SortedSet<ProcessHistory> hxEntries = new TreeSet<>(new ProcessHistoryComparator());

      hxEntries.addAll(wp_.getProcessHistoryStore()
                          .values());
      Assert.assertEquals(3, hxEntries.size());
      assertHistoryForProcess(hxEntries, processId);
      endWorkflowProcess(processId,
                         cancelAction,
                         RoleConfigurator.getFirstTestUser(),
                         CANCELED_WORKFLOW_COMMENT,
                         EndWorkflowType.CANCELED);
      assertProcessDefinition(ProcessStatus.CANCELED, mainDefinitionId, processId);
      hxEntries.clear();
      hxEntries.addAll(wp_.getProcessHistoryStore()
                          .values());
      assertCancelHistory(hxEntries.last(), processId);

      // Attempt to cancel an already launched process
      try {
         endWorkflowProcess(processId,
                            cancelAction,
                            RoleConfigurator.getFirstTestUser(),
                            CANCELED_WORKFLOW_COMMENT,
                            EndWorkflowType.CANCELED);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      // TODO: After complete the cancelation store creation & integration
   }

   /**
    * Test concluding of workflow process updates the process details as
    * expected and adds a process history entry as expected. Furthermore,
    * ensure that cannot conclude a process that has a) hasn't been created, b)
    * hasn't been launched, c) isn't at an end state or d) has already been
    * concluded
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testConcludeWorkflow()
            throws Exception {
      // Attempt to conclude a process that hasn't yet been created
      try {
         endWorkflowProcess(UUID.randomUUID(),
                            concludeAction,
                            RoleConfigurator.getFirstTestUser(),
                            CONCLUDED_WORKFLOW_COMMENT,
                            EndWorkflowType.CONCLUDED);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      final UUID processId = wp_.getWorkflowProcessInitializerConcluder()
                          .createWorkflowProcess(mainDefinitionId,
                                RoleConfigurator.getFirstTestUser(),
                                "Main Process Name",
                                "Main Process Description");

      Thread.sleep(1);

      // Attempt to conclude a process that hasn't yet been launched
      try {
         endWorkflowProcess(processId,
                            concludeAction,
                            RoleConfigurator.getFirstTestUser(),
                            CONCLUDED_WORKFLOW_COMMENT,
                            EndWorkflowType.CONCLUDED);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      addComponentsToProcess(processId, RoleConfigurator.getFirstTestUserSeq(), State.ACTIVE);
      executeSendForReviewAdvancement(processId);
      wp_.getWorkflowProcessInitializerConcluder()
         .launchProcess(processId);
      Thread.sleep(1);

      // Attempt to conclude a process that isn't at an end state
      try {
         endWorkflowProcess(processId,
                            concludeAction,
                            RoleConfigurator.getFirstTestUser(),
                            CONCLUDED_WORKFLOW_COMMENT,
                            EndWorkflowType.CONCLUDED);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      executeSendForApprovalAdvancement(processId);
      Thread.sleep(1);

      final SortedSet<ProcessHistory> hxEntries = new TreeSet<>(new ProcessHistoryComparator());

      hxEntries.addAll(wp_.getProcessHistoryStore()
                          .values());
      Assert.assertEquals(3, hxEntries.size());
      assertHistoryForProcess(hxEntries, processId);
      endWorkflowProcess(processId,
                         concludeAction,
                         RoleConfigurator.getFirstTestUser(),
                         CONCLUDED_WORKFLOW_COMMENT,
                         EndWorkflowType.CONCLUDED);
      assertProcessDefinition(ProcessStatus.CONCLUDED, mainDefinitionId, processId);
      hxEntries.clear();
      hxEntries.addAll(wp_.getProcessHistoryStore()
                          .values());
      assertConcludeHistory(hxEntries.last(), processId);

      // Attempt to cancel an already launched process
      try {
         endWorkflowProcess(processId,
                            concludeAction,
                            RoleConfigurator.getFirstTestUser(),
                            CONCLUDED_WORKFLOW_COMMENT,
                            EndWorkflowType.CONCLUDED);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }
   }

   /**
    * Test creation of workflow process creates the process details as expected
    * and adds a process history entry as expected
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testCreateWorkflowProcess()
            throws Exception {
      // Initialization
      final UUID processId = wp_.getWorkflowProcessInitializerConcluder()
                          .createWorkflowProcess(mainDefinitionId,
                                RoleConfigurator.getFirstTestUser(),
                                "Main Process Name",
                                "Main Process Description");

      addComponentsToProcess(processId, RoleConfigurator.getFirstTestUserSeq(), State.ACTIVE);

      // verify content in workflow is as expected
      assertProcessDefinition(ProcessStatus.DEFINED, mainDefinitionId, processId);

      final SortedSet<ProcessHistory> hxEntries = new TreeSet<>(new ProcessHistoryComparator());

      hxEntries.addAll(wp_.getProcessHistoryStore()
                          .values());
      Assert.assertEquals(1, hxEntries.size());
      assertHistoryForProcess(hxEntries, processId);
   }

   /**
    * Test launching of workflow process updates the process details as
    * expected and adds a process history entry as expected. Furthermore,
    * ensure that cannot launch a process that has a) hasn't been created or b)
    * already been launched
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testLaunchWorkflow()
            throws Exception {
      // Attempt to launch a process that hasn't yet been created
      try {
         wp_.getWorkflowProcessInitializerConcluder()
            .launchProcess(UUID.randomUUID());
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      final UUID processId = wp_.getWorkflowProcessInitializerConcluder()
                          .createWorkflowProcess(mainDefinitionId,
                                RoleConfigurator.getFirstTestUser(),
                                "Main Process Name",
                                "Main Process Description");

      Thread.sleep(1);
      addComponentsToProcess(processId, RoleConfigurator.getFirstTestUserSeq(), State.ACTIVE);
      executeSendForReviewAdvancement(processId);
      wp_.getWorkflowProcessInitializerConcluder()
         .launchProcess(processId);
      assertProcessDefinition(ProcessStatus.LAUNCHED, mainDefinitionId, processId);

      final SortedSet<ProcessHistory> hxEntries = new TreeSet<>(new ProcessHistoryComparator());

      hxEntries.addAll(wp_.getProcessHistoryStore()
                          .values());
      Assert.assertEquals(2, hxEntries.size());
      assertHistoryForProcess(hxEntries, processId);

      // Attempt to launch an already launched process
      try {
         wp_.getWorkflowProcessInitializerConcluder()
            .launchProcess(processId);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }
   }

   private void assertProcessDefinition(ProcessStatus processStatus, UUID definitionId, UUID processId) {
      final Set<ProcessDetail> detailEntries = new HashSet<>();

      detailEntries.addAll(wp_.getProcessDetailStore()
                              .values());

      final ProcessDetail entry = detailEntries.iterator()
                                         .next();

      Assert.assertEquals(processId, entry.getId());
      Assert.assertEquals(2, entry.getComponentToInitialEditMap()
                                  .keySet()
                                  .size());
      Assert.assertTrue(entry.getComponentToInitialEditMap()
                             .keySet()
                             .contains(-55));
      Assert.assertEquals(processStatus, entry.getStatus());
      Assert.assertNotNull(entry.getCreatorId());
      Assert.assertEquals(definitionId, entry.getDefinitionId());
      Assert.assertTrue(entry.getComponentToInitialEditMap()
                             .keySet()
                             .contains(-56));
      Assert.assertTrue(timeSinceYesterdayBeforeTomorrow(entry.getTimeCreated()));

      if (processStatus == ProcessStatus.DEFINED) {
         Assert.assertEquals(-1L, entry.getTimeLaunched());
         Assert.assertEquals(-1L, entry.getTimeCanceledOrConcluded());
      } else {
         Assert.assertTrue(timeSinceYesterdayBeforeTomorrow(entry.getTimeLaunched()));

         if (processStatus == ProcessStatus.LAUNCHED) {
            Assert.assertEquals(-1L, entry.getTimeCanceledOrConcluded());
         } else {
            Assert.assertTrue(timeSinceYesterdayBeforeTomorrow(entry.getTimeCanceledOrConcluded()));
         }
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the up.
    */
   @BeforeClass
   public static void setUpClass() {
      WorkflowProvider.BPMN_PATH = BPMN_FILE_PATH;
      LookupService.getService(ConfigurationService.class)
                   .setDataStoreFolderPath(new File("target/store").toPath());
      LookupService.startupMetadataStore();
      globalSetup();
   }
}

