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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.beans.property.*;
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
import sh.komet.gui.control.concept.ConceptLabelToolbar;
import sh.komet.gui.control.concept.ConceptLabelWithDragAndDrop;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNodeAbstract;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;
import sh.komet.gui.search.SearchToolbar;
import sh.komet.gui.util.FxGet;

import static sh.komet.gui.style.StyleClasses.ASSEMBLAGE_DETAIL;

/**
 * @author kec
 */
public class AssemblageViewProvider extends DetailNodeAbstract implements Supplier<List<MenuItem>> {
    protected static final Logger LOG = LogManager.getLogger();

    private final BorderPane assemblageDetailPane = new BorderPane();
    {
        toolTipProperty.setValue("listing of assemblage members");
        titleProperty.setValue("empty assemblage view");
        menuIconProperty.setValue(Iconography.PAPERCLIP.getIconographic());
    }
    private final ConceptLabelToolbar conceptLabelToolbar;
    private final SearchToolbar searchToolbar;
    private final AssemblageDetailController assemblageDetailController;

    private final AssemblageMenuProvider assemblageMenuProvider;

    public AssemblageViewProvider(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferencesNode) {
        super(viewProperties, activityFeed);
        try {
            if (activityFeed.isLinked()) {
                FxGet.dialogs().showErrorDialog(new IllegalStateException("Activity feed for assemblage must be unlinked... Found " +
                        activityFeed.getFeedName()));
            }
            this.activityFeedProperty.set(activityFeed);

            this.assemblageDetailPane.getStyleClass().setAll(ASSEMBLAGE_DETAIL.toString());
            this.conceptLabelToolbar = ConceptLabelToolbar.make(this.viewProperties,
                    this.identifiedObjectFocusProperty,
                    ConceptLabelWithDragAndDrop::setPreferredText,
                    this.selectionIndexProperty,
                    () -> this.unlinkFromActivityFeed(),
                    this.activityFeedProperty,
                    Optional.of(true));
            this.assemblageMenuProvider = new AssemblageMenuProvider(this.viewProperties, this.getActivityFeed());

            this.assemblageDetailPane.setTop(conceptLabelToolbar.getToolbarNode());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sh/komet/assemblage/view/AssemblageDetail.fxml"));
            BorderPane rootPane = loader.load();
            this.assemblageDetailController = loader.getController();
            this.assemblageDetailController.setViewProperties(this.viewProperties, this.activityFeedProperty);
            this.activityFeedProperty.addListener((observable, oldValue, newValue) -> {
                oldValue.feedSelectionProperty().removeListener(this::selectionChanged);
                newValue.feedSelectionProperty().addListener(this::selectionChanged);
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
    protected void setFocus(IdentifiedObject component) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    private void selectionChanged(ListChangeListener.Change<? extends IdentifiedObject> c) {
        ConceptSpecification focus = TermAux.UNINITIALIZED_COMPONENT_ID;
        if (!c.getList().isEmpty()) {
            focus = Get.concept(c.getList().get(0).getNid());
        }
        titleProperty.set(viewProperties.getPreferredDescriptionText(focus));
        toolTipProperty.set("View of all " + viewProperties.getPreferredDescriptionText(focus) + " assemblage members");
        int count = (int) Get.identifierService().getNidsForAssemblage(focus.getNid()).count();
        this.conceptLabelToolbar.getRightInfoLabel().setText(NumberUtil.formatWithGrouping(count) + " elements");
    }

    private void search(String searchString) {
        Searcher searcher = new Searcher(searchString);
        Get.executor().execute(searcher);
    }

    @Override
    public Node getNode() {
        return assemblageDetailPane;
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
