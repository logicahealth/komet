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
package org.ihtsdo.otf.tcc.ddo;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author kec
 */
public class ComponentReference implements Externalizable {

    public static final long serialVersionUID = 1;
    
    //~--- fields --------------------------------------------------------------
    private int nid = Integer.MAX_VALUE;
    private SimpleIntegerProperty nidProperty;
    private String text;
    private SimpleStringProperty textProperty;
    private long uuidMsb;
    private long uuidLsb;
    private SimpleObjectProperty<UUID> uuidProperty;
    private boolean isNull;

    //~--- constructors --------------------------------------------------------
    public ComponentReference() {
    }

    /**
     * 
     * @param concept
     * @param stampCoordinate
     * @param languageCoordinate 
     */
    public ComponentReference(ConceptChronology<?> concept, StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
        nid = concept.getNid();
        if (nid >= 0) {
            nid = Get.identifierService().getConceptNid(nid);
        }
        uuidMsb = concept.getPrimordialUuid().getMostSignificantBits();
        uuidLsb = concept.getPrimordialUuid().getLeastSignificantBits();
        Optional<LatestVersion<DescriptionSememe>> description = concept.getPreferredDescription(languageCoordinate, stampCoordinate);
        if (description.isPresent()) {
            text = description.get().value().getText();
        } else {
            text = concept.getPrimordialUuid().toString();
            Logger.getLogger(ComponentReference.class.getName()).log(Level.WARNING, 
                    "Concept with no preferred description: {0} nid({1})", 
                    new Object[]{concept.getPrimordialUuid(), concept.getNid()});
        }
    }

    /**
     * 
     * @param intId either a native id, or a concept sequence, which will be converted to a nid. 
     * @throws IOException 
     */
    public ComponentReference(int intId) throws IOException {
        if (nid >= 0) {
            this.nid = Get.identifierService().getConceptNid(intId);
        } else {
            this.nid = intId;
        }
         
    }

    public ComponentReference(UUID uuid) {
        uuidMsb = uuid.getMostSignificantBits();
        uuidLsb = uuid.getLeastSignificantBits();
    }
    
    public ComponentReference(int nid, TaxonomyCoordinate taxonomyCoordinate)  {
        this(nid, taxonomyCoordinate.getStampCoordinate(), taxonomyCoordinate.getLanguageCoordinate());
    }

    /**
     * 
     * @param intId either a native id, or a concept sequence, which will be converted to a nid. 
     * @param stampCoordinate
     * @param languageCoordinate 
     */
    public ComponentReference(int intId, StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate)  {
        if (nid >= 0) {
            this.nid = Get.identifierService().getConceptNid(intId);
        } else {
            this.nid = intId;
        }
        Optional<? extends ObjectChronology<? extends StampedVersion>> component = Get.identifiedObjectService().getIdentifiedObjectChronology(nid);
        if (component.isPresent()) {
            setupComponent(component.get(), languageCoordinate, stampCoordinate);
        } else {
            text = "null component";
            isNull = true;
        }
    }

    public ComponentReference(ObjectChronology<?> component, TaxonomyCoordinate taxonomyCoordinate)  {
        this.nid = component.getNid();
        setupComponent(component, taxonomyCoordinate.getLanguageCoordinate(), taxonomyCoordinate.getStampCoordinate());
    }
    
    private void setupComponent(ObjectChronology<? extends StampedVersion> chronology, LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate) {
        uuidMsb = chronology.getPrimordialUuid().getMostSignificantBits();
        uuidLsb = chronology.getPrimordialUuid().getLeastSignificantBits();
        isNull = false;
        
        if (chronology instanceof ConceptChronology) {
            ConceptChronology<?> conceptChronology = (ConceptChronology<?>) chronology;
            Optional<LatestVersion<DescriptionSememe>> preferredDescription =
                    conceptChronology.getPreferredDescription(languageCoordinate, stampCoordinate);
            if (preferredDescription.isPresent()) {
                text = preferredDescription.get().value().getText();
            } else {
                Optional<LatestVersion<DescriptionSememe>> fullySpecifiedDescription =
                        conceptChronology.getFullySpecifiedDescription(languageCoordinate, stampCoordinate);
                if (fullySpecifiedDescription.isPresent()) {
                    text = fullySpecifiedDescription.get().value().getText();
                } else {
                    text = conceptChronology.toUserString();
                }
            }
        } else if (chronology instanceof SememeChronology) {
            SememeChronology sememeChronology = (SememeChronology) chronology;
            if (sememeChronology.getSememeType() == SememeType.DESCRIPTION) {
                Optional<LatestVersion<DescriptionSememe>> desc = sememeChronology.getLatestVersion(DescriptionSememe.class, stampCoordinate);
                if (desc.isPresent()) {
                    text = desc.get().value().getText();
                } else {
                    text =  chronology.toUserString();
                }
                
            } else {
                text =  chronology.toUserString();
            }
        } else {
            text = chronology.getClass().getSimpleName() + " for: (cannot find description)";
        }
    }

