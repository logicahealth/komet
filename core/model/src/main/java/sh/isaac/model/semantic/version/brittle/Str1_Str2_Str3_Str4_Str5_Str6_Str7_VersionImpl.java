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
package sh.isaac.model.semantic.version.brittle;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

/**
 *
 * @author kec
 */
public class Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl 
        extends AbstractVersionImpl
         implements Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version {

   String str1 = null;
   String str2 = null;
   String str3 = null;
   String str4 = null;
   String str5 = null;
   String str6 = null;
   String str7 = null;
   
   public Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public String getStr1() {
      return str1;
   }

   @Override
   public void setStr1(String str1) {
      this.str1 = str1;
   }

   @Override
   public String getStr2() {
      return str2;
   }

   @Override
   public void setStr2(String str2) {
      this.str2 = str2;
   }

   @Override
   public String getStr3() {
      return str3;
   }

   @Override
   public void setStr3(String str3) {
      this.str3 = str3;
   }

   @Override
   public String getStr4() {
      return str4;
   }

   @Override
   public void setStr4(String str4) {
      this.str4 = str4;
   }

   @Override
   public String getStr5() {
      return str5;
   }

   @Override
   public void setStr5(String str5) {
      this.str5 = str5;
   }

   @Override
   public String getStr6() {
      return str6;
   }

   @Override
   public void setStr6(String str6) {
      this.str6 = str6;
   }

   @Override
   public String getStr7() {
      return str7;
   }

   @Override
   public void setStr7(String str7) {
      this.str7 = str7;
   }
}
