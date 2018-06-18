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
package sh.isaac.model;

import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.StaticIsaacCache;

/**
 * Provides services that are not part of the base API to the model, which makes particular
 * assumptions about implementation. 
 * @author kec
 */
//Even though this class is static, needs to be a service, so that the reset() gets fired at appropriate times.
@Service
@Singleton
public class ModelGet implements StaticIsaacCache {
   private static final Logger LOG = LogManager.getLogger();
   
   static IdentifierService identifierService;
   static TaxonomyDebugService taxonomyDebugService;
   static DataStore dataStore;
   static SequenceStore sequenceStore;
   
   private ModelGet() {
      //For HK2
   }
   
   public static IdentifierService identifierService() {
      if (identifierService == null) {
         identifierService = Get.service(IdentifierService.class);
      }
      return identifierService;
   }
   public static TaxonomyDebugService taxonomyDebugService() {
      if (taxonomyDebugService == null) {
         taxonomyDebugService = Get.service(TaxonomyDebugService.class);
      }
      return taxonomyDebugService;
   }
   
   public static DataStore dataStore() {
      if (dataStore == null) {
         dataStore = Get.service(DataStore.class);
         if (dataStore instanceof SequenceStore) {
            sequenceStore = (SequenceStore)dataStore;
         }
      }
      return dataStore;
   }
   
   /**
    * Note, this may return null, as sequenceStore is only optionally implemented by some (not all) implementations of dataStore. 
    * @return The sequenceStore, if the underlying datastore supports the sequenceStore methods.
    */
   public static SequenceStore sequenceStore() {
     if (dataStore == null) {
        dataStore();  //This populates sequenceStore, if possible
     }
     return sequenceStore;
  }
   
   @Override
   public void reset() {
      LOG.debug("ModelGet Cache clear");
      identifierService = null;
      taxonomyDebugService = null;
      dataStore = null;
      sequenceStore = null;
   }
}
