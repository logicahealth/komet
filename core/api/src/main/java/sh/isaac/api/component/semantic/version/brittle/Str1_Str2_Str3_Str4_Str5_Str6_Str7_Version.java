/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
public interface Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version 
        extends BrittleVersion {
   String getStr1();
   String getStr2();
   String getStr3();
   String getStr4();
   String getStr5();
   String getStr6();
   String getStr7();
   
   void setStr1(String value);
   void setStr2(String value);
   void setStr3(String value);
   void setStr4(String value);
   void setStr5(String value);
   void setStr6(String value);
   void setStr7(String value);
   
   @Override
   default BrittleDataTypes[] getFieldTypes() {
      return new BrittleDataTypes[] {
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
      Object[] temp = new Object[] {
            getStr1(),
            getStr2(),
            getStr3(),
            getStr4(),
            getStr5(),
            getStr6(),
            getStr7()};
       if (getFieldTypes().length != temp.length) {
          throw new RuntimeException("Mispecified brittle!");
       }
       return temp;
   }
   
   @Override
   default VersionType getSemanticType() {
      return VersionType.Str1_Str2_Str3_Str4_Str5_Str6_Str7;
   }
   
}
