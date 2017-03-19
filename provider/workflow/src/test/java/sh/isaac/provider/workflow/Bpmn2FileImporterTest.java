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

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.LookupService;
import sh.isaac.api.UserRole;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.provider.workflow.crud.AbstractWorkflowProviderTestPackage;
import sh.isaac.provider.workflow.model.WorkflowContentStore;
import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.DefinitionDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.EndWorkflowType;

//~--- classes ----------------------------------------------------------------

/**
 * Test the Bpmn2FileImporter class
 *
 * {@link Bpmn2FileImporter} {@link AbstractWorkflowProviderTestPackage}.
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class Bpmn2FileImporterTest
        extends AbstractWorkflowProviderTestPackage {
   /**
    * Before test.
    */
   @Before
   public void beforeTest() {
      wp.getProcessDetailStore()
         .clear();
      wp.getProcessHistoryStore()
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
    * Test proper definition metadata following import of a bpmn2 file.
    *
    * @throws Exception             Thrown if test fails
    */
   @Test
   public void testImportBpmn2FileMetadata()
            throws Exception {
      final WorkflowContentStore<DefinitionDetail> createdDefinitionDetailContentStore = LookupService.get()
                                                                                                      .getService(
                                                                                                         WorkflowProvider.class)
                                                                                                      .getDefinitionDetailStore();

      Assert.assertSame("Expected number of actionOutome records not what expected",
                        createdDefinitionDetailContentStore.size(),
                        1);

      final DefinitionDetail entry         = createdDefinitionDetailContentStore.values()
                                                                                .iterator()
                                                                                .next();
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
    * Test proper available actions following import of a bpmn2 file.
    *
    * @throws Exception             Thrown if test fails
    */
   @Test
   public void testStaticBpmnAvailableActions()
            throws Exception {
      final Map<String, Set<AvailableAction>> actionMap = new HashMap<>();

      for (final AvailableAction action: LookupService.get()
            .getService(WorkflowProvider.class)
            .getAvailableActionStore()
            .values()) {
         if (!actionMap.containsKey(action.getInitialState())) {
            actionMap.put(action.getInitialState(), new HashSet<AvailableAction>());
         }

         actionMap.get(action.getInitialState())
                  .add(action);
      }

      for (final String initState: actionMap.keySet()) {
         if (initState.equals("Assigned")) {
            assertAssignedActions(actionMap.get(initState));
         } else if (initState.equals("Ready for Edit")) {
            assertReadyForEditActions(actionMap.get(initState));
         } else if (initState.equals("Ready for Review")) {
            assertReadyForReviewActions(actionMap.get(initState));
         } else if (initState.equals("Ready for Approval")) {
            assertReadyForApprovalActions(actionMap.get(initState));
         }
      }
   }

   /**
    * Test proper definition nodes following import of a bpmn2 file.
    *
    * @throws Exception             Thrown if test fails
    */
   @Test
   public void testStaticBpmnSetNodes()
            throws Exception {
      final WorkflowContentStore<DefinitionDetail> createdDefinitionDetailContentStore = LookupService.get()
                                                                                                      .getService(
                                                                                                         WorkflowProvider.class)
                                                                                                      .getDefinitionDetailStore();
      final WorkflowContentStore<AvailableAction> createdAvailableActionContentStore = LookupService.get()
                                                                                                    .getService(
                                                                                                       WorkflowProvider.class)
                                                                                                    .getAvailableActionStore();

      Assert.assertSame("Expected number of actionOutome records not what expected",
                        createdAvailableActionContentStore.size(),
                        10);

      final DefinitionDetail definitionDetails = createdDefinitionDetailContentStore.values()
                                                                                    .iterator()
                                                                                    .next();
      final List<String> possibleActions = Arrays.asList("Cancel Workflow",
                                                         "Edit",
                                                         "QA Fails",
                                                         "QA Passes",
                                                         "Approve",
                                                         "Reject Edit",
                                                         "Reject Review",
                                                         "Create Workflow Process");
      final List<String> possibleStates = Arrays.asList("Assigned",
                                                        "Canceled During Edit",
                                                        "Canceled During Review",
                                                        "Canceled During Approval",
                                                        "Ready for Edit",
                                                        "Ready for Approve",
                                                        "Modeling Review Complete",
                                                        "Ready for Review");
      final Set<AvailableAction> identifiedCanceledActions  = new HashSet<>();
      final Set<AvailableAction> identifiedConcludedActions = new HashSet<>();
      final Set<AvailableAction> identifiedStartTypeActions = new HashSet<>();
      final Set<String>          identifiedEditingActions   = new HashSet<>();

      for (final AvailableAction entry: createdAvailableActionContentStore.values()) {
         if (entry.getAction()
                  .equals("Cancel Workflow")) {
            identifiedCanceledActions.add(entry);
         } else if (entry.getAction()
                         .equals("Approve")) {
            identifiedConcludedActions.add(entry);
         } else if (entry.getAction()
                         .equals("Edit")) {
            identifiedEditingActions.add(entry.getInitialState());
         } else if (entry.getInitialState()
                         .equals("Assigned")) {
            identifiedStartTypeActions.add(entry);
         }

         Assert.assertEquals(definitionDetails.getId(), entry.getDefinitionId());
         Assert.assertTrue(definitionDetails.getRoles()
                                            .contains(entry.getRole()));
         Assert.assertTrue(possibleStates.contains(entry.getOutcomeState()));
         Assert.assertTrue(possibleStates.contains(entry.getInitialState()));
         Assert.assertTrue(possibleActions.contains(entry.getAction()));
      }

      final Set<AvailableAction> concludedActions = LookupService.get()
                                                                 .getService(WorkflowProvider.class)
                                                                 .getBPMNInfo()
                                                                 .getEndWorkflowTypeMap()
                                                                 .get(EndWorkflowType.CONCLUDED);
      final Set<AvailableAction> canceledActions = LookupService.get()
                                                                .getService(WorkflowProvider.class)
                                                                .getBPMNInfo()
                                                                .getEndWorkflowTypeMap()
                                                                .get(EndWorkflowType.CANCELED);

      Assert.assertEquals(canceledActions, identifiedCanceledActions);
      Assert.assertEquals(concludedActions, identifiedConcludedActions);

      final Map<UUID, Set<AvailableAction>> defStartMap = LookupService.get()
                                                                       .getService(WorkflowProvider.class)
                                                                       .getBPMNInfo()
                                                                       .getDefinitionStartActionMap();

      Assert.assertEquals(defStartMap.keySet()
                                     .size(), 1);
      Assert.assertEquals(defStartMap.size(), identifiedStartTypeActions.size());
      Assert.assertEquals(defStartMap.keySet()
                                     .iterator()
                                     .next(), definitionDetails.getId());
      Assert.assertEquals(defStartMap.get(definitionDetails.getId()), identifiedStartTypeActions);
      Assert.assertEquals(LookupService.get()
                                       .getService(WorkflowProvider.class)
                                       .getBPMNInfo()
                                       .getEditStatesMap()
                                       .get(definitionDetails.getId()),
                          identifiedEditingActions);
   }

   /**
    * Assert assigned actions.
    *
    * @param actions the actions
    */
   private void assertAssignedActions(Set<AvailableAction> actions) {
      Assert.assertEquals(1, actions.size());
      Assert.assertEquals("Create Workflow Process", actions.iterator()
            .next()
            .getAction());
      Assert.assertEquals("Ready for Edit", actions.iterator()
            .next()
            .getOutcomeState());
      Assert.assertEquals(UserRole.AUTOMATED, actions.iterator()
            .next()
            .getRole());
   }

   /**
    * Assert ready for approval actions.
    *
    * @param actions the actions
    */
   private void assertReadyForApprovalActions(Set<AvailableAction> actions) {
      Assert.assertEquals(4, actions.size());

      for (final AvailableAction act: actions) {
         Assert.assertEquals(UserRole.APPROVER, act.getRole());

         if (act.getAction()
                .equals("Cancel Workflow")) {
            Assert.assertEquals("Canceled During Approval", act.getOutcomeState());
         } else if (act.getAction()
                       .equals("Reject Edit")) {
            Assert.assertEquals("Ready for Edit", act.getOutcomeState());
         } else if (act.getAction()
                       .equals("Reject Review")) {
            Assert.assertEquals("Reject Review", act.getOutcomeState());
         } else if (act.getAction()
                       .equals("Approve")) {
            Assert.assertEquals("Modeling Review Complete", act.getOutcomeState());
         } else {
            Assert.fail();
         }
      }
   }

   /**
    * Assert ready for edit actions.
    *
    * @param actions the actions
    */
   private void assertReadyForEditActions(Set<AvailableAction> actions) {
      Assert.assertEquals(2, actions.size());

      for (final AvailableAction act: actions) {
         Assert.assertEquals(UserRole.EDITOR, act.getRole());

         if (act.getAction()
                .equals("Cancel Workflow")) {
            Assert.assertEquals("Canceled During Edit", act.getOutcomeState());
         } else if (act.getAction()
                       .equals("Edit")) {
            Assert.assertEquals("Ready for Review", act.getOutcomeState());
         } else {
            Assert.fail();
         }
      }
   }

   /**
    * Assert ready for review actions.
    *
    * @param actions the actions
    */
   private void assertReadyForReviewActions(Set<AvailableAction> actions) {
      Assert.assertEquals(3, actions.size());

      for (final AvailableAction act: actions) {
         Assert.assertEquals(UserRole.REVIEWER, act.getRole());

         if (act.getAction()
                .equals("Cancel Workflow")) {
            Assert.assertEquals("Canceled During Review", act.getOutcomeState());
         } else if (act.getAction()
                       .equals("QA Fails")) {
            Assert.assertEquals("Ready for Edit", act.getOutcomeState());
         } else if (act.getAction()
                       .equals("QA Passes")) {
            Assert.assertEquals("Ready for Approve", act.getOutcomeState());
         } else {
            Assert.fail();
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

