/*
 * Copyright 2011
 * International Health Terminology Standards Development Organisation.
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

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf1;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRfx;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyChangeEvent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;

/**
 * The Class ConceptCB contains methods for creating a concept blueprint. This blueprint can be constructed
 * into a type of
 * <code>ConceptChronicleBI</code>. This is the preferred method for creating new concepts. Use
 * ConceptAttributeAB to amend concept attributes if the concept already exists.
 *
 * @see TerminologyBuilderBI
 * @see ConceptChronicleBI
 * @see ConceptAttributeAB
 *
 */
public final class ConceptCB extends CreateOrAmendBlueprint {

    public static final UUID conceptSpecNamespace =
            UUID.fromString("620d1f30-5285-11e0-b8af-0800200c9a66");
    private static final UUID usRefexUuid = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getUuids()[0];
    private static final UUID gbRefexUuid = SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getUuids()[0];
    private Object lastPropigationId = Long.MIN_VALUE;

    private String fullySpecifiedName;
    private String preferredName;
    private List<String> fsns = new ArrayList<>();
    private List<String> prefNames = new ArrayList<>();
    private boolean initialCaseSensitive = false;
    private String lang;
    private UUID isaType;
    private UUID moduleUuid;
    private boolean defined;
    private List<DescriptionCAB> fsnCABs = new ArrayList<>();
    private List<DescriptionCAB> prefCABs = new ArrayList<>();
    private List<DescriptionCAB> descCABs = new ArrayList<>();
    private List<RelationshipCAB> relCABs = new ArrayList<>();
    private List<MediaCAB> mediaCABs = new ArrayList<>();
    private ConceptAttributeAB conAttrAB;
    private Collection<UUID> parents = new TreeSet<UUID>() {
        @Override
        public boolean add(UUID e) {
            boolean result = super.add(e);
            setComponentUuid(computeComponentUuid());
            return result;
        }

        @Override
        public boolean addAll(Collection<? extends UUID> clctn) {
            boolean result = super.addAll(clctn);
            setComponentUuid(computeComponentUuid());
            return result;
        }

        @Override
        public boolean remove(Object obj) {
            boolean result = super.remove(obj);
            setComponentUuid(computeComponentUuid());
            return result;
        }

        @Override
        public boolean removeAll(Collection<?> clctn) {
            boolean result = super.removeAll(clctn);
            setComponentUuid(computeComponentUuid());
            return result;
        }
    };
    private boolean annotationRefexExtensionIdentity;

    /**
     * Gets the uuids of parent concept for this concept blueprint.
     *
     * @return the uuids of the parent concepts
     */
    public Collection<UUID> getParents() {
        return parents;
    }

    /**
     * Instantiates a new concept blueprint using uuid values to specify the new concept.
     *
     * @param fullySpecifiedName the text to use for the fully specified name
     * @param preferredName the text to use for the preferred name
     * @param langCode the lang code representing the language of the description
     * @param isaTypeUuid the uuid representing the relationship type to use for specifying the parent
     * concepts
     * @param idDirective generally <code><b>IdDirective.GENERATE_HASH</b></code> for new concepts
     * @param parentUuids the uuids of the parent concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a view coordinate
     */
    public ConceptCB(String fullySpecifiedName,
            String preferredName,
            LanguageCode langCode,
            UUID isaTypeUuid,
            IdDirective idDirective,
            UUID moduleUuid,
            UUID... parentUuids) throws IOException, InvalidCAB, ContradictionException {
        this(fullySpecifiedName, preferredName, langCode, isaTypeUuid, idDirective, moduleUuid, null, parentUuids);
    }

    public ConceptCB(String fullySpecifiedName,
            String preferredName,
            LanguageCode langCode,
            UUID isaTypeUuid,
            IdDirective idDirective,
            UUID moduleUuid,
            UUID conceptUuid,
            UUID... parentUuids) throws IOException, InvalidCAB, ContradictionException {
        super(conceptUuid, null, null, idDirective, RefexDirective.EXCLUDE);
        this.fsns.add(fullySpecifiedName);
        this.fullySpecifiedName = fullySpecifiedName; //@akf todo: these should be removed when NewConcept, etc. is upated
        this.prefNames.add(preferredName);
        this.preferredName = preferredName; //@akf todo: these should be removed when NewConcept, etc. is upated
        this.lang = langCode.getFormatedLanguageCode();
        this.isaType = isaTypeUuid;
        this.moduleUuid = moduleUuid;
        if (parentUuids != null) {
            this.parents.addAll(Arrays.asList(parentUuids));
        }
        pcs.addPropertyChangeListener(this);
        setComponentUuid(computeComponentUuid());
    }

