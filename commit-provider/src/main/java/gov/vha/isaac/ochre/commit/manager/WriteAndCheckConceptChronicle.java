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
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
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
public class WriteAndCheckConceptChronicle extends Task<Void> implements Callable<Void> {


    private final ConceptChronology cc;
    private final ConcurrentSkipListSet<ChangeChecker> checkers;
    private final ConcurrentSkipListSet<Alert> alertCollection;
    private final Semaphore writeSemaphore;
    private final ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners;

    public WriteAndCheckConceptChronicle(ConceptChronology cc,
            ConcurrentSkipListSet<ChangeChecker> checkers,
            ConcurrentSkipListSet<Alert> alertCollection, Semaphore writeSemaphore,
            ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners) {
        this.cc = cc;
        this.checkers = checkers;
        this.alertCollection = alertCollection;
        this.writeSemaphore = writeSemaphore;
        this.changeListeners = changeListeners;
        updateTitle("Write and check concept");
        //TODO dan disabled this, cause it keeps causing a timing based (randomly occurring) null pointer exception when it tries to read the descriptions 
        //for this new concept.  see https://slack-files.com/T04QD7FHW-F0B2PQL87-4d6e82e985
        updateMessage("writing nid " + cc.getNid()); // Get.conceptDescriptionText(cc.getConceptSequence()));
        updateProgress(-1, Long.MAX_VALUE); // Indeterminate progress
        LookupService.getService(ActiveTasks.class).get().add(this);
    }

    @Override
    public Void call() throws Exception {
        try {
            Get.conceptService().writeConcept(cc);
            updateProgress(1, 3);
            //TODO dan disabled for the same reason as above.
            updateMessage("checking nid: " + cc.getNid());// Get.conceptDescriptionText(cc.getConceptSequence()));
            
            if (cc.isUncommitted()) {
                checkers.stream().forEach((check) -> {
                    check.check(cc, alertCollection, CheckPhase.ADD_UNCOMMITTED);
                });
            }

            updateProgress(2, 3);
          //TODO dan disabled for the same reason as above.
            updateMessage("notifying nid: " + cc.getNid());// Get.conceptDescriptionText(cc.getConceptSequence()));

             changeListeners.forEach((listenerRef) -> {
                ChronologyChangeListener listener = listenerRef.get();
                if (listener == null) {
                    changeListeners.remove(listenerRef);
                } else {
                    listener.handleChange(cc);
                }
             });

            updateProgress(3, 3);
            //TODO dan disabled for the same reason as above.
            updateMessage("complete nid: " + cc.getNid());// Get.conceptDescriptionText(cc.getConceptSequence()));
            
             return null;
        } finally {
            writeSemaphore.release();
            LookupService.getService(ActiveTasks.class).get().remove(this);
        }
    }
}
