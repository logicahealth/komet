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
package gov.vha.isaac.ochre.observable.model;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.commit.ChronologyChangeListener;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.observable.ObservableChronologyService;
import gov.vha.isaac.ochre.api.observable.concept.ObservableConceptChronology;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import gov.vha.isaac.ochre.collections.jsr166y.ConcurrentReferenceHashMap;
import java.io.IOException;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class ObservableChronologyProvider
        implements ObservableChronologyService, ChronologyChangeListener {

    private static final Logger log = LogManager.getLogger();


    private final UUID listenerUuid = UUID.randomUUID();


    ConcurrentReferenceHashMap<Integer, ObservableConceptChronology> observableConceptMap = new ConcurrentReferenceHashMap<>(
            ConcurrentReferenceHashMap.ReferenceType.STRONG,
            ConcurrentReferenceHashMap.ReferenceType.WEAK);

    ConcurrentReferenceHashMap<Integer, ObservableSememeChronology> observableSememeMap = new ConcurrentReferenceHashMap<>(
            ConcurrentReferenceHashMap.ReferenceType.STRONG,
            ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private ObservableChronologyProvider() {
        //for HK2
        log.info("ObservableChronologyProvider constructed");
    }

    @PostConstruct
    private void startMe() throws IOException {
        log.info("Starting ObservableChronologyProvider post-construct");
    }

    @PreDestroy
    private void stopMe() throws IOException {
        log.info("Stopping ObservableChronologyProvider");
    }

    @Override
    public ObservableConceptChronology getObservableConceptChronology(int id) {
        if (id >= 0) {
            id = Get.identifierService().getConceptNid(id);
        }
        ObservableConceptChronology occ = observableConceptMap.get(id);
        if (occ != null) {
            return occ;
        }
        
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ObservableSememeChronology getObservableSememeChronology(int id) {
        if (id >= 0) {
            id = Get.identifierService().getConceptNid(id);
        }
        ObservableSememeChronology osc = observableSememeMap.get(id);
        if (osc != null) {
            return osc;
        }
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public UUID getListenerUuid() {
        return listenerUuid;
    }

    @Override
    public void handleChange(ConceptChronology<? extends StampedVersion> cc) {
        ObservableConceptChronology occ = observableConceptMap.get(cc.getNid());
        if (occ != null) {
            occ.handleChange(cc);
        }
    }

    @Override
    public void handleChange(SememeChronology<? extends SememeVersion> sc) {
        ObservableSememeChronology osc = observableSememeMap.get(sc.getNid());
        if (osc != null) {
            osc.handleChange(sc);
        }
        ObservableConceptChronology assemblageOcc
                = observableConceptMap.get(sc.getAssemblageSequence());
        if (assemblageOcc != null) {
            assemblageOcc.handleChange(sc);
        }
        // handle referenced component 
        // Concept, description, or sememe
        ObjectChronologyType oct
                = Get.identifierService().getChronologyTypeForNid(sc.getReferencedComponentNid());
        ChronologyChangeListener referencedComponent = null;
        switch (oct) {
            case CONCEPT:
                referencedComponent = observableConceptMap.get(sc.getReferencedComponentNid());
                break;
            case OTHER:
                referencedComponent = 
                        observableConceptMap.get(Get.identifierService().getConceptNidForDescriptionNid(sc.getReferencedComponentNid()));
                break;
            case SEMEME:
                referencedComponent = observableSememeMap.get(sc.getReferencedComponentNid());
                break;
            default:
                throw new UnsupportedOperationException("Can't handle: " + oct);
        }
        if (referencedComponent != null) {
            referencedComponent.handleChange(sc);
        }
    }

}