    public ComponentReference(UUID uuid, int nid, String text) {
        this.nid = nid;
        this.uuidMsb = uuid.getMostSignificantBits();
        this.uuidLsb = uuid.getLeastSignificantBits();
        this.text = text;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ComponentReference) {
            ComponentReference another = (ComponentReference) obj;

            return (getNid() == another.getNid()) || getUuid().equals(another.getUuid());
        }

        return false;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks to see if the
     * {@code ComponentVersionBI} specified by the
     * {@code ViewCoordinate} is null.
     *
     * @return true if the {@code ComponentVersionBI} is null
     */
    public boolean componentVersionIsNull() {
        return isNull;
    }

    public SimpleIntegerProperty nidProperty() {
        if (nidProperty == null) {
            nidProperty = new SimpleIntegerProperty(this, "nid", nid);
        }

        return nidProperty;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        text = in.readUTF();
        uuidMsb = in.readLong();
        uuidLsb = in.readLong();
        nid = in.readInt();
    }

    public SimpleStringProperty textProperty() {
        if (textProperty == null) {
            textProperty = new SimpleStringProperty(this, "text", text);
        }

        return textProperty;
    }

    @Override
    public String toString() {
        return "Ref{text=" + getText() + ", nid=" + getNid() + ", uuid=" + getUuid() + '}';
    }

    public SimpleObjectProperty<UUID> uuidProperty() {
        if (uuidProperty == null) {
            uuidProperty = new SimpleObjectProperty<>(this, "uuid", new UUID(uuidMsb, uuidLsb));
        }

        return uuidProperty;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(getText());
        out.writeLong(getUuid().getMostSignificantBits());
        out.writeLong(getUuid().getLeastSignificantBits());
        out.writeInt(getNid());
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Get the value of nid
     *
     * @return the value of nid
     */
    public int getNid() {
        return (nidProperty == null)
                ? nid
                : nidProperty.get();
    }

    /**
     * Get the value of text
     *
     * @return the value of text
     */
    public String getText() {
        return (textProperty == null)
                ? text
                : textProperty.get();
    }

    public String getHtmlFragment() {
        StringBuilder sb = new StringBuilder();
        if (Get.identifierService().getChronologyTypeForNid(nid) == ObjectChronologyType.CONCEPT) {
            sb.append("<a href=\"../concept/");
        } else {
            sb.append("<a href=\"../component/");
        }
        sb.append(getUuid());
        sb.append("\">");
        sb.append(getText());
        sb.append("</a>");


        return sb.toString();
    }

    /**
     * Get the value of uuid
     *
     * @return the value of uuid
     */
    public UUID getUuid() {
        return (uuidProperty == null)
                ? new UUID(uuidMsb, uuidLsb)
                : uuidProperty.get();
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Set the value of nid
     *
     * @param nid new value of nid
     */
    public void setNid(int nid) {
        if (nidProperty == null) {
            this.nid = nid;
        } else {
            nidProperty.set(nid);
        }
    }

    /**
     * Set the value of text
     *
     * @param text new value of text
     */
    public void setText(String text) {
        if (textProperty == null) {
            this.text = text;
        } else {
            textProperty.set(text);
        }
    }

    /**
     * Set the value of uuid
     *
     * @param uuid new value of uuid
     */
    public void setUuid(UUID uuid) {
        if (uuidProperty == null) {
            this.uuidMsb = uuid.getMostSignificantBits();
            this.uuidLsb = uuid.getLeastSignificantBits();
        } else {
            uuidProperty.set(uuid);
        }
    }
}
