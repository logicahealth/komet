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
package sh.komet.gui.provider.concept.detail.treetable;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.concept.ConceptLabelToolbar;
import sh.komet.gui.control.concept.ManifoldLinkedConceptLabel;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ConceptDetailTreeTableNode
        implements DetailNode, Supplier<List<MenuItem>> {

    private final BorderPane conceptDetailPane = new BorderPane();
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("empty");
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("empty");
    private final Manifold conceptDetailManifold;
    private ManifoldLinkedConceptLabel titleLabel = null;
    private final ConceptLabelToolbar conceptLabelToolbar;

    //~--- constructors --------------------------------------------------------
    public ConceptDetailTreeTableNode(Manifold conceptDetailManifold) {
        try {
            this.conceptDetailManifold = conceptDetailManifold;
            this.conceptDetailManifold.getStampCoordinate().allowedStatesProperty().add(Status.INACTIVE);
            conceptDetailManifold.focusedConceptProperty()
                    .addListener(this::updateTitle);
            this.conceptLabelToolbar = ConceptLabelToolbar.make(conceptDetailManifold, this, Optional.of(true));
            conceptDetailPane.setTop(this.conceptLabelToolbar.getToolbarNode());
            conceptDetailPane.getStyleClass().add(StyleClasses.CONCEPT_DETAIL_PANE.toString());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sh/komet/gui/provider/concept/detail/ConceptDetail.fxml"));

            loader.load();

            ConceptDetailTreeTableController conceptDetailController = loader.getController();

            conceptDetailController.setManifold(conceptDetailManifold);
            updateTitle(conceptDetailManifold.focusedConceptProperty(), null, 
                    conceptDetailManifold.getFocusedConcept().get());
            conceptDetailPane.setCenter(conceptDetailController.getConceptDetailRootPane());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void updateTitle(ObservableValue<? extends ConceptSpecification> observable,
            ConceptSpecification oldValue,
            ConceptSpecification newValue) {
        if (titleLabel == null) {
            if (newValue == null) {
                titleProperty.set("empty");
                toolTipProperty.set(
                        "concept details for: empty");
            } else {
                titleProperty.set(this.conceptDetailManifold.getPreferredDescriptionText(newValue));
                toolTipProperty.set(
                        "concept details for: "
                        + this.conceptDetailManifold.getFullySpecifiedDescriptionText(
                                newValue));
            }
        }

    }
    //~--- get methods ---------------------------------------------------------

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return this.titleProperty;
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.CONCEPT_TABLE.getIconographic();
    }

    @Override
    public Node getNode() {
        return conceptDetailPane;
    }

    @Override
    public Optional<Node> getTitleNode() {
        if (titleLabel == null) {
            this.titleLabel = new ManifoldLinkedConceptLabel(conceptDetailManifold, ManifoldLinkedConceptLabel::setPreferredText, this);
            this.titleLabel.setGraphic(Iconography.CONCEPT_TABLE.getIconographic());
            this.titleProperty.set("");
        }
        return Optional.of(titleLabel);
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return this.toolTipProperty;
    }

    @Override
    public List<MenuItem> get() {
        List<MenuItem> assemblageMenuList = new ArrayList<>();
        // No extra menu items added yet. 
        return assemblageMenuList;
    }

    @Override
    public Manifold getManifold() {
        return this.conceptDetailManifold;
    }

    @Override
    public boolean selectInTabOnChange() {
        return this.conceptLabelToolbar.getFocusTabOnConceptChange().get();
    }
}
