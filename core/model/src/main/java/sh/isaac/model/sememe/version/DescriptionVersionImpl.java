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



package sh.isaac.model.sememe.version;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.component.sememe.version.MutableDescriptionVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.sememe.SememeChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class DescriptionVersionImpl.
 *
 * @author kec
 */
public class DescriptionVersionImpl
        extends SememeVersionImpl
         implements MutableDescriptionVersion {
   /** The case significance concept sequence. */
   protected int caseSignificanceConceptSequence;

   /** The language concept sequence. */
   protected int languageConceptSequence;

   /** The text. */
   protected String text;

   /** The description type concept sequence. */
   protected int descriptionTypeConceptSequence;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new description sememe impl.
    *
    * @param chronicle the chronicle
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    */
   public DescriptionVersionImpl(SememeChronology chronicle,
                                int stampSequence,
                                short versionSequence) {
      super(chronicle, stampSequence, versionSequence);
   }

   /**
    * Instantiates a new description sememe impl.
    *
    * @param chronicle the chronicle
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    * @param data the data
    */
   public DescriptionVersionImpl(SememeChronology chronicle,
                                int stampSequence,
                                short versionSequence,
                                ByteArrayDataBuffer data) {
      super(chronicle, stampSequence, versionSequence);
      this.caseSignificanceConceptSequence = data.getConceptSequence();
      this.languageConceptSequence         = data.getConceptSequence();
      this.text                            = data.readUTF();
      this.descriptionTypeConceptSequence  = data.getConceptSequence();
   }
   private DescriptionVersionImpl(DescriptionVersionImpl other, int stampSequence, short versionSequence) {
      super(other.getChronology(), stampSequence, versionSequence);
      this.caseSignificanceConceptSequence = other.caseSignificanceConceptSequence;
      this.languageConceptSequence         = other.languageConceptSequence;
      this.text                            = other.text;
      this.descriptionTypeConceptSequence  = other.descriptionTypeConceptSequence;
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      final int stampSequence = Get.stampService()
                                   .getStampSequence(
                                       this.getState(),
                                       Long.MAX_VALUE,
                                       ec.getAuthorSequence(),
                                       this.getModuleSequence(),
                                       ec.getPathSequence());
      SememeChronologyImpl chronologyImpl = (SememeChronologyImpl) this.chronicle;
      final DescriptionVersionImpl newVersion = new DescriptionVersionImpl(this, stampSequence, 
              chronologyImpl.nextVersionSequence());

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
        .append(" <")
        .append(Get.identifierService()
                   .getConceptSequence(getReferencedComponentNid()))
        .append(">, ")
        .append(Get.conceptDescriptionText(this.caseSignificanceConceptSequence))
        .append(" <")
        .append(this.caseSignificanceConceptSequence)
        .append(">, ")
        .append(Get.conceptDescriptionText(this.languageConceptSequence))
        .append(" <")
        .append(this.languageConceptSequence)
        .append(">, ")
        .append(Get.conceptDescriptionText(this.descriptionTypeConceptSequence))
        .append(" <")
        .append(this.descriptionTypeConceptSequence)
        .append(">");
      toString(sb);
      sb.append("≥D}");
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
      data.putConceptSequence(this.caseSignificanceConceptSequence);
      data.putConceptSequence(this.languageConceptSequence);
      data.putUTF(this.text);
      data.putConceptSequence(this.descriptionTypeConceptSequence);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the case significance concept sequence.
    *
    * @return the case significance concept sequence
    */
   @Override
   public int getCaseSignificanceConceptSequence() {
      return this.caseSignificanceConceptSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the case significance concept sequence.
    *
    * @param caseSignificanceConceptSequence the new case significance concept sequence
    */
   @Override
   public void setCaseSignificanceConceptSequence(int caseSignificanceConceptSequence) {
      this.caseSignificanceConceptSequence = caseSignificanceConceptSequence;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description type concept sequence.
    *
    * @return the description type concept sequence
    */
   @Override
   public int getDescriptionTypeConceptSequence() {
      return this.descriptionTypeConceptSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the description type concept sequence.
    *
    * @param descriptionTypeConceptSequence the new description type concept sequence
    */
   @Override
   public void setDescriptionTypeConceptSequence(int descriptionTypeConceptSequence) {
      this.descriptionTypeConceptSequence = descriptionTypeConceptSequence;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the language concept sequence.
    *
    * @return the language concept sequence
    */
   @Override
   public int getLanguageConceptSequence() {
      return this.languageConceptSequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the language concept sequence.
    *
    * @param languageConceptSequence the new language concept sequence
    */
   @Override
   public void setLanguageConceptSequence(int languageConceptSequence) {
      this.languageConceptSequence = languageConceptSequence;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sememe type.
    *
    * @return the sememe type
    */
   @Override
   public VersionType getSememeType() {
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
}

