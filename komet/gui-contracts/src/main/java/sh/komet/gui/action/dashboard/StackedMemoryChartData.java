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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.ApplicationStates;
import sh.isaac.api.Get;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.util.FxTimer;
import sh.isaac.api.util.number.NumberUtil;

/**
 *
 * @author kec
 */
public class StackedMemoryChartData {
    
    protected static final Logger LOG = LogManager.getLogger();
    
    private static final ObservableList<StackedAreaChart.Data<Long, Long>> ASSEMBLAGE_MEMORY_USED_DATA = FXCollections.observableArrayList();
    private static final ObservableList<StackedAreaChart.Data<Long, Long>> IDENTIFIER_MEMORY_USED_DATA = FXCollections.observableArrayList();
    private static final ObservableList<StackedAreaChart.Data<Long, Long>> LUCENE_MEMORY_USED_DATA = FXCollections.observableArrayList();
    private static final ObservableList<StackedAreaChart.Data<Long, Long>> USED_MEMORY_DATA = FXCollections.observableArrayList();
    private static final ObservableList<StackedAreaChart.Data<Long, Long>> AVAILABLE_MEMORY_DATA = FXCollections.observableArrayList();
    private static final ObservableList<StackedAreaChart.Data<Long, Long>> COMMITTED_MEMORY_DATA = FXCollections.observableArrayList();
    public static int MaxDataListSize = 1000;   
    private static FxTimer fxTimer;
    private static final AtomicLong TIME_TICK = new AtomicLong(0);
    private static final long ONE_MILLION = 1000000;
    

    
    private static final StackedAreaChart.Series ASSEMBLAGE_MEMORY_SERIES = 
            new StackedAreaChart.Series("Assemblage memory",ASSEMBLAGE_MEMORY_USED_DATA);
    private static final StackedAreaChart.Series IDENTIFIER_MEMORY_SERIES = 
            new StackedAreaChart.Series("Identifier memory",IDENTIFIER_MEMORY_USED_DATA);
    private static final StackedAreaChart.Series LUCENE_MEMORY_SERIES = 
            new StackedAreaChart.Series("Lucene memory",LUCENE_MEMORY_USED_DATA);
    private static final StackedAreaChart.Series USED_MEMORY_SERIES = 
            new StackedAreaChart.Series("Used memory",USED_MEMORY_DATA);
    private static final StackedAreaChart.Series COMMITTED_MEMORY_SERIES = 
            new StackedAreaChart.Series("Committed memory",COMMITTED_MEMORY_DATA);
    private static final StackedAreaChart.Series AVAILABLE_MEMORY_SERIES = 
            new StackedAreaChart.Series("Available memory",AVAILABLE_MEMORY_DATA);
    
    private static final ObservableList<StackedAreaChart.Series> MEMORY_CHART_DATA = 
            FXCollections.observableArrayList(ASSEMBLAGE_MEMORY_SERIES, IDENTIFIER_MEMORY_SERIES, 
                    LUCENE_MEMORY_SERIES, USED_MEMORY_SERIES, COMMITTED_MEMORY_SERIES, 
                    AVAILABLE_MEMORY_SERIES);
    
    private static final MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();
    
    
    public static void addDataPoint() {
        if (Get.applicationStates().contains(ApplicationStates.STOPPING)) {
            stop();
            return;
        } 
        long timeTick = TIME_TICK.getAndIncrement();
        long assemblageMemoryUsed = 0;
        for (int assemblageNid : Get.assemblageService().getAssemblageConceptNids()) {
            assemblageMemoryUsed = assemblageMemoryUsed + Get.assemblageService().getAssemblageMemoryInUse(assemblageNid);
        }
        long identifierMemoryUsed = Get.identifierService().getMemoryInUse();
        long luceneMemoryUsed = 0;
        for (IndexBuilderService indexService: Get.services(IndexBuilderService.class)) {
            luceneMemoryUsed += indexService.getIndexMemoryInUse();
        }
        MemoryUsage memoryUsage =  MEMORY_BEAN.getHeapMemoryUsage();
        ASSEMBLAGE_MEMORY_USED_DATA.add(new XYChart.Data<>(timeTick, assemblageMemoryUsed/ONE_MILLION));
        IDENTIFIER_MEMORY_USED_DATA.add(new XYChart.Data<>(timeTick, identifierMemoryUsed/ONE_MILLION));
        LUCENE_MEMORY_USED_DATA.add(new XYChart.Data<>(timeTick, luceneMemoryUsed/ONE_MILLION));
        USED_MEMORY_DATA.add(new XYChart.Data<>(timeTick, (memoryUsage.getUsed() - assemblageMemoryUsed - identifierMemoryUsed - luceneMemoryUsed)/ONE_MILLION));
        COMMITTED_MEMORY_DATA.add(new XYChart.Data<>(timeTick, (memoryUsage.getCommitted() - memoryUsage.getUsed())/ONE_MILLION));
        AVAILABLE_MEMORY_DATA.add(new XYChart.Data<>(timeTick, (memoryUsage.getMax() - memoryUsage.getCommitted())/ONE_MILLION));
        
        while (ASSEMBLAGE_MEMORY_USED_DATA.size() > MaxDataListSize) {
            ASSEMBLAGE_MEMORY_USED_DATA.remove(0);
            IDENTIFIER_MEMORY_USED_DATA.remove(0);
            LUCENE_MEMORY_USED_DATA.remove(0);
            USED_MEMORY_DATA.remove(0);
            COMMITTED_MEMORY_DATA.remove(0);
            AVAILABLE_MEMORY_DATA.remove(0);
        }
        
        if (timeTick == 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("\n\nAssemblage memory: ")
                    .append(NumberUtil.formatWithGrouping(assemblageMemoryUsed));
            builder.append("\nIdentifier memory: ")
                    .append(NumberUtil.formatWithGrouping(identifierMemoryUsed));
            builder.append("\nLucene memory: ")
                    .append(NumberUtil.formatWithGrouping(luceneMemoryUsed));
            
            LOG.info(builder.toString());
        }

    }
       
    public static ObservableList<StackedAreaChart.Series> getMemoryChartData() {
        return MEMORY_CHART_DATA;
    }
    
    public static void clearMemoryData() {
       if (Platform.isFxApplicationThread()) {
           MEMORY_CHART_DATA.clear();
       } else {
           Platform.runLater(() -> MEMORY_CHART_DATA.clear());
       }
    }
    
    public static void start(int intervalInSeconds) {
       if (fxTimer != null) {
           fxTimer.stop();
       }
       fxTimer = FxTimer.createPeriodic(Duration.ofSeconds(intervalInSeconds), StackedMemoryChartData::addDataPoint);
       fxTimer.restart();
   }
   /**
    * Stop.
    */
   public static void stop() {
       fxTimer.stop();
   }
}
