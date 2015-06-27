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
package gov.vha.isaac.ochre.model.coordinate;

import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.PathService;
import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import java.util.Collection;

/**
 *
 * @author kec
 */
public class StampPathImpl implements StampPath {
    
    private static PathService pathService = null;
    private static PathService getPathService() {
        if (pathService == null) {
            pathService = LookupService.getService(PathService.class);
            if (pathService == null) {
                throw new RuntimeException("PathService not found.");
            }
        }
        return pathService;
    }
    private static IdentifierService identifierService;
    private static IdentifierService getIdentifierService() {
        if (identifierService == null) {
            identifierService = LookupService.getService(IdentifierService.class);
        }
        return identifierService;
    }
    
    private final int pathConceptSequence;

    public StampPathImpl(int pathConceptSequence) {
        if (pathConceptSequence < 0) {
            pathConceptSequence = getIdentifierService().getConceptSequence(pathConceptSequence);
        }
        this.pathConceptSequence = pathConceptSequence;
    }

    @Override
    public int getPathConceptSequence() {
        return pathConceptSequence;
    }

    @Override
    public Collection<? extends StampPosition> getPathOrigins() {
        return getPathService().getOrigins(pathConceptSequence);
    }
    
}
