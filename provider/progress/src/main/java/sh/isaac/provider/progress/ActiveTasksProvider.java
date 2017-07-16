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



package sh.isaac.provider.progress;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.progress.ActiveTasks;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/2/16.
 */
@Service
@Singleton
public class ActiveTasksProvider
         implements ActiveTasks {
   /** The task set. */
   ObservableSet<Task<?>> taskSet = FXCollections.observableSet(ConcurrentHashMap.newKeySet());

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the task to the active tasks set.
    *
    * @param task the task
    */
   @Override
   public void add(Task<?> task) {
      this.taskSet.add(task);
   }

   /**
    * Removes the task from the active tasks set.
    *
    * @param task the task
    */
   @Override
   public void remove(Task<?> task) {
      this.taskSet.remove(task);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the observable task set.
    *
    * @return the set
    */
   @Override
   public ObservableSet<Task<?>> get() {
      return this.taskSet;
   }
}

