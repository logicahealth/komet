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
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePosition;
import java.util.Collection;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface PathService {

	//~--- methods -------------------------------------------------------------
	StampPath getStampPath(int stampPathSequence);

	boolean exists(int pathConceptId);

	Collection<? extends StampPosition> getOrigins(int stampPathSequence);

	Collection<? extends StampPath> getPaths();

	RelativePosition getRelativePosition(StampedVersion v1, StampedVersion v2);
    
	RelativePosition getRelativePosition(int stampSequence1, int stampSequence2);
    
}
