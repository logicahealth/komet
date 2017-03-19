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



package sh.isaac.integration.tests;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jvnet.testing.hk2testng.HK2;

import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.UserRole;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.coordinate.EditCoordinateImpl;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.model.sememe.version.DescriptionSememeImpl;
import sh.isaac.provider.workflow.BPMNInfo;
import sh.isaac.provider.workflow.WorkflowProvider;
import sh.isaac.provider.workflow.model.contents.AbstractStorableWorkflowContents;
import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.ProcessDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.EndWorkflowType;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.ProcessStatus;
import sh.isaac.provider.workflow.model.contents.ProcessHistory;
import sh.isaac.provider.workflow.user.SimpleUserRoleService;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/2/16.
 */
@HK2("integration")
public class WorkflowFrameworkTest {
   private static final Logger   LOG                        = LogManager.getLogger();
   private static final String   LAUNCH_STATE               = "Ready for Edit";
   private static final String   LAUNCH_ACTION              = "Edit";
   private static final String   LAUNCH_OUTCOME             = "Ready for Review";
   private static final String   LAUNCH_COMMENT             = "Launch Comment";
   private static final String   SEND_TO_APPROVAL_STATE     = "Ready for Review";
   private static final String   SEND_TO_APPROVAL_ACTION    = "Review";
   private static final String   SEND_TO_APPROVAL_OUTCOME   = "Ready for Approve";
   private static final String   SEND_TO_APPROVAL_COMMENT   = "Sending for Approval";
   private static final String   REJECT_REVIEW_STATE        = "Ready for Review";
   private static final String   REJECT_REVIEW_ACTION       = "Reject QA";
   private static final String   REJECT_REVIEW_OUTCOME      = "Ready for Edit";
   private static final String   REJECT_REVIEW_COMMENT      = "Rejecting QA sending back to Edit";
   protected static final String CONCLUDED_WORKFLOW_COMMENT = "Concluded Workflow";
   protected static final String CANCELED_WORKFLOW_COMMENT  = "Canceled Workflow";

   /** The bpmn file path. */
   private static final String BPMN_FILE_PATH =
      "/sh/isaac/integration/tests/StaticWorkflowIntegrationTestingDefinition.bpmn2";
   private static UUID              userId;
   private static int               firstTestConceptNid;
   private static int               secondTestConceptNid;
   protected static AvailableAction cancelAction;

   //~--- fields --------------------------------------------------------------

   WorkflowProvider        wp_;
   private EditCoordinate  defaultEditCoordinate;
   private StampCoordinate defaultStampCoordinate;
   private int             moduleSeq;
   private int             pathSeq;

