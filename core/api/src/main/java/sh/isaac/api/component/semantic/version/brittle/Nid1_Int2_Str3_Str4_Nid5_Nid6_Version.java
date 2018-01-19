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
public interface Nid1_Int2_Str3_Str4_Nid5_Nid6_Version 
        extends BrittleVersion {
   
   int getNid1();
   int getInt2();
   String getStr3();
   String getStr4();
   int getNid5();
   int getNid6();
   
   void setNid1(int nid);
   void setInt2(int nid);
   void setStr3(String value);
   void setStr4(String value);
   void setNid5(int nid);
   void setNid6(int nid);

   @Override
   default VersionType getSemanticType() {
      return VersionType.Nid1_Int2_Str3_Str4_Nid5_Nid6;
   }
   
   @Override
   default BrittleDataTypes[] getFieldTypes() {
      return new BrittleDataTypes[] {
            BrittleDataTypes.NID, 
            BrittleDataTypes.INTEGER, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.STRING, 
            BrittleDataTypes.NID, 
            BrittleDataTypes.NID};
   }

   @Override
   default Object[] getDataFields() {
      Object[] temp = new Object[] {
            getNid1(),
            getInt2(),
            getStr3(),
            getStr4(),
            getNid5(),
            getNid6()};
       if (getFieldTypes().length != temp.length) {
          throw new RuntimeException("Mispecified brittle!");
       }
       return temp;
   }
   
}
