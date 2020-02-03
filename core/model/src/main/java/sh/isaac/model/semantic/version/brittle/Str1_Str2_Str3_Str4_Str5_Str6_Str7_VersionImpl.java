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



package sh.isaac.model.semantic.version.brittle;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.SemanticChronologyImpl;
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

   public Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl(SemanticChronology container, 
           int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.str1 = data.getUTF();
      this.str2 = data.getUTF();
      this.str3 = data.getUTF();
      this.str4 = data.getUTF();
      this.str5 = data.getUTF();
      this.str6 = data.getUTF();
      this.str7 = data.getUTF();
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public <V extends Version> V makeAnalog(int stampSequence) {
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl newVersion = new Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl((SemanticChronology) this, stampSequence);
      newVersion.setStr1(this.str1);
      newVersion.setStr2(this.str2);
      newVersion.setStr3(this.str3);
      newVersion.setStr4(this.str4);
      newVersion.setStr5(this.str5);
      newVersion.setStr6(this.str6);
      newVersion.setStr7(this.str7);
      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;   
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   public void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putUTF(this.str1);
      data.putUTF(this.str2);
      data.putUTF(this.str3);
      data.putUTF(this.str4);
      data.putUTF(this.str5);
      data.putUTF(this.str6);
      data.putUTF(this.str7);
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl another = (Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl) other;
      if (this.str1 == null ? another.str1 != null : !this.str1.equals(another.str1)) {
         editDistance++;
      }
      if (this.str2 == null ? another.str2 != null : !this.str2.equals(another.str2)) {
         editDistance++;
      }
      if (this.str3 == null ? another.str3 != null : !this.str3.equals(another.str3)) {
         editDistance++;
      }
      if (this.str4 == null ? another.str4 != null : !this.str4.equals(another.str4)) {
         editDistance++;
      }
      if (this.str5 == null ? another.str5 != null : !this.str5.equals(another.str5)) {
         editDistance++;
      }
      if (this.str6 == null ? another.str6 != null : !this.str6.equals(another.str6)) {
         editDistance++;
      }
      if (this.str7 == null ? another.str7 != null : !this.str7.equals(another.str7)) {
         editDistance++;
      }
      return editDistance;
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
