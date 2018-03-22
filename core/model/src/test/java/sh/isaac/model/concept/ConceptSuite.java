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

import static sh.isaac.api.bootstrap.TermAux.SOLOR_CONCEPT_ASSEMBLAGE;
import java.io.File;

//~--- JDK imports ------------------------------------------------------------

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
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.memory.HeapUseTicker;
import sh.isaac.api.progress.ActiveTasksTicker;
import sh.isaac.model.builder.ConceptBuilderImpl;
import sh.isaac.model.coordinate.LogicCoordinateImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;

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
      final int                   authorNid         = idService.getNidForUuids(authorUuid);
      final UUID                  moduleUuid        = UUID.fromString("c428399c-3888-4b88-8758-e8618b4562d3");
      final int                   moduleNid         = idService.getNidForUuids(moduleUuid);
      final UUID                  pathUuid          = UUID.fromString("1d067cb2-d0b7-4715-aefb-9e077090779e");
      final int                   pathNid           = idService.getNidForUuids(pathUuid);
      final int                   nid               = Get.identifierService()
                                                         .getNidForUuids(primordialUuid);
      final int conceptAssemblageNid = SOLOR_CONCEPT_ASSEMBLAGE.getNid();
      final ConceptChronologyImpl conceptChronology = new ConceptChronologyImpl(primordialUuid, conceptAssemblageNid);
      final int stampSequence = Get.stampService()
                                   .getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, pathNid);

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
      return createConcept(spec.getFullyQualifiedName(), spec.getUuids()[0]
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
      final int                  statedAssemblageNid = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid();
      final int inferredAssemblageNid = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid();
      final int descriptionLogicProfileNid = TermAux.EL_PLUS_PLUS_LOGIC_PROFILE.getNid();
      final int                  classifierNid = TermAux.SNOROCKET_CLASSIFIER.getNid();
      final int                  conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();
      final LogicCoordinate defaultLogicCoordinate = new LogicCoordinateImpl(statedAssemblageNid,
                                                                             inferredAssemblageNid,
                                                                             descriptionLogicProfileNid,
                                                                             classifierNid, 
                                                                             conceptAssemblageNid);
      final ConceptBuilderImpl testConceptBuilder = new ConceptBuilderImpl(conceptName,
                                                                                     semanticTag,
                                                                                     null,
                                                                                     defaultLanguageForDescriptions,
                                                                                     defaultDialectAssemblageForDescriptions,
                                                                                     defaultLogicCoordinate,
                                                                                     TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());

      testConceptBuilder.setPrimordialUuid(uuidString);

      final int authorNid = TermAux.USER.getNid();
      final int moduleNid = TermAux.SOLOR_MODULE.getNid();
      final int pathNid   = TermAux.DEVELOPMENT_PATH.getNid();
      final int stampSequence = Get.stampService()
                                   .getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, pathNid);
      final List<Chronology> builtObjects = new ArrayList<>();
      final ConceptChronology concept = testConceptBuilder.build(stampSequence, builtObjects);

      for (final Object obj: builtObjects) {
         if (obj instanceof ConceptChronologyImpl) {
            Get.conceptService()
               .writeConcept((ConceptChronology) obj);
         } else if (obj instanceof SemanticChronologyImpl) {
            Get.assemblageService()
               .writeSemanticChronology((SemanticChronology) obj);
         } else {
            throw new UnsupportedOperationException("ag Can't handle: " + obj);
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
      createConcept(TermAux.SOLOR_MODULE, time);
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
      LookupService.startupPreferenceProvider();
      Get.configurationService().setDataStoreFolderPath(new File("target/testdb").toPath());

      LOG.info("termstore folder path exists: " + Get.configurationService().getDataStoreFolderPath().toFile().exists());
      LookupService.startupIsaac();
      ActiveTasksTicker.start(10);
      HeapUseTicker.start(10);
      setupConcepts();
   }
}

