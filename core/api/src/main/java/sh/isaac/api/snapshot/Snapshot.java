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



package sh.isaac.api.snapshot;

//~--- JDK imports ------------------------------------------------------------

import java.util.stream.Stream;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Snapshot {
   // private static final Logger log = LogManager.getLogger();
   LanguageCoordinate         languageCoordinate;
   LogicCoordinate            logicCoordinate;
   StampCoordinate            stampCoordinate;
   TaxonomyCoordinate         taxonomyCoordinate;
   RelativePositionCalculator positionCalculator;

   //~--- constructors --------------------------------------------------------

   public Snapshot(LanguageCoordinate languageCoordinate,
                   LogicCoordinate logicCoordinate,
                   StampCoordinate stampCoordinate,
                   TaxonomyCoordinate taxonomyCoordinate) {
      this.languageCoordinate = languageCoordinate;
      this.logicCoordinate    = logicCoordinate;
      this.stampCoordinate    = stampCoordinate;
      this.taxonomyCoordinate = taxonomyCoordinate;
      this.positionCalculator = RelativePositionCalculator.getCalculator(stampCoordinate);
   }

   //~--- get methods ---------------------------------------------------------

   public <V extends SememeVersion<?>> SememeSnapshotService<V> getSememeSnapshotService(Class<V> type) {
      return Get.sememeService()
                .getSnapshot(type, stampCoordinate);
   }

   public TaxonomySnapshotService getTaxonomySnapshotService() {
      return Get.taxonomyService()
                .getSnapshot(taxonomyCoordinate);
   }

   public <V extends StampedVersion> Stream<? extends V> getVisible(ObjectChronology<V> chronicle) {
      return chronicle.getVersionList()
                      .stream()
                      .filter(version -> positionCalculator.onRoute(version));
   }
}

