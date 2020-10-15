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
import sh.isaac.api.component.semantic.version.brittle.Nid1_Nid2_Str3_Version;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

/**
 *
 * @author kec
 */
public class Nid1_Nid2_Str3_VersionImpl
        extends AbstractVersionImpl
         implements Nid1_Nid2_Str3_Version {
   int    nid1 = Integer.MAX_VALUE;
   int    nid2 = Integer.MAX_VALUE;
   String str3 = null;
   @Override
   public StringBuilder toString(StringBuilder builder) {
      builder.append(" ")
              .append("{Nid1: ").append(Get.getTextForComponent(nid1))
              .append(", Nid2: ").append(Get.getTextForComponent(nid2))
              .append(", str3: ").append(str3).append(" ")
              .append(Get.stampService()
                      .describeStampSequence(this.getStampSequence())).append("}");
      return builder;
   }

   public Nid1_Nid2_Str3_VersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   private Nid1_Nid2_Str3_VersionImpl(Nid1_Nid2_Str3_VersionImpl old, int stampSequence) {
      super(old.getChronology(), stampSequence);
      this.setNid1(old.nid1);
      this.setNid2(old.nid2);
      this.setStr3(old.str3);

   }

   public Nid1_Nid2_Str3_VersionImpl(SemanticChronology container, 
           int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.nid1 = data.getNid();
      this.nid2 = data.getNid();
      this.str3 = data.getUTF();
   }

   @Override
   public void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putNid(this.nid1);
      data.putNid(this.nid2);
      data.putUTF(this.str3);
   }

   @Override
   @SuppressWarnings("unchecked")
   public <V extends Version> V makeAnalog(int stampSequence) {
      final Nid1_Nid2_Str3_VersionImpl newVersion = new Nid1_Nid2_Str3_VersionImpl(this, stampSequence);
      getChronology().addVersion(newVersion);
      return (V) newVersion;
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      Nid1_Nid2_Str3_VersionImpl another = (Nid1_Nid2_Str3_VersionImpl) other;
      if (this.nid1 != another.nid1) {
         editDistance++;
      }
      if (this.nid2 != another.nid2) {
         editDistance++;
      }
      if (this.str3 == null ? another.str3 != null : !this.str3.equals(another.str3)) {
         editDistance++;
      }
      return editDistance;
   }

   @Override
   public int getNid1() {
      return this.nid1;
   }

   @Override
   public void setNid1(int nid) {
      this.nid1 = nid;
   }

   @Override
   public int getNid2() {
      return this.nid2;
   }

   @Override
   public void setNid2(int nid) {
      this.nid2 = nid;
   }

   @Override
   public String getStr3() {
      return this.str3;
   }

   @Override
   public void setStr3(String value) {
      this.str3 = value;
   }
}
