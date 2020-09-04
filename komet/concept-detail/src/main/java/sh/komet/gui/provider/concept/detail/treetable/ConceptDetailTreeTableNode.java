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
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.concept.ConceptLabelWithDragAndDrop;
import sh.komet.gui.control.concept.MenuSupplierForFocusConcept;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNodeAbstract;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ConceptDetailTreeTableNode extends DetailNodeAbstract implements Supplier<List<MenuItem>> {

    {
        titleProperty.setValue("empty");
        toolTipProperty.setValue("empty");
        menuIconProperty.setValue(Iconography.CONCEPT_TABLE.getIconographic());
    }

    ConceptDetailTreeTableController conceptDetailController;

    //~--- constructors --------------------------------------------------------
    public ConceptDetailTreeTableNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferences) {
        super(viewProperties, activityFeed, preferences, MenuSupplierForFocusConcept.getArray());
        try {

            detailPane.getStyleClass().add(StyleClasses.CONCEPT_DETAIL_PANE.toString());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sh/komet/gui/provider/concept/detail/ConceptDetail.fxml"));

            loader.load();

            this.conceptDetailController = loader.getController();

            conceptDetailController.setViewProperties(this.viewProperties);
            updateTitle();
            detailPane.setCenter(conceptDetailController.getConceptDetailRootPane());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void updateFocusedObject(IdentifiedObject component) {
        updateTitle();
        this.conceptDetailController.updateFocusedObject(component);
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.CONCEPT_TABLE.getIconographic();
    }

    private Optional<ConceptChronology> getOptionalFocusedConcept() {
        Optional<IdentifiedObject> optionalComponent = this.getFocusedObject();
        if (optionalComponent.isPresent()) {
            return Optional.of(Get.concept(optionalComponent.get().getNid()));
        }
        return Optional.empty();
    }

    @Override
    public void savePreferences() {
        Platform.runLater(() -> FxGet.dialogs().showInformationDialog("Not implemented",
                "Save preferences not yet implemented for ConceptDetailTreeTableNode"));
    }

    private void updateTitle() {
        Optional<ConceptChronology> optionalConcept = getOptionalFocusedConcept();
        if (optionalConcept.isEmpty()) {
            titleProperty.set("empty");
            toolTipProperty.set(
                    "concept details for: empty");
        } else {
            titleProperty.set(this.viewProperties.getPreferredDescriptionText(optionalConcept.get()));
            toolTipProperty.set(
                    "concept details for: "
                            + this.viewProperties.getFullyQualifiedDescriptionText(
                            optionalConcept.get()));
        }
    }
    //~--- get methods ---------------------------------------------------------

    @Override
    public Node getNode() {
        return detailPane;
    }

    @Override
    public List<MenuItem> get() {
        List<MenuItem> assemblageMenuList = new ArrayList<>();
        // No extra menu items added yet. 
        return assemblageMenuList;
    }

    @Override
    public boolean selectInTabOnChange() {
        return this.conceptLabelToolbar.getFocusTabOnConceptChange().get();
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
