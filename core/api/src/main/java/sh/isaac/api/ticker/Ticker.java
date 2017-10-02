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
package sh.isaac.api.ticker;

//~--- JDK imports ------------------------------------------------------------
import java.time.Duration;

import java.util.function.Consumer;
import javafx.application.Platform;

//~--- non-JDK imports --------------------------------------------------------
import org.reactfx.EventStreams;
import org.reactfx.Subscription;

//~--- classes ----------------------------------------------------------------
/**
 * Created by kec on 4/9/15.
 */
public class Ticker {

   /**
    * The tick subscription.
    */
   private Subscription tickSubscription;

   //~--- methods -------------------------------------------------------------
   /**
    * Start.
    *
    * @param intervalInSeconds the interval in seconds
    * @param consumer the consumer
    */
   public void start(int intervalInSeconds, Consumer consumer) {
      if (Platform.isFxApplicationThread()) {
         stop();
         this.tickSubscription = EventStreams.ticks(Duration.ofSeconds(intervalInSeconds))
                 .subscribe(tick -> {
                    consumer.accept(tick);
                 });
      } else {
         Platform.runLater(() -> start(intervalInSeconds, consumer));
      }
   }

   /**
    * Stop.
    */
   public void stop() {
      if (Platform.isFxApplicationThread()) {
         if (this.tickSubscription != null) {
            this.tickSubscription.unsubscribe();
            this.tickSubscription = null;
         }
      } else {
         Platform.runLater(() -> stop());
      }
   }
}