    /**
     * Instantiates a new concept blueprint using uuid values to specify the new concept. Allows multiple
     * fully specified names and preferred names to be specified.
     *
     * @param fullySpecifiedNames a list of strings to use for the fully specified names
     * @param preferredNames a list of strings to use for the preferred names
     * @param langCode the lang code representing the language of the description
     * @param isaTypeUuid the uuid representing the relationship type to use for specifying the parent
     * concepts
     * @param idDirective generally <code><b>IdDirective.GENERATE_HASH</b></code> for new concepts
     * @param parentUuids the uuids of the parent concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a view coordinate
     */
    public ConceptCB(List<String> fullySpecifiedNames,
            List<String> preferredNames,
            LanguageCode langCode,
            UUID isaTypeUuid,
            IdDirective idDirective,
            UUID... parentUuids) throws IOException, InvalidCAB, ContradictionException {
        super(null, null, null, idDirective, RefexDirective.EXCLUDE);
        this.fsns = fullySpecifiedNames;
        this.prefNames = preferredNames;
        this.lang = langCode.getFormatedLanguageCode();
        this.isaType = isaTypeUuid;
        if (parentUuids != null) {
            this.parents.addAll(Arrays.asList(parentUuids));
        }
        pcs.addPropertyChangeListener(this);
            setComponentUuid(computeComponentUuid());
    }

    public ConceptCB(ConceptVersionBI conceptVersion,
            IdDirective idDirective,
            RefexDirective refexDirective)
            throws IOException, ContradictionException, InvalidCAB {
        this(conceptVersion,
                null, idDirective, refexDirective);
    }

    /**
     * Instantiates a new concept blueprint based on the given
     * <code>conceptVersion</code>. Can specify a uuid for the new concept.
     *
     * @param conceptVersion the concept version to use to create this concept blueprint
     * @param newConceptUuid the uuid representing the new concept
     * @param idDirective
     * @param refexDirective
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     */
    public ConceptCB(ConceptVersionBI conceptVersion,
            UUID newConceptUuid,
            IdDirective idDirective,
            RefexDirective refexDirective)
            throws IOException, ContradictionException, InvalidCAB {
        super(newConceptUuid, conceptVersion, conceptVersion.getViewCoordinate(), idDirective, refexDirective);
        pcs.addPropertyChangeListener(this);
        conAttrAB = conceptVersion.getConceptAttributesActive().makeBlueprint(conceptVersion.getViewCoordinate(),
                idDirective, refexDirective);
        for (DescriptionVersionBI dv : conceptVersion.getDescriptionsFullySpecifiedActive()) {
            fsns.add(dv.getText());
            DescriptionCAB fsnBp = dv.makeBlueprint(conceptVersion.getViewCoordinate(), idDirective, refexDirective);
            fsnCABs.add(fsnBp);
            descCABs.add(fsnBp);
        }
        for (DescriptionVersionBI dv : conceptVersion.getDescriptionsPreferredActive()) {
            prefNames.add(dv.getText());
            DescriptionCAB prefBp = dv.makeBlueprint(conceptVersion.getViewCoordinate(), idDirective, refexDirective);
            prefCABs.add(prefBp);
            descCABs.add(prefBp);
        }
        for (DescriptionVersionBI dv : conceptVersion.getDescriptionsActive()) {
            if (conceptVersion.getDescriptionsFullySpecifiedActive().contains(dv) || conceptVersion.getDescriptionsPreferredActive().contains(dv)) {
                continue;
            }
            DescriptionCAB descBp = dv.makeBlueprint(conceptVersion.getViewCoordinate(), idDirective, refexDirective);
            descCABs.add(descBp);
        }
        for (RelationshipVersionBI rv : conceptVersion.getRelationshipsOutgoingActive()) {
            if (rv.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
                continue;
            }
            RelationshipCAB relBp = rv.makeBlueprint(conceptVersion.getViewCoordinate(), idDirective, refexDirective);
            relCABs.add(relBp);
        }
        for (MediaVersionBI mv : conceptVersion.getMediaActive()) {
            MediaCAB mediaBp = mv.makeBlueprint(conceptVersion.getViewCoordinate(), idDirective, refexDirective);
            mediaCABs.add(mediaBp);
        }
    }

