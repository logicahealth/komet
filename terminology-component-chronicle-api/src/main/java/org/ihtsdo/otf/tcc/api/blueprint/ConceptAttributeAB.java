/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.api.blueprint;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

/**
 * The Class ConceptAttributeAB contains methods for creating a concept
 * attribute blueprint. This blueprint can be constructed into a type of
 * <code>ConceptAttributeChronicleBI</code>. This is the preferred method for
 * creating new concepts. This class of blueprint can only be used to amend
 * existing concepts, use ConceptCB to create a new concept.
 *
 * @see TerminologyBuilderBI
 * @see ConceptAttributeChronicleBI
 * @see ConceptCB
 *
 */
public class ConceptAttributeAB extends CreateOrAmendBlueprint {

    public boolean defined;

    /**
     * Instantiates a new concept attribute blueprint using nid values.
     *
     * @param conceptNid the enclosing concept nid
     * @param defined set to <code>true</code> to mark the concept as defined
     * @param refexDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public ConceptAttributeAB(
            int conceptNid, boolean defined, RefexDirective refexDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimordialUuid(),
                defined, null, null, refexDirective, IdDirective.PRESERVE);
    }

    /**
     * Instantiates a new concept attribute blueprint using nid values and a
     * given
     * <code>conceptAttributeVersion</code>.
     *
     * @param conceptNid the enclosing concept nid
     * @param defined set to <code>true</code> to mark the concept as defined
     * @param conceptAttributeVersion the concept attribute version to use as a
     * pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @param refexDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public ConceptAttributeAB(
            int conceptNid, boolean defined, 
            ConceptAttributeVersionBI conceptAttributeVersion,
            ViewCoordinate viewCoordinate, RefexDirective refexDirective, 
            IdDirective idDirective) 
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimordialUuid(),
                defined, conceptAttributeVersion, viewCoordinate, refexDirective, idDirective);
    }

    /**
     * Instantiates a new concept attribute blueprint using uuid values.
     *
     * @param componentUuid the uuid associated with concept
     * @param defined set to <code>true</code> to mark the concept as defined
     * @param conceptAttributeVersion the concept attribute version to use as a
     * pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @param  refexDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public ConceptAttributeAB(
            UUID componentUuid, 
            boolean defined, 
            ConceptAttributeVersionBI conceptAttributeVersion,
            ViewCoordinate viewCoordinate,
            RefexDirective refexDirective, 
            IdDirective idDirective) 
            throws IOException, InvalidCAB, ContradictionException {
        super(componentUuid, conceptAttributeVersion, viewCoordinate, 
                idDirective, refexDirective);
        this.defined = defined;
    }

    /**
     * Validates this concept attribute blueprint's fields against the given
     * <code>conceptAttributeVersion</code>. Compares the status nid, component
     * nid, and boolean value for defined.
     *
     * @param conceptAttributeVersion the concept attribute version to use for
     * validation
     * @return <code>true</code>, if this concept attribute blueprint's fields
     * are equal to the specified concept attribute version
     * @throws IOException signals that an I/O exception has occurred
     */
    public boolean validate(ConceptAttributeVersionBI conceptAttributeVersion) throws IOException {
        if (conceptAttributeVersion.getStatus() != getStatus()) {
            return false;
        }
        if (conceptAttributeVersion.getNid() != getComponentNid()) {
            return false;
        }
        if (conceptAttributeVersion.isDefined() != defined) {
            return false;
        }
        return true;
    }

    /**
     * The UUID for ConAttrAB is set when the enclosing concept is created. Throws an
     * <code>InvalidCAB</code> if called.
     *
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        throw new InvalidCAB("UUID for ConAttrAB is set when concept is created");
    }
}
