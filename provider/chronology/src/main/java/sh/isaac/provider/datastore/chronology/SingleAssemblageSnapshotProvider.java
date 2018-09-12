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
package sh.isaac.provider.datastore.chronology;

import java.util.List;
import sh.isaac.api.ProgressTracker;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.stream.VersionStream;

/**
 *
 * @author kec
 * @param <V>
 */
public class SingleAssemblageSnapshotProvider <V extends SemanticVersion> implements SingleAssemblageSnapshot<V> {
   SemanticSnapshotService<V> assemblageProvider;
   
   final int assemblageConceptNid;

    public SingleAssemblageSnapshotProvider(int assemblageConceptNid, SemanticSnapshotService<V> assemblageProvider) {
        this.assemblageConceptNid = assemblageConceptNid;
        this.assemblageProvider = assemblageProvider;
    }
   
   

    @Override
    public int getAssemblageNid() {
        return assemblageConceptNid;
    }

    @Override
    public LatestVersion<V> getLatestSemanticVersion(int semanticNid) {
        return assemblageProvider.getLatestSemanticVersion(semanticNid);
    }

    @Override
    public List<LatestVersion<V>> getLatestSemanticVersionsForComponentFromAssemblage(int componentNid) {
        return assemblageProvider.getLatestSemanticVersionsForComponentFromAssemblage(componentNid, assemblageConceptNid);
    }

    @Override
    public VersionStream<V> getLatestSemanticVersionsFromAssemblage(ProgressTracker... progressTrackers) {
        return assemblageProvider.getLatestSemanticVersionsFromAssemblage(assemblageConceptNid, progressTrackers);
    }
    
}
