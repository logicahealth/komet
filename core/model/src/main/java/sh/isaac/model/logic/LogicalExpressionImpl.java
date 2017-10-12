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
package sh.isaac.model.logic;

//~--- JDK imports ------------------------------------------------------------
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.time.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.DataSource;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.ConnectorNode;
import sh.isaac.model.logic.node.DisjointWithNode;
import sh.isaac.model.logic.node.LiteralNode;
import sh.isaac.model.logic.node.LiteralNodeBoolean;
import sh.isaac.model.logic.node.LiteralNodeFloat;
import sh.isaac.model.logic.node.LiteralNodeInstant;
import sh.isaac.model.logic.node.LiteralNodeInteger;
import sh.isaac.model.logic.node.LiteralNodeString;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.OrNode;
import sh.isaac.model.logic.node.RootNode;
import sh.isaac.model.logic.node.SubstitutionNode;
import sh.isaac.model.logic.node.SubstitutionNodeBoolean;
import sh.isaac.model.logic.node.SubstitutionNodeConcept;
import sh.isaac.model.logic.node.SubstitutionNodeFloat;
import sh.isaac.model.logic.node.SubstitutionNodeInstant;
import sh.isaac.model.logic.node.SubstitutionNodeInteger;
import sh.isaac.model.logic.node.SubstitutionNodeLiteral;
import sh.isaac.model.logic.node.SubstitutionNodeString;
import sh.isaac.model.logic.node.SufficientSetNode;
import sh.isaac.model.logic.node.external.ConceptNodeWithUuids;
import sh.isaac.model.logic.node.external.FeatureNodeWithUuids;
import sh.isaac.model.logic.node.external.RoleNodeAllWithUuids;
import sh.isaac.model.logic.node.external.RoleNodeSomeWithUuids;
import sh.isaac.model.logic.node.external.TemplateNodeWithUuids;
import sh.isaac.model.logic.node.internal.ConceptNodeWithSequences;
import sh.isaac.model.logic.node.internal.FeatureNodeWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithSequences;
import sh.isaac.model.logic.node.internal.TemplateNodeWithSequences;
import sh.isaac.model.logic.node.internal.TypedNodeWithSequences;

//~--- classes ----------------------------------------------------------------
/**
 * Created by kec on 12/6/14.
 *
 * TODO need version of Pack that uses UUIDs for change sets
 *
 * TODO need unique way of identifying data columns for substitution: Use enumerations for now
 *
 * TODO Standard refset for never grouped roles
 *
 * TODO Standard refset for right identities
 */