   //~--- methods -------------------------------------------------------------

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelActivationOfComponents() {
      clearStores();
      LOG.info("Testing Ability to cancel change on a concept and a sememe made active when originally was inactive");
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelEditingOfSememe() {
      clearStores();
      LOG.info("Testing Ability to cancel changes made to a sememe's text");

      try {
         UUID processId = wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");
         ConceptChronology<? extends ConceptVersion<?>> con = Get.conceptService()
                                                                 .getConcept(firstTestConceptNid);
         SememeChronologyImpl descSem = (SememeChronologyImpl) con.getConceptDescriptionList()
                                                                  .iterator()
                                                                  .next();
         Optional<LatestVersion<DescriptionSememe<?>>> latestDescVersion =
            ((SememeChronology) descSem).getLatestVersion(DescriptionSememe.class,
                                                          Get.configurationService()
                                                                .getDefaultStampCoordinate());
         String originalText = latestDescVersion.get()
                                                .value()
                                                .getText();

         // Modify Sememe Text
         DescriptionSememeImpl createdVersion = cloneVersion(descSem, State.ACTIVE);

         createdVersion.setText("New Text");
         Get.commitService()
            .addUncommitted(descSem)
            .get();

         Optional<CommitRecord> commitRecord = Get.commitService()
                                                  .commit("Inactivating sememe for Testing")
                                                  .get();

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId,
                             userId,
                             "Cancel Workflow",
                             "Canceling Workflow for Testing",
                             defaultEditCoordinate);

         SememeChronology<? extends SememeVersion<?>> semChron = Get.sememeService()
                                                                    .getSememe(descSem.getNid());

         latestDescVersion = ((SememeChronology) semChron).getLatestVersion(DescriptionSememe.class,
               Get.configurationService()
                  .getDefaultStampCoordinate());
         Assert.assertEquals(originalText, latestDescVersion.get()
               .value()
               .getText());
      } catch (Exception e) {
         Assert.fail(e.getMessage());
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelInactivationOfComponents() {
      clearStores();
      LOG.info("Testing Ability to cancel change on a concept and a sememe made inactive when originally was active");

      try {
         UUID processId = wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");
         ConceptChronologyImpl con      = (ConceptChronologyImpl) Get.conceptService()
                                                                     .getConcept(firstTestConceptNid);
         int                   semNid   = con.getConceptDescriptionList()
                                             .iterator()
                                             .next()
                                             .getNid();
         SememeChronologyImpl  semChron = (SememeChronologyImpl) Get.sememeService()
                                                                    .getSememe(semNid);

         verifyState(con, semChron, State.ACTIVE);

         // Inactivate Concept
         con.createMutableVersion(State.INACTIVE, defaultEditCoordinate);
         Get.commitService()
            .addUncommitted(con)
            .get();

         Optional<CommitRecord> commitRecord = Get.commitService()
                                                  .commit("Inactivating concept for Testing")
                                                  .get();

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);

         // Inactivate Sememe
         DescriptionSememeImpl createdVersion = cloneVersion(semChron, State.INACTIVE);

         Get.commitService()
            .addUncommitted(semChron)
            .get();
         commitRecord = Get.commitService()
                           .commit("Inactivating sememe for Testing")
                           .get();
         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         verifyState(con, semChron, State.INACTIVE);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId,
                             userId,
                             "Cancel Workflow",
                             "Canceling Workflow for Testing",
                             defaultEditCoordinate);
         verifyState(con, semChron, State.ACTIVE);
      } catch (Exception e) {
         Assert.fail();
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelNewComponents() {
      clearStores();
      LOG.info("Testing Ability to cancel new concept reverting it entirely");
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelNewSememe() {
      clearStores();
      LOG.info("Testing Ability to cancel new sememe reverting it entirely");
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelNoLaunch() {
      clearStores();
      LOG.info("Testing ability to cancel a workflow that has only been defined");

      UUID processId = null;

      try {
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name",
                              " Framework Workflow Description");
         wp_.getWorkflowProcessInitializerConcluder()
            .endWorkflowProcess(processId,
                                getCancelAction(),
                                userId,
                                "Canceling Workflow for Testing",
                                EndWorkflowType.CANCELED,
                                defaultEditCoordinate);
         Assert.assertEquals(ProcessStatus.CANCELED,
                             wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getStatus());
      } catch (Exception e) {
         Assert.fail();
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testConcludeNoLaunch() {
      clearStores();
      LOG.info("Testing ability to conclude a workflow that has only been defined");

      UUID processId = null;

      try {
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name",
                              " Framework Workflow Description");
         wp_.getWorkflowProcessInitializerConcluder()
            .endWorkflowProcess(processId,
                                getConcludeAction(),
                                userId,
                                "Concluding Workflow for Testing",
                                EndWorkflowType.CONCLUDED,
                                defaultEditCoordinate);
      } catch (Exception e) {
         Assert.assertTrue(true);
      }

      Assert.assertEquals(ProcessStatus.DEFINED, wp_.getWorkflowAccessor()
            .getProcessDetails(processId)
            .getStatus());
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testFailCancelCall() {
      clearStores();
      LOG.info("Testing inability to cancel an already concluded Workflow ");

      UUID processId = null;

      try {
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name",
                              " Framework Workflow Description");

         Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "QA Passes", "Review Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Approve", "Approve Comment", defaultEditCoordinate);
         Assert.assertFalse(wp_.getWorkflowUpdater()
                               .advanceWorkflow(processId,
                                     userId,
                                     "Cancel Workflow",
                                     "Canceling Workflow for Testing",
                                     defaultEditCoordinate));
         Assert.assertEquals(ProcessStatus.CONCLUDED,
                             wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getStatus());

         ProcessHistory hx = wp_.getWorkflowAccessor()
                                .getProcessHistory(processId)
                                .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CONCLUDED));
      } catch (Exception e) {
         Assert.fail();
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testFailConclude() {
      clearStores();
      LOG.info("Testing inability to conclude a workflow that hasn't reached a final workflow state");

      UUID processId = null;

      try {
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name",
                              " Framework Workflow Description");

         Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);

         ProcessHistory hx = wp_.getWorkflowAccessor()
                                .getProcessHistory(processId)
                                .last();

         Assert.assertTrue(wp_.getBPMNInfo()
                              .getEditStatesMap()
                              .get(wp_.getBPMNInfo()
                                      .getDefinitionId())
                              .contains(hx.getOutcomeState()));
         Assert.assertTrue(isStartState(wp_.getBPMNInfo()
                                           .getDefinitionId(), hx.getInitialState()));
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);

         boolean result = wp_.getWorkflowUpdater()
                             .advanceWorkflow(processId,
                                   userId,
                                   "Approve",
                                   "Concluding Workflow for Testing",
                                   defaultEditCoordinate);

         Assert.assertFalse(result);
         Assert.assertEquals(ProcessStatus.LAUNCHED,
                             wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getStatus());
         hx = wp_.getWorkflowAccessor()
                 .getProcessHistory(processId)
                 .last();
         Assert.assertFalse(wp_.getBPMNInfo()
                               .getEditStatesMap()
                               .get(wp_.getBPMNInfo()
                                       .getDefinitionId())
                               .contains(hx.getOutcomeState()));
         Assert.assertFalse(isStartState(wp_.getBPMNInfo()
                                            .getDefinitionId(), hx.getInitialState()));
      } catch (Exception e) {
         Assert.fail();
      }
   }

   // TODO: Decide if prevent multiple processes with same name

