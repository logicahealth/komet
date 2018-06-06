/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.model.semantic.version;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.component.semantic.version.MutableDescriptionVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class DescriptionVersionImpl.
 *
 * @author kec
 */
public class DescriptionVersionImpl
        extends AbstractVersionImpl
         implements MutableDescriptionVersion {
   /** The case significance concept sequence. */
   protected int caseSignificanceConceptNid;

   /** The language concept sequence. */
   protected int languageConceptNid;

   /** The text. */
   protected String text;

   /** The description type concept sequence. */
   protected int descriptionTypeConceptNid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new description semantic impl.
    *
    * @param chronicle the chronicle
    * @param stampSequence the stamp sequence
    */
   public DescriptionVersionImpl(SemanticChronology chronicle,
                                int stampSequence) {
      super(chronicle, stampSequence);
   }

   /**
    * Instantiates a new description semantic impl.
    *
    * @param chronicle the chronicle
    * @param stampSequence the stamp sequence
    * @param data the data
    */
   public DescriptionVersionImpl(SemanticChronology chronicle,
                                int stampSequence,
                                ByteArrayDataBuffer data) {
      super(chronicle, stampSequence);
      this.caseSignificanceConceptNid = data.getNid();
      this.languageConceptNid         = data.getNid();
      this.text                       = data.getUTF();
      this.descriptionTypeConceptNid  = data.getNid();
   }
   private DescriptionVersionImpl(DescriptionVersionImpl other, int stampSequence) {
      super(other.getChronology(), stampSequence);
      this.caseSignificanceConceptNid = other.caseSignificanceConceptNid;
      this.languageConceptNid         = other.languageConceptNid;
      this.text                            = other.text;
      this.descriptionTypeConceptNid  = other.descriptionTypeConceptNid;
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      final int stampSequence = Get.stampService()
                                   .getStampSequence(
                                       this.getStatus(),
                                       Long.MAX_VALUE,
                                       ec.getAuthorNid(),
                                       this.getModuleNid(),
                                       ec.getPathNid());
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final DescriptionVersionImpl newVersion = new DescriptionVersionImpl(this, stampSequence);

      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;   
   }


   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("{Description≤")
        .append(this.text)
        .append(", rc: ")
        .append(getReferencedComponentNid())
        .append(" ")
        .append(Get.conceptDescriptionText(this.caseSignificanceConceptNid))
        .append(" <")
        .append(this.caseSignificanceConceptNid)
        .append(">, ")
        .append(Get.conceptDescriptionText(this.languageConceptNid))
        .append(" <")
        .append(this.languageConceptNid)
        .append(">, ")
        .append(Get.conceptDescriptionText(this.descriptionTypeConceptNid))
        .append(" <")
        .append(this.descriptionTypeConceptNid)
        .append(">");
      toString(sb);
      sb.append("≥}");
      return sb.toString();
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putNid(this.caseSignificanceConceptNid);
      data.putNid(this.languageConceptNid);
      data.putUTF(this.text);
      data.putNid(this.descriptionTypeConceptNid);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the case significance concept sequence.
    *
    * @return the case significance concept sequence
    */
   @Override
   public int getCaseSignificanceConceptNid() {
      return this.caseSignificanceConceptNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the case significance concept sequence.
    *
    * @param caseSignificanceConceptNid the new case significance concept sequence
    */
   @Override
   public void setCaseSignificanceConceptNid(int caseSignificanceConceptNid) {
      this.caseSignificanceConceptNid = caseSignificanceConceptNid;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description type concept sequence.
    *
    * @return the description type concept sequence
    */
   @Override
   public int getDescriptionTypeConceptNid() {
      return this.descriptionTypeConceptNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the description type concept sequence.
    *
    * @param descriptionTypeConceptNid the new description type concept sequence
    */
   @Override
   public void setDescriptionTypeConceptNid(int descriptionTypeConceptNid) {
      this.descriptionTypeConceptNid = descriptionTypeConceptNid;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the language concept sequence.
    *
    * @return the language concept sequence
    */
   @Override
   public int getLanguageConceptNid() {
      return this.languageConceptNid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the language concept sequence.
    *
    * @param languageConceptNid the new language concept sequence
    */
   @Override
   public void setLanguageConceptNid(int languageConceptNid) {
      this.languageConceptNid = languageConceptNid;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the semantic type.
    *
    * @return the semantic type
    */
   @Override
   public VersionType getSemanticType() {
      return VersionType.DESCRIPTION;
   }

   /**
    * Gets the text.
    *
    * @return the text
    */
   @Override
   public String getText() {
      return this.text;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the text.
    *
    * @param text the new text
    */
   @Override
   public void setText(String text) {
      this.text = text;
   }
   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      DescriptionVersionImpl otherImpl = (DescriptionVersionImpl) other;
      if (this.caseSignificanceConceptNid != otherImpl.caseSignificanceConceptNid) {
         editDistance++;
      }
      if (this.descriptionTypeConceptNid != otherImpl.descriptionTypeConceptNid) {
         editDistance++;
      }
      if (this.languageConceptNid != otherImpl.languageConceptNid) {
         editDistance++;
      }
      if (!this.text.equals(otherImpl.text)) {
         editDistance++;
      }
      return editDistance;
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      if (!(other instanceof DescriptionVersionImpl)) {
         return false;
      }
      DescriptionVersionImpl otherImpl = (DescriptionVersionImpl) other;
      if (this.caseSignificanceConceptNid != otherImpl.caseSignificanceConceptNid) {
         return false;
      }
      if (this.descriptionTypeConceptNid != otherImpl.descriptionTypeConceptNid) {
         return false;
      }
      if (this.languageConceptNid != otherImpl.languageConceptNid) {
         return false;
      }
      return this.text.equals(otherImpl.text);
   }
   
}

