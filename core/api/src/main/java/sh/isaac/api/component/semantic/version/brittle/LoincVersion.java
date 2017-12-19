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



package sh.isaac.api.component.semantic.version.brittle;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.SemanticVersion;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
public interface LoincVersion
        extends SemanticVersion {
   String getComponent();

   //~--- set methods ---------------------------------------------------------

   void setComponent(String value);

   //~--- get methods ---------------------------------------------------------

   String getLoincNum();

   //~--- set methods ---------------------------------------------------------

   void setLoincNum(String value);

   //~--- get methods ---------------------------------------------------------

   String getLongCommonName();

   //~--- set methods ---------------------------------------------------------

   void setLongCommonName(String value);

   //~--- get methods ---------------------------------------------------------

   String getMethodType();

   //~--- set methods ---------------------------------------------------------

   void setMethodType(String value);

   //~--- get methods ---------------------------------------------------------

   String getProperty();

   //~--- set methods ---------------------------------------------------------

   void setProperty(String value);

   //~--- get methods ---------------------------------------------------------

   String getScaleType();

   //~--- set methods ---------------------------------------------------------

   void setScaleType(String value);

   //~--- get methods ---------------------------------------------------------

   String getShortName();

   //~--- set methods ---------------------------------------------------------

   void setShortName(String value);

   //~--- get methods ---------------------------------------------------------

   String getLoincStatus();

   //~--- set methods ---------------------------------------------------------

   void setLoincStatus(String value);

   //~--- get methods ---------------------------------------------------------

   String getSystem();

   //~--- set methods ---------------------------------------------------------

   void setSystem(String value);

   //~--- get methods ---------------------------------------------------------

   String getTimeAspect();

   //~--- set methods ---------------------------------------------------------

   void setTimeAspect(String value);
   
   @Override
   default VersionType getSemanticType() {
      return VersionType.LOINC_RECORD;
   }
}

