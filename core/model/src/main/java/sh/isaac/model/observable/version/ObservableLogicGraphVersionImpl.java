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



package sh.isaac.model.observable.version;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;
import sh.isaac.api.component.sememe.version.MutableLogicGraphVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableLogicGraphVersion;
import sh.isaac.model.observable.CommitAwareObjectProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.sememe.version.LogicGraphVersionImpl;

//~--- classes ----------------------------------------------------------------

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
   public ObservableLogicGraphVersionImpl(LogicGraphVersion version, ObservableSememeChronology chronology) {
      super(version, chronology);
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
         this.graphProperty = new CommitAwareObjectProperty(
             this,
             ObservableFields.LOGIC_GRAPH_FOR_SEMEME.toExternalString(),
             getGraphData());
         this.graphProperty.addListener(
             (observable, oldValue, newValue) -> {
                ((LogicGraphVersionImpl) this.stampedVersionProperty.get()).setGraphData(newValue);
             });
      }

      return this.graphProperty;
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      LogicGraphVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      ObservableLogicGraphVersionImpl newObservableVersion = new ObservableLogicGraphVersionImpl(
                                                                 newVersion,
                                                                       (ObservableSememeChronology) chronology);

      ((ObservableChronologyImpl) chronology).getVersionList()
            .add(newObservableVersion);
      return (V) newObservableVersion;
   }

   @Override
   public String toString() {
      return "ObservableLogicGraphVersionImpl{data[][]:" +
             ((MutableLogicGraphVersion) this.stampedVersionProperty.get()).getLogicalExpression() + '}';
   }

   @Override
   protected void updateVersion() {
      super.updateVersion();
      if (this.graphProperty != null && !Arrays.deepEquals(this.graphProperty.get(), ((LogicGraphVersion) this.stampedVersionProperty.get()).getGraphData())) {
         this.graphProperty.set(((LogicGraphVersion) this.stampedVersionProperty.get()).getGraphData());
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public byte[][] getExternalGraphData() {
      return ((MutableLogicGraphVersion) this.stampedVersionProperty.get()).getExternalGraphData();
   }

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

      return ((LogicGraphVersion) this.stampedVersionProperty.get()).getGraphData();
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
      }

      ((MutableLogicGraphVersion) this.stampedVersionProperty.get()).setGraphData(graphData);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public LogicalExpression getLogicalExpression() {
      return ((MutableLogicGraphVersion) this.stampedVersionProperty.get()).getLogicalExpression();
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(logicGraphProperty());
      return properties;
   }
}

