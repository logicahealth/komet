/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api.commit;

import sh.isaac.api.Get;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.transaction.Transaction;
import java.util.Optional;


/**
 * The Interface ChangeChecker.
 * This must be comparable, because it gets used in ConcurrentSkipListSet in the CommitProvider, which assumes things are comparable

 *
 * @author kec
 */
public interface ChangeChecker
        extends Comparable<ChangeChecker> {
    /**
     * Check.
     *
     * @param chronology the to test
     * @param pathConceptNid the pathConcept for the version to be tested the chronology
     * @param transaction the transaction governing the change
     * @return An AlertObject that typically has an {@link AlertType} of {@link AlertType#ERROR} or  {@link AlertType#SUCCESS}
     * To prevent a commit, return an AlertObject which responds true for {@link AlertType#preventsCheckerPass()}
     */
    default Optional<AlertObject> check(Chronology chronology,
                                int pathConceptNid,
                                Transaction transaction) {


        for (Version v: chronology.getVersionList()) {
            if (v.getPathNid() == pathConceptNid && transaction.getStampsForTransaction().contains(v.getStampSequence())) {
                return check(v, transaction);
            }
        }

        throw new IllegalStateException("No version to test for: " + chronology + " checking path: " + Get.conceptDescriptionText(pathConceptNid) + 
            " for transaction " + transaction);
    }


    Optional<AlertObject> check(Version version, Transaction transaction);

    /**
    * The description of the change checker (which should describe what it is checking for)
    * @return
    */
   public String getDescription();
    
    /**
     * @return the desired ordering of your change checker, lower numbers execute first.  Used in the implementation of comparable.
     */
    default int getRank() {
        return 1;
    }

    /**
     * Sorts based on {@link #getRank()}
     */
    @Override
    default int compareTo(ChangeChecker o) {
        return Integer.compare(getRank(), o.getRank());
    }
}

