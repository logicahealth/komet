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
import sh.isaac.api.IsaacCache;

/**
 * Provides services that are not part of the base API to the model, which makes particular
 * assumptions about implementation. 
 * @author kec
 */
//Even though this class is static, needs to be a service, so that the reset() gets fired at appropriate times.
@Service
@Singleton
public class ModelGet implements IsaacCache {
   private static final Logger LOG = LogManager.getLogger();
   
   static ContainerSequenceService containerSequenceService;
   static TaxonomyDebugService taxonomyDebugService;
   
   private ModelGet() {
      //For HK2
   }
   
   public static ContainerSequenceService identifierService() {
      if (containerSequenceService == null) {
         containerSequenceService = Get.service(ContainerSequenceService.class);
      }
      return containerSequenceService;
   }
   public static TaxonomyDebugService taxonomyDebugService() {
      if (taxonomyDebugService == null) {
         taxonomyDebugService = Get.service(TaxonomyDebugService.class);
      }
      return taxonomyDebugService;
   }
   
   @Override
   public void reset() {
      LOG.debug("ModelGet Cache clear");
      containerSequenceService = null;
      taxonomyDebugService = null;
   }
}
