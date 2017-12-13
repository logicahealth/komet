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
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.semantic.version.AbstractVersionImpl;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Str3_Str4_Nid5_Nid6_Version;

/**
 *
 * @author kec
 */
public class Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl  
        extends AbstractVersionImpl
         implements Nid1_Int2_Str3_Str4_Nid5_Nid6_Version {

   int nid1 = Integer.MAX_VALUE;
   int int2 = Integer.MAX_VALUE;
   String str3 = null;
   String str4 = null;
   int nid5 = Integer.MAX_VALUE;
   int nid6 = Integer.MAX_VALUE;

   public Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl(SemanticChronology container, int stampSequence) {
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
   public int getNid1() {
      return nid1;
   }

   @Override
   public void setNid1(int nid1) {
      this.nid1 = nid1;
   }

   @Override
   public int getInt2() {
      return int2;
   }

   @Override
   public void setInt2(int int2) {
      this.int2 = int2;
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
   public int getNid5() {
      return nid5;
   }

   @Override
   public void setNid5(int nid5) {
      this.nid5 = nid5;
   }

   @Override
   public int getNid6() {
      return nid6;
   }

   @Override
   public void setNid6(int nid6) {
      this.nid6 = nid6;
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
   
}
