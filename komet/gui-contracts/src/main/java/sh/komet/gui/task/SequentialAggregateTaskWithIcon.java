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
package sh.komet.gui.task;

import java.util.Collection;
import javafx.concurrent.Task;
import javafx.scene.Node;
import sh.isaac.api.task.SequentialAggregateTask;
import sh.komet.gui.interfaces.IconProvider;

/**
 *
 * @author kec
 * @param <T> the generic type
 */
public class SequentialAggregateTaskWithIcon<T> extends SequentialAggregateTask<T> implements IconProvider {

   private Node taskIcon;
   public SequentialAggregateTaskWithIcon(String title, Collection<Task<?>> subTasks) {
      super(title, subTasks);
      Task<?> firstTask = subTasks.iterator().next();
      if (firstTask instanceof IconProvider) {
         taskIcon = ((IconProvider)firstTask).getIcon();
      }
   }

   public SequentialAggregateTaskWithIcon(String title, Task<?>[] subTasks) {
      super(title, subTasks);
      if (subTasks[0] instanceof IconProvider) {
         taskIcon = ((IconProvider)subTasks[0]).getIcon();
      }
   }

   @Override
   public Node getIcon() {
      return taskIcon;
   }
   
}
