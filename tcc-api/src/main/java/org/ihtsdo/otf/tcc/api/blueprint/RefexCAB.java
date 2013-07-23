/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.AnalogBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.AUTHOR_ID;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.COMPONENT_EXTENSION_1_ID;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.COMPONENT_EXTENSION_2_ID;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.COMPONENT_EXTENSION_3_ID;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.COMPONENT_ID;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.MODULE_ID;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.PATH_ID;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.REFERENCED_COMPONENT_ID;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.REFEX_EXTENSION_ID;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.STATUS;
import static org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty.TIME_IN_MS;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRfx;
import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_float.RefexFloatAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_float.RefexFloatVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;

/**
 * The Class RefexCAB contains methods for creating a media blueprint. This blueprint can be constructed into
 * a type of
 * <code>MediaChronicleBI</code>. This is the preferred method for updating or creating new descriptions.
 *
 * @see TerminologyBuilderBI
 * @see RefexChronicleBI
 *
 */
public class RefexCAB extends CreateOrAmendBlueprint {

    public static final UUID refexSpecNamespace =
            UUID.fromString("c44bc030-1166-11e0-ac64-0800200c9a66");
    private RefexType memberType;

    /**
     * Computes the uuid of the refex member based on the member type, refex collection, and referenced
     * component. Should be used when there is a 1-1 relationship between the refex collection and the
     * referenced component. Otherwise use
     * <code>computeMemberContentUuid()</code>.
     *
     * @return the uuid of the refex member
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     */
    private UUID computeMemberComponentUuid() throws IOException, InvalidCAB {
        try {
            UUID memberComponentUuid = UuidT5Generator.get(refexSpecNamespace,
                    memberType.name()
                    + getPrimordialUuidStringForNidProp(ComponentProperty.REFEX_EXTENSION_ID)
                    + getReferencedComponentUuid().toString());
            properties.put(ComponentProperty.COMPONENT_ID, memberComponentUuid);
            return memberComponentUuid;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Computes the uuid of the refex member and sets the member uuid property. Uses
     * <code>computeMemberContentUuid()</code> to compute the uuid.
     *
     * @return the uuid of the refex member
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws IOException signals that an I/O exception has occurred
     */
    public UUID setMemberContentUuid() throws InvalidCAB, IOException {
        UUID memberContentUuid = computeMemberContentUuid();
        properties.put(ComponentProperty.COMPONENT_ID, memberContentUuid);
        return memberContentUuid;
    }

    /**
     * Computes the uuid of a the refex member based on the refex properties. Use when the 1-1 relationship
     * between a refex and a referenced component does not apply.
     *
     * @return A <code>UUID</code> based on a Type 5 generator that uses the content fields of the refex.
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws IOException signals that an I/O exception has occurred
     */
    private UUID computeMemberContentUuid() throws InvalidCAB, IOException {
        try {
            StringBuilder sb = new StringBuilder();
            for (ComponentProperty prop : ComponentProperty.values()) {
                switch (prop) {
                    case COMPONENT_ID:
                    case STATUS:
                    case REFEX_EXTENSION_ID:
                    case REFERENCED_COMPONENT_ID:
                        break;
                    default:
                        if (properties.get(prop) != null) {
                            sb.append(properties.get(prop).toString());
                        }
                }
            }
            return UuidT5Generator.get(refexSpecNamespace,
                    memberType.name()
                    + getPrimordialUuidStringForNidProp(ComponentProperty.REFEX_EXTENSION_ID)
                    + getPrimordialUuidStringForNidProp(ComponentProperty.REFERENCED_COMPONENT_ID)
                    + sb.toString());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Recomputes the refex member uuid. Component
     *
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    @Override
    public void recomputeUuid()
            throws InvalidCAB, IOException, ContradictionException {
        switch (idDirective) {
            case PRESERVE_CONCEPT_REST_HASH:
            case GENERATE_RANDOM_CONCEPT_REST_HASH:
            case GENERATE_HASH:
                if (getReferencedComponentUuid() != null) {
                    setComponentUuidNoRecompute(computeMemberComponentUuid());
                }
                break;
            case GENERATE_REFEX_CONTENT_HASH:
                if (getReferencedComponentUuid() != null) {
                    setComponentUuidNoRecompute(computeMemberContentUuid());
                }
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
     * Gets a string representing the primordial uuid of the specified nid-based
     * <code>refexProperty</code>.
     *
     * @param refexProperty the refexProperty representing the nid-bsed property
     * @return a String representing the primordial uuid of the refex property
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     */
    private String getPrimordialUuidStringForNidProp(ComponentProperty refexProperty)
            throws IOException, InvalidCAB {
        Object idObj = properties.get(refexProperty);
        if (idObj == null) {
            throw new InvalidCAB(
                    "No data for: " + refexProperty);
        }
        if (idObj instanceof UUID) {
            return ((UUID) idObj).toString();
        }
        int nid = (Integer) idObj;
        ComponentBI component = Ts.get().getComponent(nid);
        if (component != null) {
            return component.getPrimordialUuid().toString();
        }
        List<UUID> uuids = Ts.get().getUuidsForNid(nid);
        if (uuids.size() == 1) {
            return uuids.get(0).toString();
        }
        throw new InvalidCAB("Can't find nid for: " + refexProperty
                + " props: " + this.properties);
    }

    /**
     * Instantiates a new refex blueprint using nid values. This constructor creates a refex member uuid that
     * is computed from a type 5 UUID generator that uses a hash of the
     * <code>memberType</code>,
     * <code>referencedComponentNid</code>, and
     * <code>collectionNid</code>. This member ID is suitable for all refex collections where there should be
     * no more than one refex member per referenced component.
     *
     * @param memberType the refex member type
     * @param referencedComponentNid the nid of the referenced component
     * @param collectionNid the nid of the refex collection concept
     * @param idDirective - typically IdDirective.GENERATE_HASH
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public RefexCAB(RefexType memberType,
            int referencedComponentNid,
            int collectionNid,
            IdDirective idDirective,
            RefexDirective refexDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(memberType,
                Ts.get().getUuidPrimordialForNid(referencedComponentNid),
                collectionNid,
                null, null, null,
                idDirective,
                refexDirective);
        recomputeUuid();
        this.properties.put(ComponentProperty.COMPONENT_ID,
                getComponentUuid());
    }

    public RefexCAB(RefexType memberType,
            UUID referencedComponentUUID,
            UUID collectionUuid,
            IdDirective idDirective,
            RefexDirective refexDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(memberType,
                referencedComponentUUID,
                Ts.get().getNidForUuids(collectionUuid),
                null, null, null,
                idDirective,
                refexDirective);
        recomputeUuid();
        this.properties.put(ComponentProperty.COMPONENT_ID,
                getComponentUuid());
    }

    /**
     * Instantiates a new refex blueprint using nid values and a given
     * <code>refexVersion</code>. This constructor creates a refex member uuid that is computed from a type 5
     * UUID generator that uses a hash of the
     * <code>memberType</code>,
     * <code>referencedComponentNid</code>, and
     * <code>collectionNid</code>. This member ID is suitable for all refex collections where there should be
     * no more than one refex member per referenced component.
     *
     * @param memberType the refex member type
     * @param referencedComponentNid the nid of the referenced component
     * @param collectionNid the nid of the refex collection concept
     * @param refexVersion the refex version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are active and inactive
     * @param idDirective - typically IdDirective.GENERATE_HASH
     * @param refexDirective
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public RefexCAB(
            RefexType memberType,
            UUID referencedComponentUuid,
            int collectionNid,
            RefexVersionBI refexVersion,
            ViewCoordinate viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective)
            throws IOException, InvalidCAB, ContradictionException {
        this(memberType, referencedComponentUuid, collectionNid, null, refexVersion, viewCoordinate, idDirective, refexDirective);
        recomputeUuid();
        this.properties.put(ComponentProperty.COMPONENT_ID,
                getComponentUuid());
    }

    /**
     * Instantiates a new refex blueprint using nid values and a given
     * <code>refexVersion</code>. Uses the given
     * <code>memberUuid</code> as the refex member uuid.
     *
     * @param memberType the refex member type
     * @param referencedComponentNid the nid of the referenced component
     * @param collectionNid the nid of the refex collection concept
     * @param memberUuid the uuid of the refex member
     * @param refexVersion the refex version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are active and inactive
     * @param idDirective - typically IdDirective.GENERATE_HASH
     * @param refexDirective
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view
     * coordinate
     */
    public RefexCAB(RefexType memberType,
            UUID referencedComponentUuid,
            int collectionNid,
            UUID memberUuid,
            RefexVersionBI refexVersion,
            ViewCoordinate viewCoordinate,
            IdDirective idDirective,
            RefexDirective refexDirective)
            throws IOException, InvalidCAB, ContradictionException {
        super(memberUuid, refexVersion, viewCoordinate, idDirective, refexDirective);
        this.memberType = memberType;
        this.properties.put(ComponentProperty.REFERENCED_COMPONENT_ID, referencedComponentUuid);
        this.properties.put(ComponentProperty.REFEX_EXTENSION_ID, collectionNid);
        this.properties.put(ComponentProperty.STATUS,
                Status.ACTIVE);
        if (getMemberUUID() != null) {
            this.properties.put(ComponentProperty.COMPONENT_ID, memberUuid);
        }
        if (this.properties.get(ComponentProperty.STATUS) != null) {
            setStatus( (Status) this.properties.get(ComponentProperty.STATUS));
        }
    }


    /**
     * Sets the uuid for the referenced component associated with this refex blueprint.
     *
     * @param referencedComponentUuid the uuid of the referenced component
     */
    public void setReferencedComponentUuid(UUID referencedComponentUuid) {
        this.properties.put(ComponentProperty.REFERENCED_COMPONENT_ID, referencedComponentUuid);
    }

    /**
     * Sets the refex member uuid associated with this refex blueprint.
     *
     * @param memberUuid the refex member uuid
     */
    public void setMemberUuid(UUID memberUuid) {
        setComponentUuid(memberUuid);
        properties.put(ComponentProperty.COMPONENT_ID, memberUuid);
    }

    /**
     * Checks if the refex properties contain the specified property.
     *
     * @param key the refex property in question
     * @return <code>true</code>, if the refex properties contain the specified property
     */
    public boolean containsKey(ComponentProperty key) {
        return properties.containsKey(key);
    }

    /**
     * Gets a set of refex properties mapped to their corresponding values.
     *
     * @return a set of refex properties mapped to their corresponding values
     */
    public Set<Entry<ComponentProperty, Object>> entrySet() {
        return properties.entrySet();
    }

    /**
     * Gets the refex properties.
     *
     * @return a set of refex properties
     */
    public Set<ComponentProperty> keySet() {
        return properties.keySet();
    }

    /**
     * Maps the given Number
     * <code>value</code> to the specified refex property
     * <code>key</code>.
     *
     * @param key the refex property
     * @param value the value to associate with the refex property
     * @return the previous value associated with the specified * key, <code>null</code> if no value was
     * previously associated
     */
    public Object put(ComponentProperty key, Number value) {
        return properties.put(key, value);
    }

    public Object put(ComponentProperty key, ConceptSpec spec) {
        return put(key, spec.getUuids()[0]);
    }

    /**
     * Puts the given
     * <code>stringValue</code> in the
     * <code>ComponentProperty.STRING1</code>. Will throw an assertion error if a different property is used
     * for the key.
     *
     * @param key ComponentProperty.STRING1
     * @param stringValue the string to associate with this refex blueprint
     * @return the previous value associated with the specified * key, <code>null</code> if no value was
     * previously associated
     */
    public Object put(ComponentProperty key, String stringValue) {
        assert key == ComponentProperty.STRING_EXTENSION_1;
        return properties.put(key, stringValue);
    }

    /**
     * Puts the given
     * <code>booleanValue</code> in the
     * <code>ComponentProperty.BOOLEAN1</code>. Will throw an assertion error if a different property is used
     * for the key.
     *
     * @param key ComponentProperty.BOOLEAN1
     * @param booleanValue the boolean to associate with this refex blueprint
     * @return the previous value associated with the specified * key, <code>null</code> if no value was
     * previously associated
     */
    public Object put(ComponentProperty key, Boolean booleanValue) {
        assert key == ComponentProperty.BOOLEAN_EXTENSION_1;
        return properties.put(key, booleanValue);
    }

    /**
     * Puts the given
     * <code>uuidValue</code> in a uuid based refex property. Will throw an assertion error if the property
     * used for the key is not one of the following:
     * <code>ComponentProperty.MEMBER_UUID</code>,
     * <code>ComponentProperty.UUID1</code>,
     * <code>ComponentProperty.UUID2</code>, or
     * <code>ComponentProperty.UUID3</code>.
     *
     * @param key a uuid based refex property
     * @param uuidValue the uuid to associate with this refex blueprint
     * @return the previous value associated with the specified * key, <code>null</code> if no value was
     * previously associated
     */
    public Object put(ComponentProperty key, UUID uuidValue) {
        assert key == ComponentProperty.COMPONENT_ID
                || key == ComponentProperty.ENCLOSING_CONCEPT_ID
                || key == ComponentProperty.AUTHOR_ID
                || key == ComponentProperty.MODULE_ID
                || key == ComponentProperty.PATH_ID
                || key == ComponentProperty.REFERENCED_COMPONENT_ID
                || key == ComponentProperty.REFEX_EXTENSION_ID
                || key == ComponentProperty.COMPONENT_EXTENSION_1_ID
                || key == ComponentProperty.COMPONENT_EXTENSION_2_ID
                || key == ComponentProperty.COMPONENT_EXTENSION_3_ID;
        return properties.put(key,
                uuidValue);
    }

    /**
     * Puts the given
     * <code>arrayOfByteArray</code> in the
     * <code>ComponentProperty.ARRAY_BYTEARRAY</code>. Will throw an assertion error if a different property
     * is used for the key.
     *
     * @param key ComponentProperty.ARRAY_BYTEARRAY
     * @param arrayOfByteArray the array of byte array to associate with this refex blueprint
     * @return the previous value associated with the specified * key, <code>null</code> if no value was
     * previously associated
     */
    public Object put(ComponentProperty key, byte[][] arrayOfByteArray) {
        assert key == ComponentProperty.ARRAY_OF_BYTEARRAY;
        return properties.put(ComponentProperty.ARRAY_OF_BYTEARRAY,
                arrayOfByteArray);
    }

    /**
     * Generates a string representation of this refex blueprint. Includes the refex member type and the
     * properties.
     *
     * @return a string representation of this refex blueprint
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append(" ");
        sb.append(memberType);
        sb.append(" COMPONENT_ID: ");
        sb.append(properties.get(COMPONENT_ID));
        TerminologyStoreDI s = Ts.get();
        for (Entry<ComponentProperty, Object> entry : properties.entrySet()) {
            if (entry.getKey() != COMPONENT_ID) {
                sb.append("  \n");
                sb.append(entry.getKey());
                sb.append(": ");
                if (s != null) {
                    sb.append(s.informAboutId(entry.getValue()));
                } else {
                    sb.append(entry.getValue());
                }
            }
        }

        return sb.toString();
    }

    /**
     * Maps the given number
     * <code>value</code> to the specified refex property
     * <code>key</code>. Returns this refex member with the new property.
     *
     * @param key a refex property
     * @param value a number value to associate with this refex member
     * @return this refex member blueprint with the new property
     */
    public RefexCAB with(ComponentProperty key, Number value) {
        put(key, value);
        return this;
    }

    /**
     * Puts the given
     * <code>stringValue</code> in the
     * <code>ComponentProperty.STRING1</code>. Will throw an assertion error if a different property is used
     * for the key. Returns this refex member blueprint with the new string property.
     *
     * @param key ComponentProperty.STRING1
     * @param stringValue the string to associate with this refex blueprint
     * @return this refex member blueprint with the new string property
     */
    public RefexCAB with(ComponentProperty key, String stringValue) {
        assert key == ComponentProperty.STRING_EXTENSION_1;
        properties.put(key, stringValue);
        return this;
    }

    /**
     * Puts the given
     * <code>booleanValue</code> in the
     * <code>ComponentProperty.BOOLEAN1</code>. Will throw an assertion error if a different property is used
     * for the key. Returns this refex member blueprint with the new boolean property.
     *
     * @param key ComponentProperty.BOOLEAN1
     * @param booleanValue the boolean to associate with this refex blueprint
     * @return this refex member blueprint with the new boolean property
     */
    public RefexCAB with(ComponentProperty key, Boolean booleanValue) {
        assert key == ComponentProperty.BOOLEAN_EXTENSION_1;
        properties.put(key, booleanValue);
        return this;
    }

    /**
     * Puts the given
     * <code>arrayOfByteArray</code> in the
     * <code>ComponentProperty.ARRAY_BYTEARRAY</code>. Will throw an assertion error if a different property
     * is used for the key. Returns this refex member blueprint with the new array of byte array property.
     *
     * @param key ComponentProperty.ARRAY_BYTEARRAY
     * @param arrayOfByteArray the array of byte array to associate with this refex blueprint
     * @return this refex member blueprint with the new array byte array property
     */
    public RefexCAB with(ComponentProperty key, byte[][] arrayOfByteArray) {
        assert key == ComponentProperty.ARRAY_OF_BYTEARRAY;
        properties.put(key, arrayOfByteArray);
        return this;
    }

    /**
     * Checks if the refex properties contain the specified property.
     *
     * @param key the refex property in question
     * @return <code>true</code>, if the refex properties contain the specified property
     */
    public boolean hasProperty(ComponentProperty key) {
        return properties.containsKey(key);
    }

    /**
     * Writes this refex member blueprint to the given
     * <code>refexAnalog</code>.
     *
     * @param refexAnalog the refex analog to write this refex blueprint to
     * @throws PropertyVetoException if the new value is not valid
     * @throws IOException signals that an I/O exception has occurred
     */
    public void writeTo(RefexAnalogBI<?> refexAnalog) throws PropertyVetoException, IOException {
        setProperties(refexAnalog);
    }

    /**
     * Sets the properties in the given
     * <code>refexAnalog</code> based on the properties of this refex blueprint.
     *
     * @param refexAnalog the refex analog to write this refex blueprint to
     * @throws PropertyVetoException if the new value is not valid
     * @throws IOException signals that an I/O exception has occurred
     */
    public void setProperties(RefexAnalogBI<?> refexAnalog) throws PropertyVetoException, IOException {
        for (Entry<ComponentProperty, Object> entry : properties.entrySet()) {
            switch (entry.getKey()) {
                case COMPONENT_ID:
                    refexAnalog.setNid(getInt(ComponentProperty.COMPONENT_ID));
                    break;
                case REFERENCED_COMPONENT_ID:
                    refexAnalog.setReferencedComponentNid(getInt(ComponentProperty.REFERENCED_COMPONENT_ID));
                    break;
                case BOOLEAN_EXTENSION_1:
                    RefexBooleanAnalogBI<?> booleanPart = (RefexBooleanAnalogBI<?>) refexAnalog;
                    booleanPart.setBoolean1((Boolean) entry.getValue());
                    break;
                case COMPONENT_EXTENSION_1_ID:
                    RefexNidAnalogBI<?> cv1part = (RefexNidAnalogBI<?>) refexAnalog;
                    cv1part.setNid1(getInt(ComponentProperty.COMPONENT_EXTENSION_1_ID));
                    break;
                case COMPONENT_EXTENSION_2_ID:
                    RefexNidNidAnalogBI<?> cv2part = (RefexNidNidAnalogBI<?>) refexAnalog;
                    cv2part.setNid2(getInt(ComponentProperty.COMPONENT_EXTENSION_2_ID));
                    break;
                case COMPONENT_EXTENSION_3_ID:
                    RefexNidNidNidAnalogBI<?> cv3part = (RefexNidNidNidAnalogBI<?>) refexAnalog;
                    cv3part.setNid3(getInt(ComponentProperty.COMPONENT_EXTENSION_3_ID));
                    break;
                case INTEGER_EXTENSION_1:
                    RefexIntAnalogBI<?> intPart = (RefexIntAnalogBI<?>) refexAnalog;
                    intPart.setInt1((Integer) entry.getValue());
                    break;
                case LONG_EXTENSION_1:
                    RefexLongAnalogBI<?> longPart = (RefexLongAnalogBI<?>) refexAnalog;
                    longPart.setLong1((Long) entry.getValue());
                    break;
                case STRING_EXTENSION_1:
                    RefexStringAnalogBI<?> strPart = (RefexStringAnalogBI<?>) refexAnalog;
                    strPart.setString1((String) entry.getValue());
                    break;
                case FLOAT_EXTENSION_1:
                    RefexFloatAnalogBI<?> floatPart = (RefexFloatAnalogBI<?>) refexAnalog;
                    floatPart.setFloat1((Float.parseFloat(entry.getValue().toString())));
                    break;
                case STATUS:
                    ((AnalogBI) refexAnalog).setStatus(getStatus());
                    break;
                case TIME_IN_MS:
                    ((AnalogBI) refexAnalog).setTime((Long) entry.getValue());
                    break;
                case AUTHOR_ID:
                    ((AnalogBI) refexAnalog).setAuthorNid(getInt(ComponentProperty.AUTHOR_ID));
                    break;
                case MODULE_ID:
                    ((AnalogBI) refexAnalog).setModuleNid(getInt(ComponentProperty.MODULE_ID));
                    break;
                case PATH_ID:
                    ((AnalogBI) refexAnalog).setPathNid(getInt(ComponentProperty.PATH_ID));
                    break;

                default:
                    throw new RuntimeException("Can't handle: " + entry.getKey());
            }
        }
    }

    /**
     * Sets the properties in the given
     * <code>refexAnalog</code> based on the properties of this refex blueprint. Does not set the status
     * property.
     *
     * @param refexAnalog the refex analog to write this refex blueprint to
     * @throws PropertyVetoException if the new value is not valid
     * @throws IOException signals that an I/O exception has occurred
     */
    public void setPropertiesExceptStamp(RefexAnalogBI<?> refexAnalog) throws PropertyVetoException, IOException {
        for (Entry<ComponentProperty, Object> entry : properties.entrySet()) {
            switch (entry.getKey()) {
                case COMPONENT_ID:
                    try {
                        int nid = Ts.get().getNidForUuids((UUID) entry.getValue());
                        if (refexAnalog.getNid() != nid) {
                            refexAnalog.setNid(nid);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case BOOLEAN_EXTENSION_1:
                    RefexBooleanAnalogBI<?> booleanPart = (RefexBooleanAnalogBI<?>) refexAnalog;
                    booleanPart.setBoolean1((Boolean) entry.getValue());
                    break;
                case REFEX_EXTENSION_ID:
                    refexAnalog.setRefexExtensionNid((Integer) entry.getValue());
                    break;
                case REFERENCED_COMPONENT_ID:
                    refexAnalog.setReferencedComponentNid(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    break;
                case COMPONENT_EXTENSION_1_ID:
                    RefexNidAnalogBI<?> c1Uuid = (RefexNidAnalogBI<?>) refexAnalog;
                    c1Uuid.setNid1(getInt(COMPONENT_EXTENSION_1_ID));
                    break;
                case COMPONENT_EXTENSION_2_ID:
                    RefexNidNidAnalogBI<?> c2Uuid = (RefexNidNidAnalogBI<?>) refexAnalog;
                    c2Uuid.setNid2(getInt(COMPONENT_EXTENSION_2_ID));
                    break;
                case COMPONENT_EXTENSION_3_ID:
                    RefexNidNidNidAnalogBI<?> c3Uuid = (RefexNidNidNidAnalogBI<?>) refexAnalog;
                    c3Uuid.setNid3(getInt(COMPONENT_EXTENSION_3_ID));
                    break;
                case INTEGER_EXTENSION_1:
                    RefexIntAnalogBI<?> intPart = (RefexIntAnalogBI<?>) refexAnalog;
                    intPart.setInt1((Integer) entry.getValue());
                    break;
                case LONG_EXTENSION_1:
                    RefexLongAnalogBI<?> longPart = (RefexLongAnalogBI<?>) refexAnalog;
                    longPart.setLong1((Long) entry.getValue());
                    break;
                case STRING_EXTENSION_1:
                    RefexStringAnalogBI<?> strPart = (RefexStringAnalogBI<?>) refexAnalog;
                    strPart.setString1((String) entry.getValue());
                    break;
                case ARRAY_OF_BYTEARRAY:
                    RefexArrayOfBytearrayAnalogBI<?> arrayPart = (RefexArrayOfBytearrayAnalogBI<?>) refexAnalog;
                    arrayPart.setArrayOfByteArray((byte[][]) entry.getValue());
                    break;
                case FLOAT_EXTENSION_1:
                    RefexFloatAnalogBI<?> floatPart = (RefexFloatAnalogBI<?>) refexAnalog;
                    floatPart.setFloat1((Float.parseFloat(entry.getValue().toString())));
                    break;
                case ENCLOSING_CONCEPT_ID:
                // Not setable 
                case STATUS:
                case TIME_IN_MS:
                case AUTHOR_ID:
                case MODULE_ID:
                case PATH_ID:
                    // STAMP property
                    break;
                default:
                    throw new RuntimeException("Can't handle: " + entry.getKey());
            }
        }
    }

    /**
     * Validates this refex blueprint's properties against the given
     * <code>refexVersion</code>.
     *
     * @param refexVersion the refex version
     * @return <code>true</code>, if this refex blueprint's properties are equal to the specified refex
     * version
     * @see RefexCAB.ComponentProperty
     */
    public boolean validate(RefexVersionBI<?> refexVersion) {
        if (memberType != null) {
            if (RefexType.classToType(refexVersion.getClass()) != memberType) {
                return false;
            }
        }
        for (Entry<ComponentProperty, Object> entry : properties.entrySet()) {
            switch (entry.getKey()) {
                case REFERENCED_COMPONENT_ID:
                    if (getInt(REFERENCED_COMPONENT_ID) != refexVersion.getReferencedComponentNid()) {
                        return false;
                    }
                    break;
                case REFEX_EXTENSION_ID:
                    if (getInt(REFEX_EXTENSION_ID) != refexVersion.getRefexExtensionNid()) {
                        return false;
                    }
                    break;
                case COMPONENT_ID:
                    if (refexVersion.getNid() != getInt(COMPONENT_ID)) {
                        return false;
                    }
                case BOOLEAN_EXTENSION_1:
                    if (!RefexBooleanVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexBooleanVersionBI<?> booleanPart = (RefexBooleanVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(booleanPart.getBoolean1())) {
                        return false;
                    }
                    break;
                case COMPONENT_EXTENSION_1_ID:
                    if (!RefexNidVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexNidVersionBI<?> c1part = (RefexNidVersionBI<?>) refexVersion;
                    if (c1part.getNid1() != getInt(COMPONENT_EXTENSION_1_ID)) {
                        return false;
                    }
                    break;
                case COMPONENT_EXTENSION_2_ID:
                    if (!RefexNidNidVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexNidNidVersionBI<?> c2part = (RefexNidNidVersionBI<?>) refexVersion;
                    if (getInt(COMPONENT_EXTENSION_2_ID) != c2part.getNid2()) {
                        return false;
                    }
                    break;
                case COMPONENT_EXTENSION_3_ID:
                    if (!RefexNidNidNidVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexNidNidNidVersionBI<?> c3part = (RefexNidNidNidVersionBI<?>) refexVersion;
                    if (getInt(COMPONENT_EXTENSION_3_ID) != c3part.getNid3()) {
                        return false;
                    }
                    break;

                case INTEGER_EXTENSION_1:
                    if (!RefexIntVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexIntVersionBI<?> intPart = (RefexIntVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(intPart.getInt1())) {
                        return false;
                    }
                    break;
                case LONG_EXTENSION_1:
                    if (!RefexLongVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexLongVersionBI<?> longPart = (RefexLongVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(longPart.getLong1())) {
                        return false;
                    }
                    break;
                case STATUS:
                    if (getStatus() != refexVersion.getStatus()) {
                        return false;
                    }
                    break;
                case STRING_EXTENSION_1:
                    if (!RefexStringVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexStringVersionBI<?> strPart = (RefexStringVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(strPart.getString1())) {
                        return false;
                    }
                    break;
                case ARRAY_OF_BYTEARRAY:
                    if (!RefexArrayOfBytearrayVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexArrayOfBytearrayVersionBI<?> arrayPart = (RefexArrayOfBytearrayVersionBI<?>) refexVersion;
                    if (!Arrays.equals((byte[][]) entry.getValue(), arrayPart.getArrayOfByteArray())) {
                        return false;
                    }
                    break;
                case FLOAT_EXTENSION_1:
                    if (!RefexFloatVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexFloatVersionBI<?> floatPart = (RefexFloatVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(floatPart.getFloat1())) {
                        return false;
                    }
                    break;
                default:
                    throw new RuntimeException("Can't handle: " + entry.getKey());
            }
        }
        return true;
    }

    /**
     * Gets the nid of the refex collection concept associated with this refex blueprint.
     *
     * @return the refex collection nid
     */
    public int getRefexCollectionNid() {
        return getInt(ComponentProperty.REFEX_EXTENSION_ID);
    }

    public UUID getRefexCollectionUuid() {
        return getUuid(ComponentProperty.REFEX_EXTENSION_ID);
    }

    /**
     * Gets the uuid of the referenced component associated with this refex blueprint.
     *
     * @return the referenced component uuid
     */
    public UUID getReferencedComponentUuid() {
        return getUuid(ComponentProperty.REFERENCED_COMPONENT_ID);
    }

    /**
     * Gets the string associated with this refex blueprint. Will throw an assertion error if the given key is
     * not
     * <code>ComponentProperty.STRING1<code>.
     *
     * @param key ComponentProperty.STRING1
     * @return the string associated with this refex blueprint
     */
    public String getString(ComponentProperty key) {
        assert key == ComponentProperty.STRING_EXTENSION_1;
        return (String) properties.get(key);
    }

    /**
     * Gets the boolean associated with this refex blueprint. Will throw an assertion error if the given key
     * is not
     * <code>ComponentProperty.BOOLEAN1<code>.
     *
     * @param key ComponentProperty.BOOLEAN1
     * @return the boolean associated with this refex blueprint
     */
    public boolean getBoolean(ComponentProperty key) {
        assert key == ComponentProperty.BOOLEAN_EXTENSION_1;
        return (Boolean) properties.get(key);
    }

    public float getFloat(ComponentProperty key) {
        assert key == ComponentProperty.FLOAT_EXTENSION_1;
        return (Float) properties.get(key);
    }

    public long getLong(ComponentProperty key) {
        assert key == ComponentProperty.LONG_EXTENSION_1;
        return (Long) properties.get(key);
    }

    public byte[][] getArrayOfByteArray() {
        return (byte[][]) properties.get(ComponentProperty.ARRAY_OF_BYTEARRAY);
    }

    /**
     * Gets the refex member uuid of this refex blueprint. Will throw an assertion error if the given key is
     * not
     * <code>ComponentProperty.MEMBER_UUID<code>.
     *
     * @param key ComponentProperty.MEMBER_UUID
     * @return the refex member uuid
     */
    public UUID getMemberUuid(ComponentProperty key) {
        assert key == ComponentProperty.COMPONENT_ID;
        return getUuid(key);
    }

    /**
     * Gets the refex member uuid of this refex blueprint.
     *
     * @return the refex member uuid
     */
    public UUID getMemberUUID() {
        return getUuid(ComponentProperty.COMPONENT_ID);
    }

    /**
     * Gets the refex member type of the refex associated with this refex blueprint.
     *
     * @return the refex member type
     */
    public RefexType getMemberType() {
        return memberType;
    }

    /**
     * Sets the refex member type of the refex associated with this refex blueprint.
     *
     * @param memberType the refex member type
     */
    public void setMemberType(RefexType memberType) {
        this.memberType = memberType;
    }

    /**
     * Computes the uuid of the refex member and sets the member uuid property. Uses
     * <code>computeMemberContentUuid()</code> to compute the uuid.
     *
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws IOException signals that an I/O exception has occurred
     */
    public void setContentUuid() throws InvalidCAB, IOException {
        this.properties.put(ComponentProperty.COMPONENT_ID,
                computeMemberContentUuid());
    }
}
