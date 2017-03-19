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



package sh.isaac.model.observable;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.observable.ObservableChronologyService;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableChronologyProvider.
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class ObservableChronologyProvider
         implements ObservableChronologyService, ChronologyChangeListener {
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The listener uuid. */
   private final UUID listenerUuid = UUID.randomUUID();

   /** The observable concept map. */
   ConcurrentReferenceHashMap<Integer, ObservableConceptChronology<?>> observableConceptMap =
      new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                                       ConcurrentReferenceHashMap.ReferenceType.WEAK);

   /** The observable sememe map. */
   ConcurrentReferenceHashMap<Integer, ObservableSememeChronology<?>> observableSememeMap =
      new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                                       ConcurrentReferenceHashMap.ReferenceType.WEAK);

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable chronology provider.
    */
   private ObservableChronologyProvider() {
      // for HK2
      log.info("ObservableChronologyProvider constructed");
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Handle change.
    *
    * @param cc the cc
    */
   @Override
   public void handleChange(ConceptChronology<? extends StampedVersion> cc) {
      final ObservableConceptChronology<?> occ = this.observableConceptMap.get(cc.getNid());

      if (occ != null) {
         occ.handleChange(cc);
      }
   }

   /**
    * Handle change.
    *
    * @param sc the sc
    */
   @Override
   public void handleChange(SememeChronology<? extends SememeVersion<?>> sc) {
      final ObservableSememeChronology<?> osc = this.observableSememeMap.get(sc.getNid());

      if (osc != null) {
         osc.handleChange(sc);
      }

      final ObservableConceptChronology<?> assemblageOcc = this.observableConceptMap.get(sc.getAssemblageSequence());

      if (assemblageOcc != null) {
         assemblageOcc.handleChange(sc);
      }

      // handle referenced component
      // Concept, description, or sememe
      final ObjectChronologyType oct = Get.identifierService()
                                          .getChronologyTypeForNid(sc.getReferencedComponentNid());
      ChronologyChangeListener   referencedComponent = null;

      switch (oct) {
      case CONCEPT:
         referencedComponent = this.observableConceptMap.get(sc.getReferencedComponentNid());
         break;

      case SEMEME:
         referencedComponent = this.observableSememeMap.get(sc.getReferencedComponentNid());
         break;

      default:
         throw new UnsupportedOperationException("Can't handle: " + oct);
      }

      if (referencedComponent != null) {
         referencedComponent.handleChange(sc);
      }
   }

   /**
    * Handle commit.
    *
    * @param commitRecord the commit record
    */
   @Override
   public void handleCommit(CommitRecord commitRecord) {
      // TODO implement handle commit...
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      log.info("Starting ObservableChronologyProvider post-construct");
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      log.info("Stopping ObservableChronologyProvider");
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the listener uuid.
    *
    * @return the listener uuid
    */
   @Override
   public UUID getListenerUuid() {
      return this.listenerUuid;
   }

   /**
    * Gets the observable concept chronology.
    *
    * @param id the id
    * @return the observable concept chronology
    */
   @Override
   public ObservableConceptChronology<?> getObservableConceptChronology(int id) {
      if (id >= 0) {
         id = Get.identifierService()
                 .getConceptNid(id);
      }

      final ObservableConceptChronology<?> occ = this.observableConceptMap.get(id);

      if (occ != null) {
         return occ;
      }

      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the observable sememe chronology.
    *
    * @param id the id
    * @return the observable sememe chronology
    */
   @Override
   public ObservableSememeChronology<?> getObservableSememeChronology(int id) {
      if (id >= 0) {
         id = Get.identifierService()
                 .getConceptNid(id);
      }

      final ObservableSememeChronology<?> osc = this.observableSememeMap.get(id);

      if (osc != null) {
         return osc;
      }

      throw new UnsupportedOperationException("Not supported yet.");
   }
}

