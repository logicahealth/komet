/*
 * Copyright 2014 Informatics, Inc..
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

package org.ihtsdo.otf.tcc.model.services;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javafx.concurrent.Task;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.ihtsdo.otf.lookup.contracts.contracts.ActiveTaskSet;
/**
 *
 * @author kec
 */
@Singleton
@Service
public class ConcurrentActiveTaskSet implements ActiveTaskSet {
    private final Set<Task> taskSet = new CopyOnWriteArraySet<>();

    @Override
    public Set<Task> get() {
        return taskSet;
    }

    @Override
    public void add(Task task) {
        taskSet.add(task);
    }

    @Override
    public void remove(Task task) {
        taskSet.remove(task);
    }
    
}
