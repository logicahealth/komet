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
package sh.isaac.model.semantic.version.brittle;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Nid3_Nid4_Nid5_Version;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

/**
 *
 * @author kec
 */
public class Str1_Str2_Nid3_Nid4_Nid5_VersionImpl 
        extends AbstractVersionImpl
         implements Str1_Str2_Nid3_Nid4_Nid5_Version {
   String str1 = null;
   String str2 = null;
   int    nid3 = Integer.MAX_VALUE;
   int    nid4 = Integer.MAX_VALUE;
   int    nid5 = Integer.MAX_VALUE;

   //~--- constructors --------------------------------------------------------

   public Str1_Str2_Nid3_Nid4_Nid5_VersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   public Str1_Str2_Nid3_Nid4_Nid5_VersionImpl(SemanticChronology container, 
           int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.str1 = data.getUTF();
      this.str2 = data.getUTF();
      this.nid3 = data.getNid();
      this.nid4 = data.getNid();
      this.nid5 = data.getNid();
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putUTF(this.str1);
      data.putUTF(this.str2);
      data.putNid(this.nid3);
      data.putNid(this.nid4);
      data.putNid(this.nid5);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      final int stampSequence = Get.stampService()
                                   .getStampSequence(
                                       this.getState(),
                                       Long.MAX_VALUE,
                                       ec.getAuthorNid(),
                                       this.getModuleNid(),
                                       ec.getPathNid());
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final Str1_Str2_Nid3_Nid4_Nid5_Version newVersion = new Str1_Str2_Nid3_Nid4_Nid5_VersionImpl((SemanticChronology) this, stampSequence);
      newVersion.setStr1(this.str1);
      newVersion.setStr2(this.str2);
      newVersion.setNid3(this.nid3);
      newVersion.setNid4(this.nid4);
      newVersion.setNid5(this.nid5);
      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;   
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      Str1_Str2_Nid3_Nid4_Nid5_VersionImpl another = (Str1_Str2_Nid3_Nid4_Nid5_VersionImpl) other;
      if (this.str1 == null ? another.str1 != null : !this.str1.equals(another.str1)) {
         editDistance++;
      }
      if (this.str2 == null ? another.str2 != null : !this.str2.equals(another.str2)) {
         editDistance++;
      }
      if (this.nid3 != another.nid3) {
         editDistance++;
      }
      if (this.nid4 != another.nid4) {
         editDistance++;
      }
      if (this.nid5 != another.nid5) {
         editDistance++;
      }

      return editDistance;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid3() {
      return nid3;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid3(int nid3) {
      this.nid3 = nid3;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid4() {
      return nid4;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid4(int nid4) {
      this.nid4 = nid4;
   }

   @Override
   public int getNid5() {
      return nid5;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid5(int nid5) {
      this.nid5 = nid5;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr1() {
      return str1;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setStr1(String str1) {
      this.str1 = str1;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getStr2() {
      return str2;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setStr2(String str2) {
      this.str2 = str2;
   }
}

