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

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Nid3_Nid4_Version;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.version.AbstractVersionImpl;


/**
 *
 * @author kec
 */
public class Str1_Str2_Nid3_Nid4_VersionImpl
        extends AbstractVersionImpl
         implements Str1_Str2_Nid3_Nid4_Version {
   String str1 = null;
   String str2 = null;
   int    nid3 = Integer.MAX_VALUE;
   int    nid4 = Integer.MAX_VALUE;
   
   @Override
   public StringBuilder toString(StringBuilder builder) {
      builder.append(" ")
              .append("{Str1: ").append(str1).append(" ")
              .append(", Str2: ").append(str2).append(" ")
              .append(", Nid3: ").append(Get.getTextForComponent(nid3))
              .append(", Nid4: ").append(Get.getTextForComponent(nid4))
              .append(Get.stampService()
                      .describeStampSequence(this.getStampSequence())).append("}");
      return builder;
   }

   public Str1_Str2_Nid3_Nid4_VersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   public Str1_Str2_Nid3_Nid4_VersionImpl(SemanticChronology container, 
           int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.str1 = data.getUTF();
      this.str2 = data.getUTF();
      this.nid3 = data.getNid();
      this.nid4 = data.getNid();
   }
   
   private Str1_Str2_Nid3_Nid4_VersionImpl(Str1_Str2_Nid3_Nid4_VersionImpl version,
                                          int stampSequence) {
      super(version.getChronology(), stampSequence);
      setStr1(version.str1);
      setStr2(version.str2);
      setNid3(version.nid3);
      setNid4(version.nid4);
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
      data.putNid(this.nid3);
      data.putNid(this.nid4);
   }

   @Override
   @SuppressWarnings("unchecked")
   public <V extends Version> V makeAnalog(int stampSequence) {
      final Str1_Str2_Nid3_Nid4_VersionImpl newVersion = new Str1_Str2_Nid3_Nid4_VersionImpl(this, stampSequence);
      getChronology().addVersion(newVersion);
      return (V) newVersion;
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      Str1_Str2_Nid3_Nid4_VersionImpl another = (Str1_Str2_Nid3_Nid4_VersionImpl) other;
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

      return editDistance;
   }

   @Override
   public int getNid3() {
      return nid3;
   }

   @Override
   public void setNid3(int nid3) {
      this.nid3 = nid3;
   }

   @Override
   public int getNid4() {
      return nid4;
   }

   @Override
   public void setNid4(int nid4) {
      this.nid4 = nid4;
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
}