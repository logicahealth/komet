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



package sh.isaac.model.concept;

//~--- JDK imports ------------------------------------------------------------

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jvnet.testing.hk2testng.HK2;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.memory.HeapUseTicker;
import sh.isaac.api.progress.ActiveTasksTicker;
import sh.isaac.model.builder.ConceptBuilderOchreImpl;
import sh.isaac.model.coordinate.LogicCoordinateImpl;
import sh.isaac.model.sememe.SememeChronologyImpl;

import static sh.isaac.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/2/16.
 */
@HK2("model")
public class ConceptSuite {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   //~--- methods -------------------------------------------------------------

   /**
    * Tear down suite.
    *
    * @throws Exception the exception
    */
   @AfterGroups(groups = { "services" })
   public void tearDownSuite()
            throws Exception {
      LOG.info("ModelSuiteManagement tear down");
      LookupService.shutdownSystem();
      ActiveTasksTicker.stop();
      HeapUseTicker.stop();
   }

   /**
    * Test from builder.
    */
   @Test(groups = { "services" })
   public void testFromBuilder() {
      final String                conceptName  = "Test concept";
      final String                uuidString   = "bd4d197d-0d88-4543-83dc-09deb2321ee7";
      final long                  time         = System.currentTimeMillis();
      final ConceptChronologyImpl testConcept  = (ConceptChronologyImpl) createConcept(conceptName, uuidString, time);
      final byte[]                data         = testConcept.getDataToWrite();
      final ByteArrayDataBuffer   buffer       = new ByteArrayDataBuffer(data);
      final ConceptChronologyImpl testConcept2 = ConceptChronologyImpl.make(buffer);

      Assert.assertEquals(testConcept, testConcept2);

      final ByteArrayDataBuffer externalBuffer = new ByteArrayDataBuffer();

      externalBuffer.setExternalData(true);
      testConcept2.putExternal(externalBuffer);
      externalBuffer.clear();

      final ConceptChronologyImpl testConcept3 = ConceptChronologyImpl.make(externalBuffer);

      Assert.assertEquals(testConcept, testConcept3);
      testConcept3.toString();
   }

   /**
    * Test serialization no versions.
    *
    * @throws Exception the exception
    */
   @Test(groups = { "services" })
   public void testSerializationNoVersions()
            throws Exception {
      final IdentifierService     idService         = Get.identifierService();
      final UUID                  primordialUuid    = UUID.fromString("2b2b14cd-ea97-4bbc-a3e7-6f7f00e6eff1");
      final long                  time              = System.currentTimeMillis();
      final UUID                  authorUuid        = UUID.fromString("e6cb85c8-852a-4990-ae16-f8f3c83340b4");
      final int                   authorSequence    =
         idService.getConceptSequence(idService.getNidForUuids(authorUuid));
      final UUID                  moduleUuid        = UUID.fromString("c428399c-3888-4b88-8758-e8618b4562d3");
      final int                   moduleSequence    =
         idService.getConceptSequence(idService.getNidForUuids(moduleUuid));
      final UUID                  pathUuid          = UUID.fromString("1d067cb2-d0b7-4715-aefb-9e077090779e");
      final int                   pathSequence      = idService.getConceptSequence(idService.getNidForUuids(pathUuid));
      final int                   nid               = Get.identifierService()
                                                         .getNidForUuids(primordialUuid);
      final int                   containerSequence = Get.identifierService()
                                                         .getConceptSequence(nid);
      final ConceptChronologyImpl conceptChronology = new ConceptChronologyImpl(primordialUuid, nid, containerSequence);
      final int stampSequence = Get.stampService()
                                   .getStampSequence(State.ACTIVE, time, authorSequence, moduleSequence, pathSequence);

      conceptChronology.createMutableVersion(stampSequence);

      final byte[]                data               = conceptChronology.getDataToWrite();
      final ByteArrayDataBuffer   buffer             = new ByteArrayDataBuffer(data);
      final ConceptChronologyImpl conceptChronology2 = ConceptChronologyImpl.make(buffer);

      Assert.assertEquals(conceptChronology, conceptChronology2);

      final ByteArrayDataBuffer externalBuffer = new ByteArrayDataBuffer();

      externalBuffer.setExternalData(true);
      conceptChronology2.putExternal(externalBuffer);
      externalBuffer.clear();

      final ConceptChronologyImpl conceptChronology3 = ConceptChronologyImpl.make(externalBuffer);

      Assert.assertEquals(conceptChronology, conceptChronology3);
   }

