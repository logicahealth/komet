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
package sh.komet.gui.action.dashboard;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.skins.BarChartItem;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.cell.list.ConceptCell;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class DashboardView
        extends BorderPane
        implements ExplorationNode {

    private static final double TILE_WIDTH = 150;
    private static final double TILE_HEIGHT = 150;

    public static final Color RED = Tile.TileColor.RED.color;
    public static final Color LIGHT_RED = Tile.TileColor.LIGHT_RED.color;
    public static final Color GREEN = Tile.TileColor.GREEN.color;
    public static final Color LIGHT_GREEN = Tile.TileColor.LIGHT_GREEN.color;
    public static final Color BLUE = Tile.TileColor.BLUE.color;
    public static final Color DARK_BLUE = Tile.TileColor.DARK_BLUE.color;
    public static final Color ORANGE = Tile.TileColor.ORANGE.color;
    public static final Color YELLOW_ORANGE = Tile.TileColor.YELLOW_ORANGE.color;
    public static final Color YELLOW = Tile.TileColor.YELLOW.color;
    public static final Color MAGENTA = Tile.TileColor.MAGENTA.color;
    public static final Color PINK = Tile.TileColor.PINK.color;

    private static final Color[] COLORS = new Color[]{RED, GREEN, BLUE, ORANGE, YELLOW, PINK,
        LIGHT_RED, LIGHT_GREEN, DARK_BLUE, YELLOW_ORANGE, MAGENTA};

    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Information about memory use and other system aspects. ");
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("System Dashboard");
    private final Label titleLabel = new Label();
    private final Manifold manifold;
    private final ListView<Integer> assemblageListView;
    private AssemblageDashboardStats assemblageStats;
    private Future<?> statsFuture;

    public DashboardView(Manifold manifold) {
        this.manifold = manifold;
        this.titleLabel.graphicProperty().set(Iconography.DASHBOARD.getIconographic());

        ObservableList<Integer> assemblageList = FXCollections.observableArrayList();

        for (int assemblageNid : Get.assemblageService().getAssemblageConceptNids()) {
            assemblageList.add(assemblageNid);
        }
        assemblageList.sort((o1, o2) -> {
            return manifold.getPreferredDescriptionText(o1)
                    .compareTo(manifold.getPreferredDescriptionText(o2));
        });
        assemblageListView = new ListView(assemblageList);
        assemblageListView.setCellFactory((ListView<Integer> list) -> new ConceptCell(this.manifold));
        this.setLeft(assemblageListView);
        assemblageListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        assemblageListView.getSelectionModel().selectedItemProperty().addListener(this::selectionListener);

    }

    private void selectionListener(Observable observable, Integer oldValue, Integer newAssemblageValue) {
        assemblageStats = null;

        TilePane pane = new TilePane();
        pane.setBackground(new Background(new BackgroundFill(Color.web("#c3cdd3"), CornerRadii.EMPTY, Insets.EMPTY)));
        this.setCenter(pane);
        assemblageStats = new AssemblageDashboardStats(newAssemblageValue);
        assemblageStats.stateProperty().addListener(this::stateChangedListener);
        statsFuture = Get.executor().submit(assemblageStats);
    }

    private void stateChangedListener(ObservableValue<? extends Worker.State> observable,
            Worker.State oldValue, Worker.State newValue) {
        if (newValue == Worker.State.SUCCEEDED) {
            try {
                statsFuture.get();
            } catch (Throwable ex) {
                assemblageStats = null;
                ex.printStackTrace();
            }
            if (assemblageStats != null) {
                Platform.runLater(() -> {
                    AssemblageDashboardStats localStats = assemblageStats;
                    if (localStats != null) {
                        NumberFormat groupedFormat = NumberFormat.getIntegerInstance();
                        groupedFormat.setGroupingUsed(true);
                        List<Tile> tiles = new ArrayList<>();

                        String semanticCountString = groupedFormat.format(localStats.getSemanticCount().get());
                        setupTile(TileBuilder.create()
                                .skinType(Tile.SkinType.TEXT)
                                .title("Semantics in Assemblage")
                                .description(semanticCountString + "\nelements")
                                .textVisible(true)
                                .build(), tiles);
                        int versionCount = localStats.getVersionCount().get();
                        String versionCountString = groupedFormat.format(versionCount);
                        setupTile(TileBuilder.create()
                                .skinType(Tile.SkinType.TEXT)
                                .title("Versions in Assemblage")
                                .description(versionCountString + "\nversions")
                                .textVisible(true)
                                .build(), tiles);

                        setupModuleBarChart(localStats, versionCount, tiles);
                        setupTimeBarChart(localStats, versionCount, tiles);
                        setupSemanticTypeBarChart(localStats, versionCount, tiles);
                        TilePane pane = new TilePane();
                        pane.setHgap(5);
                        pane.setVgap(5);
                        pane.setAlignment(Pos.TOP_LEFT);
                        pane.setCenterShape(true);
                        pane.setPadding(new Insets(5));
                        //pane.setPrefSize(800, 600);
                        pane.setBackground(new Background(new BackgroundFill(Color.web("#c3cdd3"), CornerRadii.EMPTY, Insets.EMPTY)));
                        for (Tile tile : tiles) {
                            pane.getChildren().add(tile);
                        }
                        this.setCenter(pane);
                    }

                });
            }
        }
    }

    private void setupTimeBarChart(AssemblageDashboardStats localStats, int versionCount, List<Tile> tiles) {
        BarChartItem[] barsForTimes = new BarChartItem[localStats.getCommitTimes().size()];
        int index = 0;
        int colorIndex = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Map.Entry<Long, AtomicInteger> entry : localStats.getCommitTimes().entrySet()) {
            String commitTimeString = formatter.format(Instant.ofEpochMilli(entry.getKey()).atZone(ZoneOffset.UTC));
            double value = entry.getValue().get() * 100 / versionCount;
            barsForTimes[index++] = new BarChartItem(commitTimeString, value, COLORS[colorIndex++]);
            if (colorIndex >= COLORS.length) {
                colorIndex = 0;
            }
        }

        setupTile(TileBuilder.create()
                .skinType(Tile.SkinType.BAR_CHART)
                .title("Commit times in Assemblage")
                .barChartItems(barsForTimes)
                .decimals(0)
                .build(), tiles);
    }

    private void setupModuleBarChart(AssemblageDashboardStats localStats, int versionCount, List<Tile> tiles) {
        BarChartItem[] barsForModules = new BarChartItem[localStats.getModules().size()];
        int index = 0;
        int colorIndex = 0;
        for (Map.Entry<Integer, AtomicInteger> entry : localStats.getModules().entrySet()) {
            String moduleName = manifold.getPreferredDescriptionText(entry.getKey());
            double value = entry.getValue().get() * 100 / versionCount;
            barsForModules[index++] = new BarChartItem(moduleName, value, COLORS[colorIndex++]);
            if (colorIndex >= COLORS.length) {
                colorIndex = 0;
            }
        }

        setupTile(TileBuilder.create()
                .skinType(Tile.SkinType.BAR_CHART)
                .title("Modules in Assemblage")
                .barChartItems(barsForModules)
                .decimals(0)
                .build(), tiles);
    }


    private void setupSemanticTypeBarChart(AssemblageDashboardStats localStats, int versionCount, List<Tile> tiles) {
        BarChartItem[] barsForModules = new BarChartItem[localStats.getSemanticTypes().size()];
        int index = 0;
        int colorIndex = 0;
        for (Map.Entry<VersionType, AtomicInteger> entry : localStats.getSemanticTypes().entrySet()) {
            String semanticType = entry.getKey().toString();
            double value = entry.getValue().get() * 100 / versionCount;
            barsForModules[index++] = new BarChartItem(semanticType, value, COLORS[colorIndex++]);
            if (colorIndex >= COLORS.length) {
                colorIndex = 0;
            }
        }

        setupTile(TileBuilder.create()
                .skinType(Tile.SkinType.BAR_CHART)
                .title("Semantic types in Assemblage")
                .barChartItems(barsForModules)
                .decimals(0)
                .build(), tiles);
    }

    private void setupTile(Tile textNumberTile, List<Tile> tiles) {
        textNumberTile.setPrefSize(TILE_WIDTH, TILE_HEIGHT);
        textNumberTile.setMinSize(TILE_WIDTH, TILE_HEIGHT);
        textNumberTile.setMaxSize(TILE_WIDTH, TILE_HEIGHT);
        tiles.add(textNumberTile);
    }

    @Override
    public Manifold getManifold() {
        return this.manifold;
    }

    @Override
    public Node getNode() {
        return this;
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
        return Optional.of(titleLabel);
    }
}
