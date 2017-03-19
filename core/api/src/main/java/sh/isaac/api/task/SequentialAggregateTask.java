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



package sh.isaac.api.task;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import sh.isaac.api.Get;

//~--- classes ----------------------------------------------------------------

/**
 * The Class SequentialAggregateTask.
 *
 * @author kec
 * @param <T> the generic type
 */
public class SequentialAggregateTask<T>
        extends TimedTask<T> {
   /** The current task. */
   int currentTask = 0;

   /** The sub tasks. */
   Task<?>[] subTasks;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new sequential aggregate task.
    *
    * @param title the title
    * @param subTasks the sub tasks
    */
   public SequentialAggregateTask(String title, Collection<Task<?>> subTasks) {
      this(title, subTasks.toArray(new Task<?>[subTasks.size()]));
   }

   /**
    * Instantiates a new sequential aggregate task.
    *
    * @param title Title for the aggregate task
    * @param subTasks the sub tasks
    */
   public SequentialAggregateTask(String title, Task<?>[] subTasks) {
      this.subTasks = subTasks;
      this.updateTitle(title);
      this.setProgressMessageGenerator((task) -> {
                                          final int taskId = this.currentTask;

                                          if (taskId < subTasks.length) {
                                             updateMessage("Executing subtask: " + subTasks[taskId].getTitle() + " [" +
                                             Integer.toString(this.currentTask + 1) + " of " + subTasks.length +
                                             " tasks]");
                                             updateProgress((this.currentTask * 100) + Math.max(0,
                                                   subTasks[taskId].getProgress() * 100),
                                                   subTasks.length * 100);
                                          }
                                       });
      this.setCompleteMessageGenerator((task) -> {
                                          updateMessage(getState() + " in " + getFormattedDuration());
                                          updateProgress(subTasks.length * 100, subTasks.length * 100);
                                       });
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Sequentially execute the subTasks using the WorkExecutor service, and
    * return the value of the last task in the sequence.
    *
    * @return T value returned by call() method of the last task
    * @throws Exception exception thrown by any subtask
    */
   @Override
   protected T call()
            throws Exception {
      setStartTime();

      try {
         Object returnValue = null;

         for (; this.currentTask < this.subTasks.length; this.currentTask++) {
            Get.workExecutors()
               .getExecutor()
               .execute(this.subTasks[this.currentTask]);
            returnValue = this.subTasks[this.currentTask].get();
         }

         return (T) returnValue;
      } finally {
         Get.activeTasks()
            .remove(this);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sub tasks.
    *
    * @return the sub tasks of this aggregate task.
    */
   public Task<?>[] getSubTasks() {
      return this.subTasks;
   }
}

