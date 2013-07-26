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
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;

/**
 * The Class DescriptionCAB contains methods for creating a description
 * blueprint. This blueprint can be constructed into a type of
 * <code>DescriptionChronicleBI</code>. This is the preferred method for
 * updating or creating new descriptions.
 *
 * @see TerminologyBuilderBI
 * @see DescriptionChronicleBI
 *
 */
public class DescriptionCAB extends CreateOrAmendBlueprint {

    public static final UUID descSpecNamespace =
            UUID.fromString("457e4a20-5284-11e0-b8af-0800200c9a66");
    private UUID conceptUuid;
    private UUID typeUuid;
    public String lang;
    public String text;
    public boolean initialCaseSignificant;

    /**
     * Instantiates a new description blueprint using nids values.
     *
     * @param conceptNid the nid of the enclosing concept
     * @param typeNid the nid of the description type
     * @param langCode the lang code representing the language of the
     * description
     * @param text the text of the description
     * @param initialCaseSignificant set to <code>true</code> to mark the
     * description as initial case significant
     * @param idDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public DescriptionCAB(
            int conceptNid, int typeNid, LanguageCode langCode, 
            String text, 
            boolean initialCaseSignificant, 
            IdDirective idDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimordialUuid(),
                Ts.get().getComponent(typeNid).getPrimordialUuid(),
                langCode, text, initialCaseSignificant, idDirective);
    }

    /**
     * Instantiates a new description blueprint using uuid values.
     *
     * @param conceptUuid the uuid of the enclosing concept
     * @param typeUuid the uuid of the description type
     * @param langCode the lang code representing the language of the
     * description
     * @param text the lang code representing the language of the description
     * @param initialCaseSignificant set to <code>true</code> to mark the
     * description as initial case significant
     * @param idDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public DescriptionCAB(
            UUID conceptUuid, UUID typeUuid, LanguageCode langCode, String text, boolean initialCaseSignificant, IdDirective idDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(conceptUuid, typeUuid, langCode, text, initialCaseSignificant,
                null, null, null, idDirective,
                RefexDirective.EXCLUDE);
    }

    /**
     * Instantiates a new description blueprint using nid values and a given
     * <code>descriptionVersion</code>.
     *
     * @param conceptNid the nid of the enclosing concept
     * @param typeNid the nid of the description type
     * @param langCode the lang code representing the language of the
     * description
     * @param text the lang code representing the language of the description
     * @param initialCaseSignificant set to <code>true</code> to mark the
     * description as initial case significant
     * @param descriptionVersion the description version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @param idDirective 
     * @param refexDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public DescriptionCAB(
            int conceptNid, int typeNid, LanguageCode langCode, String text, 
            boolean initialCaseSignificant,
            DescriptionVersionBI descriptionVersion, 
            ViewCoordinate viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective) 
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimordialUuid(),
                Ts.get().getComponent(typeNid).getPrimordialUuid(),
                langCode, text, initialCaseSignificant, descriptionVersion, 
                viewCoordinate,
                idDirective, refexDirective);
    }

    /**
     * Instantiates a new description blueprint using uuid values and a given
     * <code>descriptionVersion</code>.
     *
     * @param conceptUuid the uuid of the enclosing concept
     * @param typeUuid the uuid of the description type
     * @param langCode the lang code representing the language of the
     * description
     * @param text the lang code representing the language of the description
     * @param initialCaseSignificant set to <code>true</code> to mark the
     * description as initial case significant
     * @param descriptionVersion the description version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @param idDirective 
     * @param refexDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public DescriptionCAB(
            UUID conceptUuid, UUID typeUuid, LanguageCode langCode, String text,
            boolean initialCaseSignificant, 
            DescriptionVersionBI descriptionVersion, 
            ViewCoordinate viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective) throws
            IOException, InvalidCAB, ContradictionException {
        this(conceptUuid, typeUuid, langCode, text, initialCaseSignificant,
                null, descriptionVersion, viewCoordinate, 
                idDirective, 
                refexDirective);
    }

    /**
     * Instantiates a new description blueprint using uuid values and a given
     * <code>descriptionVersion</code>. Can specify the uuid to be associated
     * with the new description.
     *
     * @param conceptUuid the uuid of the enclosing concept
     * @param typeUuid the uuid of the description type
     * @param langCode the lang code representing the language of the
     * description
     * @param text the lang code representing the language of the description
     * @param initialCaseSignificant set to <code>true</code> to mark the
     * description as initial case significant
     * @param componentUuid the uuid representing the new description
     * @param descriptionVersion the description version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public DescriptionCAB(
            UUID conceptUuid, UUID typeUuid, LanguageCode langCode, String text,
            boolean initialCaseSignificant, 
            UUID componentUuid,
            DescriptionVersionBI descriptionVersion, 
            ViewCoordinate viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective) 
            throws IOException, InvalidCAB, ContradictionException {
        super(getComponentUUID(componentUuid,descriptionVersion,idDirective), 
                descriptionVersion, viewCoordinate,
                idDirective, refexDirective);

        this.conceptUuid = conceptUuid;
        this.lang = langCode.getFormatedLanguageNoDialectCode();
        this.text = text;
        this.initialCaseSignificant = initialCaseSignificant;
        this.typeUuid = typeUuid;
        if (getComponentUuid() == null) {
            try {
                recomputeUuid();
            } catch (IOException | InvalidCAB | NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Computes the uuid for this description based on the enclosing concept
     * uuid, description type uuid, language, and description text. Recomputes
     * the uuids for dependent annotations.
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
    public void recomputeUuid()
            throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException, InvalidCAB, ContradictionException {
        switch (idDirective) {
            case PRESERVE_CONCEPT_REST_HASH:
            case GENERATE_RANDOM_CONCEPT_REST_HASH:
            case GENERATE_HASH:
            case GENERATE_REFEX_CONTENT_HASH:
                setComponentUuid(UuidT5Generator.get(descSpecNamespace,
                        getPrimoridalUuidString(conceptUuid)
                        + typeUuid
                        + lang
                        + text));
                break;
                
            case GENERATE_RANDOM:
                setComponentUuidNoRecompute(UUID.randomUUID());
                break;

            case PRESERVE:
            default:
            // nothing to do...

        }

        for (RefexCAB annotBp : getAnnotationBlueprints()) {
            annotBp.setReferencedComponentUuid(getComponentUuid());
            annotBp.recomputeUuid();
        }
    }

    /**
     * Gets the description type uuid associated with this description
     * blueprint.
     *
     * @return the description type uuid
     */
    public UUID getTypeUuid() {
        return typeUuid;
    }

