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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

/**
 *
 * @author kec
 */
public class AssemblageDashboardStats extends TimedTaskWithProgressTracker<Void> {
    
    private final int assemblageNid;
    private final AtomicInteger semanticCount = new AtomicInteger();
    private final AtomicInteger versionCount = new AtomicInteger();
    private final ConcurrentHashMap<Long, AtomicInteger> commitTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, AtomicInteger> modules = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<VersionType, AtomicInteger> semanticTypes = new ConcurrentHashMap<>();
    
    public AssemblageDashboardStats(int assemblageNid) {
        this.assemblageNid = assemblageNid;
        updateTitle("Computing assemblage statistics");
        Get.activeTasks().add(this);
        
    }
    
    @Override
    protected Void call() throws Exception {
        try {
            int count = Get.assemblageService().getSemanticCount(assemblageNid);
            addToTotalWork(count);
            
            Stream<Chronology> chronologyStream
                    = Get.assemblageService().getChronologyStream(assemblageNid);
            
            chronologyStream.forEach((Chronology chronology) -> {
                semanticCount.incrementAndGet();
                completedUnitOfWork();
                for (Version version : chronology.getVersionList()) {
                    versionCount.incrementAndGet();
                    commitTimes.computeIfAbsent(version.getTime(), (t) -> new AtomicInteger()).incrementAndGet();
                    semanticTypes.computeIfAbsent(version.getSemanticType(), (t) -> new AtomicInteger()).incrementAndGet();
                    modules.computeIfAbsent(version.getModuleNid(), (t) -> new AtomicInteger()).incrementAndGet();
                }
            });
            
            return null;
        } finally {
            Get.activeTasks().remove(this);
        }
    }
    
    public AtomicInteger getSemanticCount() {
        return semanticCount;
    }
    
    public AtomicInteger getVersionCount() {
        return versionCount;
    }
    
    public ConcurrentHashMap<Long, AtomicInteger> getCommitTimes() {
        return commitTimes;
    }
    
    public ConcurrentHashMap<Integer, AtomicInteger> getModules() {
        return modules;
    }
    
    public ConcurrentHashMap<VersionType, AtomicInteger> getSemanticTypes() {
        return semanticTypes;
    }
    
}
