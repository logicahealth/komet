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
import sh.isaac.provider.workflow.user.RoleConfigurator;

//~--- classes ----------------------------------------------------------------

/**
 * Test the WorkflowUpdater class
 *
 * {@link WorkflowUpdater}. {@link AbstractWorkflowProviderTestPackage}.
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class WorkflowUpdaterTest
        extends AbstractWorkflowProviderTestPackage {
   private static int firstConceptNid  = 0;
   private static int secondConceptNid = 0;

   //~--- methods -------------------------------------------------------------

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
    * Test ability to add components and stamps to the process.
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test

   /*
    * Note this is a simplified test as using addComponentToWorkflow. More
    * realistic and complex test, using addCommitRecordToWorkflow, is found in
    * WorkflowFrameworkTest as commitRecords require IdentifierService.
    */
   public void testAddComponentsToProcess()
            throws Exception {
      UUID          processId = createFirstWorkflowProcess(mainDefinitionId);
      ProcessDetail details   = wp_.getProcessDetailStore()
                                   .get(processId);

      Assert.assertFalse(details.getComponentToInitialEditMap()
                                .keySet()
                                .contains(firstConceptNid));
      addComponentsToProcess(processId, RoleConfigurator.getFirstTestUserSeq(), State.ACTIVE);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(2, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      Assert.assertTrue(details.getComponentToInitialEditMap()
                               .keySet()
                               .contains(firstConceptNid));
      Assert.assertTrue(details.getComponentToInitialEditMap()
                               .keySet()
                               .contains(secondConceptNid));
      Assert.assertEquals(RoleConfigurator.getFirstTestUserSeq(),
                          details.getComponentToInitialEditMap()
                                 .get(firstConceptNid)
                                 .getAuthorSequence());
      Assert.assertEquals(RoleConfigurator.getFirstTestUserSeq(),
                          details.getComponentToInitialEditMap()
                                 .get(secondConceptNid)
                                 .getAuthorSequence());
      addComponentsToProcess(processId, RoleConfigurator.getSecondTestUserSeq(), State.ACTIVE);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(2, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      Assert.assertTrue(details.getComponentToInitialEditMap()
                               .keySet()
                               .contains(firstConceptNid));
      Assert.assertTrue(details.getComponentToInitialEditMap()
                               .keySet()
                               .contains(secondConceptNid));
      Assert.assertEquals(RoleConfigurator.getSecondTestUserSeq(),
                          details.getComponentToInitialEditMap()
                                 .get(firstConceptNid)
                                 .getAuthorSequence());
      Assert.assertEquals(RoleConfigurator.getSecondTestUserSeq(),
                          details.getComponentToInitialEditMap()
                                 .get(secondConceptNid)
                                 .getAuthorSequence());
   }

   /**
    * Test that advancing process not only works, but only is permitted based
    * on current state (modified while advancing) only available actions based
    * on user roles can advance process.
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test
   public void testAdvanceWorkflow()
            throws Exception {
      UUID processId = createFirstWorkflowProcess(mainDefinitionId);

      addComponentsToProcess(processId, RoleConfigurator.getFirstTestUserSeq(), State.ACTIVE);
      executeLaunchWorkflow(processId);

      // Process in Ready to Edit state: Can execute action "Edit" by
      // firstUser
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "QA Passes", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "Edit", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "Approve", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "QA Passes", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "Approve", "Comment #1"));
      Assert.assertTrue(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "Edit", "Comment #1"));

      // Process in Ready for Review state: Can execute action "QA Passes" by
      // secondUser
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "Edit", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "QA Passes", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "Approve", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "Edit", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "Approve", "Comment #1"));
      Assert.assertTrue(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "QA Passes", "Comment #1"));

      // Process in Ready for Approve state: Can execute action "Approve" by
      // firstUser
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "Edit", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "Approve", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "QA Passes", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "Edit", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "QA Passes", "Comment #1"));
      Assert.assertTrue(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "Approve", "Comment #1"));

      // Process in Publish state: no one can advance
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "Edit", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "Approve", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getSecondTestUser(), "QA Passes", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "Edit", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "QA Passes", "Comment #1"));
      Assert.assertFalse(advanceWorkflow(processId, RoleConfigurator.getFirstTestUser(), "Approve", "Comment #1"));
   }

   /**
    * Test ability to add and then remove components and stamps to the process.
    *
    * @throws Exception
    *             Thrown if test fails
    */
   @Test

   /*
    * Note this is a simplified test as using addComponentToWorkflow. More
    * realistic and complex test, using addCommitRecordToWorkflow, is found in
    * WorkflowFrameworkTest as commitRecords require IdentifierService.
    */
   public void testRemoveComponentsFromProcess()
            throws Exception {
      UUID          processId = createFirstWorkflowProcess(mainDefinitionId);
      ProcessDetail details   = wp_.getProcessDetailStore()
                                   .get(processId);

      Assert.assertEquals(0, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      addComponentsToProcess(processId, RoleConfigurator.getFirstTestUserSeq(), State.ACTIVE);
      addComponentsToProcess(processId, RoleConfigurator.getSecondTestUserSeq(), State.ACTIVE);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(2, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      wp_.getWorkflowUpdater()
         .removeComponentFromWorkflow(processId, firstConceptNid, null);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(1, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
      Assert.assertFalse(details.getComponentToInitialEditMap()
                                .keySet()
                                .contains(firstConceptNid));
      Assert.assertTrue(details.getComponentToInitialEditMap()
                               .keySet()
                               .contains(secondConceptNid));
      wp_.getWorkflowUpdater()
         .removeComponentFromWorkflow(processId, secondConceptNid, null);
      details = wp_.getProcessDetailStore()
                   .get(processId);
      Assert.assertEquals(0, details.getComponentToInitialEditMap()
                                    .keySet()
                                    .size());
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

      for (Integer nid: conceptsForTesting) {
         if (firstConceptNid == 0) {
            firstConceptNid = nid;
         } else {
            secondConceptNid = nid;
         }
      }
   }
}

