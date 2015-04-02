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
package gov.vha.isaac.ochre.model.coordinate;

import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;


/**
 *
 * @author kec
 */
public class EditCoordinateImpl implements EditCoordinate {
    int authorSequence;
    int moduleSequence;
    int pathSequence;

    public EditCoordinateImpl(int authorSequence, int moduleSequence, int pathSequence) {
        this.authorSequence = authorSequence;
        this.moduleSequence = moduleSequence;
        this.pathSequence = pathSequence;
    }

    @Override
    public int getAuthorSequence() {
        return authorSequence;
    }

    @Override
    public int getModuleSequence() {
        return moduleSequence;
    }

    @Override
    public int getPathSequence() {
        return pathSequence;
    }
    
}
