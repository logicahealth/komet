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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.DataSource;
import sh.isaac.api.DataTarget;
import sh.isaac.api.chronicle.Chronology;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.version.ObservableLogicGraphVersion;
import sh.isaac.model.observable.CommitAwareObjectProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.definition.LogicalExpressionBuilderImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableLogicGraphVersionImpl
        extends ObservableAbstractSemanticVersionImpl
         implements ObservableLogicGraphVersion {
   /** The graph property. */
   ObjectProperty<byte[][]> logicGraphProperty;

   //~--- constructors --------------------------------------------------------
   public ObservableLogicGraphVersionImpl(UUID referencedComponentUuid, int assemblageNid) {
      super(VersionType.LOGIC_GRAPH, UUID.randomUUID(), referencedComponentUuid, assemblageNid);
       LogicalExpressionBuilderImpl builder = new LogicalExpressionBuilderImpl();
       LogicalExpression emptyExpression = builder.build();
       setGraphData(emptyExpression.getData(DataTarget.INTERNAL));
   }
   


   /**
    * Instantiates a new observable component nid version impl.
    *
    * @param version the stamped version
    * @param chronology the chronology
    */
   public ObservableLogicGraphVersionImpl(LogicGraphVersion version, ObservableSemanticChronology chronology) {
      super(version, chronology);
   }

   public ObservableLogicGraphVersionImpl(ObservableLogicGraphVersionImpl versionToClone, ObservableSemanticChronology chronology) {
      super(versionToClone, chronology);
      setGraphData(versionToClone.getGraphData());
   }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        ObservableLogicGraphVersionImpl analog = new ObservableLogicGraphVersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(ec.getModuleNid());
        analog.setAuthorNid(ec.getAuthorNid());
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }

   //~--- methods -------------------------------------------------------------

   /**
    * Case significance concept nid property.
    *
    * @return the integer property
    */
   @Override
   public ObjectProperty<byte[][]> logicGraphProperty() {
      if (this.stampedVersionProperty == null && this.logicGraphProperty == null) {
         this.logicGraphProperty = new CommitAwareObjectProperty(
             this,
             ObservableFields.LOGIC_GRAPH_FOR_SEMANTIC.toExternalString(),
                 null);
      }
      if (this.logicGraphProperty == null) {
         this.logicGraphProperty = new CommitAwareObjectProperty(
             this,
             ObservableFields.LOGIC_GRAPH_FOR_SEMANTIC.toExternalString(),
             getGraphData());
         this.logicGraphProperty.addListener(
             (observable, oldValue, newValue) -> {
                ((LogicGraphVersionImpl) this.stampedVersionProperty.get()).setGraphData(newValue);
             });
      }

      return this.logicGraphProperty;
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      LogicGraphVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      ObservableLogicGraphVersionImpl newObservableVersion = new ObservableLogicGraphVersionImpl(
                                                                 newVersion,
                                                                       (ObservableSemanticChronology) chronology);

      ((ObservableChronologyImpl) chronology).getVersionList()
            .add(newObservableVersion);
      return (V) newObservableVersion;
   }

   @Override
   public String toString() {
      if (this.logicGraphProperty != null) {
          return "ObservableLogicGraphVersionImpl{data[][]:" + new LogicalExpressionImpl(this.logicGraphProperty.get(), DataSource.INTERNAL);
      } 
       
      return "ObservableLogicGraphVersionImpl{data[][]:" +
             ((MutableLogicGraphVersion) this.stampedVersionProperty.get()).getLogicalExpression() + '}';
   }

   @Override
   protected void updateVersion() {
      if (this.logicGraphProperty != null && !Arrays.deepEquals(this.logicGraphProperty.get(), ((LogicGraphVersion) this.stampedVersionProperty.get()).getGraphData())) {
         this.logicGraphProperty.set(((LogicGraphVersion) this.stampedVersionProperty.get()).getGraphData());
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
      if (this.logicGraphProperty != null) {
         return this.logicGraphProperty.get();
      }

      return ((LogicGraphVersion) this.stampedVersionProperty.get()).getGraphData();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the case significance concept nid.
    *
    * @param graphData the new case significance concept nid
    */
   @Override
   public final void setGraphData(byte[][] graphData) {
       if (this.stampedVersionProperty == null) {
           this.logicGraphProperty();
       }
      if (this.logicGraphProperty != null) {
         this.logicGraphProperty.set(graphData);
      }
      if (this.stampedVersionProperty != null) {
        ((MutableLogicGraphVersion) this.stampedVersionProperty.get()).setGraphData(graphData);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public LogicalExpression getLogicalExpression() {
      if (this.logicGraphProperty != null) {
         return new LogicalExpressionImpl(this.logicGraphProperty.get(), DataSource.INTERNAL);
      }
      return ((MutableLogicGraphVersion) this.stampedVersionProperty.get()).getLogicalExpression();
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();

      properties.add(logicGraphProperty());
      return properties;
   }

    @Override
    protected List<Property<?>> getEditableProperties3() {
      List<Property<?>> properties = new ArrayList<>();
      properties.add(logicGraphProperty());
      return properties;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof ObservableLogicGraphVersionImpl) {
            ObservableLogicGraphVersionImpl observableAnalog = (ObservableLogicGraphVersionImpl) analog;
            observableAnalog.setGraphData(this.getGraphData());
        } else if (analog instanceof LogicGraphVersionImpl) {
             LogicGraphVersionImpl simpleAnalog = (LogicGraphVersionImpl) analog;
             simpleAnalog.setGraphData(this.getGraphData());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }
   
    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, 
                getPrimordialUuid(), 
                getAssemblageNid(), 
                this.getReferencedComponentNid());
        LogicGraphVersionImpl newVersion = new LogicGraphVersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }
}

