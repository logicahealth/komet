/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.provider.commit;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TestConcept;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

/**
 *
 * @author kec
 */
public class WriteSemanticChronologyWithProgress 
        extends TimedTaskWithProgressTracker<Void> {

    /**
     * The sc.
     */
    private SemanticChronology sc;

    /**
     * The write semaphore.
     */
    private final Semaphore writeSemaphore;

    /**
     * The change listeners.
     */
    private final ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners;

    /**
     * The uncommitted tracking.
     */
    private final BiConsumer<Chronology, Boolean> uncommittedTracking;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new write semantic chronicle.
     *
     * @param sc the sc
     * @param writeSemaphore the write semaphore
     * @param changeListeners the change listeners
     * @param uncommittedTracking A handle to call back to the caller to notify
     * it that the semantic has been written to the AssemblageService. Parameter 1
     * is the Semantic, Parameter two is true to indicate that the change checker
     * is active for this implementation.
     */
    public WriteSemanticChronologyWithProgress(SemanticChronology sc,
            Semaphore writeSemaphore,
            ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners,
            BiConsumer<Chronology, Boolean> uncommittedTracking) {
        this.sc = sc;
        this.writeSemaphore = writeSemaphore;
        this.changeListeners = changeListeners;
        this.uncommittedTracking = uncommittedTracking;
        updateTitle("Write and notify semantic change");
        updateMessage("write: " + sc.getVersionType() + " " + sc.getNid());
        addToTotalWork(3);
        Get.activeTasks().add(this);
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Call.
     *
     * @return the void
     * @throws Exception the exception
     */
    @Override
    public Void call()
            throws Exception {
        try {
            completedUnitOfWork();
            Get.assemblageService()
                    .writeSemanticChronology(this.sc);
            this.sc = Get.assemblageService().getSemanticChronology(this.sc.getNid());
            this.uncommittedTracking.accept(this.sc, false);
            completedUnitOfWork();
            updateMessage("notifying: " + this.sc.getAssemblageNid());
            this.changeListeners.forEach((listenerRef) -> {
                try {
                    final ChronologyChangeListener listener = listenerRef.get();

                    if (listener == null) {
                        this.changeListeners.remove(listenerRef);
                    } else {
                        listener.handleChange(this.sc);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            completedUnitOfWork();
            updateMessage("complete: " + this.sc.getVersionType() + " " + this.sc.getNid());
            return null;
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }
    }
}
