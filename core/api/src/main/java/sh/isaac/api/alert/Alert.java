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


import java.util.concurrent.ConcurrentSkipListSet;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import sh.isaac.api.Get;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Alert {
   private static final Logger LOG = LogManager.getLogger();
   private static final ConcurrentSkipListSet<WeakEventHandler<AlertEvent>> resolverFactories =
      new ConcurrentSkipListSet<>();
   private static final ConcurrentSkipListSet<WeakEventHandler<AlertEvent>> alertListeners =
      new ConcurrentSkipListSet<>();
   private static final RingBuffer<AlertEvent> ringBuffer;

   //~--- static initializers -------------------------------------------------

   static {
      Disruptor<AlertEvent> disruptor = Get.alertDisruptor();

      disruptor.handleEventsWith(
          (event, sequence, endOfBatch) -> {
             System.out.println("adding resolvers for: " + sequence + " " + event);
             resolverFactories.forEach(
                 (handler) -> {
                    try {

                       if (handler.wasGarbageCollected()) {
                          resolverFactories.remove(handler);
                       } else {
                          handler.onEvent(event, sequence, endOfBatch);
                       }
                    } catch (Exception ex) {
                       resolverFactories.remove(handler);
                       LOG.error(ex);
                    }
                 });
          })
               .then(
                   (event, sequence, endOfBatch) -> {
                      System.out.println("alerting listeners for: " + sequence + " " + event);
                      alertListeners.forEach(
                          (handler) -> {
                             try {
                                if (handler.wasGarbageCollected()) {
                                   alertListeners.remove(handler);
                                } else {
                                   handler.onEvent(event, sequence, endOfBatch);
                                }
                             } catch (Exception ex) {
                                alertListeners.remove(handler);
                                LOG.error(ex);
                             }
                          });
                   });
      ringBuffer = disruptor.start();
      
      Get.services(ResolverService.class)
              .forEach((resolverService) -> addResolverFactory(resolverService));
      
   }

   //~--- methods -------------------------------------------------------------

   public static void addResolverFactory(EventHandler<AlertEvent> resolverFactory) {
      resolverFactories.add(new WeakEventHandler<>(resolverFactory));
   }
   public static void addAlertListener(EventHandler<AlertEvent> alertListener) {
      alertListeners.add(new WeakEventHandler<>(alertListener));
   }

   public static void publishAddition(AlertObject alertObject) {
      Get.alertDisruptor()
         .publishEvent(Alert::translateForAddition, alertObject);
   }

   public static void publishRetraction(AlertObject alertObject) {
      Get.alertDisruptor()
         .publishEvent(Alert::translateForRetract, alertObject);
   }

   public static void translateForAddition(AlertEvent event, long sequence, AlertObject alertObject) {
      event.setAlertObject(alertObject, AlertAction.ADD);
   }
   
   public static void translateForRetract(AlertEvent event, long sequence, AlertObject alertObject) {
      event.setAlertObject(alertObject, AlertAction.RETRACT);
   }
}

