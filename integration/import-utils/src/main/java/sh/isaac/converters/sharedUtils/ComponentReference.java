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



package sh.isaac.converters.sharedUtils;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.identity.StampedVersion;

//~--- classes ----------------------------------------------------------------

public class ComponentReference {
   private IntSupplier      sequenceProvider_;
   private Supplier<UUID>   uuidProvider_;
   private Supplier<Long>   timeProvider_;
   private IntSupplier      nidProvider_;
   private Supplier<String> typeLabelSupplier_;

   //~--- constructors --------------------------------------------------------

   private ComponentReference(Supplier<UUID> uuidProvider, IntSupplier sequenceProvider) {
      uuidProvider_     = uuidProvider;
      timeProvider_     = () -> null;                                     // a lambda that retuns null time.
      sequenceProvider_ = sequenceProvider;
      nidProvider_ = () -> Get.identifierService()
                              .getNidForUuids(this.uuidProvider_.get());  // a lambda that returns a nid
   }

   private ComponentReference(Supplier<UUID> uuidProvider,
                              IntSupplier sequenceProvider,
                              Supplier<String> typeLabelSupplier) {
      this(uuidProvider, sequenceProvider);
      typeLabelSupplier_ = typeLabelSupplier;
   }

   //~--- methods -------------------------------------------------------------

   public static ComponentReference fromChronology(ObjectChronology<?> object) {
      return fromChronology(object, null);
   }

   @SuppressWarnings("rawtypes")
   public static ComponentReference fromChronology(ObjectChronology<?> object, Supplier<String> typeLabelSupplier) {
      ComponentReference cr;

      if (object instanceof SememeChronology) {
         cr = new ComponentReference(() -> object.getPrimordialUuid(),
                                     () -> Get.identifierService()
                                           .getSememeSequence(object.getNid()));
         cr.typeLabelSupplier_ = () -> {
                                    if (((SememeChronology) object).getSememeType() == SememeType.DESCRIPTION) {
                                       return "Description";
                                    } else if (((SememeChronology) object).getSememeType() == SememeType.LOGIC_GRAPH) {
                                       return "Graph";
                                    }

                                    return "";
                                 };
      } else if (object instanceof ConceptChronology) {
         cr = new ComponentReference(() -> object.getPrimordialUuid(),
                                     () -> Get.identifierService()
                                           .getConceptSequence(object.getNid()),
                                     () -> "Concept");
      } else {
         cr = new ComponentReference(() -> object.getPrimordialUuid(),
                                     () -> {
                                        throw new RuntimeException("unsupported");
                                     });
      }

      if (typeLabelSupplier != null) {
         cr.typeLabelSupplier_ = typeLabelSupplier;
      }

      cr.nidProvider_  = () -> object.getNid();
      cr.timeProvider_ = () -> {
                            @SuppressWarnings({ "unchecked" })
                            Optional<LatestVersion<StampedVersion>> latest =
                               ((ObjectChronology) object).getLatestVersion(StampedVersion.class,
                                                                            IBDFCreationUtility.readBackStamp_);

                            return latest.get()
                                         .value()
                                         .getTime();
                         };
      return cr;
   }

   public static ComponentReference fromConcept(ConceptChronology<? extends ConceptVersion<?>> concept) {
      ComponentReference cr = new ComponentReference(() -> concept.getPrimordialUuid(),
                                                     () -> concept.getConceptSequence(),
                                                     () -> "Concept");

      cr.nidProvider_  = () -> concept.getNid();
      cr.timeProvider_ = () -> {
                            @SuppressWarnings({ "rawtypes", "unchecked" })
                            Optional<LatestVersion<StampedVersion>> latest =
                               ((ObjectChronology) concept).getLatestVersion(StampedVersion.class,
                                                                             IBDFCreationUtility.readBackStamp_);

                            return latest.get()
                                         .value()
                                         .getTime();
                         };
      return cr;
   }

   public static ComponentReference fromConcept(UUID uuid) {
      return new ComponentReference(() -> uuid,
                                    () -> Get.identifierService()
                                          .getConceptSequenceForUuids(uuid),
                                    () -> "Concept");
   }

   public static ComponentReference fromConcept(UUID uuid, int seq) {
      return new ComponentReference(() -> uuid, () -> seq, () -> "Concept");
   }

   public static ComponentReference fromSememe(UUID uuid) {
      return new ComponentReference(() -> uuid,
                                    () -> Get.identifierService()
                                          .getSememeSequenceForUuids(uuid),
                                    () -> "Sememe");
   }

   //~--- get methods ---------------------------------------------------------

   public int getNid() {
      return nidProvider_.getAsInt();
   }

   public UUID getPrimordialUuid() {
      return uuidProvider_.get();
   }

   /**
    * Danger Danger
    * Don't use this unless you KNOW the type of component you have a handle to....
    */
   protected int getSequence() {
      return sequenceProvider_.getAsInt();
   }

   public Long getTime() {
      return timeProvider_.get();
   }

   public String getTypeString() {
      return typeLabelSupplier_.get();
   }
}

