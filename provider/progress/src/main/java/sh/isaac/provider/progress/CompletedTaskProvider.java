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

import jakarta.inject.Singleton;

import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.progress.CompletedTasks;

import java.util.Deque;
import java.util.LinkedList;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class CompletedTaskProvider  extends TaskListProvider
         implements CompletedTasks {
    private static final int MAX_LIST_SIZE = 100;
    private final Deque<Task<?>> completedTasks = new LinkedList<>();


    public CompletedTaskProvider() {
        Get.activeTasks().addListener(this::onChanged);
    }

    private void onChanged(SetChangeListener.Change<? extends Task<?>> change) {
        if (change.wasRemoved()) {
            Task<?> element = change.getElementRemoved();
            this.add(element);
            this.completedTasks.addFirst(element);
            while (completedTasks.size() > MAX_LIST_SIZE) {
                Task<?> elementToRemove = this.completedTasks.removeLast();
                this.remove(elementToRemove);
            }
        }
    }
}
