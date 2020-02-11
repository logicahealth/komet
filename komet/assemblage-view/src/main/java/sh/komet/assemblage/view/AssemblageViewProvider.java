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
package sh.komet.assemblage.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.Arrays;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.util.number.NumberUtil;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.concept.ConceptLabelToolbar;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.search.SearchToolbar;
import sh.komet.gui.util.FxGet;

import static sh.komet.gui.style.StyleClasses.ASSEMBLAGE_DETAIL;

/**
 * @author kec
 */
public class AssemblageViewProvider implements ExplorationNode, Supplier<List<MenuItem>> {
    protected static final Logger LOG = LogManager.getLogger();

    private final BorderPane assemblageDetailPane = new BorderPane();
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("listing of assemblage members");
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("empty assemblage view");
    private final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(Iconography.PAPERCLIP.getIconographic());
    private final ConceptLabelToolbar conceptLabelToolbar;
    private final SearchToolbar searchToolbar;
    private final AssemblageDetailController assemblageDetailController;

    private final SimpleObjectProperty<Manifold> manifoldProperty = new SimpleObjectProperty<>();
    private final SimpleIntegerProperty selectionIndexProperty = new SimpleIntegerProperty(0);
    private final AssemblageMenuProvider assemblageMenuProvider = new AssemblageMenuProvider(this.manifoldProperty);
    private final ListChangeListener<ComponentProxy> selectionChangedListener = c -> this.selectionChanged(c);

    public AssemblageViewProvider(Manifold manifold) {
        try {
            if (!manifold.getGroupName().equals(ManifoldGroup.UNLINKED.getGroupName())) {
                FxGet.dialogs().showErrorDialog(new IllegalStateException("Manifold for assemblage must be unlinked..."));
            }
            this.manifoldProperty.set(manifold);

            this.assemblageDetailPane.getStyleClass().setAll(ASSEMBLAGE_DETAIL.toString());
            this.conceptLabelToolbar = ConceptLabelToolbar.make(manifoldProperty, selectionIndexProperty, this, Optional.empty());
            this.assemblageDetailPane.setTop(conceptLabelToolbar.getToolbarNode());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sh/komet/assemblage/view/AssemblageDetail.fxml"));
            BorderPane rootPane = loader.load();
            this.assemblageDetailController = loader.getController();
            this.assemblageDetailController.setManifoldProperty(manifoldProperty);
            manifold.manifoldSelectionProperty().addListener(this.selectionChangedListener);
            manifoldProperty.addListener((observable, oldValue, newValue) -> {
                oldValue.manifoldSelectionProperty().removeListener(this.selectionChangedListener);
                newValue.manifoldSelectionProperty().addListener(this.selectionChangedListener);
            });
            assemblageDetailPane.setCenter(assemblageDetailController.getAssemblageDetailRootPane());

            this.searchToolbar = new SearchToolbar();
            this.searchToolbar.setSearchConsumer(this::search);
            this.searchToolbar.selectedObjectProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    TreeItem<ObservableCategorizedVersion> newTreeSelection = (TreeItem<ObservableCategorizedVersion>) newValue;
                    TreeItem<ObservableCategorizedVersion> parentOfSelection = newTreeSelection.getParent();
                    while (parentOfSelection != null) {
                        if (!parentOfSelection.isExpanded()) {
                            parentOfSelection.setExpanded(true);
                        }
                        parentOfSelection = parentOfSelection.getParent();
                    }
                    this.assemblageDetailController.getAssemblageExtensionTreeTable()
                            .getSelectionModel().select(newTreeSelection);
                    int selectedIndex = this.assemblageDetailController.getAssemblageExtensionTreeTable()
                            .getSelectionModel().getSelectedIndex();
                    FxGet.statusMessageService().reportStatus("Scrolling to selected index: " + selectedIndex);
                    Platform.runLater(() -> this.assemblageDetailController.getAssemblageExtensionTreeTable()
                            .scrollTo(selectedIndex));


                }
            });
            rootPane.setTop(searchToolbar.getSearchToolbar());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }

    private void selectionChanged(ListChangeListener.Change<? extends ComponentProxy> c) {
        ConceptSpecification focus = TermAux.UNINITIALIZED_COMPONENT_ID;
        if (!c.getList().isEmpty()) {
            focus = Get.concept(c.getList().get(0).getNid());
        }
        titleProperty.set(manifoldProperty.get().getPreferredDescriptionText(focus));
        toolTipProperty.set("View of all " + manifoldProperty.get().getPreferredDescriptionText(focus) + " assemblage members");
        int count = (int) Get.identifierService().getNidsForAssemblage(focus.getNid()).count();
        this.conceptLabelToolbar.getRightInfoLabel().setText(NumberUtil.formatWithGrouping(count) + " elements");
    }

    private void search(String searchString) {
        Searcher searcher = new Searcher(searchString);
        Get.executor().execute(searcher);
    }

    @Override
    public SimpleObjectProperty getMenuIconProperty() {
        return menuIconProperty;
    }

    @Override
    public Manifold getManifold() {
        return this.manifoldProperty.get();
    }

    @Override
    public Node getNode() {
        return assemblageDetailPane;
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTipProperty;
    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public List<MenuItem> get() {
        return assemblageMenuProvider.get();
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }

    public class Searcher implements Runnable {
        final String searchString;

        public Searcher(String searchString) {
            this.searchString = searchString;
        }

        @Override
        public void run() {
            handleTreeItem(assemblageDetailController.getAssemblageExtensionTreeTable().getRoot());
        }

        private void handleTreeItem(TreeItem<ObservableCategorizedVersion> item) {
            if (item.getValue() != null) {
                ObservableCategorizedVersion version = item.getValue();
                if (version.unwrap().toString().contains(searchString)) {
                    searchToolbar.addResult(item);
                }
            }
            Platform.runLater(() -> {
                for (TreeItem<ObservableCategorizedVersion> child : item.getChildren()) {
                    Get.executor().execute(() -> handleTreeItem(child));
                }
            });

        }
    }
}
