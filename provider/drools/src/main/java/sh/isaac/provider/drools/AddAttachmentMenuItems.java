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
package sh.isaac.provider.drools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.MenuItem;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.task.OptionalWaitTask;
import sh.komet.gui.control.PropertySheetMenuItem;
import sh.komet.gui.manifold.Manifold;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

/**
 *
 * @author kec
 */
public class AddAttachmentMenuItems {
   final List<MenuItem> menuItems = new ArrayList<>();
   final Manifold manifold;
   final ObservableCategorizedVersion categorizedVersion;
   final BiConsumer<PropertySheetMenuItem, ConceptSpecification> newAttachmentConsumer;

   public AddAttachmentMenuItems(Manifold manifold, ObservableCategorizedVersion categorizedVersion, 
           BiConsumer<PropertySheetMenuItem, ConceptSpecification> newAttachmentConsumer) {
      this.manifold = manifold;
      this.categorizedVersion = categorizedVersion;
      this.newAttachmentConsumer = newAttachmentConsumer;
   }

   public List<MenuItem> getMenuItems() {
      return menuItems;
   }
   public PropertySheetMenuItem makePropertySheetMenuItem(String menuText, ConceptSpecification assemblageSpecification) {
      PropertySheetMenuItem propertySheetMenuItem = new PropertySheetMenuItem(manifold, categorizedVersion, false);
      MenuItem menuItem = new MenuItem(menuText);
      menuItem.setOnAction((event) -> {
         try {
            SemanticBuilder<? extends SemanticChronology> builder = Get.semanticBuilderService().getStringSemanticBuilder("",
                    this.categorizedVersion.getNid(),
                    assemblageSpecification.getNid());
            
            OptionalWaitTask<? extends SemanticChronology> buildTask = builder.build(manifold.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
            
            // this step does an add uncommitted...
            SemanticChronology newChronology = buildTask.get();
            
            ObservableSemanticChronology newObservableChronology = Get.observableChronologyService().getObservableSememeChronology(newChronology.getNid());
            CategorizedVersions<ObservableCategorizedVersion> categorizedVersions = newObservableChronology.getCategorizedVersions(manifold);
            ObservableStringVersion newStringVersion = (ObservableStringVersion) categorizedVersions.getUncommittedVersions().get(0).getObservableVersion();

            propertySheetMenuItem.setVersionInFlight(newStringVersion);
            
            propertySheetMenuItem.prepareToExecute();
            newAttachmentConsumer.accept(propertySheetMenuItem, assemblageSpecification);
         } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(AddAttachmentMenuItems.class.getName()).log(Level.SEVERE, null, ex);
         }
      });
      menuItems.add(menuItem);
      return propertySheetMenuItem;
   }
   
}
