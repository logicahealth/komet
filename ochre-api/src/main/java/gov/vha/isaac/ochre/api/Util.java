/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.progress.ActiveTasks;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
public class Util {
    public static Path[] stringArrayToPathArray(String... strings) {
        Path[] paths = new Path[strings.length];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = Paths.get(strings[i]);
        }
        return paths;
    }
    
    public static <T> T addToTaskSetAndWaitTillDone(Task<T> task) throws InterruptedException, ExecutionException {
        LookupService.get().getService(ActiveTasks.class).get().add(task);
        try {
            T returnValue = task.get();
            return returnValue;
        } finally {
            LookupService.get().getService(ActiveTasks.class).get().remove(task);
        }
    }
}
