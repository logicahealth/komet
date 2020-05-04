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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Nid2_Version;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class Nid1_Nid2_VersionImpl
        extends AbstractVersionImpl
         implements Nid1_Nid2_Version {
   int nid1 = Integer.MAX_VALUE;
   int nid2 = Integer.MAX_VALUE;
   @Override
   public StringBuilder toString(StringBuilder builder) {
      builder.append(" ")
              .append("{Nid1: ").append(Get.getTextForComponent(nid1))
              .append(", Nid2: ").append(Get.getTextForComponent(nid2))
              .append(Get.stampService()
                      .describeStampSequence(this.getStampSequence())).append("}");
      return builder;
   }

   //~--- constructors --------------------------------------------------------

   public Nid1_Nid2_VersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   public Nid1_Nid2_VersionImpl(SemanticChronology container, 
           int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.nid1 = data.getNid();
      this.nid2 = data.getNid();
   }
   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   public void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putNid(this.nid1);
      data.putNid(this.nid2);
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
      final Nid1_Nid2_VersionImpl newVersion = new Nid1_Nid2_VersionImpl((SemanticChronology) this, stampSequence);
      newVersion.setNid1(this.nid1);
      newVersion.setNid2(this.nid2);

      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      Nid1_Nid2_VersionImpl another = (Nid1_Nid2_VersionImpl) other;
      if (this.nid1 != another.nid1) {
         editDistance++;
      }
      if (this.nid2 != another.nid2) {
         editDistance++;
      }
      
      return editDistance;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid1() {
      return nid1;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid1(int nid1) {
      this.nid1 = nid1;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid2() {
      return nid2;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid2(int nid2) {
      this.nid2 = nid2;
   }
}

