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



package sh.isaac.model.sememe.version;

//~--- non-JDK imports --------------------------------------------------------

import org.glassfish.hk2.api.MultiException;

import sh.isaac.api.DataSource;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionByteArrayConverter;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.api.component.sememe.version.MutableLogicGraphVersion;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LogicGraphVersionImpl.
 *
 * @author kec
 */
public class LogicGraphVersionImpl
        extends SememeVersionImpl
         implements MutableLogicGraphVersion {
   /** The converter. */
   private static LogicalExpressionByteArrayConverter converter;

   //~--- fields --------------------------------------------------------------

   /** The graph data. */
   byte[][] graphData = null;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new logic graph sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    */
   public LogicGraphVersionImpl(SememeChronologyImpl container,
                               int stampSequence,
                               short versionSequence) {
      super(container, stampSequence, versionSequence);
   }

   /**
    * Instantiates a new logic graph sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    * @param data the data
    */
   public LogicGraphVersionImpl(SememeChronologyImpl container,
                               int stampSequence,
                               short versionSequence,
                               ByteArrayDataBuffer data) {
      super(container, stampSequence, versionSequence);

      final int graphNodes = data.getInt();

      this.graphData = new byte[graphNodes][];

      for (int i = 0; i < graphNodes; i++) {
         try {
            this.graphData[i] = data.getByteArrayField();
         } catch (final ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
         }
      }

      if (data.isExternalData()) {
         this.graphData = getExternalDataConverter().convertLogicGraphForm(this.graphData, DataTarget.INTERNAL);
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append(getSememeType().toString());

      final LogicalExpressionImpl lg = new LogicalExpressionImpl(this.graphData,
                                                                           DataSource.INTERNAL,
                                                                           Get.identifierService().getConceptSequence(
                                                                              getReferencedComponentNid()));

      sb.append("\n ");
      sb.append(lg.toString());
      toString(sb);
      return sb.toString();
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);

      byte[][] temp = this.graphData;

      if (data.isExternalData()) {
         temp = getExternalGraphData();
      }

      data.putInt(temp.length);

      for (final byte[] graphDataElement: temp) {
         data.putByteArrayField(graphDataElement);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the external data converter.
    *
    * @return the external data converter
    * @throws MultiException the multi exception
    */
   private static LogicalExpressionByteArrayConverter getExternalDataConverter()
            throws MultiException {
      if (converter == null) {
         converter = LookupService.get()
                                  .getService(LogicalExpressionByteArrayConverter.class);
      }

      return converter;
   }

   /**
    * Gets the external graph data.
    *
    * @return the external graph data
    */
   @Override
   public byte[][] getExternalGraphData() {
      return getExternalDataConverter().convertLogicGraphForm(this.graphData, DataTarget.EXTERNAL);
   }

   /**
    * Gets the graph data.
    *
    * @return the graph data
    */
   @Override
   public byte[][] getGraphData() {
      return this.graphData;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the graph data.
    *
    * @param graphData the new graph data
    */
   @Override
   public void setGraphData(byte[][] graphData) {
      if (this.graphData != null) {
         checkUncommitted();
      }

      this.graphData = graphData;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the logical expression.
    *
    * @return the logical expression
    */
   @Override
   public LogicalExpression getLogicalExpression() {
      return new LogicalExpressionImpl(this.graphData, DataSource.INTERNAL, getReferencedComponentNid());
   }

   /**
    * Gets the sememe type.
    *
    * @return the sememe type
    */
   @Override
   public SememeType getSememeType() {
      return SememeType.LOGIC_GRAPH;
   }
}

