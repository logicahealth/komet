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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.MenuItem;
import javax.inject.Singleton;
import org.controlsfx.control.PropertySheet;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.BusinessRulesService;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.State;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.contract.RulesDrivenKometService;
import sh.komet.gui.control.BadgedVersionPanel;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class DroolsRulesProvider implements BusinessRulesService, RulesDrivenKometService {

   @Override
   public List<MenuItem> getEditMenuItems(Manifold manifold, BadgedVersionPanel panel) {
      ArrayList<MenuItem> items = new ArrayList<>();
      MenuItem dummyDroolsItem = new MenuItem("Dummy edit item for: " + manifold.getPreferredDescriptionText(panel.getCategorizedVersion().getNid()));
      items.add(dummyDroolsItem);
      return items;
   }
   @Override
   public List<MenuItem> getAttachmentMenuItems(Manifold manifold, BadgedVersionPanel panel) {
      ArrayList<MenuItem> items = new ArrayList<>();
      MenuItem dummyDroolsItem = new MenuItem("Dummy attachment item for: " + manifold.getPreferredDescriptionText(panel.getCategorizedVersion().getNid()));
      items.add(dummyDroolsItem);
      MenuItem editPropertySheetItem = new MenuItem("edit property sheet for: " + manifold.getPreferredDescriptionText(panel.getCategorizedVersion().getNid()));
      addPropertySheetEditorAction(manifold, panel, editPropertySheetItem);
      items.add(editPropertySheetItem);
      return items;
   }
   
   private void addPropertySheetEditorAction(Manifold manifold, BadgedVersionPanel panel, MenuItem editPropertySheetItem) {
      ObservableCategorizedVersion version = panel.getCategorizedVersion();
      PropertySheet propertySheet = new PropertySheet();
      propertySheet.setPropertyEditorFactory(new DroolsPropertyEditorFactory(version));
      // add status and module items...
      IntegerProperty moduleSequenceProperty = version.moduleSequenceProperty();
      // the name of the property is the external string of the concept that specifies the semantics of this field. 
      // TODO: add a gui dialect...
      // TODO: how do we know it is a concept field?
      // TODO: how to determine the constraints? 
      // Need finer granularity than just an Assemblage...
      // Pass the version + field semantics concept spec to Drools. 
      // default rule is to use the field constraints/binding. 
      // Add class-based rules that specialize the constraints. zx
      ConceptProxy moduleSequenceConceptSpec = new ConceptProxy(moduleSequenceProperty.getName());
      
      // TODO: add a gui dialect...
      // TODO: how do we know it is a concept field?
      ObjectProperty<State> stateProperty = version.stateProperty();
      ConceptProxy stateSpecConceptSpec = new ConceptProxy(stateProperty.getName());
      
      
      
   }
   
}
