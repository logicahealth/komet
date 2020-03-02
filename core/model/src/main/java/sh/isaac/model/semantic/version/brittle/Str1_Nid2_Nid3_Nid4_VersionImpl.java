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
import sh.isaac.api.component.semantic.version.brittle.Str1_Nid2_Nid3_Nid4_Version;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

import java.util.Objects;

/**
 *
 * @author kec
 */
public class Str1_Nid2_Nid3_Nid4_VersionImpl 
        extends AbstractVersionImpl
         implements Str1_Nid2_Nid3_Nid4_Version {
   String str1 = null;
   int    nid2 = Integer.MAX_VALUE;
   int    nid3 = Integer.MAX_VALUE;
   int    nid4 = Integer.MAX_VALUE;
   @Override
   public StringBuilder toString(StringBuilder builder) {
      builder.append(" ")
              .append("{Str1: ").append(str1).append(" ")
              .append(", nid2: ").append(Get.conceptDescriptionText(nid2))
              .append(", Nid3: ").append(Get.conceptDescriptionText(nid3))
              .append(", Nid4: ").append(Get.conceptDescriptionText(nid4))
              .append(Get.stampService()
                      .describeStampSequence(this.getStampSequence())).append("}");
      return builder;
   }

   //~--- constructors --------------------------------------------------------

   public Str1_Nid2_Nid3_Nid4_VersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   public Str1_Nid2_Nid3_Nid4_VersionImpl(SemanticChronology container, 
           int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.str1 = data.getUTF();
      this.nid2 = data.getNid();
      this.nid3 = data.getNid();
      this.nid4 = data.getNid();
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
      data.putNid(this.nid2);
      data.putNid(this.nid3);
      data.putNid(this.nid4);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      final int stampSequence = Get.stampService()
              .getStampSequence(
                      this.getStatus(),
                      Long.MAX_VALUE,
                      ec.getAuthorNid(),
                      this.getModuleNid(),
                      ec.getPathNid());
      return setupAnalog(stampSequence);
   }


   @Override
   public <V extends Version> V makeAnalog(Transaction transaction, int authorNid) {
      final int stampSequence = Get.stampService()
              .getStampSequence(transaction,
                      this.getStatus(),
                      Long.MAX_VALUE,
                      authorNid,
                      this.getModuleNid(),
                      this.getPathNid());
      return setupAnalog(stampSequence);
   }

   public <V extends Version> V setupAnalog(int stampSequence) {
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final Str1_Nid2_Nid3_Nid4_VersionImpl newVersion = new Str1_Nid2_Nid3_Nid4_VersionImpl((SemanticChronology) this, stampSequence);
      newVersion.setStr1(this.str1);
      newVersion.setNid2(this.nid2);
      newVersion.setNid3(this.nid3);
      newVersion.setNid4(this.nid4);
      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      Str1_Nid2_Nid3_Nid4_VersionImpl another = (Str1_Nid2_Nid3_Nid4_VersionImpl) other;
      if (!Objects.equals(this.str1, another.str1)) {
         editDistance++;
      }
      if (this.nid2 != another.nid2) {
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
   public int getNid2() {
      return nid2;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid2(int nid) {
      this.nid2 = nid;
   }
}

