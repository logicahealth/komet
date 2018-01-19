/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.api.component.semantic.version.brittle;

import sh.isaac.api.chronicle.VersionType;

/**
 *
 * @author kec
 */
public interface Str1_Str2_Nid3_Nid4_Nid5_Version  
        extends BrittleVersion {
   String getStr1();
   String getStr2();
   int getNid3();
   int getNid4();
   int getNid5();
   
   void setStr1(String value);
   void setStr2(String value);
   void setNid3(int nid);
   void setNid4(int nid);
   void setNid5(int nid);
   
   @Override
   default BrittleDataTypes[] getFieldTypes() {
      return new BrittleDataTypes[] {
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.NID, 
            BrittleDataTypes.NID, 
            BrittleDataTypes.NID};
   }

   @Override
   default Object[] getDataFields() {
      Object[] temp = new Object[] {
            getStr1(),
            getStr2(),
            getNid3(),
            getNid4(),
            getNid5()};
       if (getFieldTypes().length != temp.length) {
          throw new RuntimeException("Mispecified brittle!");
       }
       return temp;
   }
   
   @Override
   default VersionType getSemanticType() {
      return VersionType.Str1_Str2_Nid3_Nid4_Nid5;
   }
   
}
