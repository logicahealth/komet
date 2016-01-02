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
import gov.vha.isaac.ochre.api.commit.Alert;
import gov.vha.isaac.ochre.api.commit.ChangeChecker;
import gov.vha.isaac.ochre.api.commit.ChronologyChangeListener;
import gov.vha.isaac.ochre.api.commit.CheckPhase;
import gov.vha.isaac.ochre.api.commit.CommitStates;
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
public class WriteAndCheckSememeChronicle extends Task<Void> implements Callable<Void> {

    private final SememeChronology sc;
    private final ConcurrentSkipListSet<ChangeChecker> checkers;
    private final ConcurrentSkipListSet<Alert> alertCollection;
    private final Semaphore writeSemaphore;
    private final ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners;

    public WriteAndCheckSememeChronicle(SememeChronology sc,
            ConcurrentSkipListSet<ChangeChecker> checkers,
            ConcurrentSkipListSet<Alert> alertCollection, Semaphore writeSemaphore,
            ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners) {
        this.sc = sc;
        this.checkers = checkers;
        this.alertCollection = alertCollection;
        this.writeSemaphore = writeSemaphore;
        this.changeListeners = changeListeners;
        updateTitle("Write, check, and notify for sememe change");
        updateMessage("write: " + sc.getSememeType() + " " + sc.getSememeSequence());
        updateProgress(-1, Long.MAX_VALUE); // Indeterminate progress
        LookupService.getService(ActiveTasks.class).get().add(this);
    }

    @Override
    public Void call() throws Exception {
        try {
            Get.sememeService().writeSememe(sc);
            updateProgress(1, 3);
            updateMessage("checking: " + sc.getSememeType() + " " + sc.getSememeSequence());
            if (sc.getCommitState() == CommitStates.UNCOMMITTED) {
                checkers.stream().forEach((check) -> {
                    check.check(sc, alertCollection, CheckPhase.ADD_UNCOMMITTED);
                });
            }

            updateProgress(2, 3);
            updateMessage("notifying: " + sc.getSememeType() + " " + sc.getSememeSequence());
             
             changeListeners.forEach((listenerRef) -> {
                ChronologyChangeListener listener = listenerRef.get();
                if (listener == null) {
                    changeListeners.remove(listenerRef);
                } else {
                    listener.handleChange(sc);
                }
             });
             
            updateProgress(3, 3);
            updateMessage("completed change: " + sc.getSememeType() + " " + sc.getSememeSequence());
            return null;
        } finally {
            writeSemaphore.release();
            LookupService.getService(ActiveTasks.class).get().remove(this);
        }
    }
}
