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

import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.chronicle.Chronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ComponentReference.
 */
public class ComponentReference {
   /** The sequence provider. */
   private final IntSupplier sequenceProvider;

   /** The uuid provider. */
   private final Supplier<UUID> uuidProvider;

   /** The time provider. */
   private Supplier<Long> timeProvider;

   /** The nid provider. */
   private IntSupplier nidProvider;

   /** The type label supplier. */
   private Supplier<String> typeLabelSupplier;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new component reference.
    *
    * @param uuidProvider the uuid provider
    * @param sequenceProvider the sequence provider
    */
   private ComponentReference(Supplier<UUID> uuidProvider, IntSupplier sequenceProvider) {
      this.uuidProvider     = uuidProvider;
      this.timeProvider     = () -> null;                                     // a lambda that retuns null time.
      this.sequenceProvider = sequenceProvider;
      this.nidProvider = () -> Get.identifierService()
                                   .getNidForUuids(this.uuidProvider.get());  // a lambda that returns a nid
   }

   /**
    * Instantiates a new component reference.
    *
    * @param uuidProvider the uuid provider
    * @param sequenceProvider the sequence provider
    * @param typeLabelSupplier the type label supplier
    */
   private ComponentReference(Supplier<UUID> uuidProvider,
                              IntSupplier sequenceProvider,
                              Supplier<String> typeLabelSupplier) {
      this(uuidProvider, sequenceProvider);
      this.typeLabelSupplier = typeLabelSupplier;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * From chronology.
    *
    * @param object the object
    * @return the component reference
    */
   public static ComponentReference fromChronology(Chronology<?> object) {
      return fromChronology(object, null);
   }

   /**
    * From chronology.
    *
    * @param object the object
    * @param typeLabelSupplier the type label supplier
    * @return the component reference
    */
   @SuppressWarnings("rawtypes")
   public static ComponentReference fromChronology(Chronology<?> object, Supplier<String> typeLabelSupplier) {
      ComponentReference cr;

      if (object instanceof SememeChronology) {
         cr = new ComponentReference(() -> object.getPrimordialUuid(),
                                     () -> Get.identifierService()
                                           .getSememeSequence(object.getNid()));
         cr.typeLabelSupplier = () -> {
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
         cr.typeLabelSupplier = typeLabelSupplier;
      }

      cr.nidProvider  = () -> object.getNid();
      cr.timeProvider = () -> {
                            @SuppressWarnings({ "unchecked" })
                            final LatestVersion<StampedVersion> latest =
                               ((Chronology) object).getLatestVersion(StampedVersion.class,
                                                                            IBDFCreationUtility.readBackStamp);

                            return latest.get()
                                         .getTime();
                         };
      return cr;
   }

   /**
    * From concept.
    *
    * @param concept the concept
    * @return the component reference
    */
   public static ComponentReference fromConcept(ConceptChronology concept) {
      final ComponentReference cr = new ComponentReference(() -> concept.getPrimordialUuid(),
                                                           () -> concept.getConceptSequence(),
                                                           () -> "Concept");

      cr.nidProvider  = () -> concept.getNid();
      cr.timeProvider = () -> {
                            @SuppressWarnings({ "rawtypes", "unchecked" })
                            final LatestVersion<StampedVersion> latest =
                               ((Chronology) concept).getLatestVersion(StampedVersion.class,
                                                                             IBDFCreationUtility.readBackStamp);

                            return latest.get()
                                         .getTime();
                         };
      return cr;
   }

   /**
    * From concept.
    *
    * @param uuid the uuid
    * @return the component reference
    */
   public static ComponentReference fromConcept(UUID uuid) {
      return new ComponentReference(() -> uuid,
                                    () -> Get.identifierService()
                                          .getConceptSequenceForUuids(uuid),
                                    () -> "Concept");
   }

   /**
    * From concept.
    *
    * @param uuid the uuid
    * @param seq the seq
    * @return the component reference
    */
   public static ComponentReference fromConcept(UUID uuid, int seq) {
      return new ComponentReference(() -> uuid, () -> seq, () -> "Concept");
   }

   /**
    * From sememe.
    *
    * @param uuid the uuid
    * @return the component reference
    */
   public static ComponentReference fromSememe(UUID uuid) {
      return new ComponentReference(() -> uuid,
                                    () -> Get.identifierService()
                                          .getSememeSequenceForUuids(uuid),
                                    () -> "Sememe");
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   public int getNid() {
      return this.nidProvider.getAsInt();
   }

   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   public UUID getPrimordialUuid() {
      return this.uuidProvider.get();
   }

   /**
    * Danger Danger
    * Don't use this unless you KNOW the type of component you have a handle to....
    *
    * @return the sequence
    */
   protected int getSequence() {
      return this.sequenceProvider.getAsInt();
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   public Long getTime() {
      return this.timeProvider.get();
   }

   /**
    * Gets the type string.
    *
    * @return the type string
    */
   public String getTypeString() {
      return this.typeLabelSupplier.get();
   }
}

