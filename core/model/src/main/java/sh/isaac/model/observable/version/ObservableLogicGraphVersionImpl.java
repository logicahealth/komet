/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.model.observable.version;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;
import sh.isaac.api.component.sememe.version.MutableLogicGraphVersion;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableLogicGraphVersion;
import sh.isaac.model.observable.CommitAwareObjectProperty;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author kec
 */
public class ObservableLogicGraphVersionImpl 
        extends ObservableSememeVersionImpl 
        implements ObservableLogicGraphVersion {
   /** The graph property. */
   ObjectProperty<byte[][]> graphProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable component nid version impl.
    *
    * @param version the stamped version
    * @param chronology the chronology
    */
   public ObservableLogicGraphVersionImpl(LogicGraphVersion version,
                                    ObservableSememeChronology chronology) {
      super(version, 
              chronology);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Case significance concept sequence property.
    *
    * @return the integer property
    */
   @Override
   public ObjectProperty<byte[][]> logicGraphProperty() {
      if (this.graphProperty == null) {
         this.graphProperty = new CommitAwareObjectProperty(this,
               ObservableFields.LOGIC_GRAPH_FOR_SEMEME.toExternalString(),
               getGraphData());
      }

      return this.graphProperty;
   }


   @Override
   public List<Property<?>> getProperties() {
      List<Property<?>> properties = super.getProperties();
      properties.add(logicGraphProperty());
      return properties;
   }  

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the graph data.
    *
    * @return the graph data
    */
   @Override
   public byte[][] getGraphData() {
      if (this.graphProperty != null) {
         return this.graphProperty.get();
      }

      return ((LogicGraphVersion) this.stampedVersion).getGraphData();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the case significance concept sequence.
    *
    * @param graphData the new case significance concept sequence
    */
   @Override
   public void setGraphData(byte[][] graphData) {
      if (this.graphProperty != null) {
         this.graphProperty.set(graphData);
      } else {
         ((MutableLogicGraphVersion) this.stampedVersion).setGraphData(graphData);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String toString() {
      return "ObservableLogicGraphVersionImpl{data[][]:" + ((MutableLogicGraphVersion) this.stampedVersion).getLogicalExpression() + '}';
   }

   @Override
   public byte[][] getExternalGraphData() {
      return ((MutableLogicGraphVersion) this.stampedVersion).getExternalGraphData();
   }

   @Override
   public LogicalExpression getLogicalExpression() {
      return ((MutableLogicGraphVersion) this.stampedVersion).getLogicalExpression();
   }
}
   