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
package sh.komet.gui.control;

//~--- JDK imports ------------------------------------------------------------
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import sh.isaac.api.Get;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.ComponentNidVersion;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;
import sh.isaac.api.component.sememe.version.LongVersion;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.StringVersion;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.StyleClasses;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public final class ComponentPanel
        extends BadgedVersionPanel {

   private final CategorizedVersions<ObservableCategorizedVersion> categorizedVersions;
   private final ObservableList<VersionPanel> versionPanels = FXCollections.observableArrayList();
   protected final ObservableList<ComponentPanel> extensionPanels = FXCollections.observableArrayList();

   //~--- constructors --------------------------------------------------------
   public ComponentPanel(Manifold manifold, CategorizedVersions<ObservableCategorizedVersion> categorizedVersions) {
      super(manifold, categorizedVersions.getLatestVersion().get());
      if (categorizedVersions.getLatestVersion().isAbsent()) {
         throw new IllegalStateException("Must have a latest version: " + categorizedVersions);
      }
      this.categorizedVersions = categorizedVersions;

      // gridpane.gridLinesVisibleProperty().set(true);
      this.getStyleClass()
              .add(StyleClasses.COMPONENT_PANEL.toString());

      ObservableVersion observableVersion = getCategorizedVersion().getObservableVersion();
      isContradiction.set(this.categorizedVersions.getLatestVersion().isContradicted());
      if (observableVersion instanceof DescriptionVersion) {
         isDescription.set(true);
         setupDescription((DescriptionVersion) observableVersion);
      } else if (observableVersion instanceof ConceptVersion) {
         isConcept.set(true);
         setupConcept((ConceptVersion) observableVersion);
      } else if (observableVersion instanceof LogicGraphVersion) {
         isLogicalDefinition.set(true);
         setupDef((LogicGraphVersion) observableVersion);
      } else {
         setupOther(observableVersion);
      }

      if (this.categorizedVersions.getLatestVersion().isContradicted()) {
         this.categorizedVersions.getLatestVersion().contradictions().forEach((contradiction) -> {
            versionPanels.add(new VersionPanel(manifold, contradiction));
         });
      }
      
      this.categorizedVersions.getHistoricVersions().forEach((historicVersion) -> {
         versionPanels.add(new VersionPanel(manifold, historicVersion));
      });
      
      observableVersion.getChronology().getObservableSememeList().forEach((osc) -> {
            switch (osc.getSememeType()) {
               case DESCRIPTION:
               case LOGIC_GRAPH:
                  break; // Ignore, description and logic graph where already added as an independent panel
               default: 
                  addChronology(osc); 
            }
         
      });
      
      expandControl.setVisible(!versionPanels.isEmpty() || !extensionPanels.isEmpty());
   }
   //~--- methods -------------------------------------------------------------
   //TODO add add show/hide actions.

   private void addChronology(ObservableChronology observableChronology) {

      CategorizedVersions<ObservableCategorizedVersion> oscCategorizedVersions =
         observableChronology.getCategorizedVersions(
             getManifold());

      if (oscCategorizedVersions.getLatestVersion()
                                .isPresent()) {
         extensionPanels.add(new ComponentPanel(getManifold(), oscCategorizedVersions));
      }
   }


   private void setupOther(Version version) {
      if (version instanceof SememeVersion) {
         SememeVersion sememeVersion = (SememeVersion) version;
         SememeType sememeType = sememeVersion.getChronology().getSememeType();
         componentType.setText(sememeType.toString());
         switch (sememeType) {
            case STRING:
               componentType.setText("EXT");
               componentText.setText(getManifold().getPreferredDescriptionText(sememeVersion.getAssemblageSequence()) + "\n"
                       + ((StringVersion) sememeVersion).getString());
               break;
            case COMPONENT_NID:
               componentType.setText("REF");
               int nid = ((ComponentNidVersion) sememeVersion).getComponentNid();
               switch (Get.identifierService().getChronologyTypeForNid(nid)) {
                  case CONCEPT:
                     componentText.setText(getManifold().getFullySpecifiedDescriptionText(nid));
                     break;
                  case SEMEME:
                     SememeChronology sc = Get.sememeService().getSememe(nid);
                     componentText.setText("References: " + sc.getSememeType().toString());
                     break;
                  case UNKNOWN_NID:
                  default:
                     componentText.setText(Get.identifierService().getChronologyTypeForNid(nid).toString());
               }
               break;
            case LOGIC_GRAPH:
               componentText.setText(((LogicGraphVersion) sememeVersion).getLogicalExpression().toString());
               break;
            case LONG:
               componentText.setText(Long.toString(((LongVersion) sememeVersion).getLongValue()));
               break;
            case MEMBER:
               componentText.setText("Member");
               break;
            case DYNAMIC:
            case UNKNOWN:
            case DESCRIPTION:
            default:
               throw new UnsupportedOperationException("Can't handle: " + sememeType);

         }
      } else {
         componentText.setText(version.getClass().getSimpleName());
      }

   }


   private void setupConcept(ConceptVersion conceptVersion) {
      componentType.setText("Concept");
      componentText.setText("");
   }

   private void setupDescription(DescriptionVersion description) {
      componentText.setText(description.getText());

      int descriptionType = description.getDescriptionTypeConceptSequence();

      if (descriptionType == TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence()) {
         componentType.setText("FSN");
      } else if (descriptionType == TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence()) {
         componentType.setText("SYN");
      } else if (descriptionType == TermAux.DEFINITION_DESCRIPTION_TYPE.getConceptSequence()) {
         componentType.setText("DEF");
      } else {
         componentType.setText(getManifold().getPreferredDescriptionText(descriptionType));
      }
      if (description.getCaseSignificanceConceptSequence() == TermAux.DESCRIPTION_CASE_SENSITIVE.getConceptSequence()) {
         badges.add(Iconography.CASE_SENSITIVE.getIconographic());
      } else if (description.getCaseSignificanceConceptSequence() == TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getConceptSequence()) {
         // TODO get iconographic for initial character sensitive
         badges.add(Iconography.CASE_SENSITIVE.getIconographic());
      } else if (description.getCaseSignificanceConceptSequence() == TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence()) {
         badges.add(Iconography.CASE_SENSITIVE_NOT.getIconographic());
      }

   }

   private void setupDef(LogicGraphVersion logicGraphVersion) {
      componentType.setText("DEF");
      componentText.setText(logicGraphVersion.getLogicalExpression().toString());
      if (getManifold().getLogicCoordinate().getInferredAssemblageSequence() == logicGraphVersion.getAssemblageSequence()) {
         badges.add(Iconography.SETTINGS_GEAR.getIconographic());
      } else if (getManifold().getLogicCoordinate().getStatedAssemblageSequence() == logicGraphVersion.getAssemblageSequence()) {
         badges.add(Iconography.ICON_EXPORT.getIconographic());
      }
   }

   @Override
   protected void expand(ObservableValue<? extends ExpandAction> observable, ExpandAction oldValue, ExpandAction newValue) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
}
