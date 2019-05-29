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
package sh.isaac.api.progress;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;

import java.util.Set;

/**
 *
 * @author kec
 */
public interface TaskList {
   /**
    * Adds the.
    *
    * @param task the task
    */
   void add(Task<?> task);

   /**
    * Removes the.
    *
    * @param task the task
    */
   void remove(Task<?> task);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the set of tasks.
    *
    * @return an unmodifiable set
    */
   Set<Task<?>> get();

   void addListener(SetChangeListener<? super Task<?>> listener);

   void removeListener(SetChangeListener<? super Task<?>> listener);

   
}
