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
package sh.isaac.provider.resolver;

import jakarta.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.alert.AlertEvent;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.ResolverService;
import sh.isaac.model.tree.TreeCycleError;
import sh.isaac.model.tree.TreeCycleResolver;

/**
 * TODO move to an independent provider module. 
 * @author kec
 */
@Service
@Singleton
public class ResolverProvider implements ResolverService {

   @Override
   public void onEvent(AlertEvent event, long sequence, boolean endOfBatch) throws Exception {
      switch (event.getAlertObject().getAlertCategory()) {
         
         case CLASSIFIER:
            handleClassifierEvent(event.getAlertObject());
            break;
         case TAXONOMY:
            handleTaxonomyEvent(event.getAlertObject());
            break;
         case COMMIT:
            handleCommitEvent(event.getAlertObject());
            break;
      }
   }

   private void handleTaxonomyEvent(AlertObject alertObject) {
      if (alertObject instanceof TreeCycleError) {
         alertObject.getResolvers().add(new TreeCycleResolver((TreeCycleError) alertObject));
      }
   }
   
   private void handleClassifierEvent(AlertObject alertObject) {
      //
   }
   
   private void handleCommitEvent(AlertObject alertObject) {
      //
   }
   
   
   
}
