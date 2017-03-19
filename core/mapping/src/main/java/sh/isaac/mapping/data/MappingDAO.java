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



package sh.isaac.mapping.data;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.MutableDynamicSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MappingDAO}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class MappingDAO {
   protected static final Logger LOG = LoggerFactory.getLogger(MappingDAO.class);

   //~--- methods -------------------------------------------------------------

   protected static DynamicSememe<?> readCurrentRefex(UUID refexUUID,
         StampCoordinate stampCoord)
            throws RuntimeException {
      final SememeChronology<? extends SememeVersion<?>> sc = Get.sememeService()
                                                           .getSememe(Get.identifierService()
                                                                 .getSememeSequenceForUuids(refexUUID));
      @SuppressWarnings({ "unchecked", "rawtypes" })
	final
      Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology) sc).getLatestVersion(DynamicSememe.class,
                                                                                                  stampCoord.makeAnalog(
                                                                                                     State.ACTIVE,
                                                                                                           State.INACTIVE));

      return latest.get()
                   .value();
   }

   //~--- set methods ---------------------------------------------------------

   @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
   protected static void setConceptStatus(UUID conceptUUID,
         State state,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws RuntimeException {
      final ConceptChronology<? extends ConceptVersion<?>> cc = Get.conceptService()
                                                             .getConcept(conceptUUID);
      final Optional<LatestVersion<ConceptVersion>> cv = ((ConceptChronology) cc).getLatestVersion(ConceptVersion.class,
                                                                                             stampCoord.makeAnalog(
                                                                                                State.ACTIVE,
                                                                                                      State.INACTIVE));

      if (cv.isPresent() &&!cv.get().contradictions().isPresent() && (cv.get().value().getState() == state)) {
         LOG.warn("Tried set the status to the value it already has.  Doing nothing");
      } else {
         cc.createMutableVersion(state, editCoord);
         Get.commitService()
            .addUncommitted(cc);
         Get.commitService()
            .commit("Changing map concept state");
      }
   }

   @SuppressWarnings("deprecation")
   protected static void setSememeStatus(UUID refexUUID,
         State state,
         StampCoordinate stampCoord,
         EditCoordinate editCoord)
            throws RuntimeException {
      final DynamicSememe<?> ds = readCurrentRefex(refexUUID, stampCoord);

      if (ds.getState() == state) {
         LOG.warn("Tried set the status to the value it already has.  Doing nothing");
      } else {
         @SuppressWarnings("unchecked")
		final
         MutableDynamicSememe<?> mds =
            ((SememeChronology<DynamicSememe<?>>) ds.getChronology()).createMutableVersion(MutableDynamicSememe.class,
                                                                                           state,
                                                                                           editCoord);

         mds.setData(ds.getData());
         Get.commitService()
            .addUncommitted(ds.getChronology());
         Get.commitService()
            .commit("Changing sememe state");
      }
   }
}

