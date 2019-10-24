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

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
public interface LoincVersion extends BrittleVersion {
   String getComponent();

   void setComponent(String value);

   String getLoincNum();

   void setLoincNum(String value);

   String getLongCommonName();

   void setLongCommonName(String value);

   String getMethodType();

   void setMethodType(String value);

   String getProperty();

   void setProperty(String value);

   String getScaleType();

   void setScaleType(String value);

   String getShortName();

   void setShortName(String value);

   String getLoincStatus();

   void setLoincStatus(String value);

   String getSystem();

   void setSystem(String value);

   String getTimeAspect();

   void setTimeAspect(String value);
   
   @Override
   default BrittleDataTypes[] getFieldTypes() {
      return new BrittleDataTypes[] {
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING,
            BrittleDataTypes.STRING,
            BrittleDataTypes.STRING,
            BrittleDataTypes.STRING};
   }

   @Override
   default Object[] getDataFields() {
      Object[] temp = {
            getComponent(),
            getLoincNum(),
            getLoincStatus(),
            getLongCommonName(),
            getMethodType(),
            getProperty(),
            getScaleType(),
            getShortName(),
            getSystem(),
            getTimeAspect()};
      
      if (getFieldTypes().length != temp.length) {
         throw new RuntimeException("Misspecified brittle!");
      }
      return temp;
   }

   @Override
   default VersionType getSemanticType() {
      return VersionType.LOINC_RECORD;
   }
}
