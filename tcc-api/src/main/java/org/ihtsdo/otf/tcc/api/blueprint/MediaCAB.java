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
import java.util.Arrays;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.media.MediaChronicleBI;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;

/**
 * The Class MediaCAB contains methods for creating a media blueprint. This
 * blueprint can be constructed into a type of
 * <code>MediaChronicleBI</code>. This is the preferred method for updating or
 * creating new descriptions.
 *
 * @see TerminologyBuilderBI
 * @see MediaChronicleBI
 *
 */
public class MediaCAB extends CreateOrAmendBlueprint {

    public static final UUID mediaSpecNamespace =
            UUID.fromString("743f0510-5285-11e0-b8af-0800200c9a66");
    private UUID conceptUuid;
    private UUID typeUuid;
    public String format;
    public String textDescription;
    public byte[] dataBytes;

    /**
     * Instantiates a new media blueprint using nid values.
     *
     * @param conceptNid the nid of the enclosing concept
     * @param typeNid the nid representing the type of media
     * @param format a string describing the media format
     * @param textDescription a string describing the media
     * @param dataBytes the data bytes representing the media
     * @param idDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public MediaCAB(
            int conceptNid, int typeNid, String format, String textDescription,
            byte[] dataBytes, IdDirective idDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimordialUuid(),
                Ts.get().getComponent(typeNid).getPrimordialUuid(),
                format, textDescription, dataBytes, idDirective);
    }

    /**
     * Instantiates a new media blueprint using uuid values.
     *
     * @param conceptUuid the uuid of the enclosing concept
     * @param typeUuid the uuid representing the type of media
     * @param format a string describing the media format
     * @param textDescription a string describing the media
     * @param dataBytes the data bytes representing the media
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public MediaCAB(
            UUID conceptUuid, UUID typeUuid, String format, String textDescription,
            byte[] dataBytes, IdDirective idDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(conceptUuid, typeUuid, format, textDescription, dataBytes,
                null, null, null, idDirective, RefexDirective.EXCLUDE);
    }

    /**
     * Instantiates a new media blueprint using nid values and a given
     * <code>mediaVersion</code>.
     *
     * @param conceptNid the nid of the enclosing concept
     * @param typeNid the nid representing the type of media
     * @param format a string describing the media format
     * @param textDescription a string describing the media
     * @param dataBytes the data bytes representing the media
     * @param mediaVersion the media version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @param idDirective 
     * @param refexDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public MediaCAB(
            int conceptNid, int typeNid, 
            String format, 
            String textDescription,
            byte[] dataBytes, 
            MediaVersionBI mediaVersion, 
            ViewCoordinate viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimordialUuid(),
                Ts.get().getComponent(typeNid).getPrimordialUuid(),
                format, textDescription, dataBytes, mediaVersion, viewCoordinate,
                idDirective, refexDirective);
    }

    /**
     * Instantiates a new media blueprint using uuid values and a given
     * <code>mediaVersion</code>.
     *
     * @param conceptUuid the uuid of the enclosing concept
     * @param typeUuid the uuid representing the type of media
     * @param format a string describing the media format
     * @param textDescription a string describing the media
     * @param dataBytes the data bytes representing the media
     * @param mediaVersion the media version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @param idDirective 
     * @param refexDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public MediaCAB(
            UUID conceptUuid, UUID typeUuid, String format, String textDescription,
            byte[] dataBytes, MediaVersionBI mediaVersion, ViewCoordinate viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(conceptUuid, typeUuid, format, textDescription, dataBytes,
                null, mediaVersion, viewCoordinate, idDirective, refexDirective);
    }

    /**
     * Instantiates a new media blueprint using uuid values and a given
     * <code>mediaVersion</code>. Can specify a uuid to represent the media
     * component.
     *
     * @param conceptUuid the uuid of the enclosing concept
     * @param typeUuid the uuid representing the type of media
     * @param format a string describing the media format
     * @param textDescription a string describing the media
     * @param dataBytes the data bytes representing the media
     * @param componentUuid the uuid to represent the media component
     * @param mediaVersion the media version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @param idDirective 
     * @param refexDirective 
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public MediaCAB(
            UUID conceptUuid, UUID typeUuid, String format, String textDescription,
            byte[] dataBytes, UUID componentUuid, MediaVersionBI mediaVersion,
            ViewCoordinate viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective) throws IOException, InvalidCAB, ContradictionException {
        super(getComponentUUID(componentUuid,mediaVersion,idDirective), 
                mediaVersion, viewCoordinate, idDirective, refexDirective);

        this.conceptUuid = conceptUuid;
        this.typeUuid = typeUuid;
        this.format = format;
        this.textDescription = textDescription;
        this.dataBytes = dataBytes;
        if (getComponentUuid() == null) {
            try {
                recomputeUuid();
            } catch (IOException | InvalidCAB | NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Computes the uuid for this media based on the enclosing concept uuid and
     * the data bytes. Recomputes the uuids for dependent annotations.
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
    public void recomputeUuid() throws NoSuchAlgorithmException, IOException, InvalidCAB, ContradictionException {
        switch (idDirective) {
            case PRESERVE_CONCEPT_REST_HASH:
            case GENERATE_RANDOM_CONCEPT_REST_HASH:
            case GENERATE_HASH:
            case GENERATE_REFEX_CONTENT_HASH:
                setComponentUuid(
                        UuidT5Generator.get(mediaSpecNamespace,
                        getPrimoridalUuidString(conceptUuid)
                        + dataBytes));
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
     * Gets the media type uuid associated with this media blueprint.
     *
     * @return the media type uuid
     */
    public UUID getTypeUuid() {
        return typeUuid;
    }

