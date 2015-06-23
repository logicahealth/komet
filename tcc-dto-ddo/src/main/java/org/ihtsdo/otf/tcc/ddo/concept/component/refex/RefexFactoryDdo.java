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
package org.ihtsdo.otf.tcc.ddo.concept.component.refex;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp.RefexCompChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_long.RefexLongChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_member.RefexMembershipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_string.RefexStringChronicleDdo;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;
import java.util.Optional;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.logicgraph.LogicGraphChronicleDdo;

/**
 *
 * @author kec
 */
public class RefexFactoryDdo {

    /**
     * Method description
     *
     *
     * @param taxonomyCoordinate
     * @param concept
     * @param another
     *
     * @return
     *
     * @throws ContradictionException
     * @throws IOException
     */
    public static Optional<RefexChronicleDdo<?,?>> make(TaxonomyCoordinate taxonomyCoordinate, ConceptChronicleDdo concept, SememeChronology another)
            throws IOException, ContradictionException {
        switch (another.getSememeType()) {
            case COMPONENT_NID:
                return Optional.of(new RefexCompChronicleDdo(taxonomyCoordinate, concept, another));
            case LONG:
                return Optional.of(new RefexLongChronicleDdo(taxonomyCoordinate, concept, another));
            case MEMBER:
                return Optional.of(new RefexMembershipChronicleDdo(taxonomyCoordinate, concept, another));
            case STRING:
                return Optional.of(new RefexStringChronicleDdo(taxonomyCoordinate, concept, another));
            case LOGIC_GRAPH:
                return Optional.of(new LogicGraphChronicleDdo(taxonomyCoordinate, concept, another));
            case DESCRIPTION:
                return Optional.empty();
            case DYNAMIC:
                return Optional.empty();
            default:
                throw new UnsupportedOperationException("Can't handle: " + another.getSememeType());
        }
    }
}
