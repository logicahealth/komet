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
package sh.isaac.provider.progress;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
public class TaskListProvider {

   /** The task set. */
   ObservableSet<Task<?>> taskSet = FXCollections.observableSet(ConcurrentHashMap.newKeySet());
   Set<Task<?>> unmodifiableSet = FXCollections.unmodifiableObservableSet(this.taskSet);
   //~--- methods -------------------------------------------------------------
   /**
    * Adds the task to the active tasks set.
    *
    * @param task the task
    */
   public final void add(Task<?> task) {
      if (Platform.isFxApplicationThread()) {
         checkTitle(task);
         this.taskSet.add(task);
      } else {
         Platform.runLater(() -> {
            checkTitle(task);
            this.taskSet.add(task);
         });
      }
   }

   private void checkTitle(Task<?> task) {
      if (task.getTitle() == null || task.getTitle().isEmpty()) {
         System.out.println("Task with no title: " + task.getClass().getName());
      }
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets an unmodifiable wrapper of the observable task set.
    *
    * @return the set
    */
   public Set<Task<?>> get() {
      return unmodifiableSet;
   }

   /**
    * Removes the task from the active tasks set.
    *
    * @param task the task
    */
   public final void remove(Task<?> task) {
      Platform.runLater(() -> {
            this.taskSet.remove(task);
      });
   }

   public void addListener(SetChangeListener<? super Task<?>> listener) {
      taskSet.addListener(listener);
   }

   public void removeListener(SetChangeListener<? super Task<?>> listener) {
      taskSet.removeListener(listener);
   }
}
