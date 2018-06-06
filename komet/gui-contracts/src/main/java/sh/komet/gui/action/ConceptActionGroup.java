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
package sh.komet.gui.action;

import java.util.Collection;
import java.util.Optional;
import javafx.scene.Node;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 * TODO: maybe menu icons come from the terminology?
 * @author kec
 */
public class ConceptActionGroup extends ActionGroup {
   ConceptSpecification actionGroupConcept;
   public ConceptActionGroup(ConceptSpecification actionGroupConcept, ConceptAction... actions) {
      super(null, actions);
      setupProxy(actionGroupConcept);
   }

   private void setupProxy(ConceptSpecification actionGroupConcept) {
      this.actionGroupConcept = actionGroupConcept;
      Optional<String> optionalDescription = actionGroupConcept.getRegularName();
      if (optionalDescription.isPresent()) {
         this.setText(optionalDescription.get());
      } else {
         this.setText(actionGroupConcept.getFullyQualifiedName());
      }
   }

   public ConceptActionGroup(ConceptSpecification actionGroupConcept, Collection<? extends Action> actions) {
      super(null, (Collection<Action>) actions);
      setupProxy(actionGroupConcept);
   }

   public ConceptActionGroup(ConceptSpecification actionGroupConcept, Node icon, Action... actions) {
      super(null, icon, actions);
      setupProxy(actionGroupConcept);
   }

   public ConceptActionGroup(ConceptSpecification actionGroupConcept, Node icon, Collection<? extends Action> actions) {
      super(null, icon, (Collection<Action>) actions);
      setupProxy(actionGroupConcept);
   }
   
}