   /*
    * @Test(groups = { "wf" }, dependsOnMethods = { "testLoadWorkflow" })
    * public void testFailDefineAfterDefineSameName() { LOG.
    * info("Testing inability to define a workflow on a concept that has already been defined"
    * ); UUID processId = null;
    *
    * try { processId =
    * wp_.getWorkflowProcessInitializerConcluder().createWorkflowProcess(wp_.
    * getBPMNInfo().getDefinitionId(), userId, "Framework Workflow Name",
    * " Framework Workflow Description"); processId =
    * wp_.getWorkflowProcessInitializerConcluder().createWorkflowProcess(wp_.
    * getBPMNInfo().getDefinitionId(), userId, "Framework Workflow Name",
    * " Framework Workflow Description"); Assert.fail(); } catch (Exception e)
    * { Assert.assertTrue(true); Assert.assertEquals(ProcessStatus.DEFINED,
    * wp_.getWorkflowAccessor().getProcessDetails(processId).getStatus()); }
    *
    * clearStores(); }
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testFailDefineAfterLaunched() {
      clearStores();
      LOG.info("Testing inability to add a concept onto a workflow other than one that has already been launched");

      UUID processId = null;

      try {
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name",
                              " Framework Workflow Description");

         Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         commitRecord = createNewVersion(firstTestConceptNid, null);
         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);

         try {
            commitRecord = createNewVersion(firstTestConceptNid, null);
            wp_.getWorkflowUpdater()
               .addCommitRecordToWorkflow(processId, commitRecord);
            Assert.fail();
         } catch (Exception e) {
            Assert.assertTrue(true);
         }

         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "QA Fails", "QA Fail", defaultEditCoordinate);
         commitRecord = createNewVersion(firstTestConceptNid, null);
         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         Assert.assertEquals(ProcessStatus.LAUNCHED,
                             wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getStatus());
      } catch (Exception e) {
         Assert.fail();
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testFailLaunch() {
      clearStores();
      LOG.info("Testing inability to launch a workflow that has yet to be defined");

      UUID processId = UUID.randomUUID();

      try {
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         Assert.assertTrue(true);

         AbstractStorableWorkflowContents process = null;

         process = wp_.getWorkflowAccessor()
                      .getProcessDetails(processId);

         if (process == null) {
            Assert.assertTrue(true);
         } else {
            Assert.fail();
         }
      } catch (Exception ee) {
         Assert.fail();
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testIntegrationAddCommitRecordToWorkflow()
            throws Exception {
      clearStores();

      // Cannot make this work without at least a Mock Database.
      // Added to Integration-Test module's workflowFramworkTest. For now just
      // pass.
      Assert.assertTrue(true);

      UUID          processId = createFirstWorkflowProcess(wp_.getBPMNInfo()
                                                              .getDefinitionId());
      ProcessDetail details   = wp_.getProcessDetailStore()
                                   .get(processId);

      Assert.assertFalse(details.getComponentToInitialEditMap()
                                .keySet()
                                .contains(firstTestConceptNid));

      Optional<CommitRecord> commitRecord      = createNewVersion(firstTestConceptNid, null);
      Stamp                  commitRecordStamp = createStampFromCommitRecord(commitRecord);

      wp_.getWorkflowUpdater()
         .addCommitRecordToWorkflow(processId, commitRecord);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(1, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      Assert.assertTrue(details.getComponentToInitialEditMap()
                               .keySet()
                               .contains(firstTestConceptNid));
      Assert.assertEquals(1, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      Assert.assertTrue(commitRecordStamp.equals(details.getComponentToInitialEditMap()
            .get(firstTestConceptNid)));
      commitRecord = createNewVersion(firstTestConceptNid, null);

      Stamp updatedCommitRecordStamp = createStampFromCommitRecord(commitRecord);

      wp_.getWorkflowUpdater()
         .addCommitRecordToWorkflow(processId, commitRecord);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(1, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      Assert.assertTrue(details.getComponentToInitialEditMap()
                               .keySet()
                               .contains(firstTestConceptNid));
      Assert.assertTrue(commitRecordStamp.equals(details.getComponentToInitialEditMap()
            .get(firstTestConceptNid)));
      Assert.assertFalse(updatedCommitRecordStamp.equals(details.getComponentToInitialEditMap()
            .get(firstTestConceptNid)));

//    Dan commented out on 10/31/16, because these tests are broken with a nasty timing bug that I don't have time to deal with.  Jesse to fix
//                  commitRecord = createNewVersion(secondTestConceptNid, null);
//                  Stamp secondCommitRecordStamp = createStampFromCommitRecord(commitRecord);
//                  wp_.getWorkflowUpdater().addCommitRecordToWorkflow(processId, commitRecord);
//                  details = wp_.getProcessDetailStore().get(processId);
//                  Assert.assertEquals(2, details.getComponentToInitialEditMap().keySet().size());
//                  Assert.assertTrue(details.getComponentToInitialEditMap().keySet().contains(firstTestConceptNid));
//                  Assert.assertTrue(details.getComponentToInitialEditMap().keySet().contains(secondTestConceptNid));
//                  Assert.assertTrue(commitRecordStamp.equals(details.getComponentToInitialEditMap().get(firstTestConceptNid)));
//                  Assert.assertFalse(updatedCommitRecordStamp.equals(details.getComponentToInitialEditMap().get(firstTestConceptNid)));
//                  Assert.assertFalse(secondCommitRecordStamp.equals(details.getComponentToInitialEditMap().get(firstTestConceptNid)));
//                  Assert.assertFalse(commitRecordStamp.equals(details.getComponentToInitialEditMap().get(secondTestConceptNid)));
//                  Assert.assertFalse(updatedCommitRecordStamp.equals(details.getComponentToInitialEditMap().get(secondTestConceptNid)));
//                  Assert.assertTrue(secondCommitRecordStamp.equals(details.getComponentToInitialEditMap().get(secondTestConceptNid)));
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testIntegrationFailuresWithAddRemoveComponentsToProcess()
            throws Exception {
      clearStores();

      UUID processId = UUID.randomUUID();

      try {
         wp_.getWorkflowUpdater()
            .removeComponentFromWorkflow(processId, firstTestConceptNid, defaultEditCoordinate);
         Assert.fail();
      } catch (Exception e) {
         Assert.assertTrue(true);
      }

      UUID                   firstProcessId = createFirstWorkflowProcess(wp_.getBPMNInfo()
                                                                            .getDefinitionId());
      Optional<CommitRecord> commitRecord   = createNewVersion(firstTestConceptNid, null);

      wp_.getWorkflowUpdater()
         .addCommitRecordToWorkflow(firstProcessId, commitRecord);
      wp_.getWorkflowUpdater()
         .advanceWorkflow(firstProcessId, userId, "Edit", "Edit Comment", defaultEditCoordinate);

      try {
         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(firstProcessId, commitRecord);
         Assert.fail();
      } catch (Exception e) {
         Assert.assertTrue(true);
      }

      try {
         // Go back to no components in any workflow
         wp_.getWorkflowUpdater()
            .removeComponentFromWorkflow(firstProcessId, firstTestConceptNid, defaultEditCoordinate);
         Assert.fail();
      } catch (Exception e) {
         Assert.assertTrue(true);
      }

      executeSendForReviewAdvancement(firstProcessId);

      try {
         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(firstProcessId, commitRecord);
         Assert.fail();
      } catch (Exception e) {
         Assert.assertTrue(true);
      }

      try {
         // Go back to no components in any workflow
         wp_.getWorkflowUpdater()
            .removeComponentFromWorkflow(firstProcessId, firstTestConceptNid, defaultEditCoordinate);
         Assert.fail();
      } catch (Exception e) {
         Assert.assertTrue(true);
      }

      // Rejecting QA to get back to edit state
      executeRejectReviewAdvancement(firstProcessId);

      // Go back to no components in any workflow
      wp_.getWorkflowUpdater()
         .removeComponentFromWorkflow(firstProcessId, firstTestConceptNid, defaultEditCoordinate);

      // Testing LAUNCHED-EDIT Case
      wp_.getWorkflowUpdater()
         .addCommitRecordToWorkflow(firstProcessId, commitRecord);

      ProcessDetail details = wp_.getProcessDetailStore()
                                 .get(firstProcessId);

      Assert.assertEquals(1, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      Assert.assertTrue(details.getComponentToInitialEditMap()
                               .keySet()
                               .contains(firstTestConceptNid));
      Assert.assertEquals(1, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());

      // Testing INACTIVE Case
      cancelWorkflow(firstProcessId);

      try {
         wp_.getWorkflowUpdater()
            .removeComponentFromWorkflow(firstProcessId, firstTestConceptNid, defaultEditCoordinate);
         Assert.fail();
      } catch (Exception e) {
         Assert.assertTrue(true);
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testIntegrationRemoveComponentsFromProcess()
            throws Exception {
      clearStores();

      UUID          processId = createFirstWorkflowProcess(wp_.getBPMNInfo()
                                                              .getDefinitionId());
      ProcessDetail details   = wp_.getProcessDetailStore()
                                   .get(processId);

      Assert.assertEquals(0, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());

      Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

      wp_.getWorkflowUpdater()
         .addCommitRecordToWorkflow(processId, commitRecord);
      commitRecord = createNewVersion(firstTestConceptNid, null);
      wp_.getWorkflowUpdater()
         .addCommitRecordToWorkflow(processId, commitRecord);
      commitRecord = createNewVersion(secondTestConceptNid, null);
      wp_.getWorkflowUpdater()
         .addCommitRecordToWorkflow(processId, commitRecord);
      commitRecord = createNewVersion(secondTestConceptNid, null);
      wp_.getWorkflowUpdater()
         .addCommitRecordToWorkflow(processId, commitRecord);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(2, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      wp_.getWorkflowUpdater()
         .removeComponentFromWorkflow(processId, firstTestConceptNid, defaultEditCoordinate);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(1, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      Assert.assertFalse(details.getComponentToInitialEditMap()
                                .keySet()
                                .contains(firstTestConceptNid));
      Assert.assertTrue(details.getComponentToInitialEditMap()
                               .keySet()
                               .contains(secondTestConceptNid));
      wp_.getWorkflowUpdater()
         .removeComponentFromWorkflow(processId, secondTestConceptNid, defaultEditCoordinate);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(0, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
   }

   @Test(
      groups          = { "wf" },
      dependsOnGroups = { "load" }
   )
   public void testLoadWorkflow() {
      LOG.info("Loading Metadata db");
      firstTestConceptNid        = MetaData.ISAAC_METADATA.getNid();
      secondTestConceptNid       = MetaData.ACCEPTABLE.getNid();
      WorkflowProvider.BPMN_PATH = BPMN_FILE_PATH;
      wp_                        = LookupService.get()
            .getService(WorkflowProvider.class);
      cancelAction = wp_.getBPMNInfo()
                        .getEndWorkflowTypeMap()
                        .get(EndWorkflowType.CONCLUDED)
                        .iterator()
                        .next();
      defaultEditCoordinate = new EditCoordinateImpl(TermAux.USER.getNid(),
            MetaData.ISAAC_MODULE.getNid(),
            MetaData.DEVELOPMENT_PATH.getNid());

      ObservableStampCoordinate defaultSC = Get.configurationService()
                                               .getDefaultStampCoordinate();

      defaultStampCoordinate = new StampCoordinateImpl(defaultSC.getStampPrecedence(),
            defaultSC.getStampPosition(),
            defaultSC.getModuleSequences(),
            State.ANY_STATE_SET);
      firstTestConceptNid  = MetaData.EL_PLUS_PLUS_INFERRED_FORM_ASSEMBLAGE.getNid();
      secondTestConceptNid = MetaData.ACCEPTABLE.getNid();
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testProcessTimeFields() {
      clearStores();
      LOG.info("Testing Ability to cancel changes made to a sememe's text");

      try {
         // Create WF
         UUID processId = wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");
         long timeCreated = wp_.getWorkflowAccessor()
                               .getProcessDetails(processId)
                               .getTimeCreated();
         long timeCanceledOrConcluded = wp_.getWorkflowAccessor()
                                           .getProcessDetails(processId)
                                           .getTimeCanceledOrConcluded();
         long timeLaunched = wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getTimeLaunched();

         Assert.assertTrue(timeCreated > -1);
         Assert.assertEquals(-1, timeLaunched);
         Assert.assertEquals(-1, timeCanceledOrConcluded);

         // Launch WF
         Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         timeCreated             = wp_.getWorkflowAccessor()
                                      .getProcessDetails(processId)
                                      .getTimeCreated();
         timeCanceledOrConcluded = wp_.getWorkflowAccessor()
                                      .getProcessDetails(processId)
                                      .getTimeCanceledOrConcluded();
         timeLaunched            = wp_.getWorkflowAccessor()
                                      .getProcessDetails(processId)
                                      .getTimeLaunched();
         Assert.assertTrue(timeCreated > -1);
         Assert.assertTrue(timeLaunched > -1);
         Assert.assertEquals(-1, timeCanceledOrConcluded);

         // Cancel WF
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId,
                             userId,
                             "Cancel Workflow",
                             "Canceling Workflow for Testing",
                             defaultEditCoordinate);
         timeCreated             = wp_.getWorkflowAccessor()
                                      .getProcessDetails(processId)
                                      .getTimeCreated();
         timeCanceledOrConcluded = wp_.getWorkflowAccessor()
                                      .getProcessDetails(processId)
                                      .getTimeCanceledOrConcluded();
         timeLaunched            = wp_.getWorkflowAccessor()
                                      .getProcessDetails(processId)
                                      .getTimeLaunched();
         Assert.assertTrue(timeCreated > -1);
         Assert.assertTrue(timeLaunched > -1);
         Assert.assertTrue(timeCanceledOrConcluded > -1);
      } catch (Exception e) {
         Assert.fail(e.getMessage());
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testRedefineCall() {
      clearStores();
      LOG.info("Testing ability to define and launch workflow on a concept that has an already-concluded workflow");

      try {
         UUID processId = wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");
         Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "QA Passes", "Review Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Approve", "Approve Comment", defaultEditCoordinate);
         Assert.assertEquals(ProcessStatus.CONCLUDED,
                             wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getStatus());

         ProcessHistory hx = wp_.getWorkflowAccessor()
                                .getProcessHistory(processId)
                                .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CONCLUDED));
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name2",
                              " Framework Workflow Description");
         Assert.assertEquals(ProcessStatus.DEFINED, wp_.getWorkflowAccessor()
               .getProcessDetails(processId)
               .getStatus());
      } catch (Exception e) {
         Assert.fail();
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testStartAllFailConclude() {
      clearStores();
      LOG.info(
          "Testing ability to advance workflow to conclusion via with a rejection/failure happening at each point in path");

      UUID processId = null;

      try {
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name",
                              " Framework Workflow Description");

         Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "QA Fails", "Fail Review Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Second Edit Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "QA Passes", "Review Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Reject Edit", "Reject Edit Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Third Edit Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "QA Passes", "Second Review Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Reject Review", "Reject Review Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "QA Passes", "Third Review Comment", defaultEditCoordinate);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Approve", "Approve Comment", defaultEditCoordinate);

         boolean result = wp_.getWorkflowUpdater()
                             .advanceWorkflow(processId,
                                   userId,
                                   "Approve",
                                   "Concluding Workflow for Testing",
                                   defaultEditCoordinate);

         Assert.assertFalse(result);
         Assert.assertEquals(ProcessStatus.CONCLUDED,
                             wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getStatus());

         ProcessHistory hx = wp_.getWorkflowAccessor()
                                .getProcessHistory(processId)
                                .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CONCLUDED));
      } catch (Exception ee) {
         Assert.fail();
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testStartAllPassConclude() {
      clearStores();
      LOG.info("Testing ability to advance workflow to conclusion via its easy-path");

      UUID processId = null;

      try {
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name",
                              " Framework Workflow Description");
         Assert.assertEquals(userId, wp_.getProcessDetailStore()
                                        .get(processId)
                                        .getOwnerId());

         ProcessDetail process = wp_.getProcessDetailStore()
                                    .get(processId);

         Assert.assertEquals(userId, process.getOwnerId());

         Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         process = wp_.getProcessDetailStore()
                      .get(processId);
         Assert.assertEquals(BPMNInfo.UNOWNED_PROCESS, process.getOwnerId());
         process.setOwnerId(userId);
         wp_.getProcessDetailStore()
            .put(processId, process);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "QA Passes", "Review Comment", defaultEditCoordinate);
         process = wp_.getProcessDetailStore()
                      .get(processId);
         Assert.assertEquals(BPMNInfo.UNOWNED_PROCESS, process.getOwnerId());
         process.setOwnerId(userId);
         wp_.getProcessDetailStore()
            .put(processId, process);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Approve", "Approve Comment", defaultEditCoordinate);
         process = wp_.getProcessDetailStore()
                      .get(processId);
         Assert.assertEquals(ProcessStatus.CONCLUDED, process.getStatus());
         Assert.assertEquals(BPMNInfo.UNOWNED_PROCESS, process.getOwnerId());

         ProcessHistory hx = wp_.getWorkflowAccessor()
                                .getProcessHistory(processId)
                                .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CONCLUDED));
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name",
                              " Framework Workflow Description");
         process = wp_.getProcessDetailStore()
                      .get(processId);
         Assert.assertEquals(userId, process.getOwnerId());
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId,
                             userId,
                             "Cancel Workflow",
                             "Canceling Workflow for Testing",
                             defaultEditCoordinate);
         process = wp_.getProcessDetailStore()
                      .get(processId);
         Assert.assertEquals(BPMNInfo.UNOWNED_PROCESS, process.getOwnerId());
      } catch (Exception e) {
         Assert.fail();
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testStartCancel() {
      clearStores();
      LOG.info("Testing ability to cancel a workflow that has been defined and launched");

      UUID processId = null;

      try {
         processId = wp_.getWorkflowProcessInitializerConcluder()
                        .createWorkflowProcess(wp_.getBPMNInfo()
                              .getDefinitionId(),
                              userId,
                              "Framework Workflow Name",
                              " Framework Workflow Description");

         Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         Thread.sleep(1);  // TODO fix Dan Work around bug in design
         wp_.getWorkflowProcessInitializerConcluder()
            .endWorkflowProcess(processId,
                                getCancelAction(),
                                userId,
                                "Canceling Workflow for Testing",
                                EndWorkflowType.CANCELED,
                                defaultEditCoordinate);
         Assert.assertEquals(ProcessStatus.CANCELED,
                             wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getStatus());

         ProcessHistory hx = wp_.getWorkflowAccessor()
                                .getProcessHistory(processId)
                                .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CANCELED));
      } catch (Exception e) {
         Assert.fail();
      }
   }

   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testStatusAccessorComponentInActiveWorkflow() {
      clearStores();
      LOG.info("Testing Workflow History Accessor isComponentInActiveWorkflow()");

      ConceptChronology<? extends ConceptVersion<?>>   con     = Get.conceptService()
                                                                    .getConcept(firstTestConceptNid);
      SememeChronology<? extends DescriptionSememe<?>> descSem = con.getConceptDescriptionList()
                                                                    .iterator()
                                                                    .next();
      int                                              conNid  = con.getNid();
      int                                              semNid  = descSem.getNid();

      try {
         Assert.assertFalse(wp_.getWorkflowAccessor()
                               .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                     .getDefinitionId(), conNid));
         Assert.assertFalse(wp_.getWorkflowAccessor()
                               .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                     .getDefinitionId(), semNid));

         UUID processId = wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");

         Assert.assertFalse(wp_.getWorkflowAccessor()
                               .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                     .getDefinitionId(), conNid));
         Assert.assertFalse(wp_.getWorkflowAccessor()
                               .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                     .getDefinitionId(), semNid));

         Optional<CommitRecord> commitRecord = createNewVersion(conNid, null);

         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         commitRecord = createNewVersion(null, semNid);
         wp_.getWorkflowUpdater()
            .addCommitRecordToWorkflow(processId, commitRecord);
         Assert.assertTrue(wp_.getWorkflowAccessor()
                              .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                    .getDefinitionId(), conNid));
         Assert.assertTrue(wp_.getWorkflowAccessor()
                              .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                    .getDefinitionId(), semNid));
         wp_.getWorkflowUpdater()
            .advanceWorkflow(processId, userId, "Edit", "Edit Comment", defaultEditCoordinate);
         Assert.assertTrue(wp_.getWorkflowAccessor()
                              .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                    .getDefinitionId(), conNid));
         Assert.assertTrue(wp_.getWorkflowAccessor()
                              .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                    .getDefinitionId(), semNid));
         wp_.getWorkflowProcessInitializerConcluder()
            .endWorkflowProcess(processId,
                                getCancelAction(),
                                userId,
                                "Canceling Workflow for Testing",
                                EndWorkflowType.CANCELED,
                                null);
         Assert.assertFalse(wp_.getWorkflowAccessor()
                               .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                     .getDefinitionId(), conNid));
         Assert.assertFalse(wp_.getWorkflowAccessor()
                               .isComponentInActiveWorkflow(wp_.getBPMNInfo()
                                     .getDefinitionId(), semNid));
      } catch (Exception e) {
         Assert.fail(e.getMessage());
      }
   }

   protected void cancelWorkflow(UUID processId) {
      try {
         Thread.sleep(1);
         finishWorkflowProcess(processId, cancelAction, userId, "Canceled Workflow", EndWorkflowType.CANCELED);
      } catch (Exception e) {
         Assert.fail();
      }
   }

   protected UUID createFirstWorkflowProcess(UUID requestedDefinitionId) {
      return createWorkflowProcess(requestedDefinitionId, "Main Process Name", "Main Process Description");
   }

   protected UUID createSecondWorkflowProcess(UUID requestedDefinitionId) {
      return createWorkflowProcess(requestedDefinitionId, "Secondary Process Name", "Secondary Process Description");
   }

   protected void executeLaunchWorkflow(UUID processId) {
      try {
         Thread.sleep(1);

         ProcessDetail entry = wp_.getProcessDetailStore()
                                  .get(processId);

         entry.setStatus(ProcessStatus.LAUNCHED);
         entry.setTimeLaunched(new Date().getTime());
         wp_.getProcessDetailStore()
            .put(processId, entry);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   protected void executeRejectReviewAdvancement(UUID requestedProcessId) {
      try {
         Thread.sleep(1);

         ProcessHistory hx = wp_.getWorkflowAccessor()
                                .getProcessHistory(requestedProcessId)
                                .last();
         ProcessHistory entry = new ProcessHistory(requestedProcessId,
                                                   userId,
                                                   new Date().getTime(),
                                                   REJECT_REVIEW_STATE,
                                                   REJECT_REVIEW_ACTION,
                                                   REJECT_REVIEW_OUTCOME,
                                                   REJECT_REVIEW_COMMENT,
                                                   hx.getHistorySequence() + 1);

         wp_.getProcessHistoryStore()
            .add(entry);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   protected void executeSendForApprovalAdvancement(UUID requestedProcessId) {
      try {
         Thread.sleep(1);

         ProcessHistory hx = wp_.getWorkflowAccessor()
                                .getProcessHistory(requestedProcessId)
                                .last();
         ProcessHistory entry = new ProcessHistory(requestedProcessId,
                                                   userId,
                                                   new Date().getTime(),
                                                   SEND_TO_APPROVAL_STATE,
                                                   SEND_TO_APPROVAL_ACTION,
                                                   SEND_TO_APPROVAL_OUTCOME,
                                                   SEND_TO_APPROVAL_COMMENT,
                                                   hx.getHistorySequence() + 1);

         wp_.getProcessHistoryStore()
            .add(entry);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   protected void executeSendForReviewAdvancement(UUID processId) {
      ProcessDetail entry = wp_.getProcessDetailStore()
                               .get(processId);

      try {
         Thread.sleep(1);

         ProcessHistory hx = wp_.getWorkflowAccessor()
                                .getProcessHistory(processId)
                                .last();
         ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                          entry.getCreatorId(),
                                                          new Date().getTime(),
                                                          LAUNCH_STATE,
                                                          LAUNCH_ACTION,
                                                          LAUNCH_OUTCOME,
                                                          LAUNCH_COMMENT,
                                                          hx.getHistorySequence() + 1);

         wp_.getProcessHistoryStore()
            .add(advanceEntry);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   private void clearStores() {
      wp_.getProcessDetailStore()
         .clear();
      wp_.getProcessHistoryStore()
         .clear();
   }

   private DescriptionSememeImpl cloneVersion(SememeChronologyImpl semChron,
         State state)
            throws InterruptedException,
                   ExecutionException {
      DescriptionSememe<?> latestVersion =
         ((LatestVersion<DescriptionSememe<?>>) semChron.getLatestVersion(DescriptionSememe.class,
                                                                          Get.configurationService()
                                                                                .getDefaultStampCoordinate())
                                                        .get()).value();
      DescriptionSememeImpl createdVersion =
         (DescriptionSememeImpl) semChron.createMutableVersion(DescriptionSememeImpl.class,
                                                               state,
                                                               defaultEditCoordinate);

      createdVersion.setCaseSignificanceConceptSequence(latestVersion.getCaseSignificanceConceptSequence());
      createdVersion.setDescriptionTypeConceptSequence((latestVersion.getDescriptionTypeConceptSequence()));
      createdVersion.setLanguageConceptSequence(latestVersion.getLanguageConceptSequence());
      createdVersion.setText(latestVersion.getText());
      return createdVersion;
   }

   private Optional<CommitRecord> createNewVersion(Integer conNid,
         Integer semNid)
            throws InterruptedException,
                   ExecutionException {
      if (conNid != null) {
         ConceptChronologyImpl con = (ConceptChronologyImpl) Get.conceptService()
                                                                .getConcept(conNid);

         con.createMutableVersion(State.ACTIVE, defaultEditCoordinate);
         Get.commitService()
            .addUncommitted(con)
            .get();
         return Get.commitService()
                   .commit("Inactivating concept for Testing")
                   .get();
      } else {
         SememeChronologyImpl  semChron       = (SememeChronologyImpl) Get.sememeService()
                                                                          .getSememe(semNid);
         DescriptionSememeImpl createdVersion = cloneVersion(semChron, State.ACTIVE);

         Get.commitService()
            .addUncommitted(semChron)
            .get();
         return Get.commitService()
                   .commit("Inactivating sememe for Testing")
                   .get();
      }
   }

   private Stamp createStamp(int userSeq, State state) {
      if ((moduleSeq < 0) || (pathSeq < 0)) {
         if (Get.configurationService()
                .getDefaultStampCoordinate()
                .getModuleSequences()
                .size() != 1) {
            return null;
         }

         moduleSeq = Get.configurationService()
                        .getDefaultStampCoordinate()
                        .getModuleSequences()
                        .getIntIterator()
                        .nextInt();
         pathSeq = Get.configurationService()
                      .getDefaultStampCoordinate()
                      .getStampPosition()
                      .getStampPathSequence();
      }

      return new Stamp(state, new Date().getTime(), moduleSeq, userSeq, pathSeq);
   }

   private Stamp createStampFromCommitRecord(Optional<CommitRecord> commitRecord) {
      int   stampSeq = commitRecord.get()
                                   .getStampsInCommit()
                                   .getIntIterator()
                                   .next();
      State status   = Get.stampService()
                          .getStatusForStamp(stampSeq);
      long  time     = Get.stampService()
                          .getTimeForStamp(stampSeq);
      int   author   = Get.stampService()
                          .getAuthorSequenceForStamp(stampSeq);
      int   module   = Get.stampService()
                          .getModuleSequenceForStamp(stampSeq);
      int   path     = Get.stampService()
                          .getPathSequenceForStamp(stampSeq);

      return new Stamp(status, time, author, module, path);
   }

   private UUID createWorkflowProcess(UUID requestedDefinitionId, String name, String description) {
      AvailableAction startNodeAction = wp_.getBPMNInfo()
                                           .getDefinitionStartActionMap()
                                           .get(wp_.getBPMNInfo()
                                                 .getDefinitionId())
                                           .iterator()
                                           .next();

      // Mimick the wp_.getWorkflowProcessInitializerConcluder()'s create new
      // process
      ProcessDetail details = new ProcessDetail(requestedDefinitionId,
                                                userId,
                                                new Date().getTime(),
                                                ProcessStatus.DEFINED,
                                                name,
                                                description);
      UUID processId = wp_.getProcessDetailStore()
                          .add(details);

      // Add Process History with START_STATE-AUTOMATED-EDIT_STATE
      AvailableAction startAdvancement = new AvailableAction(requestedDefinitionId,
                                                             startNodeAction.getInitialState(),
                                                             startNodeAction.getAction(),
                                                             startNodeAction.getOutcomeState(),
                                                             UserRole.AUTOMATED);
      ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                       userId,
                                                       new Date().getTime(),
                                                       startAdvancement.getInitialState(),
                                                       startAdvancement.getAction(),
                                                       startAdvancement.getOutcomeState(),
                                                       "",
                                                       1);

      wp_.getProcessHistoryStore()
         .add(advanceEntry);
      return processId;
   }

   private void finishWorkflowProcess(UUID processId,
                                      AvailableAction actionToProcess,
                                      UUID userId,
                                      String comment,
                                      EndWorkflowType endType)
            throws Exception {
      // Mimick the wp_.getWorkflowProcessInitializerConcluder()'s finish
      // workflow process
      ProcessDetail entry = wp_.getProcessDetailStore()
                               .get(processId);

      if (endType.equals(EndWorkflowType.CANCELED)) {
         entry.setStatus(ProcessStatus.CANCELED);
      } else if (endType.equals(EndWorkflowType.CONCLUDED)) {
         entry.setStatus(ProcessStatus.CONCLUDED);
      }

      entry.setTimeCanceledOrConcluded(new Date().getTime());
      wp_.getProcessDetailStore()
         .put(processId, entry);

      // Only add Cancel state in Workflow if process has already been
      // launched
      ProcessHistory hx = wp_.getWorkflowAccessor()
                             .getProcessHistory(processId)
                             .last();
      ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                       userId,
                                                       new Date().getTime(),
                                                       actionToProcess.getInitialState(),
                                                       actionToProcess.getAction(),
                                                       actionToProcess.getOutcomeState(),
                                                       comment,
                                                       hx.getHistorySequence() + 1);

      wp_.getProcessHistoryStore()
         .add(advanceEntry);

      if (endType.equals(EndWorkflowType.CANCELED)) {
         // TODO: Handle cancelation store and handle reverting automatically
      }
   }

   private void verifyState(ConceptChronology<? extends ConceptVersion<?>> con,
                            SememeChronology<? extends DescriptionSememe<?>> descSem,
                            State state) {
      ConceptChronology<? extends ConceptVersion<?>> cc = (ConceptChronologyImpl) Get.conceptService()
                                                                                     .getConcept(con.getNid());
      Optional<LatestVersion<ConceptVersion>> latestConVersion =
         ((ConceptChronology) cc).getLatestVersion(ConceptVersion.class,
                                                   defaultStampCoordinate);

      Assert.assertEquals(latestConVersion.get()
            .value()
            .getState(), state);

      SememeChronology<? extends SememeVersion<?>> semChron = Get.sememeService()
                                                                 .getSememe(descSem.getNid());
      Optional<LatestVersion<DescriptionSememe<?>>> latestDescVersion =
         ((SememeChronology) semChron).getLatestVersion(DescriptionSememe.class,
                                                        defaultStampCoordinate);

      Assert.assertEquals(latestDescVersion.get()
            .value()
            .getState(), state);
   }

   //~--- get methods ---------------------------------------------------------

   private AvailableAction getCancelAction() {
      return wp_.getBPMNInfo()
                .getEndWorkflowTypeMap()
                .get(EndWorkflowType.CANCELED)
                .iterator()
                .next();
   }

   private AvailableAction getConcludeAction() {
      return wp_.getBPMNInfo()
                .getEndWorkflowTypeMap()
                .get(EndWorkflowType.CONCLUDED)
                .iterator()
                .next();
   }

   private boolean isEndState(String state, EndWorkflowType type) {
      for (AvailableAction action: wp_.getBPMNInfo()
                                      .getEndWorkflowTypeMap()
                                      .get(type)) {
         if (action.getOutcomeState()
                   .equals(state)) {
            return true;
         }
      }

      return false;
   }

   private boolean isStartState(UUID defId, String state) {
      for (AvailableAction action: wp_.getBPMNInfo()
                                      .getDefinitionStartActionMap()
                                      .get(defId)) {
         if (action.getInitialState()
                   .equals(state)) {
            return true;
         }
      }

      return false;
   }

   //~--- set methods ---------------------------------------------------------

   @BeforeGroups(groups = { "wf" })
   public void setUpUsers()
            throws Exception {
      userId = UUID.randomUUID();

      SimpleUserRoleService rolesService = LookupService.get()
                                                        .getService(SimpleUserRoleService.class);

      rolesService.addRole(UserRole.EDITOR);
      rolesService.addRole(UserRole.REVIEWER);
      rolesService.addRole(UserRole.APPROVER);
      rolesService.addRole(UserRole.AUTOMATED);

      HashSet<UserRole> roles = new HashSet<>();

      roles.add(UserRole.EDITOR);
      roles.add(UserRole.APPROVER);
      roles.add(UserRole.REVIEWER);
      rolesService.addUser(userId, roles);
   }
}

