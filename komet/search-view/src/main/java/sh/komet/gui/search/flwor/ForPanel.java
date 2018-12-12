/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.search.flwor;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.ForSet;
import static sh.isaac.model.observable.ObservableFields.ASSEMBLAGE_LIST_FOR_QUERY;
import sh.komet.gui.control.concept.PropertySheetItemAssemblageListWrapper;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ForPanel {
    BorderPane forPane = new BorderPane();
    PropertySheet sheet = new PropertySheet();
    private final SimpleListProperty<ConceptSpecification> forAssemblagesProperty = 
            new SimpleListProperty(this, 
                    ASSEMBLAGE_LIST_FOR_QUERY.toExternalString(), FXCollections.observableArrayList());

    final Manifold manifold;
    
    public ForPanel(Manifold manifold) {
        this.manifold = manifold;
        this.sheet.setPropertyEditorFactory(new PropertyEditorFactory(manifold));
        this.sheet.getItems().add(new PropertySheetItemAssemblageListWrapper(manifold, forAssemblagesProperty));
        this.sheet.setMode(PropertySheet.Mode.NAME);
        this.sheet.setModeSwitcherVisible(false);
        this.sheet.setSearchBoxVisible(false);
        AnchorPane.setTopAnchor(forPane, 0.0);
        AnchorPane.setRightAnchor(forPane, 0.0);
        AnchorPane.setBottomAnchor(forPane, 0.0);
        AnchorPane.setLeftAnchor(forPane, 0.0);
        this.forPane.setCenter(sheet);
    }

    public Node getNode() {
        return this.forPane;
    }

    public SimpleListProperty<ConceptSpecification> getForAssemblagesProperty() {
        return this.forAssemblagesProperty;
    }

    ForSet getForSetSpecification() {
        return new ForSet(this.forAssemblagesProperty);
    }

    void reset() {
        this.forAssemblagesProperty.clear();
    }
}
