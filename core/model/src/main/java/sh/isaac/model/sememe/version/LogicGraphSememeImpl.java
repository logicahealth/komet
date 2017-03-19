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
import sh.isaac.api.component.sememe.version.MutableLogicGraphSememe;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionByteArrayConverter;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.sememe.SememeChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class LogicGraphSememeImpl
        extends SememeVersionImpl<LogicGraphSememeImpl>
         implements MutableLogicGraphSememe<LogicGraphSememeImpl> {
   private static LogicalExpressionByteArrayConverter converter;

   //~--- fields --------------------------------------------------------------

   byte[][] graphData = null;

   //~--- constructors --------------------------------------------------------

   public LogicGraphSememeImpl(SememeChronologyImpl<LogicGraphSememeImpl> container,
                               int stampSequence,
                               short versionSequence) {
      super(container, stampSequence, versionSequence);
   }

   public LogicGraphSememeImpl(SememeChronologyImpl<LogicGraphSememeImpl> container,
                               int stampSequence,
                               short versionSequence,
                               ByteArrayDataBuffer data) {
      super(container, stampSequence, versionSequence);

      int graphNodes = data.getInt();

      this.graphData = new byte[graphNodes][];

      for (int i = 0; i < graphNodes; i++) {
         try {
            this.graphData[i] = data.getByteArrayField();
         } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
         }
      }

      if (data.isExternalData()) {
         graphData = getExternalDataConverter().convertLogicGraphForm(graphData, DataTarget.INTERNAL);
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();

      sb.append(getSememeType().toString());

      LogicalExpressionOchreImpl lg = new LogicalExpressionOchreImpl(graphData,
                                                                     DataSource.INTERNAL,
                                                                     Get.identifierService().getConceptSequence(
                                                                        getReferencedComponentNid()));

      sb.append("\n ");
      sb.append(lg.toString());
      toString(sb);
      return sb.toString();
   }

   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);

      byte[][] temp = graphData;

      if (data.isExternalData()) {
         temp = getExternalGraphData();
      }

      data.putInt(temp.length);

      for (byte[] graphDataElement: temp) {
         data.putByteArrayField(graphDataElement);
      }
   }

   //~--- get methods ---------------------------------------------------------

   private static LogicalExpressionByteArrayConverter getExternalDataConverter()
            throws MultiException {
      if (converter == null) {
         converter = LookupService.get()
                                  .getService(LogicalExpressionByteArrayConverter.class);
      }

      return converter;
   }

   @Override
   public byte[][] getExternalGraphData() {
      return getExternalDataConverter().convertLogicGraphForm(graphData, DataTarget.EXTERNAL);
   }

   @Override
   public byte[][] getGraphData() {
      return graphData;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setGraphData(byte[][] graphData) {
      if (this.graphData != null) {
         checkUncommitted();
      }

      this.graphData = graphData;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public LogicalExpression getLogicalExpression() {
      return new LogicalExpressionOchreImpl(graphData, DataSource.INTERNAL, getReferencedComponentNid());
   }

   @Override
   public SememeType getSememeType() {
      return SememeType.LOGIC_GRAPH;
   }
}

