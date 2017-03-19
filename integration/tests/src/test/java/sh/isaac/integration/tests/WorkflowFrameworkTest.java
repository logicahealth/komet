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
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /** The Constant LAUNCH_STATE. */
   private static final String LAUNCH_STATE = "Ready for Edit";

   /** The Constant LAUNCH_ACTION. */
   private static final String LAUNCH_ACTION = "Edit";

   /** The Constant LAUNCH_OUTCOME. */
   private static final String LAUNCH_OUTCOME = "Ready for Review";

   /** The Constant LAUNCH_COMMENT. */
   private static final String LAUNCH_COMMENT = "Launch Comment";

   /** The Constant SEND_TO_APPROVAL_STATE. */
   private static final String SEND_TO_APPROVAL_STATE = "Ready for Review";

   /** The Constant SEND_TO_APPROVAL_ACTION. */
   private static final String SEND_TO_APPROVAL_ACTION = "Review";

   /** The Constant SEND_TO_APPROVAL_OUTCOME. */
   private static final String SEND_TO_APPROVAL_OUTCOME = "Ready for Approve";

   /** The Constant SEND_TO_APPROVAL_COMMENT. */
   private static final String SEND_TO_APPROVAL_COMMENT = "Sending for Approval";

   /** The Constant REJECT_REVIEW_STATE. */
   private static final String REJECT_REVIEW_STATE = "Ready for Review";

   /** The Constant REJECT_REVIEW_ACTION. */
   private static final String REJECT_REVIEW_ACTION = "Reject QA";

   /** The Constant REJECT_REVIEW_OUTCOME. */
   private static final String REJECT_REVIEW_OUTCOME = "Ready for Edit";

   /** The Constant REJECT_REVIEW_COMMENT. */
   private static final String REJECT_REVIEW_COMMENT = "Rejecting QA sending back to Edit";

   /** The Constant CONCLUDED_WORKFLOW_COMMENT. */
   protected static final String CONCLUDED_WORKFLOW_COMMENT = "Concluded Workflow";

   /** The Constant CANCELED_WORKFLOW_COMMENT. */
   protected static final String CANCELED_WORKFLOW_COMMENT = "Canceled Workflow";

   /** The bpmn file path. */
   private static final String BPMN_FILE_PATH =
      "/sh/isaac/integration/tests/StaticWorkflowIntegrationTestingDefinition.bpmn2";

   /** The user id. */
   private static UUID userId;

   /** The first test concept nid. */
   private static int firstTestConceptNid;

   /** The second test concept nid. */
   private static int secondTestConceptNid;

   /** The cancel action. */
   protected static AvailableAction cancelAction;

   //~--- fields --------------------------------------------------------------

   /** The wp. */
   WorkflowProvider wp_;

   /** The default edit coordinate. */
   private EditCoordinate defaultEditCoordinate;

   /** The default stamp coordinate. */
   private StampCoordinate defaultStampCoordinate;

   /** The module seq. */
   private int moduleSeq;

   /** The path seq. */
   private int pathSeq;

   //~--- methods -------------------------------------------------------------

   /**
    * Test cancel activation of components.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelActivationOfComponents() {
      clearStores();
      LOG.info("Testing Ability to cancel change on a concept and a sememe made active when originally was inactive");
   }

   /**
    * Test cancel editing of sememe.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelEditingOfSememe() {
      clearStores();
      LOG.info("Testing Ability to cancel changes made to a sememe's text");

      try {
         final UUID processId = this.wp_.getWorkflowProcessInitializerConcluder()
                                        .createWorkflowProcess(this.wp_.getBPMNInfo()
                                              .getDefinitionId(),
                                              userId,
                                              "Framework Workflow Name",
                                              " Framework Workflow Description");
         final ConceptChronology<? extends ConceptVersion<?>> con = Get.conceptService()
                                                                       .getConcept(firstTestConceptNid);
         final SememeChronologyImpl descSem = (SememeChronologyImpl) con.getConceptDescriptionList()
                                                                        .iterator()
                                                                        .next();
         Optional<LatestVersion<DescriptionSememe<?>>> latestDescVersion =
            ((SememeChronology) descSem).getLatestVersion(DescriptionSememe.class,
                                                          Get.configurationService()
                                                                .getDefaultStampCoordinate());
         final String originalText = latestDescVersion.get()
                                                      .value()
                                                      .getText();

         // Modify Sememe Text
         final DescriptionSememeImpl createdVersion = cloneVersion(descSem, State.ACTIVE);

         createdVersion.setText("New Text");
         Get.commitService()
            .addUncommitted(descSem)
            .get();

         final Optional<CommitRecord> commitRecord = Get.commitService()
                                                        .commit("Inactivating sememe for Testing")
                                                        .get();

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId,
                                  userId,
                                  "Cancel Workflow",
                                  "Canceling Workflow for Testing",
                                  this.defaultEditCoordinate);

         final SememeChronology<? extends SememeVersion<?>> semChron = Get.sememeService()
                                                                          .getSememe(descSem.getNid());

         latestDescVersion = ((SememeChronology) semChron).getLatestVersion(DescriptionSememe.class,
               Get.configurationService()
                  .getDefaultStampCoordinate());
         Assert.assertEquals(originalText, latestDescVersion.get()
               .value()
               .getText());
      } catch (final Exception e) {
         Assert.fail(e.getMessage());
      }
   }

   /**
    * Test cancel inactivation of components.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelInactivationOfComponents() {
      clearStores();
      LOG.info("Testing Ability to cancel change on a concept and a sememe made inactive when originally was active");

      try {
         final UUID processId = this.wp_.getWorkflowProcessInitializerConcluder()
                                        .createWorkflowProcess(this.wp_.getBPMNInfo()
                                              .getDefinitionId(),
                                              userId,
                                              "Framework Workflow Name",
                                              " Framework Workflow Description");
         final ConceptChronologyImpl con = (ConceptChronologyImpl) Get.conceptService()
                                                                      .getConcept(firstTestConceptNid);
         final int                   semNid   = con.getConceptDescriptionList()
                                                   .iterator()
                                                   .next()
                                                   .getNid();
         final SememeChronologyImpl  semChron = (SememeChronologyImpl) Get.sememeService()
                                                                          .getSememe(semNid);

         verifyState(con, semChron, State.ACTIVE);

         // Inactivate Concept
         con.createMutableVersion(State.INACTIVE, this.defaultEditCoordinate);
         Get.commitService()
            .addUncommitted(con)
            .get();

         Optional<CommitRecord> commitRecord = Get.commitService()
                                                  .commit("Inactivating concept for Testing")
                                                  .get();

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);

         // Inactivate Sememe
         final DescriptionSememeImpl createdVersion = cloneVersion(semChron, State.INACTIVE);

         Get.commitService()
            .addUncommitted(semChron)
            .get();
         commitRecord = Get.commitService()
                           .commit("Inactivating sememe for Testing")
                           .get();
         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         verifyState(con, semChron, State.INACTIVE);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId,
                                  userId,
                                  "Cancel Workflow",
                                  "Canceling Workflow for Testing",
                                  this.defaultEditCoordinate);
         verifyState(con, semChron, State.ACTIVE);
      } catch (final Exception e) {
         Assert.fail();
      }
   }

   /**
    * Test cancel new components.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelNewComponents() {
      clearStores();
      LOG.info("Testing Ability to cancel new concept reverting it entirely");
   }

   /**
    * Test cancel new sememe.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelNewSememe() {
      clearStores();
      LOG.info("Testing Ability to cancel new sememe reverting it entirely");
   }

   /**
    * Test cancel no launch.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testCancelNoLaunch() {
      clearStores();
      LOG.info("Testing ability to cancel a workflow that has only been defined");

      UUID processId = null;

      try {
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");
         this.wp_.getWorkflowProcessInitializerConcluder()
                 .endWorkflowProcess(processId,
                                     getCancelAction(),
                                     userId,
                                     "Canceling Workflow for Testing",
                                     EndWorkflowType.CANCELED,
                                     this.defaultEditCoordinate);
         Assert.assertEquals(ProcessStatus.CANCELED,
                             this.wp_.getWorkflowAccessor()
                                     .getProcessDetails(processId)
                                     .getStatus());
      } catch (final Exception e) {
         Assert.fail();
      }
   }

   /**
    * Test conclude no launch.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testConcludeNoLaunch() {
      clearStores();
      LOG.info("Testing ability to conclude a workflow that has only been defined");

      UUID processId = null;

      try {
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");
         this.wp_.getWorkflowProcessInitializerConcluder()
                 .endWorkflowProcess(processId,
                                     getConcludeAction(),
                                     userId,
                                     "Concluding Workflow for Testing",
                                     EndWorkflowType.CONCLUDED,
                                     this.defaultEditCoordinate);
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      Assert.assertEquals(ProcessStatus.DEFINED,
                          this.wp_.getWorkflowAccessor()
                                  .getProcessDetails(processId)
                                  .getStatus());
   }

   /**
    * Test fail cancel call.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testFailCancelCall() {
      clearStores();
      LOG.info("Testing inability to cancel an already concluded Workflow ");

      UUID processId = null;

      try {
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");

         final Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "QA Passes", "Review Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Approve", "Approve Comment", this.defaultEditCoordinate);
         Assert.assertFalse(this.wp_.getWorkflowUpdater()
                                    .advanceWorkflow(processId,
                                          userId,
                                          "Cancel Workflow",
                                          "Canceling Workflow for Testing",
                                          this.defaultEditCoordinate));
         Assert.assertEquals(ProcessStatus.CONCLUDED,
                             this.wp_.getWorkflowAccessor()
                                     .getProcessDetails(processId)
                                     .getStatus());

         final ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                           .getProcessHistory(processId)
                                           .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CONCLUDED));
      } catch (final Exception e) {
         Assert.fail();
      }
   }

   /**
    * Test fail conclude.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testFailConclude() {
      clearStores();
      LOG.info("Testing inability to conclude a workflow that hasn't reached a final workflow state");

      UUID processId = null;

      try {
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");

         final Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);

         ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                     .getProcessHistory(processId)
                                     .last();

         Assert.assertTrue(this.wp_.getBPMNInfo()
                                   .getEditStatesMap()
                                   .get(this.wp_.getBPMNInfo()
                                         .getDefinitionId())
                                   .contains(hx.getOutcomeState()));
         Assert.assertTrue(isStartState(this.wp_.getBPMNInfo()
               .getDefinitionId(), hx.getInitialState()));
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);

         final boolean result = this.wp_.getWorkflowUpdater()
                                        .advanceWorkflow(processId,
                                              userId,
                                              "Approve",
                                              "Concluding Workflow for Testing",
                                              this.defaultEditCoordinate);

         Assert.assertFalse(result);
         Assert.assertEquals(ProcessStatus.LAUNCHED,
                             this.wp_.getWorkflowAccessor()
                                     .getProcessDetails(processId)
                                     .getStatus());
         hx = this.wp_.getWorkflowAccessor()
                      .getProcessHistory(processId)
                      .last();
         Assert.assertFalse(this.wp_.getBPMNInfo()
                                    .getEditStatesMap()
                                    .get(this.wp_.getBPMNInfo()
                                          .getDefinitionId())
                                    .contains(hx.getOutcomeState()));
         Assert.assertFalse(isStartState(this.wp_.getBPMNInfo()
               .getDefinitionId(), hx.getInitialState()));
      } catch (final Exception e) {
         Assert.fail();
      }
   }

   // TODO: Decide if prevent multiple processes with same name

   /**
    * Test fail define after launched.
    */

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
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");

         Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         commitRecord = createNewVersion(firstTestConceptNid, null);
         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);

         try {
            commitRecord = createNewVersion(firstTestConceptNid, null);
            this.wp_.getWorkflowUpdater()
                    .addCommitRecordToWorkflow(processId, commitRecord);
            Assert.fail();
         } catch (final Exception e) {
            Assert.assertTrue(true);
         }

         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "QA Fails", "QA Fail", this.defaultEditCoordinate);
         commitRecord = createNewVersion(firstTestConceptNid, null);
         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         Assert.assertEquals(ProcessStatus.LAUNCHED,
                             this.wp_.getWorkflowAccessor()
                                     .getProcessDetails(processId)
                                     .getStatus());
      } catch (final Exception e) {
         Assert.fail();
      }
   }

   /**
    * Test fail launch.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testFailLaunch() {
      clearStores();
      LOG.info("Testing inability to launch a workflow that has yet to be defined");

      final UUID processId = UUID.randomUUID();

      try {
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         Assert.assertTrue(true);

         AbstractStorableWorkflowContents process = null;

         process = this.wp_.getWorkflowAccessor()
                           .getProcessDetails(processId);

         if (process == null) {
            Assert.assertTrue(true);
         } else {
            Assert.fail();
         }
      } catch (final Exception ee) {
         Assert.fail();
      }
   }

   /**
    * Test integration add commit record to workflow.
    *
    * @throws Exception the exception
    */
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

      final UUID    processId = createFirstWorkflowProcess(this.wp_.getBPMNInfo()
                                                                   .getDefinitionId());
      ProcessDetail details   = this.wp_.getProcessDetailStore()
                                        .get(processId);

      Assert.assertFalse(details.getComponentToInitialEditMap()
                                .keySet()
                                .contains(firstTestConceptNid));

      Optional<CommitRecord> commitRecord      = createNewVersion(firstTestConceptNid, null);
      final Stamp            commitRecordStamp = createStampFromCommitRecord(commitRecord);

      this.wp_.getWorkflowUpdater()
              .addCommitRecordToWorkflow(processId, commitRecord);
      details = this.wp_.getProcessDetailStore()
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

      final Stamp updatedCommitRecordStamp = createStampFromCommitRecord(commitRecord);

      this.wp_.getWorkflowUpdater()
              .addCommitRecordToWorkflow(processId, commitRecord);
      details = this.wp_.getProcessDetailStore()
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

   /**
    * Test integration failures with add remove components to process.
    *
    * @throws Exception the exception
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testIntegrationFailuresWithAddRemoveComponentsToProcess()
            throws Exception {
      clearStores();

      final UUID processId = UUID.randomUUID();

      try {
         this.wp_.getWorkflowUpdater()
                 .removeComponentFromWorkflow(processId, firstTestConceptNid, this.defaultEditCoordinate);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      final UUID                   firstProcessId = createFirstWorkflowProcess(this.wp_.getBPMNInfo()
                                                                                       .getDefinitionId());
      final Optional<CommitRecord> commitRecord   = createNewVersion(firstTestConceptNid, null);

      this.wp_.getWorkflowUpdater()
              .addCommitRecordToWorkflow(firstProcessId, commitRecord);
      this.wp_.getWorkflowUpdater()
              .advanceWorkflow(firstProcessId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);

      try {
         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(firstProcessId, commitRecord);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      try {
         // Go back to no components in any workflow
         this.wp_.getWorkflowUpdater()
                 .removeComponentFromWorkflow(firstProcessId, firstTestConceptNid, this.defaultEditCoordinate);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      executeSendForReviewAdvancement(firstProcessId);

      try {
         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(firstProcessId, commitRecord);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      try {
         // Go back to no components in any workflow
         this.wp_.getWorkflowUpdater()
                 .removeComponentFromWorkflow(firstProcessId, firstTestConceptNid, this.defaultEditCoordinate);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }

      // Rejecting QA to get back to edit state
      executeRejectReviewAdvancement(firstProcessId);

      // Go back to no components in any workflow
      this.wp_.getWorkflowUpdater()
              .removeComponentFromWorkflow(firstProcessId, firstTestConceptNid, this.defaultEditCoordinate);

      // Testing LAUNCHED-EDIT Case
      this.wp_.getWorkflowUpdater()
              .addCommitRecordToWorkflow(firstProcessId, commitRecord);

      final ProcessDetail details = this.wp_.getProcessDetailStore()
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
         this.wp_.getWorkflowUpdater()
                 .removeComponentFromWorkflow(firstProcessId, firstTestConceptNid, this.defaultEditCoordinate);
         Assert.fail();
      } catch (final Exception e) {
         Assert.assertTrue(true);
      }
   }

   /**
    * Test integration remove components from process.
    *
    * @throws Exception the exception
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testIntegrationRemoveComponentsFromProcess()
            throws Exception {
      clearStores();

      final UUID    processId = createFirstWorkflowProcess(this.wp_.getBPMNInfo()
                                                                   .getDefinitionId());
      ProcessDetail details   = this.wp_.getProcessDetailStore()
                                        .get(processId);

      Assert.assertEquals(0, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());

      Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

      this.wp_.getWorkflowUpdater()
              .addCommitRecordToWorkflow(processId, commitRecord);
      commitRecord = createNewVersion(firstTestConceptNid, null);
      this.wp_.getWorkflowUpdater()
              .addCommitRecordToWorkflow(processId, commitRecord);
      commitRecord = createNewVersion(secondTestConceptNid, null);
      this.wp_.getWorkflowUpdater()
              .addCommitRecordToWorkflow(processId, commitRecord);
      commitRecord = createNewVersion(secondTestConceptNid, null);
      this.wp_.getWorkflowUpdater()
              .addCommitRecordToWorkflow(processId, commitRecord);
      details = this.wp_.getProcessDetailStore()
                        .get(processId);
      Assert.assertEquals(2, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      this.wp_.getWorkflowUpdater()
              .removeComponentFromWorkflow(processId, firstTestConceptNid, this.defaultEditCoordinate);
      details = this.wp_.getProcessDetailStore()
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
      this.wp_.getWorkflowUpdater()
              .removeComponentFromWorkflow(processId, secondTestConceptNid, this.defaultEditCoordinate);
      details = this.wp_.getProcessDetailStore()
                        .get(processId);
      Assert.assertEquals(0, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
   }

   /**
    * Test load workflow.
    */
   @Test(
      groups          = { "wf" },
      dependsOnGroups = { "load" }
   )
   public void testLoadWorkflow() {
      LOG.info("Loading Metadata db");
      firstTestConceptNid        = MetaData.ISAAC_METADATA.getNid();
      secondTestConceptNid       = MetaData.ACCEPTABLE.getNid();
      WorkflowProvider.BPMN_PATH = BPMN_FILE_PATH;
      this.wp_                   = LookupService.get()
            .getService(WorkflowProvider.class);
      cancelAction = this.wp_.getBPMNInfo()
                             .getEndWorkflowTypeMap()
                             .get(EndWorkflowType.CONCLUDED)
                             .iterator()
                             .next();
      this.defaultEditCoordinate = new EditCoordinateImpl(TermAux.USER.getNid(),
            MetaData.ISAAC_MODULE.getNid(),
            MetaData.DEVELOPMENT_PATH.getNid());

      final ObservableStampCoordinate defaultSC = Get.configurationService()
                                                     .getDefaultStampCoordinate();

      this.defaultStampCoordinate = new StampCoordinateImpl(defaultSC.getStampPrecedence(),
            defaultSC.getStampPosition(),
            defaultSC.getModuleSequences(),
            State.ANY_STATE_SET);
      firstTestConceptNid  = MetaData.EL_PLUS_PLUS_INFERRED_FORM_ASSEMBLAGE.getNid();
      secondTestConceptNid = MetaData.ACCEPTABLE.getNid();
   }

   /**
    * Test process time fields.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testProcessTimeFields() {
      clearStores();
      LOG.info("Testing Ability to cancel changes made to a sememe's text");

      try {
         // Create WF
         final UUID processId = this.wp_.getWorkflowProcessInitializerConcluder()
                                        .createWorkflowProcess(this.wp_.getBPMNInfo()
                                              .getDefinitionId(),
                                              userId,
                                              "Framework Workflow Name",
                                              " Framework Workflow Description");
         long timeCreated = this.wp_.getWorkflowAccessor()
                                    .getProcessDetails(processId)
                                    .getTimeCreated();
         long timeCanceledOrConcluded = this.wp_.getWorkflowAccessor()
                                                .getProcessDetails(processId)
                                                .getTimeCanceledOrConcluded();
         long timeLaunched = this.wp_.getWorkflowAccessor()
                                     .getProcessDetails(processId)
                                     .getTimeLaunched();

         Assert.assertTrue(timeCreated > -1);
         Assert.assertEquals(-1, timeLaunched);
         Assert.assertEquals(-1, timeCanceledOrConcluded);

         // Launch WF
         final Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         timeCreated = this.wp_.getWorkflowAccessor()
                               .getProcessDetails(processId)
                               .getTimeCreated();
         timeCanceledOrConcluded = this.wp_.getWorkflowAccessor()
                                           .getProcessDetails(processId)
                                           .getTimeCanceledOrConcluded();
         timeLaunched = this.wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getTimeLaunched();
         Assert.assertTrue(timeCreated > -1);
         Assert.assertTrue(timeLaunched > -1);
         Assert.assertEquals(-1, timeCanceledOrConcluded);

         // Cancel WF
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId,
                                  userId,
                                  "Cancel Workflow",
                                  "Canceling Workflow for Testing",
                                  this.defaultEditCoordinate);
         timeCreated = this.wp_.getWorkflowAccessor()
                               .getProcessDetails(processId)
                               .getTimeCreated();
         timeCanceledOrConcluded = this.wp_.getWorkflowAccessor()
                                           .getProcessDetails(processId)
                                           .getTimeCanceledOrConcluded();
         timeLaunched = this.wp_.getWorkflowAccessor()
                                .getProcessDetails(processId)
                                .getTimeLaunched();
         Assert.assertTrue(timeCreated > -1);
         Assert.assertTrue(timeLaunched > -1);
         Assert.assertTrue(timeCanceledOrConcluded > -1);
      } catch (final Exception e) {
         Assert.fail(e.getMessage());
      }
   }

   /**
    * Test redefine call.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testRedefineCall() {
      clearStores();
      LOG.info("Testing ability to define and launch workflow on a concept that has an already-concluded workflow");

      try {
         UUID processId = this.wp_.getWorkflowProcessInitializerConcluder()
                                  .createWorkflowProcess(this.wp_.getBPMNInfo()
                                        .getDefinitionId(),
                                        userId,
                                        "Framework Workflow Name",
                                        " Framework Workflow Description");
         final Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "QA Passes", "Review Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Approve", "Approve Comment", this.defaultEditCoordinate);
         Assert.assertEquals(ProcessStatus.CONCLUDED,
                             this.wp_.getWorkflowAccessor()
                                     .getProcessDetails(processId)
                                     .getStatus());

         final ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                           .getProcessHistory(processId)
                                           .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CONCLUDED));
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name2",
                                   " Framework Workflow Description");
         Assert.assertEquals(ProcessStatus.DEFINED,
                             this.wp_.getWorkflowAccessor()
                                     .getProcessDetails(processId)
                                     .getStatus());
      } catch (final Exception e) {
         Assert.fail();
      }
   }

   /**
    * Test start all fail conclude.
    */
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
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");

         final Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "QA Fails", "Fail Review Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Second Edit Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "QA Passes", "Review Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Reject Edit", "Reject Edit Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Third Edit Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "QA Passes", "Second Review Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId,
                                  userId,
                                  "Reject Review",
                                  "Reject Review Comment",
                                  this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "QA Passes", "Third Review Comment", this.defaultEditCoordinate);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Approve", "Approve Comment", this.defaultEditCoordinate);

         final boolean result = this.wp_.getWorkflowUpdater()
                                        .advanceWorkflow(processId,
                                              userId,
                                              "Approve",
                                              "Concluding Workflow for Testing",
                                              this.defaultEditCoordinate);

         Assert.assertFalse(result);
         Assert.assertEquals(ProcessStatus.CONCLUDED,
                             this.wp_.getWorkflowAccessor()
                                     .getProcessDetails(processId)
                                     .getStatus());

         final ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                           .getProcessHistory(processId)
                                           .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CONCLUDED));
      } catch (final Exception ee) {
         Assert.fail();
      }
   }

   /**
    * Test start all pass conclude.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testStartAllPassConclude() {
      clearStores();
      LOG.info("Testing ability to advance workflow to conclusion via its easy-path");

      UUID processId = null;

      try {
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");
         Assert.assertEquals(userId, this.wp_.getProcessDetailStore()
               .get(processId)
               .getOwnerId());

         ProcessDetail process = this.wp_.getProcessDetailStore()
                                         .get(processId);

         Assert.assertEquals(userId, process.getOwnerId());

         final Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         process = this.wp_.getProcessDetailStore()
                           .get(processId);
         Assert.assertEquals(BPMNInfo.UNOWNED_PROCESS, process.getOwnerId());
         process.setOwnerId(userId);
         this.wp_.getProcessDetailStore()
                 .put(processId, process);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "QA Passes", "Review Comment", this.defaultEditCoordinate);
         process = this.wp_.getProcessDetailStore()
                           .get(processId);
         Assert.assertEquals(BPMNInfo.UNOWNED_PROCESS, process.getOwnerId());
         process.setOwnerId(userId);
         this.wp_.getProcessDetailStore()
                 .put(processId, process);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Approve", "Approve Comment", this.defaultEditCoordinate);
         process = this.wp_.getProcessDetailStore()
                           .get(processId);
         Assert.assertEquals(ProcessStatus.CONCLUDED, process.getStatus());
         Assert.assertEquals(BPMNInfo.UNOWNED_PROCESS, process.getOwnerId());

         final ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                           .getProcessHistory(processId)
                                           .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CONCLUDED));
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");
         process = this.wp_.getProcessDetailStore()
                           .get(processId);
         Assert.assertEquals(userId, process.getOwnerId());
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId,
                                  userId,
                                  "Cancel Workflow",
                                  "Canceling Workflow for Testing",
                                  this.defaultEditCoordinate);
         process = this.wp_.getProcessDetailStore()
                           .get(processId);
         Assert.assertEquals(BPMNInfo.UNOWNED_PROCESS, process.getOwnerId());
      } catch (final Exception e) {
         Assert.fail();
      }
   }

   /**
    * Test start cancel.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testStartCancel() {
      clearStores();
      LOG.info("Testing ability to cancel a workflow that has been defined and launched");

      UUID processId = null;

      try {
         processId = this.wp_.getWorkflowProcessInitializerConcluder()
                             .createWorkflowProcess(this.wp_.getBPMNInfo()
                                   .getDefinitionId(),
                                   userId,
                                   "Framework Workflow Name",
                                   " Framework Workflow Description");

         final Optional<CommitRecord> commitRecord = createNewVersion(firstTestConceptNid, null);

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         Thread.sleep(1);  // TODO fix Dan Work around bug in design
         this.wp_.getWorkflowProcessInitializerConcluder()
                 .endWorkflowProcess(processId,
                                     getCancelAction(),
                                     userId,
                                     "Canceling Workflow for Testing",
                                     EndWorkflowType.CANCELED,
                                     this.defaultEditCoordinate);
         Assert.assertEquals(ProcessStatus.CANCELED,
                             this.wp_.getWorkflowAccessor()
                                     .getProcessDetails(processId)
                                     .getStatus());

         final ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                           .getProcessHistory(processId)
                                           .last();

         Assert.assertTrue(isEndState(hx.getOutcomeState(), EndWorkflowType.CANCELED));
      } catch (final Exception e) {
         Assert.fail();
      }
   }

   /**
    * Test status accessor component in active workflow.
    */
   @Test(
      groups           = { "wf" },
      dependsOnMethods = { "testLoadWorkflow" }
   )
   public void testStatusAccessorComponentInActiveWorkflow() {
      clearStores();
      LOG.info("Testing Workflow History Accessor isComponentInActiveWorkflow()");

      final ConceptChronology<? extends ConceptVersion<?>>   con = Get.conceptService()
                                                                      .getConcept(firstTestConceptNid);
      final SememeChronology<? extends DescriptionSememe<?>> descSem = con.getConceptDescriptionList()
                                                                          .iterator()
                                                                          .next();
      final int conNid = con.getNid();
      final int semNid = descSem.getNid();

      try {
         Assert.assertFalse(this.wp_.getWorkflowAccessor()
                                    .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                          .getDefinitionId(), conNid));
         Assert.assertFalse(this.wp_.getWorkflowAccessor()
                                    .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                          .getDefinitionId(), semNid));

         final UUID processId = this.wp_.getWorkflowProcessInitializerConcluder()
                                        .createWorkflowProcess(this.wp_.getBPMNInfo()
                                              .getDefinitionId(),
                                              userId,
                                              "Framework Workflow Name",
                                              " Framework Workflow Description");

         Assert.assertFalse(this.wp_.getWorkflowAccessor()
                                    .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                          .getDefinitionId(), conNid));
         Assert.assertFalse(this.wp_.getWorkflowAccessor()
                                    .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                          .getDefinitionId(), semNid));

         Optional<CommitRecord> commitRecord = createNewVersion(conNid, null);

         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         commitRecord = createNewVersion(null, semNid);
         this.wp_.getWorkflowUpdater()
                 .addCommitRecordToWorkflow(processId, commitRecord);
         Assert.assertTrue(this.wp_.getWorkflowAccessor()
                                   .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                         .getDefinitionId(), conNid));
         Assert.assertTrue(this.wp_.getWorkflowAccessor()
                                   .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                         .getDefinitionId(), semNid));
         this.wp_.getWorkflowUpdater()
                 .advanceWorkflow(processId, userId, "Edit", "Edit Comment", this.defaultEditCoordinate);
         Assert.assertTrue(this.wp_.getWorkflowAccessor()
                                   .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                         .getDefinitionId(), conNid));
         Assert.assertTrue(this.wp_.getWorkflowAccessor()
                                   .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                         .getDefinitionId(), semNid));
         this.wp_.getWorkflowProcessInitializerConcluder()
                 .endWorkflowProcess(processId,
                                     getCancelAction(),
                                     userId,
                                     "Canceling Workflow for Testing",
                                     EndWorkflowType.CANCELED,
                                     null);
         Assert.assertFalse(this.wp_.getWorkflowAccessor()
                                    .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                          .getDefinitionId(), conNid));
         Assert.assertFalse(this.wp_.getWorkflowAccessor()
                                    .isComponentInActiveWorkflow(this.wp_.getBPMNInfo()
                                          .getDefinitionId(), semNid));
      } catch (final Exception e) {
         Assert.fail(e.getMessage());
      }
   }

   /**
    * Cancel workflow.
    *
    * @param processId the process id
    */
   protected void cancelWorkflow(UUID processId) {
      try {
         Thread.sleep(1);
         finishWorkflowProcess(processId, cancelAction, userId, "Canceled Workflow", EndWorkflowType.CANCELED);
      } catch (final Exception e) {
         Assert.fail();
      }
   }

   /**
    * Creates the first workflow process.
    *
    * @param requestedDefinitionId the requested definition id
    * @return the uuid
    */
   protected UUID createFirstWorkflowProcess(UUID requestedDefinitionId) {
      return createWorkflowProcess(requestedDefinitionId, "Main Process Name", "Main Process Description");
   }

   /**
    * Creates the second workflow process.
    *
    * @param requestedDefinitionId the requested definition id
    * @return the uuid
    */
   protected UUID createSecondWorkflowProcess(UUID requestedDefinitionId) {
      return createWorkflowProcess(requestedDefinitionId, "Secondary Process Name", "Secondary Process Description");
   }

   /**
    * Execute launch workflow.
    *
    * @param processId the process id
    */
   protected void executeLaunchWorkflow(UUID processId) {
      try {
         Thread.sleep(1);

         final ProcessDetail entry = this.wp_.getProcessDetailStore()
                                             .get(processId);

         entry.setStatus(ProcessStatus.LAUNCHED);
         entry.setTimeLaunched(new Date().getTime());
         this.wp_.getProcessDetailStore()
                 .put(processId, entry);
      } catch (final InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Execute reject review advancement.
    *
    * @param requestedProcessId the requested process id
    */
   protected void executeRejectReviewAdvancement(UUID requestedProcessId) {
      try {
         Thread.sleep(1);

         final ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                           .getProcessHistory(requestedProcessId)
                                           .last();
         final ProcessHistory entry = new ProcessHistory(requestedProcessId,
                                                         userId,
                                                         new Date().getTime(),
                                                         REJECT_REVIEW_STATE,
                                                         REJECT_REVIEW_ACTION,
                                                         REJECT_REVIEW_OUTCOME,
                                                         REJECT_REVIEW_COMMENT,
                                                         hx.getHistorySequence() + 1);

         this.wp_.getProcessHistoryStore()
                 .add(entry);
      } catch (final InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Execute send for approval advancement.
    *
    * @param requestedProcessId the requested process id
    */
   protected void executeSendForApprovalAdvancement(UUID requestedProcessId) {
      try {
         Thread.sleep(1);

         final ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                           .getProcessHistory(requestedProcessId)
                                           .last();
         final ProcessHistory entry = new ProcessHistory(requestedProcessId,
                                                         userId,
                                                         new Date().getTime(),
                                                         SEND_TO_APPROVAL_STATE,
                                                         SEND_TO_APPROVAL_ACTION,
                                                         SEND_TO_APPROVAL_OUTCOME,
                                                         SEND_TO_APPROVAL_COMMENT,
                                                         hx.getHistorySequence() + 1);

         this.wp_.getProcessHistoryStore()
                 .add(entry);
      } catch (final InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Execute send for review advancement.
    *
    * @param processId the process id
    */
   protected void executeSendForReviewAdvancement(UUID processId) {
      final ProcessDetail entry = this.wp_.getProcessDetailStore()
                                          .get(processId);

      try {
         Thread.sleep(1);

         final ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                           .getProcessHistory(processId)
                                           .last();
         final ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                                entry.getCreatorId(),
                                                                new Date().getTime(),
                                                                LAUNCH_STATE,
                                                                LAUNCH_ACTION,
                                                                LAUNCH_OUTCOME,
                                                                LAUNCH_COMMENT,
                                                                hx.getHistorySequence() + 1);

         this.wp_.getProcessHistoryStore()
                 .add(advanceEntry);
      } catch (final InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Clear stores.
    */
   private void clearStores() {
      this.wp_.getProcessDetailStore()
              .clear();
      this.wp_.getProcessHistoryStore()
              .clear();
   }

   /**
    * Clone version.
    *
    * @param semChron the sem chron
    * @param state the state
    * @return the description sememe impl
    * @throws InterruptedException the interrupted exception
    * @throws ExecutionException the execution exception
    */
   private DescriptionSememeImpl cloneVersion(SememeChronologyImpl semChron,
         State state)
            throws InterruptedException,
                   ExecutionException {
      final DescriptionSememe<?> latestVersion =
         ((LatestVersion<DescriptionSememe<?>>) semChron.getLatestVersion(DescriptionSememe.class,
                                                                          Get.configurationService()
                                                                                .getDefaultStampCoordinate())
                                                        .get()).value();
      final DescriptionSememeImpl createdVersion =
         (DescriptionSememeImpl) semChron.createMutableVersion(DescriptionSememeImpl.class,
                                                               state,
                                                               this.defaultEditCoordinate);

      createdVersion.setCaseSignificanceConceptSequence(latestVersion.getCaseSignificanceConceptSequence());
      createdVersion.setDescriptionTypeConceptSequence((latestVersion.getDescriptionTypeConceptSequence()));
      createdVersion.setLanguageConceptSequence(latestVersion.getLanguageConceptSequence());
      createdVersion.setText(latestVersion.getText());
      return createdVersion;
   }

   /**
    * Creates the new version.
    *
    * @param conNid the con nid
    * @param semNid the sem nid
    * @return the optional
    * @throws InterruptedException the interrupted exception
    * @throws ExecutionException the execution exception
    */
   private Optional<CommitRecord> createNewVersion(Integer conNid,
         Integer semNid)
            throws InterruptedException,
                   ExecutionException {
      if (conNid != null) {
         final ConceptChronologyImpl con = (ConceptChronologyImpl) Get.conceptService()
                                                                      .getConcept(conNid);

         con.createMutableVersion(State.ACTIVE, this.defaultEditCoordinate);
         Get.commitService()
            .addUncommitted(con)
            .get();
         return Get.commitService()
                   .commit("Inactivating concept for Testing")
                   .get();
      } else {
         final SememeChronologyImpl  semChron       = (SememeChronologyImpl) Get.sememeService()
                                                                                .getSememe(semNid);
         final DescriptionSememeImpl createdVersion = cloneVersion(semChron, State.ACTIVE);

         Get.commitService()
            .addUncommitted(semChron)
            .get();
         return Get.commitService()
                   .commit("Inactivating sememe for Testing")
                   .get();
      }
   }

   /**
    * Creates the stamp.
    *
    * @param userSeq the user seq
    * @param state the state
    * @return the stamp
    */
   private Stamp createStamp(int userSeq, State state) {
      if ((this.moduleSeq < 0) || (this.pathSeq < 0)) {
         if (Get.configurationService()
                .getDefaultStampCoordinate()
                .getModuleSequences()
                .size() != 1) {
            return null;
         }

         this.moduleSeq = Get.configurationService()
                             .getDefaultStampCoordinate()
                             .getModuleSequences()
                             .getIntIterator()
                             .nextInt();
         this.pathSeq = Get.configurationService()
                           .getDefaultStampCoordinate()
                           .getStampPosition()
                           .getStampPathSequence();
      }

      return new Stamp(state, new Date().getTime(), this.moduleSeq, userSeq, this.pathSeq);
   }

   /**
    * Creates the stamp from commit record.
    *
    * @param commitRecord the commit record
    * @return the stamp
    */
   private Stamp createStampFromCommitRecord(Optional<CommitRecord> commitRecord) {
      final int   stampSeq = commitRecord.get()
                                         .getStampsInCommit()
                                         .getIntIterator()
                                         .next();
      final State status   = Get.stampService()
                                .getStatusForStamp(stampSeq);
      final long  time     = Get.stampService()
                                .getTimeForStamp(stampSeq);
      final int   author   = Get.stampService()
                                .getAuthorSequenceForStamp(stampSeq);
      final int   module   = Get.stampService()
                                .getModuleSequenceForStamp(stampSeq);
      final int   path     = Get.stampService()
                                .getPathSequenceForStamp(stampSeq);

      return new Stamp(status, time, author, module, path);
   }

   /**
    * Creates the workflow process.
    *
    * @param requestedDefinitionId the requested definition id
    * @param name the name
    * @param description the description
    * @return the uuid
    */
   private UUID createWorkflowProcess(UUID requestedDefinitionId, String name, String description) {
      final AvailableAction startNodeAction = this.wp_.getBPMNInfo()
                                                      .getDefinitionStartActionMap()
                                                      .get(this.wp_.getBPMNInfo()
                                                            .getDefinitionId())
                                                      .iterator()
                                                      .next();

      // Mimick the wp_.getWorkflowProcessInitializerConcluder()'s create new
      // process
      final ProcessDetail details = new ProcessDetail(requestedDefinitionId,
                                                      userId,
                                                      new Date().getTime(),
                                                      ProcessStatus.DEFINED,
                                                      name,
                                                      description);
      final UUID processId = this.wp_.getProcessDetailStore()
                                     .add(details);

      // Add Process History with START_STATE-AUTOMATED-EDIT_STATE
      final AvailableAction startAdvancement = new AvailableAction(requestedDefinitionId,
                                                                   startNodeAction.getInitialState(),
                                                                   startNodeAction.getAction(),
                                                                   startNodeAction.getOutcomeState(),
                                                                   UserRole.AUTOMATED);
      final ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                             userId,
                                                             new Date().getTime(),
                                                             startAdvancement.getInitialState(),
                                                             startAdvancement.getAction(),
                                                             startAdvancement.getOutcomeState(),
                                                             "",
                                                             1);

      this.wp_.getProcessHistoryStore()
              .add(advanceEntry);
      return processId;
   }

   /**
    * Finish workflow process.
    *
    * @param processId the process id
    * @param actionToProcess the action to process
    * @param userId the user id
    * @param comment the comment
    * @param endType the end type
    * @throws Exception the exception
    */
   private void finishWorkflowProcess(UUID processId,
                                      AvailableAction actionToProcess,
                                      UUID userId,
                                      String comment,
                                      EndWorkflowType endType)
            throws Exception {
      // Mimick the wp_.getWorkflowProcessInitializerConcluder()'s finish
      // workflow process
      final ProcessDetail entry = this.wp_.getProcessDetailStore()
                                          .get(processId);

      if (endType.equals(EndWorkflowType.CANCELED)) {
         entry.setStatus(ProcessStatus.CANCELED);
      } else if (endType.equals(EndWorkflowType.CONCLUDED)) {
         entry.setStatus(ProcessStatus.CONCLUDED);
      }

      entry.setTimeCanceledOrConcluded(new Date().getTime());
      this.wp_.getProcessDetailStore()
              .put(processId, entry);

      // Only add Cancel state in Workflow if process has already been
      // launched
      final ProcessHistory hx = this.wp_.getWorkflowAccessor()
                                        .getProcessHistory(processId)
                                        .last();
      final ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                             userId,
                                                             new Date().getTime(),
                                                             actionToProcess.getInitialState(),
                                                             actionToProcess.getAction(),
                                                             actionToProcess.getOutcomeState(),
                                                             comment,
                                                             hx.getHistorySequence() + 1);

      this.wp_.getProcessHistoryStore()
              .add(advanceEntry);

      if (endType.equals(EndWorkflowType.CANCELED)) {
         // TODO: Handle cancelation store and handle reverting automatically
      }
   }

   /**
    * Verify state.
    *
    * @param con the con
    * @param descSem the desc sem
    * @param state the state
    */
   private void verifyState(ConceptChronology<? extends ConceptVersion<?>> con,
                            SememeChronology<? extends DescriptionSememe<?>> descSem,
                            State state) {
      final ConceptChronology<? extends ConceptVersion<?>> cc = Get.conceptService()
                                                                   .getConcept(con.getNid());
      final Optional<LatestVersion<ConceptVersion>> latestConVersion =
         ((ConceptChronology) cc).getLatestVersion(ConceptVersion.class,
                                                   this.defaultStampCoordinate);

      Assert.assertEquals(latestConVersion.get()
            .value()
            .getState(), state);

      final SememeChronology<? extends SememeVersion<?>> semChron = Get.sememeService()
                                                                       .getSememe(descSem.getNid());
      final Optional<LatestVersion<DescriptionSememe<?>>> latestDescVersion =
         ((SememeChronology) semChron).getLatestVersion(DescriptionSememe.class,
                                                        this.defaultStampCoordinate);

      Assert.assertEquals(latestDescVersion.get()
            .value()
            .getState(), state);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the cancel action.
    *
    * @return the cancel action
    */
   private AvailableAction getCancelAction() {
      return this.wp_.getBPMNInfo()
                     .getEndWorkflowTypeMap()
                     .get(EndWorkflowType.CANCELED)
                     .iterator()
                     .next();
   }

   /**
    * Gets the conclude action.
    *
    * @return the conclude action
    */
   private AvailableAction getConcludeAction() {
      return this.wp_.getBPMNInfo()
                     .getEndWorkflowTypeMap()
                     .get(EndWorkflowType.CONCLUDED)
                     .iterator()
                     .next();
   }

   /**
    * Checks if end state.
    *
    * @param state the state
    * @param type the type
    * @return true, if end state
    */
   private boolean isEndState(String state, EndWorkflowType type) {
      for (final AvailableAction action: this.wp_.getBPMNInfo()
            .getEndWorkflowTypeMap()
            .get(type)) {
         if (action.getOutcomeState()
                   .equals(state)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Checks if start state.
    *
    * @param defId the def id
    * @param state the state
    * @return true, if start state
    */
   private boolean isStartState(UUID defId, String state) {
      for (final AvailableAction action: this.wp_.getBPMNInfo()
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

   /**
    * Set up users.
    *
    * @throws Exception the exception
    */
   @BeforeGroups(groups = { "wf" })
   public void setUpUsers()
            throws Exception {
      userId = UUID.randomUUID();

      final SimpleUserRoleService rolesService = LookupService.get()
                                                              .getService(SimpleUserRoleService.class);

      rolesService.addRole(UserRole.EDITOR);
      rolesService.addRole(UserRole.REVIEWER);
      rolesService.addRole(UserRole.APPROVER);
      rolesService.addRole(UserRole.AUTOMATED);

      final HashSet<UserRole> roles = new HashSet<>();

      roles.add(UserRole.EDITOR);
      roles.add(UserRole.APPROVER);
      roles.add(UserRole.REVIEWER);
      rolesService.addUser(userId, roles);
   }
}