    /**
     * Listens for a property change event in any of the component blueprint classes and recomputes the
     * concept blueprint's computed uuid if a dependent component has changed.
     *
     * @param propertyChangeEvent the property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (!propertyChangeEvent.getPropagationId().equals(lastPropigationId)) {
            try {
                lastPropigationId = propertyChangeEvent.getPropagationId();
                recomputeUuid();
            } catch (NoSuchAlgorithmException | InvalidCAB | ContradictionException | IOException ex) {
                Logger.getLogger(CreateOrAmendBlueprint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Computes the uuid for the concept represented by this concept blueprint based on the fully specified
     * names and preferred terms.
     *
     * @throws RuntimeException indicates a runtime exception has occurred
     */
    private UUID computeComponentUuid() throws RuntimeException {
        switch (idDirective) {
            case GENERATE_HASH:
            case GENERATE_REFEX_CONTENT_HASH:
                try {
                    StringBuilder sb = new StringBuilder();
                    List<String> descs = new ArrayList<>();
                    descs.addAll(fsns);
                    descs.addAll(prefNames);
                    java.util.Collections.sort(descs);
                    for (String desc : descs) {
                        sb.append(desc);
                    }
                    return UuidT5Generator.get(conceptSpecNamespace, sb.toString());
                } catch (IOException | NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }

            case GENERATE_RANDOM:
            case GENERATE_RANDOM_CONCEPT_REST_HASH:
                return UUID.randomUUID();

            case PRESERVE_CONCEPT_REST_HASH:
            case PRESERVE:
            default:
            return getComponentUuid();

        }
    }

    /**
     * Resets the enclosing or source concepts for the components on this concept. Then recomputes the uuids
     * of the components based on the new uuid of the concept.
     *
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException, InvalidCAB, ContradictionException {
        switch (idDirective) {
            case GENERATE_HASH:
            case GENERATE_REFEX_CONTENT_HASH:
                setComponentUuid(computeComponentUuid());
                break;
            case GENERATE_RANDOM:
            case GENERATE_RANDOM_CONCEPT_REST_HASH:
                setComponentUuidNoRecompute(UUID.randomUUID());
                break;

            case PRESERVE_CONCEPT_REST_HASH:
            case PRESERVE:
            default:
            // nothing to do...

        }

        for (DescriptionCAB descBp : getDescriptionCABs()) {
            descBp.setConceptUuid(getComponentUuid());
            descBp.recomputeUuid();
        }
        for (RelationshipCAB relBp : getRelationshipCABs()) {
            relBp.setSourceUuid(getComponentUuid());
            relBp.recomputeUuid();
        }
        for (MediaCAB mediaBp : getMediaCABs()) {
            mediaBp.setConceptUuid(getComponentUuid());
            mediaBp.recomputeUuid();
        }
    }

    /**
     * Gets the text of fully specified name associated with this concept blueprint.
     *
     * @return the fully specified name text
     */
    public String getFullySpecifiedName() {//@akf todo : update to use set when NewConcept, etc. has been updated
        return fullySpecifiedName;
    }