    /**
     * Gets the description type nid associated with this description blueprint.
     *
     * @return the description type nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getTypeNid() throws IOException {
        return Ts.get().getNidForUuids(typeUuid);
    }

    /**
     * Gets the enclosing concept nid associated with this description
     * blueprint.
     *
     * @return the enclosing concept nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getConceptNid() throws IOException {
        return Ts.get().getNidForUuids(conceptUuid);
    }

    /**
     * Gets the enclosing concept uuid associated with this description
     * blueprint.
     *
     * @return the enclosing concept uuid
     */
    public UUID getConceptUuid() {
        return conceptUuid;
    }

    /**
     * Checks if this description blueprint is marked as initial case
     * significant.
     *
     * @return <code>true</code>, if this description blueprint is initial case
     * significant
     */
    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    /**
     * Gets a two character abbreviation of language of this description
     * blueprint.
     *
     * @return a two character abbreviation of the description language
     */
    public String getLang() {
        return lang;
    }

    /**
     * Gets the text of the description blueprint.
     *
     * @return the description text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the enclosing concept for this description blueprint based on the
     * given
     * <code>enclosingConceptUuid</code>.
     *
     * @param enclosingConceptUuid the uuid of the enclosing concept
     */
    protected void setConceptUuid(UUID enclosingConceptUuid) {
        this.conceptUuid = enclosingConceptUuid;
    }

    /**
     * Sets the description text for this description blueprint.
     *
     * @param newText the description text
     */
    public void setText(String newText) {
        this.text = newText;
    }

    /**
     * Validates this description blueprint's fields against the given
     * <code>descriptionVersion</code>. Compares the status nid, component nid,
     * enclosing concept nid, description type nid, language, and text.
     *
     * @param descriptionVersion the description version to use for validation
     * @return <code>true</code>, if this description blueprint's fields are
     * equal to the specified description version
     * @throws IOException signals that an I/O exception has occurred
     */
    public boolean validate(DescriptionVersionBI descriptionVersion) throws IOException {
        if (descriptionVersion.getStatus() != getStatus()) {
            return false;
        }
        if (descriptionVersion.getNid() != getComponentNid()) {
            return false;
        }
        if (descriptionVersion.getConceptNid() != getConceptNid()) {
            return false;
        }
        if (descriptionVersion.getTypeNid() != getTypeNid()) {
            return false;
        }
        if (!descriptionVersion.getLang().equals(getLang())) {
            return false;
        }
        if (!descriptionVersion.getText().equals(getText())) {
            return false;
        }
        if (descriptionVersion.isInitialCaseSignificant() != isInitialCaseSignificant()) {
            return false;
        }
        return true;
    }
}
