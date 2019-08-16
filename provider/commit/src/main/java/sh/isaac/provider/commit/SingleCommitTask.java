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

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.Get;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.transaction.Transaction;

/**
 *
 * @author kec
 */
public class SingleCommitTask extends CommitTask {

    final ObservableVersion[] versionsToCommit;
    final Transaction transaction;
    final String commitComment;
    final Collection<ChangeChecker> checkers;

    public SingleCommitTask(
            Transaction transaction,
            String commitComment,
            Collection<ChangeChecker> checkers,
            ObservableVersion... versionsToCommit) {
        for (ObservableVersion version: versionsToCommit) {
            if (version.getAuthorNid() == 0) throw new IllegalStateException("Author cannot be zero... " + version);
            if (version.getModuleNid() == 0) throw new IllegalStateException("module cannot be zero... " + version);
            if (version.getPathNid() == 0) throw new IllegalStateException("path cannot be zero... " + version);
        }
        this.versionsToCommit = versionsToCommit;
        this.transaction = transaction;
        this.commitComment = commitComment;
        this.checkers = checkers;
    }

    @Override
    protected Optional<CommitRecord> call() throws Exception {
        NidSet conceptNidsInCommit = new NidSet();
        NidSet semanticNidsInCommit = new NidSet();

        for (ObservableVersion observableVersion : versionsToCommit) {
            Chronology independentChronology = (Chronology) observableVersion.createIndependentChronicle();

            if (independentChronology instanceof ConceptChronology) {
                conceptNidsInCommit.add(independentChronology.getNid());
            } else if (independentChronology instanceof SemanticChronology) {
                semanticNidsInCommit.add(independentChronology.getNid());
            }

            for (ChangeChecker checker : checkers) {
                Optional<AlertObject> optionalAlertObject = checker.check(observableVersion, transaction);
                if (optionalAlertObject.isPresent() && optionalAlertObject.get().failCommit()) {
                    AlertObject alertObject = optionalAlertObject.get();
                    this.alertCollection.add(alertObject);
                    LOG.info("commit '{}' prevented by changechecker {} because {}", commitComment, checker.getDescription(), alertObject);
                }
            }
            if (this.alertCollection.size() > 0) {
                return Optional.empty();
            }
        }

        // passed check, now get stamp...
        long commitTime = System.currentTimeMillis();
         StampSequenceSet stampsInCommit = new StampSequenceSet();
         OpenIntIntHashMap stampAliases = new OpenIntIntHashMap();

        for (ObservableVersion observableVersion : versionsToCommit) {
            // Status status, long time, int authorNid, int moduleNid, int pathNid
            int stampSequence = Get.stampService().getStampSequence(observableVersion.getStatus(),
                    commitTime, observableVersion.getAuthorNid(),
                    observableVersion.getModuleNid(), observableVersion.getPathNid());
            stampsInCommit.add(stampSequence);
            
            Chronology chronologyForCommit = observableVersion.createChronologyForCommit(stampSequence);
            ((CommitProvider) Get.commitService()).handleChangeNotification(chronologyForCommit);
            Get.identifiedObjectService().putChronologyData(chronologyForCommit);
            
        }
        CommitRecord commitRecord = new CommitRecord(Instant.ofEpochMilli(commitTime),
                stampsInCommit,
                stampAliases,
                conceptNidsInCommit,
                semanticNidsInCommit,
                commitComment);
        ((CommitProvider) Get.commitService()).handleCommitNotification(commitRecord);
        return Optional.of(commitRecord);
    }

}
