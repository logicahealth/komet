/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.api.commit;

import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import java.util.UUID;

/**
 * Observable update rules: 
 * 1. A Chronology update will be sent to the ObservableChronology with the 
 * same nid, to update the contents of the ObservableChronology, if it is 
 * in memory. 
 * 
 * 2. A SememeChronology update will be sent to the ObservableConceptChronology
 * of it's assemblage if it is in memory. If it is in memory, and the ObservableList
 * of the assemblage's sememes is is memory, then the observable sememe list will 
 * be properly updated. 
 * 
 * 3. A SememeChronology update will be sent to the ObservableChronology of the referenced
 * component if it is in memory, and will update the sememe for component ObservableList
 * if it is instantiated. 
 * 
 * 4. A SememeChronology update will be sent to the taxonomy manager. The taxonomy
 * manager will check for updated to the stated and inferred taxonomy. 
 * 
 * 5. A SememeChronology update will be sent to the LogicService. The logic service
 * will determine if changes impact incremental or fully classified data, and will update 
 * the logic status accordingly. 
 * 
 * 
 *
 * @author kec
 */
public interface ChronologyChangeListener {
    
    /**
     * 
     * @return a unique UUID for this listener. 
     */
    UUID getListenerUuid();
    
    /**
     * Don't do work on or block the calling thread. 
     * @param cc a ConceptChronology that has changed, but has not been committed. 
     */
    void handleChange(ConceptChronology<? extends StampedVersion> cc);
    
    /**
     * Don't do work on or block the calling thread. 
     * @param sc a SememeChronology that has changed, but has not been committed. 
     */
    void handleChange(SememeChronology<? extends SememeVersion<?>> sc);
    
    /**
     * Don't do work on or block the calling thread. 
     * @param commitRecord a record of a successful commit. 
     */
    void handleCommit(CommitRecord commitRecord);
    
}
