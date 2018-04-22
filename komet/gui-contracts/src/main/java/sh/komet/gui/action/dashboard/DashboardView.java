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
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.skins.BarChartItem;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.control.Cell;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.util.number.NumberUtil;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.komet.iconography.Iconography;
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
    private final TableView<AssemblageDashboardRow> assemblageTableView;
    private AssemblageDashboardStats assemblageStats;
    private Future<?> statsFuture;

    public DashboardView(Manifold manifold) {
        this.manifold = manifold;
        this.titleLabel.graphicProperty().set(Iconography.DASHBOARD.getIconographic());

        ObservableList<AssemblageDashboardRow> assemblageTableData = FXCollections.observableArrayList();

        for (int assemblageNid : Get.assemblageService().getAssemblageConceptNids()) {
            assemblageTableData.add(new AssemblageDashboardRow(assemblageNid, manifold));
        }

        assemblageTableData.sort((o1, o2) -> {
            return o1.getAssemblageName()
                    .compareTo(o2.getAssemblageName());
        });

        assemblageTableView = new TableView<>(assemblageTableData);

        TableColumn<AssemblageDashboardRow, String> nameColumn = new TableColumn<>("Assemblage");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("assemblageName"));

        TableColumn<AssemblageDashboardRow, Integer> countColumn = new TableColumn<>("Count");
        countColumn.setCellValueFactory(new PropertyValueFactory<>("semanticCount"));

        countColumn.setCellFactory((param) -> {
            return new TableCell<AssemblageDashboardRow, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        setText(NumberUtil.formatWithGrouping(item));
                        setCellAlignment(this);
                    }
                }

            };
        });

        TableColumn<AssemblageDashboardRow, Integer> memoryUsedColumn = new TableColumn<>("Memory Use");
        memoryUsedColumn.setCellValueFactory(new PropertyValueFactory<>("assemblageMemoryUsage"));

        memoryUsedColumn.setCellFactory((param) -> {
            return new TableCell<AssemblageDashboardRow, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        setText(NumberUtil.formatWithGrouping(item));
                        setCellAlignment(this);
                    }
                }
            };
        });

        TableColumn<AssemblageDashboardRow, Integer> diskSpaceUsedColumn = new TableColumn<>("Disk Space");
        diskSpaceUsedColumn.setCellValueFactory(new PropertyValueFactory<>("assemblageDiskSpaceUsage"));
        diskSpaceUsedColumn.setCellFactory((param) -> {
            return new TableCell<AssemblageDashboardRow, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        setText(NumberUtil.formatWithGrouping(item));
                        setCellAlignment(this);
                    }
                }
            };
        });

        assemblageTableView.getColumns().setAll(nameColumn, countColumn, memoryUsedColumn, diskSpaceUsedColumn);

        this.setCenter(assemblageTableView);
        assemblageTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        assemblageTableView.getSelectionModel().selectedItemProperty().addListener(this::selectionListener);
        updateSystemTiles();
    }


    @Override
    public Node getMenuIcon() {
        return Iconography.DASHBOARD.getIconographic();
    }

    private void setCellAlignment(Cell<Integer> cell) {
        cell.setContentDisplay(ContentDisplay.TEXT_ONLY);
        cell.setTextAlignment(TextAlignment.RIGHT);
        cell.setAlignment(Pos.BOTTOM_RIGHT);
    }

    private void updateSystemTiles() {
        List<Node> tiles = new ArrayList<>();

        StackedMemoryChartData.start(5);

        NumberAxis timeAxis = new NumberAxis();
        timeAxis.setLabel("Time");
        timeAxis.setAnimated(true);
        timeAxis.setAutoRanging(true);

        NumberAxis memoryAxis = new NumberAxis();
        memoryAxis.setLabel("Memory Used (MB)");
        memoryAxis.setAnimated(true);
        memoryAxis.setAutoRanging(true);

        StackedAreaChart<Number, Number> memoryChart
                = new StackedAreaChart<Number, Number>(timeAxis, memoryAxis, StackedMemoryChartData.getMemoryChartData());

        memoryChart.prefWidthProperty().bind(this.widthProperty().subtract(20));
        tiles.add(memoryChart);

        //setupMemoryDonutChart(tiles);
        TilePane pane = new TilePane();
        pane.setHgap(5);
        pane.setVgap(5);
        pane.setAlignment(Pos.TOP_LEFT);
        pane.setCenterShape(true);
        pane.setPadding(new Insets(5));
        //pane.setPrefSize(800, 600);
        pane.setBackground(new Background(new BackgroundFill(Color.web("#c3cdd3"), CornerRadii.EMPTY, Insets.EMPTY)));
        for (Node tile : tiles) {
            pane.getChildren().add(tile);
        }
        this.setTop(pane);
    }

    private void setupMemoryDonutChart(List<Tile> tiles) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memoryUsage = memoryBean.getHeapMemoryUsage();
        long usedMemory = memoryUsage.getUsed();
        long maxMemory = memoryUsage.getMax();
        long oneMillion = 1000000;

        double assemblageMemoryUsed = 0;
        for (int assemblageNid : Get.assemblageService().getAssemblageConceptNids()) {
            assemblageMemoryUsed = assemblageMemoryUsed + Get.assemblageService().getAssemblageMemoryInUse(assemblageNid);
        }

        // convert to megabytes of memory use. 
        ChartData assemblageMemoryUsedData = new ChartData("Assemblage", assemblageMemoryUsed / oneMillion, Tile.RED);
        ChartData unusedMemory = new ChartData("Unused", (maxMemory - usedMemory) / oneMillion, Tile.GREEN);
        ChartData otherMemory = new ChartData("Other", (usedMemory - assemblageMemoryUsed) / oneMillion, Tile.BLUE);

        setupTile(TileBuilder.create()
                .skinType(Tile.SkinType.DONUT_CHART)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .title("Memory Use")
                .chartData(assemblageMemoryUsedData, otherMemory, unusedMemory)
                .build(), tiles);
    }

    private void selectionListener(Observable observable, AssemblageDashboardRow oldValue, AssemblageDashboardRow newAssemblageValue) {
        assemblageStats = null;
        updateSystemTiles();
        TilePane pane = new TilePane();
        pane.setBackground(new Background(new BackgroundFill(Color.web("#c3cdd3"), CornerRadii.EMPTY, Insets.EMPTY)));
        this.setBottom(pane);
        assemblageStats = new AssemblageDashboardStats(newAssemblageValue.getAssemblageNid());
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
                        List<Tile> tiles = new ArrayList<>();

//                        String semanticCountString = NumberUtil.formatWithGrouping(localStats.getSemanticCount().get());
//                        setupTile(TileBuilder.create()
//                                .skinType(Tile.SkinType.TEXT)
//                                .title("Semantics in Assemblage")
//                                .description(semanticCountString + "\nelements")
//                                .textVisible(true)
//                                .build(), tiles);
                        int versionCount = localStats.getVersionCount().get();
//                        String versionCountString = NumberUtil.formatWithGrouping(versionCount);
//                        setupTile(TileBuilder.create()
//                                .skinType(Tile.SkinType.TEXT)
//                                .title("Versions in Assemblage")
//                                .description(versionCountString + "\nversions")
//                                .textVisible(true)
//                                .build(), tiles);

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
                        this.setBottom(pane);
                    }

                });
            }
        }
    }

    private void setupTimeBarChart(AssemblageDashboardStats localStats, int versionCount, List<Tile> tiles) {
        BarChartItem[] barsForTimes = new BarChartItem[localStats.getCommitTimes().size()];
        int index = 0;
        int colorIndex = 0;
        for (Map.Entry<Long, AtomicInteger> entry : localStats.getCommitTimes().entrySet()) {
            String commitTimeString = DateTimeUtil.format(entry.getKey());
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
