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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.drools.core.io.impl.InputStreamResource;
import org.drools.core.process.core.Work;
import org.drools.core.xml.SemanticModules;

import org.jbpm.bpmn2.core.SequenceFlow;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.bpmn2.xml.ProcessHandler;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.kie.services.impl.bpmn2.ProcessDescriptor;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;

import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;

import org.xml.sax.SAXException;

import sh.isaac.api.UserRole;
import sh.isaac.provider.workflow.model.contents.AvailableAction;
import sh.isaac.provider.workflow.model.contents.DefinitionDetail;
import sh.isaac.provider.workflow.model.contents.ProcessDetail.EndWorkflowType;

//~--- classes ----------------------------------------------------------------

/**
 * Routines enabling access of content built when importing a bpmn2 file
 *
 * {@link BPMNInfo} {@link WorkflowProvider}.
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class Bpmn2FileImporter {
   /** A constant used to flag which states in a BPMN2 file are editing. */
   private static final String EDITING_ACTION = "EDITING";

   //~--- fields --------------------------------------------------------------

   /**
    * A flag defining whether person importing the BPMN2 file wants the outcome
    * printed to console for analysis purposes.
    */
   final boolean printForAnalysis = false;

   /** The nodes discovered and processed when importing a BPMN2 file. */
   private final List<Node> processNodes = new ArrayList<Node>();

   /**
    * The list of nodes already processed to prevent re-processing already
    * processed nodes.
    */
   private final List<Long> visitedNodes = new ArrayList<>();

   /**
    * The list of humanNodes to prevent their being reprocessed. These define
    * user actions.
    */
   private final Set<Long> humanNodesProcessed = new HashSet<>();

   /**
    * A map detailing each node's outgoing connections to other nodes.
    * Populated during node discovery and used during node processing.
    */
   private final Map<Long, List<Long>> nodeToOutgoingMap = new HashMap<Long, List<Long>>();

   /**
    * A map of all nodes to their name. Populated during node discovery and
    * used during node processing.
    */
   private final Map<Long, String> nodeNameMap = new HashMap<Long, String>();

   /** A list of all editing states observed during importing of BPMN2 file. */
   private final Set<String> currentEditStates = new HashSet<>();

   /** A map of available actions per type of ending workflow. */
   private final HashMap<EndWorkflowType, Set<AvailableAction>> endNodeTypeMap = new HashMap<>();

   /** A map of available actions per definition from which a workflow may be started. */
   private final HashMap<UUID, Set<AvailableAction>> definitionStartActionMap = new HashMap<>();

   /** A map of all states per definition from which a process may be edited. */
   private final HashMap<UUID, Set<String>> editStatesMap = new HashMap<>();

   /** The logger. */
   private final Logger logger = LogManager.getLogger();

   /** The Definition Id to be used as DefinionDetail entry's key in the content store. */
   private UUID currentDefinitionId;

   /** The JBPMN RuleFlowProcess discovered when importing the BPMN2 file. */
   private RuleFlowProcess ruleFlow;

   /** The path to the BPMN2 file being imported. */
   private final String bpmn2ResourcePath;

   /** The provider. */
   private final WorkflowProvider provider;

   //~--- constructors --------------------------------------------------------

   /**
    * Imports a new workflow definition storing the contents into the store
    * based on the bpmn2 file passed in.
    *
    * @param bpmn2ResourcePath the bpmn 2 resource path
    * @param provider the provider
    */
   public Bpmn2FileImporter(String bpmn2ResourcePath, WorkflowProvider provider) {
      this.bpmn2ResourcePath = bpmn2ResourcePath;
      this.provider          = provider;

      try {
         // Use JBPMN to transform the bpmn2FilePath into a JBPMN class
         final ProcessDescriptor descriptor = identifyDefinitionMetadata();

         // Pull and populate Definition Details
         this.currentDefinitionId = populateWorkflowDefinitionRecords(descriptor);

         if (this.printForAnalysis) {
            printProcessDefinition(descriptor);
         }

         // Import bpmn2 file and process all nodes
         importAndProcessNodes();

         // Finalize import process
         this.editStatesMap.put(this.currentDefinitionId, this.currentEditStates);
      } catch (final Exception e) {
         this.logger.error("Failed in processing the workflow definition defined at: " + bpmn2ResourcePath);
         e.printStackTrace();
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Iterate through all the nodes, processing each, and create the complete
    * set of AvailableActions from the definition.
    *
    * @return Complete set of AvailableActions to be persisted in
    *         AvailableAction Content Store
    * @throws Exception the exception
    */
   private Set<AvailableAction> generateAvailableActions()
            throws Exception {
      final Set<AvailableAction> actions          = new HashSet<>();
      String                     initialState     = null;
      Set<UserRole>              roles            = new HashSet<>();
      final Set<AvailableAction> startNodeActions = new HashSet<>();
      String                     flagMetaDataStr  = null;
      final Set<AvailableAction> availActions     = new HashSet<>();
      final List<SequenceFlow> connections = (List<SequenceFlow>) this.ruleFlow.getMetaData(ProcessHandler.CONNECTIONS);

      for (final Node node: this.processNodes) {
         availActions.clear();

         if ((node.getName() != null) &&
               !node.getName().isEmpty() &&
               !(node instanceof HumanTaskNode) &&
               !(node instanceof EndNode)) {
            initialState = node.getName();
         }

         if ((node.getMetaData() != null) && (node.getMetaData().get("Documentation") != null)) {
            flagMetaDataStr = (String) node.getMetaData()
                                           .get("Documentation");
         }

         if (node instanceof StartNode) {
            availActions.addAll(identifyNodeActions(node,
                  initialState,
                  roles,
                  ((StartNode) node).getDefaultOutgoingConnections(),
                  connections));
            actions.addAll(availActions);
            startNodeActions.addAll(availActions);
         } else if (node instanceof Split) {
            availActions.addAll(identifyNodeActions(node,
                  initialState,
                  roles,
                  ((Split) node).getDefaultOutgoingConnections(),
                  connections));
            actions.addAll(availActions);
         } else if (node instanceof HumanTaskNode) {
            roles = getActorFromHumanTask((HumanTaskNode) node);

            if (!this.humanNodesProcessed.contains(node.getId())) {
               availActions.addAll(identifyNodeActions(node,
                     initialState,
                     roles,
                     ((HumanTaskNode) node).getDefaultOutgoingConnections(),
                     connections));
               actions.addAll(availActions);
               this.humanNodesProcessed.add(node.getId());
            }
         }

         if (flagMetaDataStr != null) {
            final boolean flagProcessed = identifySpecialNodes(availActions, actions, node, flagMetaDataStr);

            if (flagProcessed) {
               flagMetaDataStr = null;
            }
         }
      }

      // TODO in Backlog: Update to handle multiple start states
      processStartNodeStates(startNodeActions);
      return actions;
   }

   /**
    * Import workflow definition based using JBPMN methods based on the BPMN2
    * file being imported examining the definition metadata.
    *
    * @return The JBPMN process descriptor identified while importing the BPMN2
    *         file
    *
    * @throws Exception
    *             Thrown if any error arises in importing the BPMN2 file using
    *             hte JBPMN apis. Caught & re-thrown to help output the issues
    *             preventing a successful import.
    */
   private ProcessDescriptor identifyDefinitionMetadata()
            throws Exception {
      final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

      try (InputStream inputStream = Bpmn2FileImporter.class.getResourceAsStream(this.bpmn2ResourcePath);) {
         final InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

         kbuilder.add(inputStreamResource, ResourceType.BPMN2);

         final KnowledgePackage pckg    = kbuilder.getKnowledgePackages()
                                                  .iterator()
                                                  .next();
         final Process          process = pckg.getProcesses()
                                              .iterator()
                                              .next();

         return (ProcessDescriptor) process.getMetaData()
                                           .get("ProcessDescriptor");
      } catch (final Exception e) {
         throw new Exception("Failed importing the BPMN2 file due to the following errors:\n" + kbuilder.getErrors());
      }
   }

   /**
    * For a given node, process its outgoing connections to determine if any of
    * them give cause to create a new set of AvailableActions.
    *
    * @param node            The node to process
    * @param initialState            The current state when processing the current node to be used
    *            if any AvailableActions are to be created
    * @param roles            The current roles when processing the current node to be used
    *            if any AvailableActions are to be created
    * @param nodeOutgoingConnections            The node's outgoing connections.
    * @param allOutgoingConnections            The full set of Sequence Flows which contain information
    *            associated with the outgoing connections
    * @return The full set of AvailableActions based on the current node
    */
   private Set<AvailableAction> identifyNodeActions(Node node,
         String initialState,
         Set<UserRole> roles,
         List<Connection> nodeOutgoingConnections,
         List<SequenceFlow> allOutgoingConnections) {
      final Set<AvailableAction> availActions = new HashSet<>();

      // Process the node's outgoing connection.
      for (final Long id: this.nodeToOutgoingMap.get(node.getId())) {
         String       action  = null;
         String       outcome = null;
         final String state   = initialState;

         for (final Connection connection: nodeOutgoingConnections) {
            if (connection.getTo()
                          .getId() == id) {
               final String connectionId = (String) connection.getMetaData()
                                                              .get("UniqueId");

               for (final SequenceFlow sequence: allOutgoingConnections) {
                  if (sequence.getId()
                              .equals(connectionId)) {
                     action = sequence.getName();

                     if (!(connection.getTo() instanceof HumanTaskNode)) {
                        outcome = this.nodeNameMap.get(id);
                     } else {
                        outcome = ((HumanTaskNode) connection.getTo()).getDefaultOutgoingConnections()
                              .iterator()
                              .next()
                              .getTo()
                              .getName();
                        this.humanNodesProcessed.add(connection.getTo()
                              .getId());
                     }

                     break;
                  }
               }
            }

            if ((outcome != null) && (action == null)) {
               if (node instanceof HumanTaskNode) {
                  action = getHumanTaskName((HumanTaskNode) node);
               }
            }

            if ((outcome != null) || (action != null)) {
               break;
            }
         }

         if ((roles.size() == 0) && (node.getId() == this.ruleFlow.getStartNodes().iterator().next().getId())) {
            roles.add(UserRole.AUTOMATED);
         }

         // Verify that all requirements met
         if ((action != null) && (outcome != null) && (state != null) && (roles.size() > 0)) {
            // Generate a new AvailableAction for each role
            for (final UserRole role: roles) {
               final AvailableAction newAction = new AvailableAction(this.currentDefinitionId,
                                                                     state,
                                                                     action,
                                                                     outcome,
                                                                     role);

               availActions.add(newAction);
            }
         }
      }

      return availActions;
   }

   /**
    * Recursively iterate through the nodes and identify the best order to
    * process them.
    *
    * @param node
    *            The start node based on the definition and thus the initial
    *            node to process.
    * @param retList
    *            The ordered list of nodes beginning with the start node. Keeps
    *            being passed in as this is a recursive method.
    *
    * @return The ordered list of nodes
    */
   private List<Long> identifyOutputOrder(Node node, List<Long> retList) {
      // TODO in Backlog: Eventually handle case where multiple start nodes are possible
      if (this.visitedNodes.contains(node.getId())) {
         return retList;
      } else {
         this.visitedNodes.add(node.getId());
         retList.add(node.getId());

         final List<Long> outgoingNodeIds = new ArrayList<Long>();

         for (final Node n: getOutgoingNodes(node)) {
            outgoingNodeIds.add(n.getId());
            retList = identifyOutputOrder(n, retList);
         }

         this.nodeToOutgoingMap.put(node.getId(), outgoingNodeIds);

         if (node instanceof HumanTaskNode) {
            this.nodeNameMap.put(node.getId(), getHumanTaskName((HumanTaskNode) node));
         } else {
            this.nodeNameMap.put(node.getId(), node.getName());
         }

         return retList;
      }
   }

   /**
    * Examines node, current flags, and actions to identify if any special
    * handling is required. This includes EDIT nodes and FINISH Nodes.
    *
    * @param availActions
    *            The list of available actions to be processed at this
    *            iteration of node processing. May be empty.
    * @param allActions
    *            The full set of actions which are known
    * @param node
    *            The node to examine
    * @param flag
    *            The current flag. Method won't be called unless a flag exists.
    *
    * @return True if special handling performed. This causes a reset of the
    *         flag status for future processing.
    *
    * @throws Exception
    *             Thrown if a non-supported flag is encountered
    */
   private boolean identifySpecialNodes(Set<AvailableAction> availActions,
         Set<AvailableAction> allActions,
         Node node,
         String flag)
            throws Exception {
      // If empty, process all nodes
      if (availActions.isEmpty()) {
         for (final AvailableAction action: allActions) {
            if (node.getName()
                    .equalsIgnoreCase(action.getOutcomeState())) {
               availActions.add(action);
            }
         }
      }

      if (availActions.isEmpty()) {
         return false;
      }

      // Handle special cases denoted by flag
      if (flag.equalsIgnoreCase(EndWorkflowType.CANCELED.toString())) {
         if (!this.endNodeTypeMap.containsKey(EndWorkflowType.CANCELED)) {
            this.endNodeTypeMap.put(EndWorkflowType.CANCELED, new HashSet<AvailableAction>());
         }

         this.endNodeTypeMap.get(EndWorkflowType.CANCELED)
                            .addAll(availActions);
      } else if (flag.equalsIgnoreCase(EndWorkflowType.CONCLUDED.toString())) {
         if (!this.endNodeTypeMap.containsKey(EndWorkflowType.CONCLUDED)) {
            this.endNodeTypeMap.put(EndWorkflowType.CONCLUDED, new HashSet<AvailableAction>());
         }

         this.endNodeTypeMap.get(EndWorkflowType.CONCLUDED)
                            .addAll(availActions);
      } else if (flag.equalsIgnoreCase(EDITING_ACTION)) {
         for (final AvailableAction action: availActions) {
            this.currentEditStates.add(action.getInitialState());
         }
      } else {
         throw new Exception("Have unexpected flag in processNodeFlags(): " + flag);
      }

      return true;
   }

   /**
    * Reads the bpmn2 file, identifies each node, and processes each node to
    * identify the types, connections, and handling.
    *
    * Results used to populate AvailableActionContentStore.
    */
   private void importAndProcessNodes() {
      this.ruleFlow = importDefinitionRules();

      final List<Long> nodesInOrder = identifyOutputOrder(this.ruleFlow.getStartNodes()
                                                                       .iterator()
                                                                       .next(),
                                                          new ArrayList<Long>());

      // Populate the actual nodes object
      for (final Long nodeId: nodesInOrder) {
         this.processNodes.add(this.ruleFlow.getNode(nodeId));
      }

      try {
         final Set<AvailableAction> entries = generateAvailableActions();

         if (this.provider.getAvailableActionStore()
                          .size() == 0) {
            this.logger.info("Loading Available Action store from BPMN");

            for (final AvailableAction entry: entries) {
               // Write content into database
               this.provider.getAvailableActionStore()
                            .add(entry);
            }
         } else {
            this.logger.info("Not updating Action Store, because it is already populated.");
         }

         if (this.printForAnalysis) {
            printNodes();
         }
      } catch (final Exception e) {
         this.logger.error("Failed in transforming the workflow definition into Possible Actions: " +
                           this.bpmn2ResourcePath,
                           e);
      }
   }

   /**
    * Import the BPMN2 file this time analyzing it for its rules (including the
    * definition nodes).
    *
    * @return The definition's rules (nodes)
    */
   private RuleFlowProcess importDefinitionRules() {
      final SemanticModules modules = new SemanticModules();

      modules.addSemanticModule(new BPMNSemanticModule());
      modules.addSemanticModule(new BPMNDISemanticModule());

      final XmlProcessReader processReader = new XmlProcessReader(modules, getClass().getClassLoader());

      try (InputStream in = Bpmn2FileImporter.class.getResourceAsStream(this.bpmn2ResourcePath);) {
         final List<Process> processes = processReader.read(in);

         return (RuleFlowProcess) processes.get(0);
      } catch (final FileNotFoundException e) {
         this.logger.error("Couldn't Find Fine: " + this.bpmn2ResourcePath, e);
         e.printStackTrace();
      } catch (final IOException ioe) {
         this.logger.error("Error in readFile method: " + this.bpmn2ResourcePath, ioe);
         ioe.printStackTrace();
      } catch (final SAXException se) {
         this.logger.error("Error in parsing XML file: " + this.bpmn2ResourcePath, se);
         se.printStackTrace();
      }

      return null;
   }

   /**
    * Populate workflow Definition Detail meta-content store.
    *
    * Results used to populate DefinitionDetailContentStore.
    *
    * @param descriptor
    *            The process descriptor defining the process in a JBPMN manner
    *
    * @return The definition uuid to be used as the Definition Detail Entry's
    *         key
    */
   private UUID populateWorkflowDefinitionRecords(ProcessDescriptor descriptor) {
      if (this.provider.getDefinitionDetailStore()
                       .size() == 0) {
         final Set<UserRole> roles = new HashSet<>();

         roles.add(UserRole.AUTOMATED);

         final ProcessAssetDesc definition = descriptor.getProcess();

         for (final String key: descriptor.getTaskAssignments()
                                          .keySet()) {
            for (final String role: descriptor.getTaskAssignments()
                                              .get(key)) {
               roles.add(UserRole.safeValueOf(role)
                                 .get());
            }
         }

         final DefinitionDetail entry = new DefinitionDetail(definition.getId(),
                                                             definition.getName(),
                                                             definition.getNamespace(),
                                                             definition.getVersion(),
                                                             roles,
                                                             getDescription(descriptor));

         return this.provider.getDefinitionDetailStore()
                             .add(entry);
      } else {
         for (final DefinitionDetail dd: this.provider.getDefinitionDetailStore()
               .values()) {
            if (dd.getBpmn2Id()
                  .equals(descriptor.getProcess()
                                    .getId())) {
               return dd.getId();
            }
         }
      }

      throw new RuntimeException("Loaded datastore mis-aligns with bpmn file!");
   }

   /**
    * Prints the definition's node information.
    */
   private void printNodes() {
      /* Process Nodes */
      System.out.println("\n\n\n\n\t\t ***** Node Processing *****");

      final List<SequenceFlow> connections = (List<SequenceFlow>) this.ruleFlow.getMetaData(ProcessHandler.CONNECTIONS);

      // Print out remaining nodes
      for (final Node node: this.processNodes) {
         if ((node.getName() == null) || node.getName().isEmpty()) {
            System.out.println("\n\n\n**** Printing out unnamed node");
         } else {
            System.out.println("\n\n\n**** Printing out node named: " + node.getName());
         }

         System.out.println("ID: " + node.getId());

         List<Connection> outgoingConnections = null;

         if (node instanceof StartNode) {
            System.out.println("Type: StartNode");
            outgoingConnections = ((StartNode) node).getDefaultOutgoingConnections();
         } else if (node instanceof EndNode) {
            System.out.println("Type: EndNode");
         } else if (node instanceof HumanTaskNode) {
            System.out.println("Type: HumanTaskNode");
            outgoingConnections = ((HumanTaskNode) node).getDefaultOutgoingConnections();
         } else if (node instanceof Join) {
            System.out.println("Type: Join");
         } else if (node instanceof Split) {
            System.out.println("Type: Split");
            outgoingConnections = ((Split) node).getDefaultOutgoingConnections();
         }

         if (!this.nodeToOutgoingMap.get(node.getId()).isEmpty() && (outgoingConnections != null)) {
            ;
            System.out.println("This node has the following outgoing connections:");

            for (final Long id: this.nodeToOutgoingMap.get(node.getId())) {
               String divergeOption = "NOT FOUND";

               for (final Connection connection: outgoingConnections) {
                  if (connection.getTo()
                                .getId() == id) {
                     final String connectionId = (String) connection.getMetaData()
                                                                    .get("UniqueId");

                     for (final SequenceFlow sequence: connections) {
                        if (sequence.getId()
                                    .equals(connectionId)) {
                           divergeOption = sequence.getName();
                        }
                     }
                  }
               }

               if ((node instanceof Split) || (node instanceof StartNode) || (node instanceof HumanTaskNode)) {
                  System.out.println("\t" + id + " that is associated to action: " + divergeOption);
               } else {
                  System.out.println("\t" + id);
               }
            }
         }
      }
   }

   /**
    * Prints the workflow definition metadata.
    *
    * @param processDescriptor
    *            the process descriptor
    */
   private void printProcessDefinition(ProcessDescriptor processDescriptor) {
      final ProcessAssetDesc processDefinition = processDescriptor.getProcess();

      System.out.println("\t\t ***** Definition Processing *****");
      System.out.println("Definition Name: " + processDefinition.getName());
      System.out.println("Definition Namespace: " + processDefinition.getPackageName());
      System.out.println("Definition Id: " + processDefinition.getId());
      System.out.println("Definition DeploymentId: " + processDefinition.getDeploymentId());
      System.out.println("Definition Knowledge Type: " + processDefinition.getKnowledgeType());
      System.out.println("Definition Type: " + processDefinition.getType());
      System.out.println("Definition Version: " + processDefinition.getVersion());
      System.out.println("*****Printing out Global Item Definitions Map<String, String>*****");

      final Map<String, String> globalItems = processDescriptor.getGlobalItemDefinitions();

      for (final String key: globalItems.keySet()) {
         System.out.println("Key: " + key + " with values: " + globalItems.get(key));
      }

      System.out.println("\n\n\n\n*****Printing out Task AssignmentsMap<String, Collection<String>> *****");

      final Map<String, Collection<String>> taskAssignments = processDescriptor.getTaskAssignments();

      for (final String key: taskAssignments.keySet()) {
         System.out.println("\nKey: " + key + " with values:");

         for (final String colValue: taskAssignments.get(key)) {
            System.out.println("Value: " + colValue);
         }
      }

      System.out.println("\n\n\n\n*****Printing out Task Input Mappings Map<String, Map<String, String>>*****");

      final Map<String, Map<String, String>> taskInputMappings = processDescriptor.getTaskInputMappings();

      for (final String key: taskInputMappings.keySet()) {
         System.out.println("\nKey: " + key + " with sub-key/value:");

         for (final String key2: taskInputMappings.get(key)
               .keySet()) {
            final String val = taskInputMappings.get(key)
                                                .get(key2);

            System.out.println("\tKey2: " + key2 + " with value: " + val);
         }
      }

      System.out.println("\n\n\n\n*****Printing out Task Output Mappings Map<String, Map<String, String>>*****");

      final Map<String, Map<String, String>> taskOutputMappings = processDescriptor.getTaskOutputMappings();

      for (final String key: taskOutputMappings.keySet()) {
         System.out.println("\nKey: " + key + " with sub-key/value:");

         for (final String key2: taskOutputMappings.get(key)
               .keySet()) {
            final String val = taskOutputMappings.get(key)
                                                 .get(key2);

            System.out.println("\tKey2: " + key2 + " with value: " + val);
         }
      }
   }

   /**
    * Method to process and identify when the definition contains multiple
    * start states.
    *
    * @param actions
    *            The list of AvailableActions identified as start state actions
    *
    * @throws Exception
    *             Thrown if an unexpected number of start states are
    *             discovered.
    */
   private void processStartNodeStates(Set<AvailableAction> actions)
            throws Exception {
      if (actions.size() == 0) {
         throw new Exception("No Start Actions Found");
      } else if (actions.size() != 1) {
         // TODO in Backlog: Handle multiple start states. Out of scope as of now.
         throw new Exception("For R2, there may only be a single case.  If mutliple actions found an error occurred");
      } else {
         // At this point, only single start state is in scope.
      }

      this.definitionStartActionMap.put(this.currentDefinitionId, actions);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Having identified that the current node is of type HumanTaskNode,
    * identify the role associated with the human take. The role is defined via
    * the ActorId HumanTaskNode parameters.
    *
    * @param node
    *            The HumanTaskNode being investigated
    *
    * @return The set of workflow roles which defining the user role
    *         which can execute the task
    */
   private Set<UserRole> getActorFromHumanTask(HumanTaskNode node) {
      final Set<UserRole> restrictions = new HashSet<>();

      // Get HumanTaskNode's restrictions
      final Work work = node.getWork();

      if (work.getParameters() != null) {
         final String roleString = (String) work.getParameters()
                                                .get("ActorId");

         if (roleString != null) {
            final String[] roles = roleString.split(",");

            for (final String role: roles) {
               restrictions.add(UserRole.safeValueOf(role.trim())
                                        .get());
            }
         }
      }

      return restrictions;
   }

   /**
    * Gets the BPMN info.
    *
    * @return the BPMN info
    */
   public BPMNInfo getBPMNInfo() {
      return new BPMNInfo(this.currentDefinitionId,
                          this.endNodeTypeMap,
                          this.definitionStartActionMap,
                          this.editStatesMap);
   }

   /**
    * Retrieves the definition id of the last import imported BPMN2 file.
    *
    * @return The definition id used as the DefinitionDetail entry's key
    */
   public UUID getCurrentDefinitionId() {
      return this.currentDefinitionId;
   }

   /**
    * Placeholder for when the BPMN2 version is updated with the ability to
    * access the metadata associated with a VPMN2 definition as a whole.
    *
    * @param descriptor            The process descriptor containing the metadata associated with
    *            the BPMN2 file that was imported.
    * @return The BPMN2 process description
    */
   private String getDescription(ProcessDescriptor descriptor) {
      // TODO: Fix this once the next version of BPMN2 enables accessing the
      // metadata of the definition as a whole
      return "DESCRIPTION";
   }

   /**
    * Having identified that the current node is of type HumanTaskNode,
    * identify the name of the the human node. The name is defined via the
    * TaskName HumanTaskNode parameters.
    *
    * @param node
    *            The HumanTaskNode being investigated
    *
    * @return The name of the task
    */
   private String getHumanTaskName(HumanTaskNode node) {
      final Work work = node.getWork();

      if (work.getParameters() != null) {
         return (String) work.getParameters()
                             .get("TaskName");
      }

      return null;
   }

   /**
    * Identifies the outgoing nodes coming out of a given node.
    *
    * @param node
    *            The node being investigated.
    *
    * @return The outgoing nodes
    */
   private List<Node> getOutgoingNodes(Node node) {
      final List<Node> retList = new ArrayList<Node>();

      for (final Iterator<List<Connection>> it = node.getOutgoingConnections().values().iterator(); it.hasNext(); ) {
         final List<Connection> list = it.next();

         for (final Iterator<Connection> it2 = list.iterator(); it2.hasNext(); ) {
            retList.add(it2.next()
                           .getTo());
         }
      }

      return retList;
   }
}

