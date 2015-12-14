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
import java.util.Optional;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import gov.vha.isaac.ochre.api.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 * The Class DescriptionCAB contains methods for creating a description
 * blueprint. This blueprint can be constructed into a type of
 * {@code DescriptionChronicleBI}. This is the preferred method for
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
     * @param initialCaseSignificant set to {@code true} to mark the
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
     * @param initialCaseSignificant set to {@code true} to mark the
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
                Optional.empty(), Optional.empty(), Optional.empty(), idDirective,
                RefexDirective.EXCLUDE);
    }

    /**
     * Instantiates a new description blueprint using nid values and a given
     * {@code descriptionVersion}.
     *
     * @param conceptNid the nid of the enclosing concept
     * @param typeNid the nid of the description type
     * @param langCode the lang code representing the language of the
     * description
     * @param text the lang code representing the language of the description
     * @param initialCaseSignificant set to {@code true} to mark the
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
            Optional<? extends DescriptionVersionBI> descriptionVersion, 
            Optional<ViewCoordinate> viewCoordinate,
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
     * {@code descriptionVersion}.
     *
     * @param conceptUuid the uuid of the enclosing concept
     * @param typeUuid the uuid of the description type
     * @param langCode the lang code representing the language of the
     * description
     * @param text the lang code representing the language of the description
     * @param initialCaseSignificant set to {@code true} to mark the
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
            Optional<? extends DescriptionVersionBI> descriptionVersion, 
            Optional<ViewCoordinate> viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective) throws
            IOException, InvalidCAB, ContradictionException {
        this(conceptUuid, typeUuid, langCode, text, initialCaseSignificant,
                Optional.empty(), descriptionVersion, viewCoordinate, 
                idDirective, 
                refexDirective);
    }

    /**
     * Instantiates a new description blueprint using uuid values and a given
     * {@code descriptionVersion}. Can specify the uuid to be associated
     * with the new description.
     *
     * @param conceptUuid the uuid of the enclosing concept
     * @param typeUuid the uuid of the description type
     * @param langCode the lang code representing the language of the
     * description
     * @param text the lang code representing the language of the description
     * @param initialCaseSignificant set to {@code true} to mark the
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
            Optional<UUID> componentUuid,
            Optional<? extends DescriptionVersionBI> descriptionVersion, 
            Optional<ViewCoordinate> viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective) 
            throws IOException, InvalidCAB, ContradictionException {
        super(getComponentUUID(componentUuid,descriptionVersion,idDirective), 
                descriptionVersion, viewCoordinate,
                (idDirective == IdDirective.PRESERVE_CONCEPT_REST_HASH ? IdDirective.GENERATE_HASH : idDirective), refexDirective);

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
     * @return {@code true}, if this description blueprint is initial case
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
     * {@code enclosingConceptUuid}.
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
     * {@code descriptionVersion}. Compares the status nid, component nid,
     * enclosing concept nid, description type nid, language, and text.
     *
     * @param descriptionVersion the description version to use for validation
     * @return {@code true}, if this description blueprint's fields are
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

  /**
  * Adds the appropriate dialect refexes to the preferred name description blueprint.
  *
  * @param preferredBlueprint the preferred name description blueprint
  * @param dialect the dialect of the preferred name, only supports en-gb and en-us
  * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
  * @throws IOException signals that an I/O exception has occurred
  * @throws InvalidCAB if the any of the values in blueprint to make are invalid
  * @throws ContradictionException if more than one version is found for a given position or view
  * coordinate
  */
    /**
     * Adds the appropriate dialect refexes to the preferred name description blueprint.
     *
     * @param dialect the dialect of the preferred name, only supports en-gb and en-us
     * @param moduleUUID - (optional) the module to use - but mostly likely overwritten during commit anyway... no idea why its here (Dan)
     * @param pathUUID - (optional) the path to use - but mostly likely overwritten during commit anyway... no idea why its here (Dan)
     * 
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public void makePreferredNameDialectRefexes(LanguageCode dialect, UUID moduleUuid, UUID pathUuid) throws 
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException, NoSuchAlgorithmException {
        RefexCAB usAnnot;
        RefexCAB gbAnnot;
        if (dialect == LanguageCode.EN) {
            usAnnot = new RefexCAB(RefexType.CID,
                    this.getComponentUuid(),
                    ConceptCB.usRefexUuid, idDirective, refexDirective);
            usAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0]);
            if (moduleUuid != null) {
                usAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }

            gbAnnot = new RefexCAB(RefexType.CID,
                    this.getComponentUuid(),
                    ConceptCB.gbRefexUuid, idDirective, refexDirective);
            gbAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0]);
            if (moduleUuid != null) {
                gbAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }
            if (pathUuid != null) {
                gbAnnot.properties.put(ComponentProperty.PATH_ID, pathUuid);
            }
            this.addAnnotationBlueprint(usAnnot);
            this.addAnnotationBlueprint(gbAnnot);
        } else if (dialect == LanguageCode.EN_US) {
            usAnnot = new RefexCAB(RefexType.CID,
                    this.getComponentUuid(),
                    ConceptCB.usRefexUuid, idDirective, refexDirective);
            usAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0]);
            if (moduleUuid != null) {
                usAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }
            if (pathUuid != null) {
                usAnnot.properties.put(ComponentProperty.PATH_ID, pathUuid);
            }
            this.addAnnotationBlueprint(usAnnot);
        } else if (dialect == LanguageCode.EN_GB) {
            gbAnnot = new RefexCAB(RefexType.CID,
                    this.getComponentUuid(),
                    ConceptCB.gbRefexUuid, idDirective, refexDirective);
            gbAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0]);
            if (moduleUuid != null) {
                gbAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }
            this.addAnnotationBlueprint(gbAnnot);
        } else {
            throw new InvalidCAB("Dialect not supported: " + dialect.getFormatedLanguageCode());
        }
    }
}
