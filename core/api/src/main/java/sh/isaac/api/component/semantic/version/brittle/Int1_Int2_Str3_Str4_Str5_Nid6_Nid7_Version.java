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
public interface Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version 
        extends BrittleVersion {
   int getInt1();
   int getInt2();
   String getStr3();
   String getStr4();
   String getStr5();
   int getNid6();
   int getNid7();
   
   void setInt1(int nid);
   void setInt2(int nid);
   void setStr3(String value);
   void setStr4(String value);
   void setStr5(String value);
   void setNid6(int nid);
   void setNid7(int nid);
   
   @Override
   default VersionType getSemanticType() {
      return VersionType.Int1_Int2_Str3_Str4_Str5_Nid6_Nid7;
   }
   
   @Override
   default BrittleDataTypes[] getFieldTypes() {
      return new BrittleDataTypes[] {
            BrittleDataTypes.INTEGER, 
            BrittleDataTypes.INTEGER, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.NID, 
            BrittleDataTypes.NID};
   }

   @Override
   default Object[] getDataFields() {
      Object[] temp = new Object[] {
            getInt1(),
            getInt2(),
            getStr3(),
            getStr4(),
            getStr5(),
            getNid6(),
            getNid7()};
       if (getFieldTypes().length != temp.length) {
          throw new RuntimeException("Mispecified brittle!");
       }
       return temp;
   }
}
