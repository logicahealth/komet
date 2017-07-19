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



package sh.isaac.api.observable.sememe.version;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ObservableDescriptionVersion.
 *
 * @author kec
 */
public interface ObservableDescriptionVersion
        extends ObservableSememeVersion {
   /**
    * Case significance concept sequence property.
    *
    * @return the integer property
    */
   IntegerProperty caseSignificanceConceptSequenceProperty();

   /**
    * Description type concept sequence property.
    *
    * @return the integer property
    */
   IntegerProperty descriptionTypeConceptSequenceProperty();

   /**
    * Language concept sequence property.
    *
    * @return the integer property
    */
   IntegerProperty languageConceptSequenceProperty();

   /**
    * Text property.
    *
    * @return the string property
    */
   StringProperty textProperty();

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the case significance concept sequence.
    *
    * @return the case significance concept sequence
    */
   int getCaseSignificanceConceptSequence();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the case significance concept sequence.
    *
    * @param caseSignificanceConceptSequence the new case significance concept sequence
    */
   void setCaseSignificanceConceptSequence(int caseSignificanceConceptSequence);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description type concept sequence.
    *
    * @return the description type concept sequence
    */
   int getDescriptionTypeConceptSequence();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the description type concept sequence.
    *
    * @param descriptionTypeConceptSequence the new description type concept sequence
    */
   void setDescriptionTypeConceptSequence(int descriptionTypeConceptSequence);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the language concept sequence.
    *
    * @return the language concept sequence
    */
   int getLanguageConceptSequence();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the language concept sequence.
    *
    * @param languageConceptSequence the new language concept sequence
    */
   void setLanguageConceptSequence(int languageConceptSequence);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the text.
    *
    * @return the text
    */
   String getText();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the text.
    *
    * @param text the new text
    */
   void setText(String text);
}

