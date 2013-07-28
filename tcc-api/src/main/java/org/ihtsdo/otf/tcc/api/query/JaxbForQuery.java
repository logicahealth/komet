/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.api.query;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.ihtsdo.otf.tcc.api.conflict.EditPathLosesStrategy;
import org.ihtsdo.otf.tcc.api.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.otf.tcc.api.conflict.LastCommitWinsConflictResolutionStrategy;
import org.ihtsdo.otf.tcc.api.conflict.ViewPathLosesStrategy;
import org.ihtsdo.otf.tcc.api.conflict.ViewPathWinsStrategy;
import org.ihtsdo.otf.tcc.api.coordinate.PositionSet;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

/**
 *
 * @author kec
 */
public class JaxbForQuery {

    public static JAXBContext singleton;

    public static JAXBContext get() throws JAXBException {
        if (singleton == null) {
            singleton = JAXBContext.newInstance(ViewCoordinate.class, EditPathLosesStrategy.class,
                    IdentifyAllConflictStrategy.class, LastCommitWinsConflictResolutionStrategy.class,
                    ViewPathLosesStrategy.class,
                    ViewPathWinsStrategy.class, PositionSet.class);
        }
        return singleton;
    }
}
