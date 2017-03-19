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



package sh.isaac.model.configuration;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.snapshot.Snapshot;

//~--- classes ----------------------------------------------------------------

/**
 * The Class Snapshots.
 *
 * @author kec
 */
public class Snapshots {
   
   /**
    * Gets the default solor inferred snapshot.
    *
    * @return the default solor inferred snapshot
    */
   public static Snapshot getDefaultSolorInferredSnapshot() {
      final LanguageCoordinate language        = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
      final StampCoordinate    stampCoordinate = StampCoordinates.getDevelopmentLatest();

      return new Snapshot(language,
                          LogicCoordinates.getStandardElProfile(),
                          stampCoordinate,
                          TaxonomyCoordinates.getInferredTaxonomyCoordinate(stampCoordinate, language));
   }

   /**
    * Gets the default solor stated snapshot.
    *
    * @return the default solor stated snapshot
    */
   public static Snapshot getDefaultSolorStatedSnapshot() {
      final LanguageCoordinate language        = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
      final StampCoordinate    stampCoordinate = StampCoordinates.getDevelopmentLatest();

      return new Snapshot(language,
                          LogicCoordinates.getStandardElProfile(),
                          stampCoordinate,
                          TaxonomyCoordinates.getStatedTaxonomyCoordinate(stampCoordinate, language));
   }
}

