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
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ComponentReference.
 */
public class ComponentReference {
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
    * @param nidProvider the nid provider
    */
   private ComponentReference(IntSupplier nidProvider) {
      this.uuidProvider     = () -> Get.identifierService().getUuidPrimordialForNid(nidProvider.getAsInt());
      this.timeProvider     = () -> null;                                     // a lambda that retuns null time.
      this.nidProvider = nidProvider;  // a lambda that returns a nid
   }
   
   /**
    * Instantiates a new component reference.
    *
    * @param uuidProvider the uuid provider
    * @param nidProvider the nid provider
    */
   private ComponentReference(Supplier<UUID> uuidProvider, IntSupplier nidProvider) {
      this.uuidProvider     = uuidProvider;
      this.timeProvider     = () -> null;                                     // a lambda that retuns null time.
      this.nidProvider = nidProvider;  // a lambda that returns a nid
   }

   /**
    * Instantiates a new component reference.
    *
    * @param uuidProvider the uuid provider
    * @param nidProvider the nid provider
    * @param typeLabelSupplier the type label supplier
    */
   private ComponentReference(Supplier<UUID> uuidProvider,
                              IntSupplier nidProvider,
                              Supplier<String> typeLabelSupplier) {
      this(uuidProvider, nidProvider);
      this.typeLabelSupplier = typeLabelSupplier;
   }
   
   /**
    * Instantiates a new component reference.
    *
    * @param uuidSupplier the uuid provider
    * @param nidSupplier the nid provider
    * @param typeLabelSupplier the type label supplier
    */
   private ComponentReference(Supplier<UUID> uuidSupplier,
                              IntSupplier nidSupplier,
                              Supplier<String> typeLabelSupplier,
                              Supplier<Long> timeSupplier) {
      this(uuidSupplier, nidSupplier);
      this.typeLabelSupplier = typeLabelSupplier;
      this.timeProvider = timeSupplier;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * From chronology.
    *
    * @param nid the nid
    * @return the component reference
    */
   public static ComponentReference fromChronology(int nid) {
      return new ComponentReference(() -> nid);
   }
   
   /**
    * From chronology.
    *
    * @param object the object
    * @return the component reference
    */
   public static ComponentReference fromChronology(Chronology object) {
      return fromChronology(object, null);
   }

   /**
    * From chronology.
    *
    * @param object the object
    * @param typeLabelSupplier the type label supplier
    * @return the component reference
    */
   public static ComponentReference fromChronology(Chronology object, Supplier<String> typeLabelSupplier) {
      ComponentReference cr;

      if (object instanceof SemanticChronology) {
         cr = new ComponentReference(() -> object.getPrimordialUuid(),
                                     () -> object.getNid());
         cr.typeLabelSupplier = () -> {
                                    if (((SemanticChronology) object).getVersionType() == VersionType.DESCRIPTION) {
                                       return "Description";
                                    } else if (((SemanticChronology) object).getVersionType() == VersionType.LOGIC_GRAPH) {
                                       return "Graph";
                                    }

                                    return "";
                                 };
      } else if (object instanceof ConceptChronology) {
         cr = new ComponentReference(() -> object.getPrimordialUuid(),
                                     () -> object.getNid(),
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
                            final LatestVersion<Version> latest =
                               ((Chronology) object).getLatestVersion(IBDFCreationUtility.readBackStamp);

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
   public static ComponentReference fromConcept(ConceptVersion concept) {
      return new ComponentReference(() -> concept.getChronology().getPrimordialUuid(),
                                                           () -> concept.getNid(),
                                                           () -> "Concept",
                                                           () -> concept.getTime());
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
                                          .getNidForUuids(uuid),
                                    () -> "Concept");
   }

   /**
    * From concept.
    *
    * @param uuid the uuid
    * @param nid the nid
    * @return the component reference
    */
   public static ComponentReference fromConcept(UUID uuid, int nid) {
      return new ComponentReference(() -> uuid, () -> nid, () -> "Concept");
   }

   /**
    * From semantic.
    *
    * @param uuid the uuid
    * @return the component reference
    */
   public static ComponentReference fromSemantic(UUID uuid) {
      return new ComponentReference(() -> uuid,
                                    () -> Get.identifierService()
                                          .getNidForUuids(uuid),
                                    () -> "Semantic");
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

   @Override
   public String toString()
   {
      return "UUID: " + (uuidProvider != null ? uuidProvider.get() : "") + " nid: " + (nidProvider != null ? nidProvider.getAsInt() : "") + " type: " + 
            (typeLabelSupplier != null ? typeLabelSupplier.get() : "");
   }
}

