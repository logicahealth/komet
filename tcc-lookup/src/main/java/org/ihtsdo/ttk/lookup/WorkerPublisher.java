/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.ttk.lookup;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.concurrent.Worker;

import static javafx.concurrent.Worker.State.CANCELLED;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.READY;
import static javafx.concurrent.Worker.State.RUNNING;
import static javafx.concurrent.Worker.State.SUCCEEDED;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class WorkerPublisher implements ChangeListener<Worker.State> {

   /** Field description */
   UUID id = UUID.randomUUID();

   /** Field description */
   InstanceWrapper<Worker> lookupWrapper;

   /** Field description */
   String description;

   /**
    * Constructs ...
    *
    *
    * @param worker
    * @param description
    * @param properties
    */
   private WorkerPublisher(Worker worker, String description, List<? extends InstancePropertyBI> properties) {
      this.description = description;
      worker.stateProperty().addListener(this);

      switch (worker.getState()) {
      case CANCELLED :
      case FAILED :
      case SUCCEEDED :
         worker.stateProperty().removeListener(this);
         System.out.println("Remove worker publisher: " + this);

         break;

      case READY :
      case RUNNING :
         lookupWrapper = Looker.add(worker, UUID.randomUUID(), description, properties);
         System.out.println("added worker publisher: " + this);
      }
   }

   /**
    * Method description
    *
    *
    * @param ov
    * @param oldValue
    * @param newValue
    */
   @Override
   public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldValue,
                       Worker.State newValue) {
      switch (newValue) {
      case CANCELLED :
      case FAILED :
      case SUCCEEDED :
         ov.removeListener(this);
         Looker.removePair(lookupWrapper);
         System.out.println("Removed worker publisher[2]: " + this);

         break;
      }
   }

   /**
    * Method description
    *
    *
    * @param worker
    * @param description
    * @param properties
    *
    * @return
    */
   public static WorkerPublisher publish(Worker<?> worker, String description,
       List<? extends InstancePropertyBI> properties) {
      return new WorkerPublisher(worker, description, properties);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public String toString() {
      return "WorkerPublisher{" + "description=" + description + ", id=" + id + ", lookupWrapper="
             + lookupWrapper + '}';
   }
}