    /**
     * Sets the text to use in the fully specified name (FSN) associated with this concept blueprint.
     * Recomputes the uuid associated with this concept based on the updated FSN text.
     *
     * @param fullySpecifiedName the text to use for the fully specified name
     */
    public void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
        setComponentUuid(computeComponentUuid());
    }

    /**
     * Adds a description blueprint to use for the fully specified name (FSN) description associated with this
     * concept blueprint. Recomputes the uuid associated with this concept based on the updated FSN text. Adds
     * the appropriate language/dialect refexes based on the given dialect code (only supports en-us and
     * en-gb). This method does not remove existing FSN blueprints that are already associated with this
     * concept blueprint.
     *
     * @param fullySpecifiedNameBlueprint the description blueprint for the fully specified name description
     * @param dialect the language code representing the dialect of the FSN
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public void addFullySpecifiedName(DescriptionCAB fullySpecifiedNameBlueprint, LanguageCode dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        fsns.add(fullySpecifiedNameBlueprint.getText());
        addFullySpecifiedNameDialectRefexes(fullySpecifiedNameBlueprint, dialect);
        this.recomputeUuid();
    }

    /**
     * Adds the appropriate dialect refexes to the fully specified name description blueprint.
     *
     * @param fullySpecifiedNameBlueprint the fully specified name description blueprint
     * @param dialect the dialect of the FSN, only supports en-gb and en-us
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    private void addFullySpecifiedNameDialectRefexes(DescriptionCAB fullySpecifiedNameBlueprint, LanguageCode dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        RefexCAB usAnnot;
        RefexCAB gbAnnot;
        if (dialect == LanguageCode.EN) {
            usAnnot = new RefexCAB(RefexType.CID,
                    fullySpecifiedNameBlueprint.getComponentUuid(),
                    usRefexUuid, idDirective, refexDirective);
            usAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            if (moduleUuid != null) {
                usAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }

            gbAnnot = new RefexCAB(RefexType.CID,
                    fullySpecifiedNameBlueprint.getComponentUuid(),
                    gbRefexUuid, idDirective, refexDirective);
            gbAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            if (moduleUuid != null) {
                gbAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }
            fullySpecifiedNameBlueprint.addAnnotationBlueprint(usAnnot);
            fullySpecifiedNameBlueprint.addAnnotationBlueprint(gbAnnot);
        } else if (dialect == LanguageCode.EN_US) {
            usAnnot = new RefexCAB(RefexType.CID,
                    fullySpecifiedNameBlueprint.getComponentUuid(),
                    usRefexUuid, idDirective, refexDirective);
            usAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            if (moduleUuid != null) {
                usAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }
            fullySpecifiedNameBlueprint.addAnnotationBlueprint(usAnnot);
        } else if (dialect == LanguageCode.EN_GB) {
            throw new InvalidCAB("<html>Currently FSNs are only allowed for en or en-us. "
                    + "<br>Please add gb dialect variants as preferred terms.");
        } else {
            throw new InvalidCAB("Dialect not supported: " + dialect.getFormatedLanguageCode());
        }
    }

    /**
     * Updates an the text associated with the specified fully specified name description blueprint. Removes
     * previous dialect refexes associated with the FSN blueprint and remakes them with the updated text.
     *
     * @param newFullySpecifiedName the new text to use for the update
     * @param fullySpecifiedNameBlueprint the FSN description blueprint to update
     * @param dialect language code of FSN dialect, leave null if dialect isn't changing
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public void updateFullySpecifiedName(String newFullySpecifiedName, DescriptionCAB fullySpecifiedNameBlueprint, LanguageCode dialect) throws
            NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        String oldText = fullySpecifiedNameBlueprint.getText();
        fsns.remove(oldText);
        fsns.add(newFullySpecifiedName);
        this.recomputeUuid();
        fullySpecifiedNameBlueprint.setText(newFullySpecifiedName);
        if (dialect != null) {
            List<RefexCAB> annotationBlueprints = fullySpecifiedNameBlueprint.getAnnotationBlueprints();
            for (RefexCAB annot : annotationBlueprints) {
                if (annot.getRefexCollectionUuid().equals(usRefexUuid) || annot.getRefexCollectionUuid().equals(gbRefexUuid)) {
                    annotationBlueprints.remove(annot);
                }
            }
            fullySpecifiedNameBlueprint.replaceAnnotationBlueprints(annotationBlueprints);
            addFullySpecifiedNameDialectRefexes(fullySpecifiedNameBlueprint, dialect);
        }
    }

    /**
     * Gets the uuid of isa type to use for the parent relationships associated with this concept blueprint.
     *
     * @return the isa type uuid
     */
    public UUID getIsaType() {
        return isaType;
    }

    /**
     * Sets the uuid of isa type to use for the parent relationships associated with this concept blueprint.
     *
     * @param isaTypeUuid the isa type uuid
     */
    public void setIsaType(UUID isaTypeUuid) {
        this.isaType = isaTypeUuid;
        setComponentUuid(computeComponentUuid());

    }

    /**
     * Gets a two character abbreviation of the language of the descriptions associated with this concept
     * blueprint.
     *
     * @return a two character abbreviation of the language of the descriptions
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the language of the descriptions associated with this concept blueprint.
     *
     * @param lang a two character abbreviation of the language of the descriptions
     */
    public void setLang(String lang) {
        this.lang = lang;
        setComponentUuid(computeComponentUuid());
    }

    /**
     * Gets the text of the preferred name description associated with this concept blueprint.
     *
     * @return the preferred name text
     */
    public String getPreferredName() { //@akf todo : update to use set when NewConcept, etc. has been updated
        return preferredName;
    }

    /**
     * Sets the text of the preferred name associated with this concept blueprint.
     *
     * @param preferredName the new preferred name text
     */
    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        setComponentUuid(computeComponentUuid());
    }

    /**
     * Adds a description blueprint to use for the preferred name description associated with this concept
     * blueprint. Recomputes the uuid associated with this concept based on the updated preferred name text.
     * Adds the appropriate language/dialect refexes based on the given dialect code (only supports en-us and
     * en-gb). This method does not remove existing preferred name blueprints that are already associated with
     * this concept blueprint.
     *
     * @param perferredNameBlueprint the description blueprint for the preferred name description
     * @param dialect the language code representing the dialect of the preferred term
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public void addPreferredName(DescriptionCAB perferredNameBlueprint, LanguageCode dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        prefNames.add(perferredNameBlueprint.getText());
        this.recomputeUuid();
        addPreferredNameDialectRefexes(perferredNameBlueprint, dialect);
    }

    /**
     * Adds the appropriate dialect refexes to the preferred name description blueprint.
     *
     * @param preferredBlueprint the preferred name description blueprint
     * @param dialect the dialect of the preferred name, only supports en-gb and en-us
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    private void addPreferredNameDialectRefexes(DescriptionCAB preferredBlueprint, LanguageCode dialect) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        RefexCAB usAnnot;
        RefexCAB gbAnnot;
        if (dialect == LanguageCode.EN) {
            usAnnot = new RefexCAB(RefexType.CID,
                    preferredBlueprint.getComponentUuid(),
                    usRefexUuid, idDirective, refexDirective);
            usAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            if (moduleUuid != null) {
                usAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }

            gbAnnot = new RefexCAB(RefexType.CID,
                    preferredBlueprint.getComponentUuid(),
                    gbRefexUuid, idDirective, refexDirective);
            gbAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            if (moduleUuid != null) {
                gbAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }
            preferredBlueprint.addAnnotationBlueprint(usAnnot);
            preferredBlueprint.addAnnotationBlueprint(gbAnnot);
        } else if (dialect == LanguageCode.EN_US) {
            usAnnot = new RefexCAB(RefexType.CID,
                    preferredBlueprint.getComponentUuid(),
                    usRefexUuid, idDirective, refexDirective);
            usAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            if (moduleUuid != null) {
                usAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }
            preferredBlueprint.addAnnotationBlueprint(usAnnot);
        } else if (dialect == LanguageCode.EN_GB) {
            gbAnnot = new RefexCAB(RefexType.CID,
                    preferredBlueprint.getComponentUuid(),
                    gbRefexUuid, idDirective, refexDirective);
            gbAnnot.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRfx.getDESC_PREFERRED_NID());
            if (moduleUuid != null) {
                gbAnnot.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }
            preferredBlueprint.addAnnotationBlueprint(gbAnnot);
        } else {
            throw new InvalidCAB("Dialect not supported: " + dialect.getFormatedLanguageCode());
        }
    }

    /**
     * Updates an the text associated with the specified preferred name description blueprint. Removes
     * previous dialect refexes associated with the preferred name blueprint and remakes them with the updated
     * text.
     *
     * @param newPreferredName the new text to use for the update
     * @param preferredNameBlueprint the preferred name description blueprint to update
     * @param dialect language code of preferred name dialect, leave null if dialect isn't changing
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public void updatePreferredName(String newPreferredName, DescriptionCAB preferredNameBlueprint, LanguageCode dialect) throws
            NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        String oldText = preferredNameBlueprint.getText();
        prefNames.remove(oldText);
        prefNames.add(newPreferredName);
        this.recomputeUuid();
        preferredNameBlueprint.setText(newPreferredName);
        if (dialect != null) {
            List<RefexCAB> annotationBlueprints = preferredNameBlueprint.getAnnotationBlueprints();
            for (RefexCAB annot : annotationBlueprints) {
                if (annot.getRefexCollectionUuid().equals(usRefexUuid) || annot.getRefexCollectionUuid().equals(gbRefexUuid)) {
                    annotationBlueprints.remove(annot);
                }
            }
            preferredNameBlueprint.replaceAnnotationBlueprints(annotationBlueprints);
            addPreferredNameDialectRefexes(preferredNameBlueprint, dialect);
        }
    }

    /**
     * Checks if this concept blueprint is marked as defined.
     *
     * @return <code>true</code>, if the concept is defined
     */
    public boolean isDefined() {
        return defined;
    }

    /**
     * Marks this concept blueprint as defined
     *
     * @param defined set to <code>true</code> if the concept is defined
     */
    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    /**
     * Checks if the descriptions associated with this concept are marked as initial case sensitive.
     *
     * @return <code>true</code>, if the descriptions are initial case sensitive
     */
    public boolean isInitialCaseSensitive() {
        return initialCaseSensitive;
    }

    /**
     * Marks the descriptions associated with this concept are marked as initial case sensitive.
     *
     * @param initialCaseSensitive set to <code>true</code> to mark the descriptions as initial case sensitive
     */
    public void setInitialCaseSensitive(boolean initialCaseSensitive) {
        this.initialCaseSensitive = initialCaseSensitive;
    }

    /**
     * Generates a description blueprint representing the fully specified name of this blueprint.
     *
     * @param idDirective
     * @return a description blueprint representing the fully specified name
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public DescriptionCAB makeFullySpecifiedNameCAB(IdDirective idDirective)
            throws IOException, InvalidCAB, ContradictionException {
        //get rf1/rf2 concepts
        UUID fsnUuid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()[0];
        DescriptionCAB blueprint = new DescriptionCAB(
                getComponentUuid(),
                fsnUuid,
                LanguageCode.getLangCode(lang),
                getFullySpecifiedName(),
                isInitialCaseSensitive(),
                idDirective);
        if (moduleUuid != null) {
            blueprint.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
        }
        return blueprint;
    }

    /**
     * Generates a description blueprint representing the preferred name of this concept blueprint.
     *
     * @param idDirective
     * @return a description blueprint representing the preferred name
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public DescriptionCAB makePreferredCAB(IdDirective idDirective)
            throws IOException, InvalidCAB, ContradictionException {
        //get rf1/rf2 concepts
        UUID synUuid = SnomedMetadataRf2.SYNONYM_RF2.getUuids()[0];
        DescriptionCAB blueprint = new DescriptionCAB(
                getComponentUuid(),
                synUuid,
                LanguageCode.getLangCode(lang),
                getPreferredName(),
                isInitialCaseSensitive(),
                idDirective);
        if (moduleUuid != null) {
            blueprint.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
        }
        return blueprint;
    }

    /**
     * Generates relationship blueprints representing the parent relationships of this concept blueprint.
     *
     * @return a list of relationship blueprints representing the parent relationships
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public List<RelationshipCAB> getParentCABs() throws IOException, InvalidCAB, ContradictionException {
        List<RelationshipCAB> parentCabs = new ArrayList<>(getParents().size());
        for (UUID parentUuid : parents) {
            RelationshipCAB parent = new RelationshipCAB(
                    getComponentUuid(),
                    isaType,
                    parentUuid,
                    0,
                    RelationshipType.STATED_HIERARCHY,
                    idDirective);
            if (moduleUuid != null) {
                parent.properties.put(ComponentProperty.MODULE_ID, moduleUuid);
            }
            parentCabs.add(parent);
        }
        return parentCabs;
    }

    /**
     * Returns a list of the fully specified name blueprints associated with this concept blueprint. If no FSN
     * blueprints are associated, one will be generated based on the associated FSN text.
     *
     * @return a list of fully specified name blueprints
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public List<DescriptionCAB> getFullySpecifiedNameCABs() throws IOException, InvalidCAB, ContradictionException {
        if (fsnCABs.isEmpty()) {
            fsnCABs.add(makeFullySpecifiedNameCAB(idDirective));
        }
        return fsnCABs;
    }

    /**
     * Gets a list of the preferred name blueprints associated with this concept blueprint. If no preferred
     * name blueprints are associated, one will be generated based on the associated preferred name text.
     *
     * @return a list of preferred name blueprints
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public List<DescriptionCAB> getPreferredNameCABs() throws IOException, InvalidCAB, ContradictionException {
        if (prefCABs.isEmpty()) {
            prefCABs.add(makePreferredCAB(idDirective));
        }
        return prefCABs;
    }

    /**
     * Gets the description blueprints associated with this concept blueprint.
     *
     * @return a list of description blueprints
     */
    public List<DescriptionCAB> getDescriptionCABs() {
        return descCABs;
    }

    /**
     * Gets a list of relationship blueprints associated with this concept blueprint. If not relationships
     * blueprints are associated, they will be generated for the relationships to the associated parent
     * concepts.
     *
     * @return a list of parent relationship blueprints
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public List<RelationshipCAB> getRelationshipCABs() throws IOException, InvalidCAB, ContradictionException {
        List<RelationshipCAB> parentCABs = getParentCABs();
        for (RelationshipCAB parentBp : parentCABs) {
            if (!relCABs.contains(parentBp)) {
                relCABs.add(parentBp);
            }
        }
        return relCABs;
    }

    /**
     * Gets the media blueprints associated with this concept blueprint.
     *
     * @return a list of media blueprints
     */
    public List<MediaCAB> getMediaCABs() {
        return mediaCABs;
    }

    /**
     * Gets the concept attribute blueprint associated with this concept blueprint.
     *
     * @return the concept attribute blueprint
     */
    public ConceptAttributeAB getConceptAttributeAB() {
        if (conAttrAB == null) {
            try {
                conAttrAB = new ConceptAttributeAB(getComponentUuid(), defined, null, null, refexDirective, idDirective);
            } catch (IOException | InvalidCAB | ContradictionException ex) {
                throw new RuntimeException(ex);
            }
        }
        return conAttrAB;
    }

    /**
     * Adds a fully specified name description blueprint to the list of description blueprints associated with
     * this concept blueprint.
     *
     * @param fullySpecifiedNameBlueprint the fully specified name blueprint to add
     */
    public void addFullySpecifiedNameCAB(DescriptionCAB fullySpecifiedNameBlueprint) {
        fsnCABs.add(fullySpecifiedNameBlueprint);
    }

    /**
     * Adds a preferred name description blueprint to the list of description blueprints associated with this
     * concept blueprint.
     *
     * @param preferredNameBlueprint the preferred name blueprint to add
     */
    public void addPreferredNameCAB(DescriptionCAB preferredNameBlueprint) {
        prefCABs.add(preferredNameBlueprint);
    }

    /**
     * Adds a description blueprint to the list of description blueprints associated with this concept
     * blueprint.
     *
     * @param descriptionBlueprint the description blueprint to add
     */
    public void addDescriptionCAB(DescriptionCAB descriptionBlueprint) {
        descCABs.add(descriptionBlueprint);
    }

    /**
     * Adds a relationship blueprint to the list of relationship blueprints associated with this concept
     * blueprint.
     *
     * @param relationshipBlueprint the relationship blueprint to add
     */
    public void setRelationshipCAB(RelationshipCAB relationshipBlueprint) {
        relCABs.add(relationshipBlueprint);
    }

    /**
     * Adds a media blueprint to the list of media blueprints associated with this concept blueprint.
     *
     * @param mediaBlueprint the media blueprint to add
     */
    public void addMediaCAB(MediaCAB mediaBlueprint) {
        mediaCABs.add(mediaBlueprint);
    }

    /**
     * Adds a concept attribute blueprint to the list of concept attribute blueprints associated with this
     * concept blueprint.
     *
     * @param conceptAttributeBlueprint the concept attribute blueprint to add
     */
    public void setConceptAttributeAB(ConceptAttributeAB conceptAttributeBlueprint) {
        this.conAttrAB = conceptAttributeBlueprint;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public boolean isAnnotationRefexExtensionIdentity() {
        return annotationRefexExtensionIdentity;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public void setAnnotationRefexExtensionIdentity(boolean annotationRefexExtensionIdentity) {
        this.annotationRefexExtensionIdentity = annotationRefexExtensionIdentity;
    }
}