    /**
     * Gets the media type nid associated with this media blueprint.
     *
     * @return the media type nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getTypeNid() throws IOException {
        return Ts.get().getNidForUuids(typeUuid);
    }

    /**
     * Gets the enclosing concept nid associated with this media blueprint.
     *
     * @return the enclosing concept nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getConceptNid() throws IOException {
        return Ts.get().getNidForUuids(conceptUuid);
    }

    /**
     * Gets the enclosing concept uuid associated with this media blueprint.
     *
     * @return the enclosing concept uuid
     */
    public UUID getConceptUuid() {
        return conceptUuid;
    }

    /**
     * Gets the data bytes representing the media associated with this media type.
     *
     * @return the media data bytes
     */
    public byte[] getDataBytes() {
        return dataBytes;
    }

    /**
     * Gets the string representing the format associated with this media type.
     *
     * @return the media format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Gets a text description of the media associated with this media blueprint.
     *
     * @return the text description of the media
     */
    public String getTextDescription() {
        return textDescription;
    }

    /**
     * Sets the enclosing concept uuid associated with this media blueprint.
     *
     * @param conceptNewUuid the enclosing concept uuid
     */
    protected void setConceptUuid(UUID conceptNewUuid) {
        this.conceptUuid = conceptNewUuid;
    }

    /**
     * Validates this media blueprint's fields against the given
     * <code>mediaVersion</code>. Compares the status nid, component nid,
     * enclosing concept nid, media type nid, format, description, and bytes.
     *
     * @param mediaVersion the media version to use for validation
     * @return <code>true</code>, if this media blueprint's fields are
     * equal to the specified media version
     * @throws IOException signals that an I/O exception has occurred
     */
    public boolean validate(MediaVersionBI mediaVersion) throws IOException {
        if (mediaVersion.getStatus() != getStatus()) {
            return false;
        }
        if (mediaVersion.getNid() != getComponentNid()) {
            return false;
        }
        if (mediaVersion.getConceptNid() != getConceptNid()) {
            return false;
        }
        if (mediaVersion.getTypeNid() != getTypeNid()) {
            return false;
        }
        if (!mediaVersion.getFormat().equals(getFormat())) {
            return false;
        }
        if (!mediaVersion.getTextDescription().equals(getTextDescription())) {
            return false;
        }
        if (!Arrays.equals(mediaVersion.getMedia(), getDataBytes())) {
            return false;
        }
        return true;
    }
}
