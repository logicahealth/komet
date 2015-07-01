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
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;

/**
 * Provides simple static access to common services, in a lookup service 
 * aware way. Intended to be used in place of static fields placed in classes
 * that frequently use a common service. This class was added specifically 
 * to address problems when a service is used in a mojo that spans more 
 * than one project, by ensuring that static initialization of services
 * does not provide a way to retain stale services. 
 * @author kec
 */
@Service 
@Singleton
public class Get implements OchreCache {
    private static final Logger log = LogManager.getLogger();

    private static CommitService commitService;
    private static ConceptModel conceptModel;
    private static ConceptService conceptService;
    private static IdentifiedObjectService identifiedObjectService;
    private static IdentifierService identifierService;
    private static PathService pathService;
    private static SememeService sememeService;
    private static TaxonomyService taxonomyService;

    public Get() {
    }

    public static ConceptService conceptService() {
        if (conceptService == null) {
            conceptService = LookupService.getService(ConceptService.class);
        }
        return conceptService;
    }

    public static IdentifierService identifierService() {
        if (identifierService == null) {
            identifierService = LookupService.getService(IdentifierService.class);
        }
        return identifierService;
    }

    public static PathService pathService() {
        if (pathService == null) {
            pathService = LookupService.getService(PathService.class);
        }
        return pathService;
    }
    
    public static TaxonomyService taxonomyService() {
        if (taxonomyService == null) {
            taxonomyService = LookupService.getService(TaxonomyService.class);
        }
        return taxonomyService;
    }
    
    public static CommitService commitService() {
        if (commitService == null) {
            commitService = LookupService.getService(CommitService.class);
        }
        return commitService;
    }

    public static SememeService sememeService() {
        if (sememeService == null) {
            sememeService = LookupService.getService(SememeService.class);
        }
        return sememeService;
    }

    public static ConceptModel conceptModel() {
        if (conceptModel == null) {
            conceptModel = LookupService.getService(ConfigurationService.class).getConceptModel();
        }
        return conceptModel;
    }
    
    public static IdentifiedObjectService getIdentifiedObjectService() {
        if (identifiedObjectService == null) {
            identifiedObjectService = LookupService.getService(IdentifiedObjectService.class);
        }
        return identifiedObjectService;
    }
    
    
    @Override
    public void reset() {
        log.info("Resetting service cache.");
        commitService = null;
        conceptModel = null;
        conceptService = null;
        identifiedObjectService = null;
        identifierService = null;
        pathService = null;
        sememeService = null;
        taxonomyService = null;
    }
    
}
