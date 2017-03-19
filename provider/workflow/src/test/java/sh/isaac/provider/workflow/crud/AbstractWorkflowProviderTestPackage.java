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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.Assert;

import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.UserRole;
import sh.isaac.api.commit.Stamp;
import sh.isaac.provider.workflow.WorkflowProvider;
import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.DefinitionDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.EndWorkflowType;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.ProcessStatus;
import sh.isaac.provider.workflow.model.contents.ProcessHistory;
import sh.isaac.provider.workflow.user.RoleConfigurator;

//~--- classes ----------------------------------------------------------------

/**
 * Test the AbstractWorkflowProviderTestPackage class
 *
 * {@link WorkflowProcessInitializerConcluderTest}.
 * {@link WorkflowAccessorTest}.
 * {@link WorkflowUpdaterTest}.
 *
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public abstract class AbstractWorkflowProviderTestPackage {
   /** The Constant logger. */
   protected static final Logger logger = LogManager.getLogger();

   /** The bpmn file path. */
   protected static final String BPMN_FILE_PATH = "/sh/isaac/provider/workflow/StaticUnitTestingDefinition.bpmn2";
   
   /** The create role. */
   private static UserRole       createRole     = UserRole.AUTOMATED;

   /** The Constant TEST_START_TIME. */
   /* Constants throughout testclasses to simplify process */
   private static final long           TEST_START_TIME            = new Date().getTime();
   
   /** The Constant conceptsForTesting. */
   protected static final Set<Integer> conceptsForTesting         = new HashSet<>(Arrays.asList(-55, -56));
   
   /** The Constant LAUNCH_STATE. */
   private static final String         LAUNCH_STATE               = "Ready for Edit";
   
   /** The Constant LAUNCH_ACTION. */
   private static final String         LAUNCH_ACTION              = "Edit";
   
   /** The Constant LAUNCH_OUTCOME. */
   private static final String         LAUNCH_OUTCOME             = "Ready for Review";
   
   /** The Constant LAUNCH_COMMENT. */
   private static final String         LAUNCH_COMMENT             = "Launch Comment";
   
   /** The Constant SEND_TO_APPROVAL_STATE. */
   private static final String         SEND_TO_APPROVAL_STATE     = "Ready for Review";
   
   /** The Constant SEND_TO_APPROVAL_ACTION. */
   private static final String         SEND_TO_APPROVAL_ACTION    = "Review";
   
   /** The Constant SEND_TO_APPROVAL_OUTCOME. */
   private static final String         SEND_TO_APPROVAL_OUTCOME   = "Ready for Approve";
   
   /** The Constant SEND_TO_APPROVAL_COMMENT. */
   private static final String         SEND_TO_APPROVAL_COMMENT   = "Sending for Approval";
   
   /** The Constant REJECT_REVIEW_STATE. */
   private static final String         REJECT_REVIEW_STATE        = "Ready for Review";
   
   /** The Constant REJECT_REVIEW_ACTION. */
   private static final String         REJECT_REVIEW_ACTION       = "Reject QA";
   
   /** The Constant REJECT_REVIEW_OUTCOME. */
   private static final String         REJECT_REVIEW_OUTCOME      = "Ready for Edit";
   
   /** The Constant REJECT_REVIEW_COMMENT. */
   private static final String         REJECT_REVIEW_COMMENT      = "Rejecting QA sending back to Edit";
   
   /** The Constant CONCLUDED_WORKFLOW_COMMENT. */
   protected static final String       CONCLUDED_WORKFLOW_COMMENT = "Concluded Workflow";
   
   /** The Constant CANCELED_WORKFLOW_COMMENT. */
   protected static final String       CANCELED_WORKFLOW_COMMENT  = "Canceled Workflow";
   
   /** The module seq. */
   private static int                  moduleSeq                  = 99;
   
   /** The path seq. */
   private static int                  pathSeq                    = 999;
   
   /** The conclude action. */
   protected static AvailableAction    concludeAction;
   
   /** The cancel action. */
   protected static AvailableAction    cancelAction;

   /** The main definition id. */
   /*
    * Defined by importing definition and static throughout testclasses to
    * simplify process
    */
   protected static UUID             mainDefinitionId;
   
   /** The create state. */
   private static String             createState;
   
   /** The create action. */
   private static String             createAction;
   
   /** The create outcome. */
   private static String             createOutcome;
   
   /** The wp. */
   protected static WorkflowProvider wp_;

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the components to process.
    *
    * @param processId the process id
    * @param userSeq the user seq
    * @param state the state
    */
   protected void addComponentsToProcess(UUID processId, int userSeq, State state) {
      final ProcessDetail entry = wp_.getProcessDetailStore()
                               .get(processId);
      final Stamp         s     = createStamp(userSeq, state);

      for (final Integer con: conceptsForTesting) {
         entry.getComponentToInitialEditMap()
              .put(con, s);
      }

      wp_.getProcessDetailStore()
         .put(processId, entry);
   }

   /**
    * Advance workflow.
    *
    * @param processId the process id
    * @param userId the user id
    * @param actionRequested the action requested
    * @param comment the comment
    * @return true, if successful
    * @throws Exception the exception
    */
   protected boolean advanceWorkflow(UUID processId,
                                     UUID userId,
                                     String actionRequested,
                                     String comment)
            throws Exception {
      return wp_.getWorkflowUpdater()
                .advanceWorkflow(processId, userId, actionRequested, comment, null);
   }

   /**
    * Assert cancel history.
    *
    * @param entry the entry
    * @param processId the process id
    */
   protected void assertCancelHistory(ProcessHistory entry, UUID processId) {
      Assert.assertEquals(processId, entry.getProcessId());
      Assert.assertEquals(RoleConfigurator.getFirstTestUser(), entry.getUserId());
      Assert.assertTrue(TEST_START_TIME < entry.getTimeAdvanced());
      Assert.assertEquals(cancelAction.getInitialState(), entry.getInitialState());
      Assert.assertEquals(cancelAction.getAction(), entry.getAction());
      Assert.assertEquals(cancelAction.getOutcomeState(), entry.getOutcomeState());
      Assert.assertEquals(CANCELED_WORKFLOW_COMMENT, entry.getComment());
   }

   /**
    * Assert conclude history.
    *
    * @param entry the entry
    * @param processId the process id
    */
   protected void assertConcludeHistory(ProcessHistory entry, UUID processId) {
      Assert.assertEquals(processId, entry.getProcessId());
      Assert.assertEquals(RoleConfigurator.getFirstTestUser(), entry.getUserId());
      Assert.assertTrue(TEST_START_TIME < entry.getTimeAdvanced());
      Assert.assertEquals(concludeAction.getInitialState(), entry.getInitialState());
      Assert.assertEquals(concludeAction.getAction(), entry.getAction());
      Assert.assertEquals(concludeAction.getOutcomeState(), entry.getOutcomeState());
      Assert.assertEquals(CONCLUDED_WORKFLOW_COMMENT, entry.getComment());
   }

   /**
    * Assert history for process.
    *
    * @param allProcessHistory the all process history
    * @param processId the process id
    */
   protected void assertHistoryForProcess(SortedSet<ProcessHistory> allProcessHistory, UUID processId) {
      int counter = 0;

      for (final ProcessHistory entry: allProcessHistory) {
         if (counter == 0) {
            Assert.assertEquals(processId, entry.getProcessId());
            Assert.assertEquals(RoleConfigurator.getFirstTestUser(), entry.getUserId());
            Assert.assertTrue(TEST_START_TIME < entry.getTimeAdvanced());
            Assert.assertEquals(createState, entry.getInitialState());
            Assert.assertEquals(createAction, entry.getAction());
            Assert.assertEquals(createOutcome, entry.getOutcomeState());
            Assert.assertEquals("", entry.getComment());
         } else if (counter == 1) {
            Assert.assertEquals(processId, entry.getProcessId());
            Assert.assertEquals(RoleConfigurator.getFirstTestUser(), entry.getUserId());
            Assert.assertTrue(TEST_START_TIME < entry.getTimeAdvanced());
            Assert.assertEquals(LAUNCH_STATE, entry.getInitialState());
            Assert.assertEquals(LAUNCH_ACTION, entry.getAction());
            Assert.assertEquals(LAUNCH_OUTCOME, entry.getOutcomeState());
            Assert.assertEquals(LAUNCH_COMMENT, entry.getComment());
         } else if (counter == 2) {
            Assert.assertEquals(processId, entry.getProcessId());
            Assert.assertEquals(RoleConfigurator.getFirstTestUser(), entry.getUserId());
            Assert.assertTrue(TEST_START_TIME < entry.getTimeAdvanced());
            Assert.assertEquals(SEND_TO_APPROVAL_STATE, entry.getInitialState());
            Assert.assertEquals(SEND_TO_APPROVAL_ACTION, entry.getAction());
            Assert.assertEquals(SEND_TO_APPROVAL_OUTCOME, entry.getOutcomeState());
            Assert.assertEquals(SEND_TO_APPROVAL_COMMENT, entry.getComment());
         }

         counter++;
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
         finishWorkflowProcess(processId,
                               cancelAction,
                               RoleConfigurator.getFirstTestUser(),
                               "Canceled Workflow",
                               EndWorkflowType.CANCELED);
      } catch (final Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Conclude workflow.
    *
    * @param processId the process id
    */
   protected void concludeWorkflow(UUID processId) {
      try {
         Thread.sleep(1);
         finishWorkflowProcess(processId,
                               concludeAction,
                               RoleConfigurator.getFirstTestUser(),
                               "Concluded Workflow",
                               EndWorkflowType.CONCLUDED);
      } catch (final Exception e) {
         e.printStackTrace();
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
    * Creates the secondary definition.
    *
    * @return the uuid
    */
   protected UUID createSecondaryDefinition() {
      final Set<UserRole> roles = new HashSet<>();

      roles.add(UserRole.EDITOR);
      roles.add(UserRole.REVIEWER);
      roles.add(UserRole.APPROVER);

      final DefinitionDetail createdEntry = new DefinitionDetail("BPMN2 ID-X",
                                                           "JUnit BPMN2",
                                                           "Testing",
                                                           "1.0",
                                                           roles,
                                                           "Description of BPMN2 ID-X");
      final UUID defId = wp_.getDefinitionDetailStore()
                      .add(createdEntry);

      // Duplicate AvailableActions
      final Set<AvailableAction> actionsToAdd = new HashSet<>();

      for (final AvailableAction action: wp_.getAvailableActionStore()
                                      .values()) {
         actionsToAdd.add(new AvailableAction(defId,
               action.getInitialState(),
               action.getAction(),
               action.getOutcomeState(),
               action.getRole()));
      }

      for (final AvailableAction action: actionsToAdd) {
         wp_.getAvailableActionStore()
            .add(action);
      }

      return defId;
   }

   /**
    * Creates the stamp.
    *
    * @param userSeq the user seq
    * @param state the state
    * @return the stamp
    */
   protected Stamp createStamp(int userSeq, State state) {
      return new Stamp(state, new Date().getTime(), userSeq, moduleSeq, pathSeq);
   }

   /**
    * End workflow process.
    *
    * @param processId the process id
    * @param actionToProcess the action to process
    * @param userId the user id
    * @param comment the comment
    * @param endType the end type
    * @throws Exception the exception
    */
   protected void endWorkflowProcess(UUID processId,
                                     AvailableAction actionToProcess,
                                     UUID userId,
                                     String comment,
                                     EndWorkflowType endType)
            throws Exception {
      wp_.getWorkflowProcessInitializerConcluder()
         .endWorkflowProcess(processId, actionToProcess, userId, comment, endType, null);
   }

   /**
    * Execute launch workflow.
    *
    * @param processId the process id
    */
   protected void executeLaunchWorkflow(UUID processId) {
      try {
         Thread.sleep(1);

         final ProcessDetail entry = wp_.getProcessDetailStore()
                                  .get(processId);

         entry.setStatus(ProcessStatus.LAUNCHED);
         entry.setTimeLaunched(new Date().getTime());
         wp_.getProcessDetailStore()
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

         int historySequence = 1;

         if (wp_.getWorkflowAccessor()
                .getProcessHistory(requestedProcessId) != null) {
            historySequence = wp_.getWorkflowAccessor()
                                 .getProcessHistory(requestedProcessId)
                                 .last()
                                 .getHistorySequence();
         }

         final ProcessHistory entry = new ProcessHistory(requestedProcessId,
                                                   RoleConfigurator.getFirstTestUser(),
                                                   new Date().getTime(),
                                                   REJECT_REVIEW_STATE,
                                                   REJECT_REVIEW_ACTION,
                                                   REJECT_REVIEW_OUTCOME,
                                                   REJECT_REVIEW_COMMENT,
                                                   historySequence + 1);

         wp_.getProcessHistoryStore()
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

         int historySequence = 1;

         if (wp_.getWorkflowAccessor()
                .getProcessHistory(requestedProcessId) != null) {
            historySequence = wp_.getWorkflowAccessor()
                                 .getProcessHistory(requestedProcessId)
                                 .last()
                                 .getHistorySequence();
         }

         final ProcessHistory entry = new ProcessHistory(requestedProcessId,
                                                   RoleConfigurator.getFirstTestUser(),
                                                   new Date().getTime(),
                                                   SEND_TO_APPROVAL_STATE,
                                                   SEND_TO_APPROVAL_ACTION,
                                                   SEND_TO_APPROVAL_OUTCOME,
                                                   SEND_TO_APPROVAL_COMMENT,
                                                   historySequence + 1);

         wp_.getProcessHistoryStore()
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
      final ProcessDetail entry = wp_.getProcessDetailStore()
                               .get(processId);

      try {
         Thread.sleep(1);

         int historySequence = 1;

         if (wp_.getWorkflowAccessor()
                .getProcessHistory(processId) != null) {
            historySequence = wp_.getWorkflowAccessor()
                                 .getProcessHistory(processId)
                                 .last()
                                 .getHistorySequence();
         }

         final ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                          entry.getCreatorId(),
                                                          new Date().getTime(),
                                                          LAUNCH_STATE,
                                                          LAUNCH_ACTION,
                                                          LAUNCH_OUTCOME,
                                                          LAUNCH_COMMENT,
                                                          historySequence + 1);

         wp_.getProcessHistoryStore()
            .add(advanceEntry);
      } catch (final InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Global setup.
    */
   protected static void globalSetup() {
      RoleConfigurator.configureForTest();
      wp_              = LookupService.get()
                                      .getService(WorkflowProvider.class);
      mainDefinitionId = wp_.getBPMNInfo()
                            .getDefinitionId();

      final AvailableAction startNodeAction = wp_.getBPMNInfo()
                                           .getDefinitionStartActionMap()
                                           .get(mainDefinitionId)
                                           .iterator()
                                           .next();

      createState    = startNodeAction.getInitialState();
      createAction   = startNodeAction.getAction();
      createOutcome  = startNodeAction.getOutcomeState();
      cancelAction   = wp_.getBPMNInfo()
                          .getEndWorkflowTypeMap()
                          .get(EndWorkflowType.CONCLUDED)
                          .iterator()
                          .next();
      concludeAction = wp_.getBPMNInfo()
                          .getEndWorkflowTypeMap()
                          .get(EndWorkflowType.CONCLUDED)
                          .iterator()
                          .next();
   }

   /**
    * Time since yesterday before tomorrow.
    *
    * @param time the time
    * @return true, if successful
    */
   protected boolean timeSinceYesterdayBeforeTomorrow(long time) {
      Calendar cal = Calendar.getInstance();

      cal.add(Calendar.DATE, -1);

      final long yesterdayTimestamp = cal.getTimeInMillis();

      cal = Calendar.getInstance();
      cal.add(Calendar.DATE, 1);

      final long tomorrowTimestamp = cal.getTimeInMillis();

      return (time >= yesterdayTimestamp) && (time <= tomorrowTimestamp);
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
      // Mimick the initConcluder's create new process
      final ProcessDetail details = new ProcessDetail(requestedDefinitionId,
                                                RoleConfigurator.getFirstTestUser(),
                                                new Date().getTime(),
                                                ProcessStatus.DEFINED,
                                                name,
                                                description);
      final UUID processId = wp_.getProcessDetailStore()
                          .add(details);

      // Add Process History with START_STATE-AUTOMATED-EDIT_STATE
      final AvailableAction startAdvancement = new AvailableAction(requestedDefinitionId,
                                                             createState,
                                                             createAction,
                                                             createOutcome,
                                                             createRole);
      final ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                       RoleConfigurator.getFirstTestUser(),
                                                       new Date().getTime(),
                                                       startAdvancement.getInitialState(),
                                                       startAdvancement.getAction(),
                                                       startAdvancement.getOutcomeState(),
                                                       "",
                                                       0);

      wp_.getProcessHistoryStore()
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
      // Mimick the initConcluder's finish workflow process
      final ProcessDetail entry = wp_.getProcessDetailStore()
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
      int historySequence = 1;

      if (wp_.getWorkflowAccessor()
             .getProcessHistory(processId) != null) {
         historySequence = wp_.getWorkflowAccessor()
                              .getProcessHistory(processId)
                              .last()
                              .getHistorySequence();
      }

      final ProcessHistory advanceEntry = new ProcessHistory(processId,
                                                       userId,
                                                       new Date().getTime(),
                                                       actionToProcess.getInitialState(),
                                                       actionToProcess.getAction(),
                                                       actionToProcess.getOutcomeState(),
                                                       comment,
                                                       historySequence + 1);

      wp_.getProcessHistoryStore()
         .add(advanceEntry);

      if (endType.equals(EndWorkflowType.CANCELED)) {
         // TODO: Handle cancelation store and handle reverting automatically
      }
   }
}

