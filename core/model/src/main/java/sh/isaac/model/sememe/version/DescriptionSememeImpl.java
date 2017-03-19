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
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.MutableDescriptionSememe;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.sememe.SememeChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class DescriptionSememeImpl
        extends SememeVersionImpl<DescriptionSememeImpl>
         implements MutableDescriptionSememe<DescriptionSememeImpl> {
   protected int    caseSignificanceConceptSequence;
   protected int    languageConceptSequence;
   protected String text;
   protected int    descriptionTypeConceptSequence;

   //~--- constructors --------------------------------------------------------

   public DescriptionSememeImpl(SememeChronologyImpl<DescriptionSememeImpl> chronicle,
                                int stampSequence,
                                short versionSequence) {
      super(chronicle, stampSequence, versionSequence);
   }

   public DescriptionSememeImpl(SememeChronologyImpl<DescriptionSememeImpl> chronicle,
                                int stampSequence,
                                short versionSequence,
                                ByteArrayDataBuffer data) {
      super(chronicle, stampSequence, versionSequence);
      this.caseSignificanceConceptSequence = data.getConceptSequence();
      this.languageConceptSequence         = data.getConceptSequence();
      this.text                            = data.readUTF();
      this.descriptionTypeConceptSequence  = data.getConceptSequence();
   }

   //~--- methods -------------------------------------------------------------

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

   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putConceptSequence(this.caseSignificanceConceptSequence);
      data.putConceptSequence(this.languageConceptSequence);
      data.putUTF(this.text);
      data.putConceptSequence(this.descriptionTypeConceptSequence);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getCaseSignificanceConceptSequence() {
      return this.caseSignificanceConceptSequence;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setCaseSignificanceConceptSequence(int caseSignificanceConceptSequence) {
      this.caseSignificanceConceptSequence = caseSignificanceConceptSequence;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getDescriptionTypeConceptSequence() {
      return this.descriptionTypeConceptSequence;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDescriptionTypeConceptSequence(int descriptionTypeConceptSequence) {
      this.descriptionTypeConceptSequence = descriptionTypeConceptSequence;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getLanguageConceptSequence() {
      return this.languageConceptSequence;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLanguageConceptSequence(int languageConceptSequence) {
      this.languageConceptSequence = languageConceptSequence;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public SememeType getSememeType() {
      return SememeType.DESCRIPTION;
   }

   @Override
   public String getText() {
      return this.text;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setText(String text) {
      this.text = text;
   }
}