   /**
    * Creates the concept.
    *
    * @param spec the spec
    * @param time the time
    * @return the concept chronology
    */
   private ConceptChronology createConcept(ConceptSpecification spec, long time) {
      return createConcept(spec.getConceptDescriptionText(), spec.getUuids()[0]
            .toString(), time);
   }

   /**
    * Creates the concept.
    *
    * @param conceptName the concept name
    * @param uuidString the uuid string
    * @param time the time
    * @return the concept chronology
    */
   private ConceptChronology createConcept(String conceptName, String uuidString, long time) {
      final String               semanticTag                             = "unit test";
      final ConceptSpecification defaultLanguageForDescriptions          = TermAux.ENGLISH_LANGUAGE;
      final ConceptSpecification defaultDialectAssemblageForDescriptions = TermAux.US_DIALECT_ASSEMBLAGE;
      final int                  statedAssemblageSequence = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getConceptSequence();
      final int inferredAssemblageSequence = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getConceptSequence();
      final int descriptionLogicProfileSequence = TermAux.EL_PLUS_PLUS_LOGIC_PROFILE.getConceptSequence();
      final int                  classifierSequence = TermAux.SNOROCKET_CLASSIFIER.getConceptSequence();
      final LogicCoordinate defaultLogicCoordinate = new LogicCoordinateImpl(statedAssemblageSequence,
                                                                             inferredAssemblageSequence,
                                                                             descriptionLogicProfileSequence,
                                                                             classifierSequence);
      final ConceptBuilderOchreImpl testConceptBuilder = new ConceptBuilderOchreImpl(conceptName,
                                                                                     semanticTag,
                                                                                     null,
                                                                                     defaultLanguageForDescriptions,
                                                                                     defaultDialectAssemblageForDescriptions,
                                                                                     defaultLogicCoordinate);

      testConceptBuilder.setPrimordialUuid(uuidString);

      final int authorSequence = TermAux.USER.getConceptSequence();
      final int moduleSequence = TermAux.ISAAC_MODULE.getConceptSequence();
      final int pathSequence   = TermAux.DEVELOPMENT_PATH.getConceptSequence();
      final int stampSequence = Get.stampService()
                                   .getStampSequence(State.ACTIVE, time, authorSequence, moduleSequence, pathSequence);
      final List<ObjectChronology<? extends StampedVersion>> builtObjects = new ArrayList<>();
      final ConceptChronology concept = testConceptBuilder.build(stampSequence, builtObjects);

      for (final Object obj: builtObjects) {
         if (obj instanceof ConceptChronologyImpl) {
            Get.conceptService()
               .writeConcept((ConceptChronology<? extends ConceptVersion<?>>) obj);
         } else if (obj instanceof SememeChronologyImpl) {
            Get.sememeService()
               .writeSememe((SememeChronology<?>) obj);
         } else {
            throw new UnsupportedOperationException("Can't handle: " + obj);
         }
      }

      return concept;
   }

   /**
    * Setup concepts.
    */
   private void setupConcepts() {
      final long time = System.currentTimeMillis();

      createConcept(TermAux.ENGLISH_LANGUAGE, time);
      createConcept(TermAux.US_DIALECT_ASSEMBLAGE, time);
      createConcept(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE, time);
      createConcept(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE, time);
      createConcept(TermAux.EL_PLUS_PLUS_LOGIC_PROFILE, time);
      createConcept(TermAux.SNOROCKET_CLASSIFIER, time);
      createConcept(TermAux.USER, time);
      createConcept(TermAux.ISAAC_MODULE, time);
      createConcept(TermAux.DEVELOPMENT_PATH, time);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set up suite.
    *
    * @throws Exception the exception
    */
   @BeforeGroups(groups = { "services" })
   public void setUpSuite()
            throws Exception {
      LOG.info("ModelSuiteManagement setup");
      System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "target/testdb");

      final java.nio.file.Path dbFolderPath = Paths.get(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY));

      LOG.info("termstore folder path exists: " + dbFolderPath.toFile().exists());
      LookupService.startupIsaac();
      ActiveTasksTicker.start(10);
      HeapUseTicker.start(10);
      setupConcepts();
   }
}

