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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

/**
 *
 * @author kec
 */
public class ComponentReference implements Externalizable {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   private int                        nid = Integer.MAX_VALUE;
   private SimpleIntegerProperty      nidProperty;
   private String                     text;
   private SimpleStringProperty       textProperty;
   private long                       uuidMsb;
   private long                       uuidLsb;
   private SimpleObjectProperty<UUID> uuidProperty;

   //~--- constructors --------------------------------------------------------

   public ComponentReference() {}

   public ComponentReference(ConceptVersionBI concept) throws IOException, ContradictionException {
      nid  = concept.getNid();
      uuidMsb = concept.getPrimordialUuid().getMostSignificantBits();
      uuidLsb = concept.getPrimordialUuid().getLeastSignificantBits();
      text = concept.getPreferredDescription().getText();
   }

   public ComponentReference(int nid) throws IOException {
      this.nid = nid;
   }

   public ComponentReference(UUID uuid) {
      uuidMsb = uuid.getMostSignificantBits();
      uuidLsb = uuid.getLeastSignificantBits();
   }

   public ComponentReference(TerminologySnapshotDI ss, int nid) throws IOException, ContradictionException {
      this.nid = nid;

      ComponentVersionBI component = ss.getComponentVersion(nid);

      if (component != null) {
        uuidMsb = component.getPrimordialUuid().getMostSignificantBits();
        uuidLsb = component.getPrimordialUuid().getLeastSignificantBits();

         if (component instanceof ConceptVersionBI) {
            text = ((ConceptVersionBI) component).getPreferredDescription().getText();
         } else if (component instanceof DescriptionVersionBI) {
            text = ((DescriptionVersionBI) component).getText();
         } else {
            text = component.getChronicle().getClass().getSimpleName() + " for: " +
                    ss.getConceptForNid(nid).getFullySpecifiedDescription().getText();
         }
      } else {
         text = "null component";
      }
   }

   public ComponentReference(UUID uuid, int nid, String text) {
      this.nid  = nid;
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

   public SimpleIntegerProperty nidProperty() {
      if (nidProperty == null) {
         nidProperty = new SimpleIntegerProperty(this, "nid", Integer.valueOf(nid));
      }

      return nidProperty;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      text = in.readUTF();
      uuidMsb = in.readLong();
      uuidLsb = in.readLong();
      nid  = in.readInt();
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
       if (Ts.get().getConceptNidForNid(getNid()) == getNid()) {
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
