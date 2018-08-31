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
package sh.komet.gui.contract;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.action.Action;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.BusinessRulesService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.control.PropertySheetMenuItem;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
@Contract
public interface RulesDrivenKometService extends BusinessRulesService {
   List<Action> getEditLogicalExpressionNodeMenuItems(Manifold manifold, 
           LogicNode nodeToEdit, 
           LogicalExpression expressionContiningNode,
            Consumer<LogicalExpression> expressionUpdater,
            MouseEvent mouseEvent);
   
   List<MenuItem> getEditVersionMenuItems(Manifold manifold, 
           ObservableCategorizedVersion categorizedVersion, 
           Consumer<PropertySheetMenuItem> propertySheetConsumer);
   
   List<MenuItem> getAddAttachmentMenuItems(Manifold manifold, 
           ObservableCategorizedVersion categorizedVersion, 
           BiConsumer<PropertySheetMenuItem, ConceptSpecification> newAttachmentConsumer);
   
   void populatePropertySheetEditors(PropertySheetMenuItem propertySheetMenuItem);
   
   void populateWrappedProperties(List<PropertySheet.Item> items);
}
