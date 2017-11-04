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



package sh.isaac.api.alert;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.concurrent.Future;

//~--- non-JDK imports --------------------------------------------------------

import com.lmax.disruptor.EventFactory;

import sh.isaac.api.Get;

//~--- classes ----------------------------------------------------------------

/**
 * The AlertEvent class.
 *
 * @author kec
 */
public class AlertEvent {
   public static final EventFactory<AlertEvent> factory = () -> new AlertEvent();

   //~--- fields --------------------------------------------------------------

   private AlertAction alertAction;
   private AlertObject alertObject;

   //~--- methods -------------------------------------------------------------

   @Override
   public String toString() {
      return "AlertEvent{alertObject=" + alertObject + "}";
   }

   Optional<Future<Boolean>> testResolution() {
      if (alertObject != null) {
         if (alertObject.getResolutionTester()
                        .isPresent()) {
            return Optional.of(Get.executor()
                                  .submit(alertObject.getResolutionTester()
                                        .get()));
         }
      }

      return Optional.empty();
   }

   //~--- get methods ---------------------------------------------------------

   //~--- get methods ---------------------------------------------------------

   public AlertObject getAlertObject() {
      return alertObject;
   }

   public AlertAction getAlertAction() {
      return alertAction;
   }

   //~--- set methods ---------------------------------------------------------

   public void setAlertObject(AlertObject alertObject, AlertAction alertAction) {
      this.alertObject = alertObject;
      this.alertAction = alertAction;
   }
}

