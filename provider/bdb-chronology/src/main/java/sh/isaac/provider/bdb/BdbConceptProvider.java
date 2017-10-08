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



package sh.isaac.provider.bdb;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.nio.file.Path;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import sh.isaac.api.ConceptActiveService;
import sh.isaac.api.LookupService;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class BdbConceptProvider
         implements ConceptService {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private Environment myDbEnvironment;

   //~--- methods -------------------------------------------------------------

   @Override
   public void clearDatabaseValidityValue() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void writeConcept(ConceptChronology concept) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Starting BDB ConceptProvider post-construct");

      try {
         EnvironmentConfig envConfig = new EnvironmentConfig();

         envConfig.setAllowCreate(true);
         myDbEnvironment = new Environment(new File("/export/dbEnv"), envConfig);
      } catch (DatabaseException dbe) {
         throw new RuntimeException(dbe);
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping BDB ConceptProvider.");

      if (myDbEnvironment != null) {
         myDbEnvironment.close();
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ConceptChronology getConcept(ConceptSpecification conceptSpecification) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ConceptChronology getConcept(int conceptId) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ConceptChronology getConcept(UUID... conceptUuids) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean hasConcept(int conceptId) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream(ConceptSequenceSet conceptSequences) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getConceptCount() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getConceptKeyParallelStream() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getConceptKeyStream() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public UUID getDataStoreId() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Path getDatabaseFolder() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(int conceptId) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(UUID... conceptUuids) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getParallelConceptChronologyStream() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getParallelConceptChronologyStream(ConceptSequenceSet conceptSequences) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ConceptSnapshotService getSnapshot(ManifoldCoordinate manifoldCoordinate) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }
}

