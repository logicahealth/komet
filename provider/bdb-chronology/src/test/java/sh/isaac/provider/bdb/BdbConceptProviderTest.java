/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.provider.bdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;

/**
 *
 * @author kec
 */
public class BdbConceptProviderTest {

   private static final Logger LOG = LogManager.getLogger();

   /**
    * Test of writeConcept method, of class BdbConceptProvider.
    */
   @Test(
      groups = { "db" }
   )
   public void testWriteConcept() {
      try {
         System.out.println("writeConcept");

         File f = new File("target/data/IsaacMetadataAuxiliary.ibdf");
         final BinaryDataReaderQueueService reader = Get.binaryDataQueueReader(f.toPath());
         final BlockingQueue<IsaacExternalizable> queue = reader.getQueue();
         int itemCount = 0;
         while (!queue.isEmpty() || !reader.isFinished()) {
            final IsaacExternalizable object = queue.poll(500, TimeUnit.MILLISECONDS);

            if (object != null) {
               itemCount++;

               if (null != object.getIsaacObjectType()) {
                  switch (object.getIsaacObjectType()) {
                     case CONCEPT:
                        Get.conceptService()
                                .writeConcept(((ConceptChronology) object));
                        break;

                     case SEMANTIC:
                        SemanticChronology sc = (SemanticChronology) object;
                        Get.assemblageService()
                                .writeSemanticChronology(sc);
                        break;

                     case STAMP_ALIAS:
                        Get.commitService()
                                .addAlias(((StampAlias) object).getStampSequence(),
                                        ((StampAlias) object).getStampAlias(),
                                        null);
                        break;

                     case STAMP_COMMENT:
                        Get.commitService()
                                .setComment(((StampComment) object).getStampSequence(),
                                        ((StampComment) object).getComment());
                        break;

                     default:
                        throw new UnsupportedOperationException("Unknown isaac object type: " + object);
                  }
               }
            }
         }
         LOG.info("Imported: " + itemCount + " items");
      } catch (FileNotFoundException | InterruptedException ex) {
         LOG.throwing(ex);
      }
   }
}
