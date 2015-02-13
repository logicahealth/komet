/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.model.version;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.VersionPointBI;

/**
 *
 * @author kec
 */
public interface RelativePositionComputerBI {


    /**
     * Bypasses the onRoute test of {@code relativePosition}
     * @param v1 the first part of the comparison.
     * @param v2 the second part of the comparison.
     * @param precedencePolicy
     * @return the {@code RelativePosition} of part1 compared to part2
     * with respect to the destination position of the class's instance.
     */
    RelativePosition fastRelativePosition(VersionPointBI v1, VersionPointBI v2, Precedence precedencePolicy);

    Position getDestination();

    /**
     *
     * @param version
     * the part to be tested to determine if it is on route to the
     * destination.
     * @return true if the part's position is on the route to the destination of
     * the class's instance.
     */
    boolean onRoute(VersionPointBI version);

    /**
     *
     * @param v1 the first part of the comparison.
     * @param v2 the second part of the comparison.
     * @return the {@code RelativePosition} of v1 compared to v2
     * with respect to the destination position of the class's instance.
     * @throws IOException
     */
    RelativePosition relativePosition(VersionPointBI v1, VersionPointBI v2) throws IOException;

}
