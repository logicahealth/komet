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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
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
import sh.isaac.api.UserRole;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.provider.workflow.WorkflowProvider;
import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.DefinitionDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.ProcessStatus;
import sh.isaac.provider.workflow.model.contents.ProcessHistory;
import sh.isaac.provider.workflow.user.RoleConfigurator;

//~--- classes ----------------------------------------------------------------

/**
 * Test the WorkflowAccessor class
 *
 * {@link WorkflowAccessor}. {@link AbstractWorkflowProviderTestPackage}.
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class WorkflowAccessorTest
        extends AbstractWorkflowProviderTestPackage {
   /**
    * Before test.
    */
   @Before
   public void beforeTest() {
      wp_.getProcessDetailStore()
         .clear();
      wp_.getProcessHistoryStore()
         .clear();
   }

   /**
    * Tear down class.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @AfterClass
   public static void tearDownClass()
            throws IOException {
      LookupService.shutdownSystem();
      RecursiveDelete.delete(new File("target/store"));
   }

   /**
    * Test that as advance workflow, different process information is
    * associated with the process.
    *
    * @throws Exception             Thrown if test fails
    */
   @Test
   public void testGetAdvanceableProcessInformation()
            throws Exception {
      Map<ProcessDetail, SortedSet<ProcessHistory>> info = wp_.getWorkflowAccessor()
                                                              .getAdvanceableProcessInformation(mainDefinitionId,
                                                                    RoleConfigurator.getFirstTestUser());

      Assert.assertEquals(0, info.size());
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, info.size());

      // Create first process-Main definition (role is Editor)
      final UUID firstProcessId = createFirstWorkflowProcess(mainDefinitionId);

      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(1, info.size());
      Assert.assertEquals(info.keySet()
                              .iterator()
                              .next(), wp_.getWorkflowAccessor()
                                    .getProcessDetails(firstProcessId));
      Assert.assertEquals(info.get(info.keySet()
                                       .iterator()
                                       .next()),
                          wp_.getWorkflowAccessor()
                             .getProcessHistory(firstProcessId));
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, info.size());

      // Launch workflow and send for review (role is reviewer)
      executeLaunchWorkflow(firstProcessId);
      executeSendForReviewAdvancement(firstProcessId);
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(0, info.size());
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(1, info.size());
      Assert.assertEquals(info.keySet()
                              .iterator()
                              .next(), wp_.getWorkflowAccessor()
                                    .getProcessDetails(firstProcessId));
      Assert.assertEquals(info.get(info.keySet()
                                       .iterator()
                                       .next()),
                          wp_.getWorkflowAccessor()
                             .getProcessHistory(firstProcessId));

      // Make workflow ready for review (role is Approver)
      executeSendForApprovalAdvancement(firstProcessId);
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(1, info.size());
      Assert.assertEquals(info.keySet()
                              .iterator()
                              .next(), wp_.getWorkflowAccessor()
                                    .getProcessDetails(firstProcessId));
      Assert.assertEquals(info.get(info.keySet()
                                       .iterator()
                                       .next()),
                          wp_.getWorkflowAccessor()
                             .getProcessHistory(firstProcessId));
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, info.size());

      // Create second process-Main definition. (first process role is
      // Approver and second process role is Editor)
      final UUID secondProcessId = createSecondWorkflowProcess(mainDefinitionId);

      // Create testing collections
      Set<ProcessDetail> mainDefProcesses =
         new HashSet<>(Arrays.asList(wp_.getWorkflowAccessor().getProcessDetails(firstProcessId),
                                     wp_.getWorkflowAccessor().getProcessDetails(secondProcessId)));
      Set<SortedSet<ProcessHistory>> mainDefProcessHistory =
         new HashSet<>(Arrays.asList(wp_.getWorkflowAccessor().getProcessHistory(firstProcessId),
                                     wp_.getWorkflowAccessor().getProcessHistory(secondProcessId)));

      // test
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(2, info.size());
      Assert.assertEquals(info.keySet(), mainDefProcesses);

      for (final ProcessDetail process: info.keySet()) {
         Assert.assertTrue(mainDefProcessHistory.contains(info.get(process)));
      }

      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, info.size());

      // Reject first process ready for edit (role is editor for both)
      executeRejectReviewAdvancement(firstProcessId);

      // Create testing collections
      mainDefProcesses = new HashSet<>(Arrays.asList(wp_.getWorkflowAccessor().getProcessDetails(firstProcessId),
            wp_.getWorkflowAccessor().getProcessDetails(secondProcessId)));
      mainDefProcessHistory = new HashSet<>(Arrays.asList(wp_.getWorkflowAccessor().getProcessHistory(firstProcessId),
            wp_.getWorkflowAccessor().getProcessHistory(secondProcessId)));

      // test
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(2, info.size());
      Assert.assertEquals(info.keySet(), mainDefProcesses);

      for (final ProcessDetail process: info.keySet()) {
         Assert.assertTrue(mainDefProcessHistory.contains(info.get(process)));
      }

      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, info.size());

      // Cancel second process (role is editor for first and no role for
      // second) and
      cancelWorkflow(secondProcessId);
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(1, info.size());
      Assert.assertEquals(info.keySet()
                              .iterator()
                              .next(), wp_.getWorkflowAccessor()
                                    .getProcessDetails(firstProcessId));
      Assert.assertEquals(info.get(info.keySet()
                                       .iterator()
                                       .next()),
                          wp_.getWorkflowAccessor()
                             .getProcessHistory(firstProcessId));
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, info.size());

      // Create third process on second definition.
      // Thus mainDef: (role is editor for first and no role for second) and
      // secondDef: (role is
      // editor)
      final UUID secondDefinitionId = createSecondaryDefinition();

      // test first definition
      final UUID thirdProcessId = createFirstWorkflowProcess(secondDefinitionId);

      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(1, info.size());
      Assert.assertEquals(info.keySet()
                              .iterator()
                              .next(), wp_.getWorkflowAccessor()
                                    .getProcessDetails(firstProcessId));
      Assert.assertEquals(info.get(info.keySet()
                                       .iterator()
                                       .next()),
                          wp_.getWorkflowAccessor()
                             .getProcessHistory(firstProcessId));
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(mainDefinitionId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, info.size());

      // test second definition
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(secondDefinitionId, RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(1, info.size());
      Assert.assertEquals(info.keySet()
                              .iterator()
                              .next(), wp_.getWorkflowAccessor()
                                    .getProcessDetails(thirdProcessId));
      Assert.assertEquals(info.get(info.keySet()
                                       .iterator()
                                       .next()),
                          wp_.getWorkflowAccessor()
                             .getProcessHistory(thirdProcessId));
      info = wp_.getWorkflowAccessor()
                .getAdvanceableProcessInformation(secondDefinitionId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, info.size());
      wp_.getDefinitionDetailStore()
         .remove(secondDefinitionId);
   }

   /**
    * Test able to properly access definition details.
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testGetDefinitionDetails()
            throws Exception {
      final DefinitionDetail entry         = wp_.getWorkflowAccessor()
                                                .getDefinitionDetails(mainDefinitionId);
      final Set<UserRole>    expectedRoles = new HashSet<>();

      expectedRoles.add(UserRole.EDITOR);
      expectedRoles.add(UserRole.REVIEWER);
      expectedRoles.add(UserRole.APPROVER);
      expectedRoles.add(UserRole.AUTOMATED);
      Assert.assertEquals(entry.getBpmn2Id(), "VetzWorkflow");
      Assert.assertEquals(entry.getName(), "VetzWorkflow");
      Assert.assertEquals(entry.getNamespace(), "org.jbpm");
      Assert.assertEquals(entry.getVersion(), "1.2");
      Assert.assertEquals(entry.getRoles(), expectedRoles);
   }

   /**
    * Test able to properly access process details.
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testGetProcessDetails()
            throws Exception {
      final UUID    processId = createFirstWorkflowProcess(mainDefinitionId);
      ProcessDetail entry     = wp_.getWorkflowAccessor()
                                   .getProcessDetails(processId);

      Assert.assertEquals(processId, entry.getId());
      Assert.assertEquals(ProcessStatus.DEFINED, entry.getStatus());
      Assert.assertNotNull(entry.getCreatorId());
      Assert.assertEquals(mainDefinitionId, entry.getDefinitionId());
      Assert.assertTrue(timeSinceYesterdayBeforeTomorrow(entry.getTimeCreated()));
      Assert.assertEquals(-1L, entry.getTimeCanceledOrConcluded());
      Assert.assertEquals(0, entry.getComponentToInitialEditMap()
                                  .keySet()
                                  .size());
      addComponentsToProcess(processId, RoleConfigurator.getFirstTestUserSeq(), State.ACTIVE);
      entry = wp_.getWorkflowAccessor()
                 .getProcessDetails(processId);
      Assert.assertEquals(2, entry.getComponentToInitialEditMap()
                                  .keySet()
                                  .size());
      Assert.assertTrue(entry.getComponentToInitialEditMap()
                             .keySet()
                             .contains(-55));
      Assert.assertTrue(entry.getComponentToInitialEditMap()
                             .keySet()
                             .contains(-56));
      executeLaunchWorkflow(processId);
      entry = wp_.getWorkflowAccessor()
                 .getProcessDetails(processId);
      Assert.assertEquals(ProcessStatus.LAUNCHED, entry.getStatus());
   }

   /**
    * Test able to properly access process history. While doing so, advance
    * workflow such that history expands. Keep testing after each advancement.
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testGetProcessHistory()
            throws Exception {
      final UUID                processId      = createFirstWorkflowProcess(mainDefinitionId);
      SortedSet<ProcessHistory> processHistory = wp_.getWorkflowAccessor()
                                                    .getProcessHistory(processId);

      Assert.assertEquals(1, processHistory.size());
      assertHistoryForProcess(processHistory, processId);
      executeLaunchWorkflow(processId);
      executeSendForReviewAdvancement(processId);
      processHistory = wp_.getWorkflowAccessor()
                          .getProcessHistory(processId);
      Assert.assertEquals(2, processHistory.size());
      assertHistoryForProcess(processHistory, processId);
      executeSendForApprovalAdvancement(processId);
      processHistory = wp_.getWorkflowAccessor()
                          .getProcessHistory(processId);
      Assert.assertEquals(3, processHistory.size());
      assertHistoryForProcess(processHistory, processId);
      concludeWorkflow(processId);
      processHistory = wp_.getWorkflowAccessor()
                          .getProcessHistory(processId);
      Assert.assertEquals(4, processHistory.size());
      assertHistoryForProcess(processHistory, processId);
      assertConcludeHistory(processHistory.last(), processId);
   }

   /**
    * Test that as advance workflow, different users are able to advance
    * workflow based on the user roles and the process's current state.
    *
    * @throws Exception             Thrown if test fails
    */
   @Test
   public void testGetUserPermissibleActionsForProcess()
            throws Exception {
      final UUID firstProcessId = createFirstWorkflowProcess(mainDefinitionId);

      // Create Process (Is in Ready_To_Edit State)
      Set<AvailableAction> firstProcessFirstUserActions = wp_.getWorkflowAccessor()
                                                             .getUserPermissibleActionsForProcess(firstProcessId,
                                                                   RoleConfigurator.getFirstTestUser());

      Assert.assertEquals(2, firstProcessFirstUserActions.size());

      for (final AvailableAction act: firstProcessFirstUserActions) {
         Assert.assertEquals(mainDefinitionId, act.getDefinitionId());
         Assert.assertEquals("Ready for Edit", act.getInitialState());
         Assert.assertEquals(UserRole.EDITOR, act.getRole());

         if (act.getAction()
                .equals("Edit")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Ready for Review"));
         } else if (act.getAction()
                       .equals("Cancel Workflow")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Canceled During Edit"));
         } else {
            Assert.fail();
         }
      }

      Set<AvailableAction> firstProcessSecondUserActions = wp_.getWorkflowAccessor()
                                                              .getUserPermissibleActionsForProcess(firstProcessId,
                                                                    RoleConfigurator.getSecondTestUser());

      Assert.assertEquals(0, firstProcessSecondUserActions.size());

      // Launch Process and send for review (Is in Ready_To_Review State)
      executeLaunchWorkflow(firstProcessId);
      executeSendForReviewAdvancement(firstProcessId);
      firstProcessFirstUserActions = wp_.getWorkflowAccessor()
                                        .getUserPermissibleActionsForProcess(firstProcessId,
                                              RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(0, firstProcessFirstUserActions.size());
      firstProcessSecondUserActions = wp_.getWorkflowAccessor()
                                         .getUserPermissibleActionsForProcess(firstProcessId,
                                               RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(3, firstProcessSecondUserActions.size());

      for (final AvailableAction act: firstProcessSecondUserActions) {
         Assert.assertEquals(mainDefinitionId, act.getDefinitionId());
         Assert.assertEquals("Ready for Review", act.getInitialState());
         Assert.assertEquals(UserRole.REVIEWER, act.getRole());

         if (act.getAction()
                .equals("QA Passes")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Ready for Approve"));
         } else if (act.getAction()
                       .equals("QA Fails")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Ready for Edit"));
         } else if (act.getAction()
                       .equals("Cancel Workflow")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Canceled During Review"));
         } else {
            Assert.fail();
         }
      }

      // Create Process (Is in Ready_To_Edit State)
      final UUID secondProcessId = createFirstWorkflowProcess(mainDefinitionId);
      Set<AvailableAction> secondProcessFirstUserActions = wp_.getWorkflowAccessor()
                                                              .getUserPermissibleActionsForProcess(secondProcessId,
                                                                    RoleConfigurator.getFirstTestUser());

      Assert.assertEquals(2, secondProcessFirstUserActions.size());

      for (final AvailableAction act: secondProcessFirstUserActions) {
         Assert.assertEquals(mainDefinitionId, act.getDefinitionId());
         Assert.assertEquals("Ready for Edit", act.getInitialState());
         Assert.assertEquals(UserRole.EDITOR, act.getRole());

         if (act.getAction()
                .equals("Edit")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Ready for Review"));
         } else if (act.getAction()
                       .equals("Cancel Workflow")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Canceled During Edit"));
         } else {
            Assert.fail();
         }
      }

      Set<AvailableAction> secondProcessSecondUserActions = wp_.getWorkflowAccessor()
                                                               .getUserPermissibleActionsForProcess(secondProcessId,
                                                                     RoleConfigurator.getSecondTestUser());

      Assert.assertEquals(0, secondProcessSecondUserActions.size());

      // Verify the first process hasn't changed
      Assert.assertEquals(firstProcessFirstUserActions,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(firstProcessId, RoleConfigurator.getFirstTestUser()));
      Assert.assertEquals(firstProcessSecondUserActions,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(firstProcessId,
                                   RoleConfigurator.getSecondTestUser()));
      Assert.assertEquals(0, firstProcessFirstUserActions.size());
      Assert.assertEquals(3, firstProcessSecondUserActions.size());

      // Launch second Process and send for review (Is in Ready_To_Review
      // State)
      executeLaunchWorkflow(secondProcessId);
      executeSendForReviewAdvancement(secondProcessId);
      secondProcessFirstUserActions = wp_.getWorkflowAccessor()
                                         .getUserPermissibleActionsForProcess(secondProcessId,
                                               RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(0, secondProcessFirstUserActions.size());
      secondProcessSecondUserActions = wp_.getWorkflowAccessor()
            .getUserPermissibleActionsForProcess(secondProcessId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(3, secondProcessSecondUserActions.size());

      for (final AvailableAction act: secondProcessSecondUserActions) {
         Assert.assertEquals(mainDefinitionId, act.getDefinitionId());
         Assert.assertEquals("Ready for Review", act.getInitialState());
         Assert.assertEquals(UserRole.REVIEWER, act.getRole());

         if (act.getAction()
                .equals("QA Passes")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Ready for Approve"));
         } else if (act.getAction()
                       .equals("QA Fails")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Ready for Edit"));
         } else if (act.getAction()
                       .equals("Cancel Workflow")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Canceled During Review"));
         } else {
            Assert.fail();
         }
      }

      // Verify the first process hasn't changed
      Assert.assertEquals(firstProcessFirstUserActions,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(firstProcessId, RoleConfigurator.getFirstTestUser()));
      Assert.assertEquals(firstProcessSecondUserActions,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(firstProcessId,
                                   RoleConfigurator.getSecondTestUser()));
      Assert.assertEquals(0, firstProcessFirstUserActions.size());
      Assert.assertEquals(3, firstProcessSecondUserActions.size());

      // Send second process for approval (At Ready for Approve State)
      executeSendForApprovalAdvancement(secondProcessId);
      secondProcessSecondUserActions = wp_.getWorkflowAccessor()
            .getUserPermissibleActionsForProcess(secondProcessId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, secondProcessSecondUserActions.size());
      secondProcessFirstUserActions = wp_.getWorkflowAccessor()
                                         .getUserPermissibleActionsForProcess(secondProcessId,
                                               RoleConfigurator.getFirstTestUser());
      Assert.assertEquals(4, secondProcessFirstUserActions.size());

      for (final AvailableAction act: secondProcessFirstUserActions) {
         Assert.assertEquals(mainDefinitionId, act.getDefinitionId());
         Assert.assertEquals("Ready for Approve", act.getInitialState());
         Assert.assertEquals(UserRole.APPROVER, act.getRole());

         if (act.getAction()
                .equals("Reject Review")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Ready for Review"));
         } else if (act.getAction()
                       .equals("Reject Edit")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Ready for Edit"));
         } else if (act.getAction()
                       .equals("Approve")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Modeling Review Complete"));
         } else if (act.getAction()
                       .equals("Cancel Workflow")) {
            Assert.assertTrue(act.getOutcomeState()
                                 .equals("Canceled During Approval"));
         } else {
            Assert.fail();
         }
      }

      secondProcessSecondUserActions = wp_.getWorkflowAccessor()
            .getUserPermissibleActionsForProcess(secondProcessId, RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(0, secondProcessSecondUserActions.size());

      // Verify the first process hasn't changed
      Assert.assertEquals(firstProcessFirstUserActions,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(firstProcessId, RoleConfigurator.getFirstTestUser()));
      Assert.assertEquals(firstProcessSecondUserActions,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(firstProcessId,
                                   RoleConfigurator.getSecondTestUser()));
      Assert.assertEquals(0, firstProcessFirstUserActions.size());
      Assert.assertEquals(3, firstProcessSecondUserActions.size());

      // Cancel first process so should have zero roles available
      cancelWorkflow(firstProcessId);
      Assert.assertEquals(0,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(firstProcessId, RoleConfigurator.getFirstTestUser())
                             .size());
      Assert.assertEquals(0,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(firstProcessId, RoleConfigurator.getSecondTestUser())
                             .size());

      // Verify the second process hasn't changed
      Assert.assertEquals(secondProcessFirstUserActions,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(secondProcessId,
                                   RoleConfigurator.getFirstTestUser()));
      Assert.assertEquals(secondProcessSecondUserActions,
                          wp_.getWorkflowAccessor()
                             .getUserPermissibleActionsForProcess(secondProcessId,
                                   RoleConfigurator.getSecondTestUser()));
      Assert.assertEquals(0, firstProcessFirstUserActions.size());
      Assert.assertEquals(3, firstProcessSecondUserActions.size());
   }

   /**
    * Test able to properly access the user roles as expected. Do this for
    * multiple users as each contains different roles as defined in the
    * AbstractWorkflowProviderTestPackage.
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testGetUserRoles()
            throws Exception {
      Set<UserRole> roles = wp_.getUserRoleStore()
                               .getUserRoles(RoleConfigurator.getFirstTestUser());

      Assert.assertEquals(2, roles.size());

      for (final UserRole role: roles) {
         Assert.assertTrue((role == UserRole.EDITOR) || (role == UserRole.APPROVER));
      }

      roles = wp_.getUserRoleStore()
                 .getUserRoles(RoleConfigurator.getSecondTestUser());
      Assert.assertEquals(1, roles.size());

      final UserRole role = roles.iterator()
                                 .next();

      Assert.assertEquals(UserRole.REVIEWER, role);
   }

   /**
    * Cannot make this work without at least a Mock Database. Added to
    * Integration-Test module's workflowFramworkTest. For now just pass.
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testIsComponentInActiveWorkflow()
            throws Exception {
      Assert.assertTrue(true);
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

