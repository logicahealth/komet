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
package gov.vha.isaac.ochre.commit.manager;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.commit.ChronologyChangeListener;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.progress.ActiveTasks;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
public class WriteSememeChronicle extends Task<Void>  implements Callable<Void>{
    
    private final SememeChronology sc;
    private final Semaphore writeSemaphore;
    private final ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners;

    public WriteSememeChronicle(SememeChronology sc, Semaphore writeSemaphore,
            ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners) {
        this.sc = sc;
        this.writeSemaphore = writeSemaphore;
        this.changeListeners = changeListeners;
        updateTitle("Write and notify sememe change");
        updateMessage("write: " + sc.getSememeType() + " " + sc.getSememeSequence());
        updateProgress(-1, Long.MAX_VALUE); // Indeterminate progress
        LookupService.getService(ActiveTasks.class).get().add(this);
    }

    @Override
    public Void call() throws Exception {
        try {
            Get.sememeService().writeSememe(sc);
            updateProgress(1, 2); 
            updateMessage("notifying: " + sc.getAssemblageSequence());
             
             changeListeners.forEach((listenerRef) -> {
                ChronologyChangeListener listener = listenerRef.get();
                if (listener == null) {
                    changeListeners.remove(listenerRef);
                } else {
                    listener.handleChange(sc);
                }
             });
            updateProgress(2, 2); 
            updateMessage("complete: " + sc.getSememeType() + " " + sc.getSememeSequence());

            return null;
        } finally {
            writeSemaphore.release();
            LookupService.getService(ActiveTasks.class).get().remove(this);            
        }
    }
}