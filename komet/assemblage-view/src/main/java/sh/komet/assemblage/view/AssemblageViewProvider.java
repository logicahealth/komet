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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.util.number.NumberUtil;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.concept.AddToContextMenu;
import sh.komet.gui.control.concept.MenuSupplierForFocusConcept;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNodeAbstract;
import sh.komet.gui.search.SearchToolbar;
import sh.komet.gui.util.FxGet;

import static sh.komet.gui.style.StyleClasses.ASSEMBLAGE_DETAIL;

/**
 * @author kec
 */
public class AssemblageViewProvider extends DetailNodeAbstract {
    protected static final Logger LOG = LogManager.getLogger();

    {
        toolTipProperty.setValue("listing of assemblage members");
        titleProperty.setValue("empty assemblage view");
        menuIconProperty.setValue(Iconography.PAPERCLIP.getIconographic());
    }
    private static AddToContextMenu[] getContextMenuProviders(ViewProperties viewProperties, ActivityFeed activityFeed) {
        return new AddToContextMenu[] {
                AssemblageMenuProvider.get(),
                MenuSupplierForFocusConcept.get()
        };
    }
    private final SearchToolbar searchToolbar;
    private final AssemblageDetailController assemblageDetailController;

    public AssemblageViewProvider(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferencesNode) {
        super(viewProperties, activityFeed, preferencesNode, getContextMenuProviders(viewProperties, activityFeed));
        try {
            if (activityFeed.isLinked()) {
                FxGet.dialogs().showErrorDialog(new IllegalStateException("Activity feed for assemblage must be unlinked... Found " +
                        activityFeed.getFeedName()));
            }

            this.detailPane.getStyleClass().setAll(ASSEMBLAGE_DETAIL.toString());
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sh/komet/assemblage/view/AssemblageDetail.fxml"));
            BorderPane rootPane = loader.load();
            this.assemblageDetailController = loader.getController();
            this.assemblageDetailController.setViewProperties(this.viewProperties);
            this.detailPane.setCenter(assemblageDetailController.getAssemblageDetailRootPane());

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
    public void updateFocusedObject(IdentifiedObject component) {
        ConceptSpecification focus = TermAux.UNINITIALIZED_COMPONENT_ID;
        if (component != null) {
            int count = (int) Get.identifierService().getNidsForAssemblage(focus.getNid()).count();
            toolTipProperty.set("View of all " + count + " " + viewProperties.getPreferredDescriptionText(focus) + " assemblage elements");
            this.conceptLabelToolbar.getRightInfoLabel().setText(NumberUtil.formatWithGrouping(count) + " ");
            focus = Get.concept(component.getNid());
            this.assemblageDetailController.updateFocus(component, count);
        }
    }

    @Override
    public boolean selectInTabOnChange() {
        return false;
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.PAPERCLIP.getIconographic();
    }

    @Override
    public void savePreferences() {
        // TODO save more preferences.
    }

    private void search(String searchString) {
        Searcher searcher = new Searcher(searchString);
        Get.executor().execute(searcher);
    }

    @Override
    public Node getNode() {
        return detailPane;
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
        final AtomicInteger nodeCount = new AtomicInteger();
        final AtomicInteger testedNodes = new AtomicInteger();

        public Searcher(String searchString) {
            this.searchString = searchString;
            countNodes(assemblageDetailController.getAssemblageExtensionTreeTable().getRoot());
        }

        private void countNodes(TreeItem<ObservableCategorizedVersion>  treeNode) {
            nodeCount.incrementAndGet();
            for (TreeItem<ObservableCategorizedVersion>  child : treeNode.getChildren()) {
                countNodes(child);
            }
        }

        @Override
        public void run() {
            handleTreeItem(assemblageDetailController.getAssemblageExtensionTreeTable().getRoot());
        }

        private void handleTreeItem(TreeItem<ObservableCategorizedVersion> item) {
            if (item != null && item.getValue() != null) {
                ObservableCategorizedVersion version = item.getValue();
                if (version.unwrap().toString().contains(searchString)) {
                    searchToolbar.addResult(item);
                }
                Platform.runLater(() -> {
                    for (TreeItem<ObservableCategorizedVersion> child : item.getChildren()) {
                        Get.executor().execute(() -> handleTreeItem(child));
                    }
                });
            }
            testedNodes.incrementAndGet();
            searchToolbar.setProgress(testedNodes.doubleValue()/nodeCount.doubleValue());
        }
    }
}
