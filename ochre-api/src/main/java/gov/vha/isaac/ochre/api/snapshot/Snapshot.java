/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.snapshot;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.TaxonomyService;
import gov.vha.isaac.ochre.api.TaxonomySnapshotService;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kec
 */
public class Snapshot {
    
    private static final Logger log = LogManager.getLogger();

    LanguageCoordinate languageCoordinate;
    LogicCoordinate logicCoordinate;
    StampCoordinate stampCoordinate;
    TaxonomyCoordinate taxonomyCoordinate;
    RelativePositionCalculator positionCalculator;

    public Snapshot(LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate, StampCoordinate stampCoordinate, TaxonomyCoordinate taxonomyCoordinate) {
        this.languageCoordinate = languageCoordinate;
        this.logicCoordinate = logicCoordinate;
        this.stampCoordinate = stampCoordinate;
        this.taxonomyCoordinate = taxonomyCoordinate;
        this.positionCalculator = RelativePositionCalculator.getCalculator(stampCoordinate);
    }

    public <V extends SememeVersion> SememeSnapshotService<V> getSememeSnapshotService(Class<V> type) {
        return LookupService.getService(SememeService.class).getSnapshot(type, stampCoordinate);
    }

    public TaxonomySnapshotService getTaxonomySnapshotService() {
        return LookupService.getService(TaxonomyService.class).getSnapshot(taxonomyCoordinate);
    }

    public <V extends StampedVersion> Stream<? extends V> getVisible(ObjectChronology<V> chronicle) {
        return chronicle.getVersionList().stream().filter((V version) -> positionCalculator.onRoute(version));
    }
    
}