public class LogicalExpressionImpl
        implements LogicalExpression {

   private static final String CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE = "ConceptsReferencedAtNodeOrAbove";

   public static final byte SERIAL_FORMAT_VERSION = 1;

   /**
    * The Constant NODE_SEMANTICS.
    */
   private static final NodeSemantic[] NODE_SEMANTICS = NodeSemantic.values();

   /**
    * The Constant MEANINGFUL_NODE_SEMANTICS.
    */
   private static final EnumSet<NodeSemantic> MEANINGFUL_NODE_SEMANTICS = EnumSet.of(NodeSemantic.CONCEPT,
           NodeSemantic.SUBSTITUTION_CONCEPT);

   /**
    * The isa nid.
    */
   protected static int isaNid = 0;

   //~--- fields --------------------------------------------------------------
   /**
    * The concept sequence.
    */
   transient int conceptSequence = -1;

   /**
    * The logic nodes.
    */
   ArrayList<LogicNode> logicNodes = new ArrayList<>();

   /**
    * The root node index.
    */
   int rootNodeIndex = -1;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new logical expression ochre impl.
    */
   public LogicalExpressionImpl() {
   }

   /**
    * Instantiates a new logical expression ochre impl.
    *
    * @param nodeDataArray the node data array
    * @param dataSource the data source
    */
   public LogicalExpressionImpl(byte[][] nodeDataArray, DataSource dataSource) {
      if (nodeDataArray == null) {
         this.logicNodes = new ArrayList<>(0);
      } else {
         this.logicNodes = new ArrayList<>(nodeDataArray.length);
         for (final byte[] nodeDataArray1 : nodeDataArray) {
            final ByteArrayDataBuffer dataInputStream = new ByteArrayDataBuffer(nodeDataArray1);
            dataInputStream.setObjectDataFormatVersion(dataInputStream.getByte());
            if (dataInputStream.getObjectDataFormatVersion() != SERIAL_FORMAT_VERSION) {
               throw new IllegalStateException("Data format: " + dataInputStream.getObjectDataFormatVersion() + " does not equal SERIAL_FORMAT_VERSION");
            }
            final byte nodeSemanticIndex = dataInputStream.getByte();
            final NodeSemantic nodeSemantic = NODE_SEMANTICS[nodeSemanticIndex];

            switch (nodeSemantic) {
               case DEFINITION_ROOT:
                  Root(dataInputStream);
                  break;

               case NECESSARY_SET:
                  NecessarySet(dataInputStream);
                  break;

               case SUFFICIENT_SET:
                  SufficientSet(dataInputStream);
                  break;

               case AND:
                  And(dataInputStream);
                  break;

               case OR:
                  Or(dataInputStream);
                  break;

               case DISJOINT_WITH:
                  DisjointWith(dataInputStream);
                  break;

               case ROLE_ALL:
                  switch (dataSource) {
                     case EXTERNAL:
                        AllRoleWithUuids(dataInputStream);
                        break;

                     case INTERNAL:
                        AllRole(dataInputStream);
                        break;

                     default:
                        throw new UnsupportedOperationException("v Can't handle: " + dataSource);
                  }

                  break;

               case ROLE_SOME:
                  switch (dataSource) {
                     case EXTERNAL:
                        SomeRoleWithUuids(dataInputStream);
                        break;

                     case INTERNAL:
                        SomeRole(dataInputStream);
                        break;

                     default:
                        throw new UnsupportedOperationException("w Can't handle: " + dataSource);
                  }

                  break;

               case FEATURE:
                  switch (dataSource) {
                     case EXTERNAL:
                        FeatureWithUuids(dataInputStream);
                        break;

                     case INTERNAL:
                        Feature(dataInputStream);
                        break;

                     default:
                        throw new UnsupportedOperationException("x Can't handle: " + dataSource);
                  }

                  break;

               case LITERAL_BOOLEAN:
                  BooleanLiteral(dataInputStream);
                  break;

               case LITERAL_FLOAT:
                  FloatLiteral(dataInputStream);
                  break;

               case LITERAL_INSTANT:
                  InstantLiteral(dataInputStream);
                  break;

               case LITERAL_INTEGER:
                  IntegerLiteral(dataInputStream);
                  break;

               case LITERAL_STRING:
                  StringLiteral(dataInputStream);
                  break;

               case CONCEPT:
                  switch (dataSource) {
                     case EXTERNAL:
                        ConceptWithUuids(dataInputStream);
                        break;

                     case INTERNAL:
                        Concept(dataInputStream);
                        break;

                     default:
                        throw new UnsupportedOperationException("y Can't handle: " + dataSource);
                  }

                  break;

               case TEMPLATE:
                  switch (dataSource) {
                     case EXTERNAL:
                        TemplateWithUuids(dataInputStream);
                        break;

                     case INTERNAL:
                        Template(dataInputStream);
                        break;

                     default:
                        throw new UnsupportedOperationException("z Can't handle: " + dataSource);
                  }

                  break;

               case SUBSTITUTION_BOOLEAN:
                  BooleanSubstitution(dataInputStream);
                  break;

               case SUBSTITUTION_CONCEPT:
                  ConceptSubstitution(dataInputStream);
                  break;

               case SUBSTITUTION_FLOAT:
                  FloatSubstitution(dataInputStream);
                  break;

               case SUBSTITUTION_INSTANT:
                  InstantSubstitution(dataInputStream);
                  break;

               case SUBSTITUTION_INTEGER:
                  IntegerSubstitution(dataInputStream);
                  break;

               case SUBSTITUTION_STRING:
                  StringSubstitution(dataInputStream);
                  break;

               default:
                  throw new UnsupportedOperationException("aa Can't handle: " + nodeSemantic);
            }
         }
         this.logicNodes.trimToSize();
      }

      
   }

   /**
    * Called to generate an isomorphicExpression and a mergedExpression.
    *
    * @param another the logical expression to add nodes from.
    * @param solution an array mapping from the nodeId in another to the nodeId in this expression. If the value of the
    * solution element == -1, that node is not added to this logical expression, otherwise the value of the solution
    * element is used for the nodeId in this logical expression.
    */
   public LogicalExpressionImpl(LogicalExpressionImpl another, int[] solution) {
      addNodesWithMap(another, solution, new int[another.getNodeCount()], another.rootNodeIndex);
      this.logicNodes.trimToSize();
   }

   /**
    * Instantiates a new logical expression ochre impl.
    *
    * @param nodeDataArray the node data array
    * @param dataSource the data source
    * @param conceptId Either a nid or sequence of a concept is acceptable.
    */
   public LogicalExpressionImpl(byte[][] nodeDataArray, DataSource dataSource, int conceptId) {
      this(nodeDataArray, dataSource);

      if (conceptId < 0) {
         conceptId = Get.identifierService()
                 .getConceptSequence(conceptId);
      }

      this.conceptSequence = conceptId;
   }

   /**
    * Called to generate an isomorphicExpression and a mergedExpression.
    *
    * @param another the logical expression to add nodes from.
    * @param solution an array mapping from the nodeId in another to the nodeId in this expression. If the value of the
    * solution element == -1, that node is not added to this logical expression, otherwise the value of the solution
    * element is used for the nodeId in this logical expression.
    * @param anotherToThisNodeIdMap contains a mapping from nodeId in another to nodeId in this constructed expression.
    */
   public LogicalExpressionImpl(LogicalExpressionImpl another, int[] solution, int[] anotherToThisNodeIdMap) {
      addNodesWithMap(another, solution, anotherToThisNodeIdMap, another.rootNodeIndex);
      this.logicNodes.trimToSize();
   }

   //~--- methods -------------------------------------------------------------
   /**
    * All role.
    *
    * @param dataInputStream the data input stream
    * @return the role node all with sequences
    */
   public final RoleNodeAllWithSequences AllRole(ByteArrayDataBuffer dataInputStream) {
      return new RoleNodeAllWithSequences(this, dataInputStream);
   }

   /**
    * All role.
    *
    * @param typeNid the type nid
    * @param restriction the restriction
    * @return the role node all with sequences
    */
   public RoleNodeAllWithSequences AllRole(int typeNid, AbstractLogicNode restriction) {
      return new RoleNodeAllWithSequences(this, typeNid, restriction);
   }

   /**
    * All role with uuids.
    *
    * @param dataInputStream the data input stream
    * @return the role node all with uuids
    */
   public final RoleNodeAllWithUuids AllRoleWithUuids(ByteArrayDataBuffer dataInputStream) {
      return new RoleNodeAllWithUuids(this, dataInputStream);
   }

   /**
    * And.
    *
    * @param children the children
    * @return the and node
    */
   public final AndNode And(AbstractLogicNode... children) {
      return new AndNode(this, children);
   }

   /**
    * And.
    *
    * @param dataInputStream the data input stream
    * @return the and node
    */
   public final AndNode And(ByteArrayDataBuffer dataInputStream) {
      return new AndNode(this, dataInputStream);
   }

   /**
    * Boolean literal.
    *
    * @param literalValue the literal value
    * @return the literal node boolean
    */
   public LiteralNodeBoolean BooleanLiteral(boolean literalValue) {
      return new LiteralNodeBoolean(this, literalValue);
   }

   /**
    * Boolean literal.
    *
    * @param dataInputStream the data input stream
    * @return the literal node boolean
    */
   public final LiteralNodeBoolean BooleanLiteral(ByteArrayDataBuffer dataInputStream) {
      return new LiteralNodeBoolean(this, dataInputStream);
   }

   /**
    * Boolean substitution.
    *
    * @param dataInputStream the data input stream
    * @return the substitution node boolean
    */
   public final SubstitutionNodeBoolean BooleanSubstitution(ByteArrayDataBuffer dataInputStream) {
      return new SubstitutionNodeBoolean(this, dataInputStream);
   }

   /**
    * Boolean substitution.
    *
    * @param substitutionFieldSpecification the substitution field specification
    * @return the substitution node boolean
    */
   public SubstitutionNodeBoolean BooleanSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
      return new SubstitutionNodeBoolean(this, substitutionFieldSpecification);
   }

   /**
    * Concept.
    *
    * @param dataInputStream the data input stream
    * @return the concept node with sequences
    */
   public final ConceptNodeWithSequences Concept(ByteArrayDataBuffer dataInputStream) {
      return new ConceptNodeWithSequences(this, dataInputStream);
   }

   /**
    * Concept.
    *
    * @param conceptSequence the concept sequence
    * @return the concept node with sequences
    */
   public final ConceptNodeWithSequences Concept(int conceptSequence) {
      return new ConceptNodeWithSequences(this, conceptSequence);
   }

   /**
    * Concept substitution.
    *
    * @param dataInputStream the data input stream
    * @return the substitution node concept
    */
   public final SubstitutionNodeConcept ConceptSubstitution(ByteArrayDataBuffer dataInputStream) {
      return new SubstitutionNodeConcept(this, dataInputStream);
   }

   /**
    * Concept substitution.
    *
    * @param substitutionFieldSpecification the substitution field specification
    * @return the substitution node concept
    */
   public SubstitutionNodeConcept ConceptSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
      return new SubstitutionNodeConcept(this, substitutionFieldSpecification);
   }

   /**
    * Concept with uuids.
    *
    * @param dataInputStream the data input stream
    * @return the concept node with uuids
    */
   public final ConceptNodeWithUuids ConceptWithUuids(ByteArrayDataBuffer dataInputStream) {
      return new ConceptNodeWithUuids(this, dataInputStream);
   }

   /**
    * Disjoint with.
    *
    * @param children the children
    * @return the disjoint with node
    */
   public DisjointWithNode DisjointWith(AbstractLogicNode... children) {
      return new DisjointWithNode(this, children);
   }

   /**
    * Disjoint with.
    *
    * @param dataInputStream the data input stream
    * @return the disjoint with node
    */
   public final DisjointWithNode DisjointWith(ByteArrayDataBuffer dataInputStream) {
      return new DisjointWithNode(this, dataInputStream);
   }

   /**
    * Feature.
    *
    * @param dataInputStream the data input stream
    * @return the feature node with sequences
    */
   public final FeatureNodeWithSequences Feature(ByteArrayDataBuffer dataInputStream) {
      return new FeatureNodeWithSequences(this, dataInputStream);
   }

   /**
    * Feature.
    *
    * @param typeNid the type nid
    * @param literal the literal
    * @return the feature node with sequences
    */
   public FeatureNodeWithSequences Feature(int typeNid, AbstractLogicNode literal) {
      // check for LiteralNode or SubstitutionNodeLiteral
      if ((literal instanceof LiteralNode) || (literal instanceof SubstitutionNodeLiteral)) {
         return new FeatureNodeWithSequences(this, typeNid, literal);
      }

      throw new IllegalStateException("LogicNode must be of type LiteralNode or SubstitutionNodeLiteral. Found: "
              + literal);
   }

   /**
    * Feature with uuids.
    *
    * @param dataInputStream the data input stream
    * @return the feature node with uuids
    */
   public final FeatureNodeWithUuids FeatureWithUuids(ByteArrayDataBuffer dataInputStream) {
      return new FeatureNodeWithUuids(this, dataInputStream);
   }

   /**
    * Float literal.
    *
    * @param dataInputStream the data input stream
    * @return the literal node float
    */
   public final LiteralNodeFloat FloatLiteral(ByteArrayDataBuffer dataInputStream) {
      return new LiteralNodeFloat(this, dataInputStream);
   }

   /**
    * Float literal.
    *
    * @param literalValue the literal value
    * @return the literal node float
    */
   public LiteralNodeFloat FloatLiteral(float literalValue) {
      return new LiteralNodeFloat(this, literalValue);
   }

   /**
    * Float substitution.
    *
    * @param dataInputStream the data input stream
    * @return the substitution node float
    */
   public final SubstitutionNodeFloat FloatSubstitution(ByteArrayDataBuffer dataInputStream) {
      return new SubstitutionNodeFloat(this, dataInputStream);
   }

   /**
    * Float substitution.
    *
    * @param substitutionFieldSpecification the substitution field specification
    * @return the substitution node float
    */
   public SubstitutionNodeFloat FloatSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
      return new SubstitutionNodeFloat(this, substitutionFieldSpecification);
   }

   /**
    * Instant literal.
    *
    * @param dataInputStream the data input stream
    * @return the literal node instant
    */
   public final LiteralNodeInstant InstantLiteral(ByteArrayDataBuffer dataInputStream) {
      return new LiteralNodeInstant(this, dataInputStream);
   }

   /**
    * Instant literal.
    *
    * @param literalValue the literal value
    * @return the literal node instant
    */
   public LiteralNodeInstant InstantLiteral(Instant literalValue) {
      return new LiteralNodeInstant(this, literalValue);
   }

   /**
    * Instant substitution.
    *
    * @param dataInputStream the data input stream
    * @return the substitution node instant
    */
   public final SubstitutionNodeInstant InstantSubstitution(ByteArrayDataBuffer dataInputStream) {
      return new SubstitutionNodeInstant(this, dataInputStream);
   }

   /**
    * Instant substitution.
    *
    * @param substitutionFieldSpecification the substitution field specification
    * @return the substitution node instant
    */
   public SubstitutionNodeInstant InstantSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
      return new SubstitutionNodeInstant(this, substitutionFieldSpecification);
   }

   /**
    * Integer literal.
    *
    * @param dataInputStream the data input stream
    * @return the literal node integer
    */
   public final LiteralNodeInteger IntegerLiteral(ByteArrayDataBuffer dataInputStream) {
      return new LiteralNodeInteger(this, dataInputStream);
   }

   /**
    * Integer literal.
    *
    * @param literalValue the literal value
    * @return the literal node integer
    */
   public LiteralNodeInteger IntegerLiteral(int literalValue) {
      return new LiteralNodeInteger(this, literalValue);
   }

   /**
    * Integer substitution.
    *
    * @param dataInputStream the data input stream
    * @return the substitution node integer
    */
   public final SubstitutionNodeInteger IntegerSubstitution(ByteArrayDataBuffer dataInputStream) {
      return new SubstitutionNodeInteger(this, dataInputStream);
   }

   /**
    * Integer substitution.
    *
    * @param substitutionFieldSpecification the substitution field specification
    * @return the substitution node integer
    */
   public SubstitutionNodeInteger IntegerSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
      return new SubstitutionNodeInteger(this, substitutionFieldSpecification);
   }

   /**
    * Necessary set.
    *
    * @param children the children
    * @return the necessary set node
    */
   public final NecessarySetNode NecessarySet(AbstractLogicNode... children) {
      return new NecessarySetNode(this, children);
   }

   /**
    * Necessary set.
    *
    * @param dataInputStream the data input stream
    * @return the necessary set node
    */
   public final NecessarySetNode NecessarySet(ByteArrayDataBuffer dataInputStream) {
      return new NecessarySetNode(this, dataInputStream);
   }

   /**
    * Or.
    *
    * @param children the children
    * @return the or node
    */
   public OrNode Or(AbstractLogicNode... children) {
      return new OrNode(this, children);
   }

   /**
    * Or.
    *
    * @param dataInputStream the data input stream
    * @return the or node
    */
   public final OrNode Or(ByteArrayDataBuffer dataInputStream) {
      return new OrNode(this, dataInputStream);
   }

   /**
    * Root.
    *
    * @param children the children
    * @return the root node
    */
   public RootNode Root(ConnectorNode... children) {
      final RootNode rootNode = new RootNode(this, children);

      this.rootNodeIndex = rootNode.getNodeIndex();
      return rootNode;
   }

   /**
    * Root.
    *
    * @param dataInputStream the data input stream
    * @return the root node
    */
   public final RootNode Root(ByteArrayDataBuffer dataInputStream) {
      final RootNode rootNode = new RootNode(this, dataInputStream);

      this.rootNodeIndex = rootNode.getNodeIndex();
      return rootNode;
   }

   /**
    * Some role.
    *
    * @param dataInputStream the data input stream
    * @return the role node some with sequences
    */
   public final RoleNodeSomeWithSequences SomeRole(ByteArrayDataBuffer dataInputStream) {
      return new RoleNodeSomeWithSequences(this, dataInputStream);
   }

   /**
    * Some role.
    *
    * @param typeNid the type nid
    * @param restriction the restriction
    * @return the role node some with sequences
    */
   public final RoleNodeSomeWithSequences SomeRole(int typeNid, AbstractLogicNode restriction) {
      return new RoleNodeSomeWithSequences(this, typeNid, restriction);
   }

   /**
    * Some role with uuids.
    *
    * @param dataInputStream the data input stream
    * @return the role node some with uuids
    */
   public final RoleNodeSomeWithUuids SomeRoleWithUuids(ByteArrayDataBuffer dataInputStream) {
      return new RoleNodeSomeWithUuids(this, dataInputStream);
   }

   /**
    * String literal.
    *
    * @param dataInputStream the data input stream
    * @return the literal node string
    */
   public final LiteralNodeString StringLiteral(ByteArrayDataBuffer dataInputStream) {
      return new LiteralNodeString(this, dataInputStream);
   }

   /**
    * String literal.
    *
    * @param literalValue the literal value
    * @return the literal node string
    */
   public LiteralNodeString StringLiteral(String literalValue) {
      return new LiteralNodeString(this, literalValue);
   }

   /**
    * String substitution.
    *
    * @param dataInputStream the data input stream
    * @return the substitution node string
    */
   public final SubstitutionNodeString StringSubstitution(ByteArrayDataBuffer dataInputStream) {
      return new SubstitutionNodeString(this, dataInputStream);
   }

   /**
    * String substitution.
    *
    * @param substitutionFieldSpecification the substitution field specification
    * @return the substitution node string
    */
   public SubstitutionNodeString StringSubstitution(SubstitutionFieldSpecification substitutionFieldSpecification) {
      return new SubstitutionNodeString(this, substitutionFieldSpecification);
   }

   /**
    * Sufficient set.
    *
    * @param children the children
    * @return the sufficient set node
    */
   public final SufficientSetNode SufficientSet(AbstractLogicNode... children) {
      return new SufficientSetNode(this, children);
   }

   /**
    * Sufficient set.
    *
    * @param dataInputStream the data input stream
    * @return the sufficient set node
    */
   public final SufficientSetNode SufficientSet(ByteArrayDataBuffer dataInputStream) {
      return new SufficientSetNode(this, dataInputStream);
   }

   /**
    * Template.
    *
    * @param dataInputStream the data input stream
    * @return the template node with sequences
    */
   public final TemplateNodeWithSequences Template(ByteArrayDataBuffer dataInputStream) {
      return new TemplateNodeWithSequences(this, dataInputStream);
   }

   /**
    * Template.
    *
    * @param templateConceptId the template concept id
    * @param assemblageConceptId the assemblage concept id
    * @return the template node with sequences
    */
   public TemplateNodeWithSequences Template(int templateConceptId, int assemblageConceptId) {
      return new TemplateNodeWithSequences(this, templateConceptId, assemblageConceptId);
   }

   /**
    * Template with uuids.
    *
    * @param dataInputStream the data input stream
    * @return the template node with uuids
    */
   public final TemplateNodeWithUuids TemplateWithUuids(ByteArrayDataBuffer dataInputStream) {
      return new TemplateNodeWithUuids(this, dataInputStream);
   }

   /**
    * Adds the node.
    *
    * @param logicNode the logic node
    */
   public void addNode(LogicNode logicNode) {
      logicNode.setNodeIndex((short) this.logicNodes.size());
      this.logicNodes.add(logicNode);
   }

   /**
    * Adds the nodes.
    *
    * @param another the logical expression to add nodes from.
    * @param solution an array mapping from the nodeId in another to the nodeId in this expression. If the value of the
    * solution element == -1, that node is not added to this logical expression, otherwise the value of the solution
    * element is used for the nodeId in this logical expression.
    * @param oldIds the list of nodeIds in the provided logical expression (another) to add to this logical expression
    * on this invocation. Note that children of the nodes indicated by oldIds may be added by recursive calls to this
    * method, if the oldId index in the solution array is >= 0.
    * @return the LogicNode elements added as a result of this instance of the call, not including any children
    * LogicNode elements added by recursive calls. Those children LogicNode elements can be retrieved by recursively
    * traversing the children of these returned LogicNode elements.
    */
   public final LogicNode[] addNodes(LogicalExpressionImpl another, int[] solution, int... oldIds) {
      return this.addNodesWithMap(another, solution, null, oldIds);
   }

   /**
    * Contains.
    *
    * @param semantic the semantic
    * @return true, if successful
    */
   @Override
   public boolean contains(NodeSemantic semantic) {
      return this.logicNodes.stream()
              .anyMatch((node) -> (node.getNodeSemantic() == semantic));
   }

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final LogicalExpressionImpl other = (LogicalExpressionImpl) obj;

      if (this.logicNodes == other.logicNodes) {
         return true;
      }

      if (this.logicNodes != null) {
         if (this.logicNodes.size() != other.logicNodes.size()) {
            return false;
         }

         final TreeNodeVisitData graphVisitData = new TreeNodeVisitData(this.logicNodes.size());

         depthFirstVisit(null, getRoot(), graphVisitData, 0);
         return graphsEqual(this.getRoot(), other.getRoot(), 0, graphVisitData.getMaxDepth());
      }

      return true;
   }

   /**
    * Find isomorphisms.
    *
    * @param another the another
    * @return the isomorphic results
    */
   @Override
   public IsomorphicResults findIsomorphisms(LogicalExpression another) {
      return new IsomorphicResultsBottomUp(this, another);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 7;

      hash = 29 * hash + this.conceptSequence;
      return hash;
   }

   /**
    * Process depth first. The consumer will be presented with the current logic node, and with graph visit data that
    * provides information about the other nodes that have been encountered. To get the current id of the visit, within
    * the consumers {@code accept(LogicNode logicNode, TreeNodeVisitData visitData)} method, get the node id of the
    * presented logic node.
    *
    * @param consumer the consumer
    */
   @Override
   public void processDepthFirst(BiConsumer<LogicNode, TreeNodeVisitData> consumer) {
      processDepthFirst(getRoot(), consumer);
   }

   /**
    * Process the fragment starting at root in a depth first manner.
    *
    * @param fragmentRoot the fragment root
    * @param consumer the consumer
    */
   @Override
   public void processDepthFirst(LogicNode fragmentRoot, BiConsumer<LogicNode, TreeNodeVisitData> consumer) {
      init();

      final TreeNodeVisitData graphVisitData = new TreeNodeVisitData(this.logicNodes.size());

      depthFirstVisit(consumer, fragmentRoot, graphVisitData, 0);
   }

   /**
    * Sort.
    */
   public void sort() {
      this.logicNodes.forEach((node) -> node.sort());
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return toString("");
   }

   @Override
   public String toSimpleString() {
      final StringBuilder builder = new StringBuilder();

      processDepthFirst((LogicNode logicNode,
              TreeNodeVisitData graphVisitData) -> {
         if (!(logicNode instanceof RootNode)) {
            for (int i = 1; i < graphVisitData.getDistance(logicNode.getNodeIndex()); i++) {
               builder.append("    ");
            }

            builder.append(logicNode.toSimpleString());
            builder.append("\n");
         }
      });
      builder.deleteCharAt(builder.length() - 1);
      return builder.toString();
   }

   /**
    * To string.
    *
    * @param nodeIdSuffix the node id suffix
    * @return the string
    */
   @Override
   public String toString(String nodeIdSuffix) {
      final StringBuilder builder = new StringBuilder();

      processDepthFirst((LogicNode logicNode,
              TreeNodeVisitData graphVisitData) -> {
         for (int i = 0; i < graphVisitData.getDistance(logicNode.getNodeIndex()); i++) {
            builder.append("    ");
         }

         builder.append(logicNode.toString(nodeIdSuffix));
         builder.append("\n");
      });
      return builder.toString();
   }

   /**
    * Depth first visit. The consumer will be presented with the current logic node, and with graph visit data that
    * provides information about the other nodes that have been encountered. To get the current id of the visit, get the
    * node id of the presented logic node.
    *
    * @param consumer the consumer
    * @param logicNode the logic node
    * @param graphVisitData the graph visit data
    * @param depth the depth
    */
   protected void depthFirstVisit(BiConsumer<LogicNode, TreeNodeVisitData> consumer,
           LogicNode logicNode,
           TreeNodeVisitData graphVisitData,
           int depth) {
      if (depth > 100) {
         // toString depends on this method, so we can't include this.toString() in the exception...
         throw new RuntimeException("Depth limit exceeded for logicNode: " + logicNode);  // + " in graph: " + this);
      }

      graphVisitData.startNodeVisit(logicNode.getNodeIndex(), depth);

      final OpenIntHashSet conceptsReferencedByNode = new OpenIntHashSet();

      logicNode.addConceptsReferencedByNode(conceptsReferencedByNode);

      graphVisitData.getUserNodeSet(CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE, logicNode.getNodeIndex());

      logicNode.addConceptsReferencedByNode(graphVisitData.getUserNodeSet(CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE, logicNode.getNodeIndex()));

      int predecessorSequence = graphVisitData.getPredecessorSequence(logicNode.getNodeIndex());
      if (predecessorSequence >= 0) {
         
         graphVisitData.getUserNodeSet(CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE, predecessorSequence).forEachKey((node) -> {
            conceptsReferencedByNode.add(node);
            return true;
         });
         graphVisitData.setUserNodeSet(CONCEPTS_REFERENCED_AT_NODE_OR_ABOVE, logicNode.getNodeIndex(), conceptsReferencedByNode);
      }

      if (consumer != null) {
         consumer.accept(logicNode, graphVisitData);
      }

      if (logicNode.getChildren().length == 0) {
         graphVisitData.setLeafNode(logicNode.getNodeIndex());
      } else {
         int siblingGroupSequence;

         switch (logicNode.getNodeSemantic()) {
            case AND:
            case OR:
            case SUFFICIENT_SET:
            case NECESSARY_SET:
            case DISJOINT_WITH:
            case DEFINITION_ROOT:
               siblingGroupSequence = logicNode.getNodeIndex();
               break;

            default:
               siblingGroupSequence = graphVisitData.getSiblingGroupForSequence(logicNode.getNodeIndex());
         }

         for (final LogicNode child : logicNode.getChildren()) {
            graphVisitData.setSiblingGroupForSequence(child.getNodeIndex(), siblingGroupSequence);
            graphVisitData.setPredecessorSequence(child.getNodeIndex(), logicNode.getNodeIndex());
            depthFirstVisit(consumer, child, graphVisitData, depth + 1);
         }
      }

      graphVisitData.endNodeVisit(logicNode.getNodeIndex());
   }

   /**
    * Inits the.
    */
   protected void init() {
      this.logicNodes.trimToSize();
   }

   /**
    * Adds the nodes with map.
    *
    * @param another the logical expression to add nodes from.
    * @param solution an array mapping from the nodeId in another to the nodeId in this expression. If the value of the
    * solution element == -1, that node is not added to this logical expression, otherwise the value of the solution
    * element is used for the nodeId in this logical expression.
    * @param anotherToThisNodeIdMap contains a mapping from nodeId in another to nodeId in this constructed expression.
    * @param oldIds the list of nodeIds in the provided logical expression (another) to add to this logical expression
    * on this invocation. Note that children of the nodes indicated by oldIds may be added by recursive calls to this
    * method, if the oldId index in the solution array is >= 0.
    * @return the LogicNode elements added as a result of this instance of the call, not including any children
    * LogicNode elements added by recursive calls. Those children LogicNode elements can be retrieved by recursively
    * traversing the children of these returned LogicNode elements.
    */
   private LogicNode[] addNodesWithMap(LogicalExpressionImpl another,
           int[] solution,
           int[] anotherToThisNodeIdMap,
           int... oldIds) {
      final AbstractLogicNode[] results = new AbstractLogicNode[oldIds.length];

      for (int i = 0; i < oldIds.length; i++) {
         final LogicNode oldLogicNode = another.getNode(oldIds[i]);

         switch (oldLogicNode.getNodeSemantic()) {
            case DEFINITION_ROOT:
               results[i] = Root(Arrays.stream(addNodesWithMap(another,
                       solution,
                       anotherToThisNodeIdMap,
                       oldLogicNode.getChildStream()
                               .filter( // the int[] of oldIds to add to the expression
                                       (oldChildNode) -> solution[oldChildNode.getNodeIndex()]
                                       >= 0 // if the solution element == -1, filter out
                               )
                               .mapToInt(
                                       (oldChildNode) -> oldChildNode.getNodeIndex() // the nodeId in the original expression
                               )
                               .toArray() // create the oldIds passed into the addNodes recursive call
               ) // end of addNodes parameters; returns LogicNode[]
               )
                       .map((LogicNode t) -> (ConnectorNode) t)
                       .toArray(
                               ConnectorNode[]::new) // convert LogicNode[] to ConnectorNode[] to pass into the Root method call.
               );
               this.rootNodeIndex = results[i].getNodeIndex();
               break;

            case NECESSARY_SET:
               results[i] = NecessarySet((AbstractLogicNode[]) addNodesWithMap(another,
                       solution,
                       anotherToThisNodeIdMap,
                       oldLogicNode.getChildStream()
                               .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                               .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                               .toArray()));
               break;

            case SUFFICIENT_SET:
               results[i] = SufficientSet((AbstractLogicNode[]) addNodesWithMap(another,
                       solution,
                       anotherToThisNodeIdMap,
                       oldLogicNode.getChildStream()
                               .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                               .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                               .toArray()));
               break;

            case AND:
               results[i] = And((AbstractLogicNode[]) addNodesWithMap(another,
                       solution,
                       anotherToThisNodeIdMap,
                       oldLogicNode.getChildStream()
                               .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                               .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                               .toArray()));
               break;

            case OR:
               results[i] = Or((AbstractLogicNode[]) addNodesWithMap(another,
                       solution,
                       anotherToThisNodeIdMap,
                       oldLogicNode.getChildStream()
                               .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                               .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                               .toArray()));
               break;

            case DISJOINT_WITH:
               results[i] = DisjointWith((AbstractLogicNode[]) addNodesWithMap(another,
                       solution,
                       anotherToThisNodeIdMap,
                       oldLogicNode.getChildStream()
                               .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                               .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                               .toArray()));
               break;

            case ROLE_ALL:
               results[i] = AllRole(((TypedNodeWithSequences) oldLogicNode).getTypeConceptSequence(),
                       (AbstractLogicNode) addNodesWithMap(another,
                               solution,
                               anotherToThisNodeIdMap,
                               oldLogicNode.getChildStream()
                                       .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                       .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                                       .toArray())[0]);
               break;

            case ROLE_SOME:
               results[i] = SomeRole(((TypedNodeWithSequences) oldLogicNode).getTypeConceptSequence(),
                       (AbstractLogicNode) addNodesWithMap(another,
                               solution,
                               anotherToThisNodeIdMap,
                               oldLogicNode.getChildStream()
                                       .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                       .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                                       .toArray())[0]);
               break;

            case FEATURE:
               results[i] = Feature(((TypedNodeWithSequences) oldLogicNode).getTypeConceptSequence(),
                       (AbstractLogicNode) addNodesWithMap(another,
                               solution,
                               anotherToThisNodeIdMap,
                               oldLogicNode.getChildStream()
                                       .filter((oldChildNode) -> solution[oldChildNode.getNodeIndex()] >= 0)
                                       .mapToInt((oldChildNode) -> oldChildNode.getNodeIndex())
                                       .toArray())[0]);
               break;

            case LITERAL_BOOLEAN:
               results[i] = BooleanLiteral(((LiteralNodeBoolean) oldLogicNode).getLiteralValue());
               break;

            case LITERAL_FLOAT:
               results[i] = FloatLiteral(((LiteralNodeFloat) oldLogicNode).getLiteralValue());
               break;

            case LITERAL_INSTANT:
               results[i] = InstantLiteral(((LiteralNodeInstant) oldLogicNode).getLiteralValue());
               break;

            case LITERAL_INTEGER:
               results[i] = IntegerLiteral(((LiteralNodeInteger) oldLogicNode).getLiteralValue());
               break;

            case LITERAL_STRING:
               results[i] = StringLiteral(((LiteralNodeString) oldLogicNode).getLiteralValue());
               break;

            case CONCEPT:
               results[i] = Concept(((ConceptNodeWithSequences) oldLogicNode).getConceptSequence());
               break;

            case TEMPLATE:
               results[i] = Template(((TemplateNodeWithSequences) oldLogicNode).getTemplateConceptSequence(),
                       ((TemplateNodeWithSequences) oldLogicNode).getAssemblageConceptSequence());
               break;

            case SUBSTITUTION_BOOLEAN:
               results[i] = BooleanSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
               break;

            case SUBSTITUTION_CONCEPT:
               results[i] = ConceptSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
               break;

            case SUBSTITUTION_FLOAT:
               results[i] = FloatSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
               break;

            case SUBSTITUTION_INSTANT:
               results[i] = InstantSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
               break;

            case SUBSTITUTION_INTEGER:
               results[i] = IntegerSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
               break;

            case SUBSTITUTION_STRING:
               results[i] = StringSubstitution(((SubstitutionNode) oldLogicNode).getSubstitutionFieldSpecification());
               break;

            default:
               throw new UnsupportedOperationException("ab Can't handle: " + oldLogicNode.getNodeSemantic());
         }

         if (anotherToThisNodeIdMap != null) {
            anotherToThisNodeIdMap[oldLogicNode.getNodeIndex()] = results[i].getNodeIndex();
         }
      }

      return results;
   }

   /**
    * Graphs equal.
    *
    * @param g1 the g 1
    * @param g2 the g 2
    * @param depth the depth
    * @param maxDepth the max depth
    * @return true, if successful
    */
   private boolean graphsEqual(AbstractLogicNode g1, AbstractLogicNode g2, int depth, int maxDepth) {
      if (g1.equals(g2)) {
         final AbstractLogicNode[] g1children = g1.getChildren();
         final AbstractLogicNode[] g2children = g2.getChildren();

         if (g1children.length != g2children.length) {
            return false;
         }

         if (g1children.length == 0) {
            return true;
         }

         final HashMap<Set<UUID>, IntArrayList> uuidSetNodeListMap = new HashMap<>();
         int depthToTest = 0;

         while ((uuidSetNodeListMap.size() < g1children.length) && (depthToTest < maxDepth - depth)) {
            depthToTest++;
            uuidSetNodeListMap.clear();

            for (final AbstractLogicNode child : g1children) {
               final Set<UUID> nodeUuidSetForDepth = child.getNodeUuidSetForDepth(depthToTest);

               if (!uuidSetNodeListMap.containsKey(nodeUuidSetForDepth)) {
                  final IntArrayList nodeList = new IntArrayList();

                  nodeList.add(child.getNodeIndex());
                  uuidSetNodeListMap.put(nodeUuidSetForDepth, nodeList);
               } else {
                  uuidSetNodeListMap.get(nodeUuidSetForDepth)
                          .add(child.getNodeIndex());
               }
            }
         }

         // need to try all combinations
         for (final AbstractLogicNode g2Child : g2children) {
            final Set<UUID> nodeUuidSetForDepth = g2Child.getNodeUuidSetForDepth(depthToTest);
            final IntArrayList possibleMatches = uuidSetNodeListMap.get(nodeUuidSetForDepth);

            if (possibleMatches == null) {
               return false;
            }

            int match = -1;

            for (final int possibleMatchIndex : possibleMatches.elements()) {
               if (graphsEqual((AbstractLogicNode) this.logicNodes.get(possibleMatchIndex),
                       g2Child,
                       depth + 1,
                       maxDepth)) {
                  match = possibleMatchIndex;
                  break;
               }
            }

            if (match == -1) {
               return false;
            }

            possibleMatches.delete(match);
         }

         return true;
      }

      return false;
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the concept sequence.
    *
    * @return the concept sequence
    */
   @Override
   public int getConceptSequence() {
      return this.conceptSequence;
   }

   /**
    * Gets the data.
    *
    * @param dataTarget the data target
    * @return the data
    */
   @Override
   public byte[][] getData(DataTarget dataTarget) {
      init();

      final byte[][] byteArrayArray = new byte[this.logicNodes.size()][];

      for (int index = 0; index < byteArrayArray.length; index++) {
         byteArrayArray[index] = this.logicNodes.get(index)
                 .getBytes(dataTarget);
      }

      return byteArrayArray;
   }

   /**
    * Checks if meaningful.
    *
    * @return true, if meaningful
    */
   @Override
   public boolean isMeaningful() {
      return this.logicNodes.stream()
              .anyMatch((node) -> (MEANINGFUL_NODE_SEMANTICS.contains(node.getNodeSemantic())));
   }

   /**
    * Gets the node.
    *
    * @param nodeIndex the node index
    * @return the node
    */
   @Override
   public LogicNode getNode(int nodeIndex) {
      return this.logicNodes.get(nodeIndex);
   }

   /**
    * Gets the node count.
    *
    * @return the node count
    */
   @Override
   public int getNodeCount() {
      return this.logicNodes.size();
   }

   /**
    * Gets the nodes of type.
    *
    * @param semantic the semantic
    * @return the nodes of type
    */
   @Override
   public Stream<LogicNode> getNodesOfType(NodeSemantic semantic) {
      return this.logicNodes.stream()
              .filter((node) -> (node.getNodeSemantic() == semantic));
   }

   /**
    * Gets the root.
    *
    * @return the root
    */
   @Override
   public final RootNode getRoot() {
      if (this.logicNodes.isEmpty()) {
         return Root();
      }

      return (RootNode) this.logicNodes.get(this.rootNodeIndex);
   }
}
