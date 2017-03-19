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



package sh.isaac.converters.sharedUtils.umlsUtils.rrf;

//~--- JDK imports ------------------------------------------------------------

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.converters.sharedUtils.umlsUtils.Relationship;

//~--- classes ----------------------------------------------------------------

public class REL {
   private final String cui1, aui1, stype1;
private String rel;
private final String cui2;
private final String aui2;
private final String stype2;
private String rela;
private final String rui;
private final String srui;
private final String sab;
private final String sl;
private final String rg;
private final String dir;
private final String suppress;
private final String cvf;
private String targetSAB;
private String targetCODE;
private final String sourceSAB;
   private UUID    sourceUUID_, targetUUID_, relHash_;
   private final boolean lookedUp2_;

   //~--- constructors --------------------------------------------------------

   private REL(String sourceSab,
               ResultSet rs,
               boolean lookedUp2,
               boolean isRxNorm,
               Function<String, String> relReverser)
            throws SQLException {
      this.sourceSAB  = sourceSab;
      this.lookedUp2_ = lookedUp2;
      this.cui1       = rs.getString(isRxNorm ? "RXCUI1"
                                         : "CUI1");
      this.aui1       = rs.getString(isRxNorm ? "RXAUI1"
                                         : "AUI1");
      this.stype1     = rs.getString("STYPE1");
      this.rel        = rs.getString("REL");
      this.cui2       = rs.getString(isRxNorm ? "RXCUI2"
                                         : "CUI2");
      this.aui2       = rs.getString(isRxNorm ? "RXAUI2"
                                         : "AUI2");
      this.stype2     = rs.getString("STYPE2");
      this.rela       = rs.getString("RELA");
      this.rui        = rs.getString("RUI");
      this.srui       = rs.getString("SRUI");
      this.sab        = rs.getString("SAB");
      this.sl         = rs.getString("SL");
      this.rg         = rs.getString("RG");
      this.dir        = rs.getString("DIR");
      this.suppress   = rs.getString("SUPPRESS");
      this.cvf        = (rs.getObject("CVF") == null) ? null
            : rs.getString("CVF");  // integer or string

      if ((lookedUp2 ? this.aui2
                     : this.aui1) != null) {
         // when the AUI is not null, we have a couple extra vars to read
         this.targetSAB  = rs.getString("targetSAB");
         this.targetCODE = rs.getString("targetCODE");
      }

      if (!this.lookedUp2_) {
         this.rel  = relReverser.apply(this.rel);
         this.rela = relReverser.apply(this.rela);
      }
   }

   //~--- methods -------------------------------------------------------------

   public static List<REL> read(String sourceSab,
                                ResultSet rs,
                                boolean lookedUp2,
                                Set<String> allowedCUIs,
                                AtomicInteger cuiSkipCounter,
                                boolean isRxNorm,
                                Function<String, String> relReverser)
            throws SQLException {
      final ArrayList<REL> result = new ArrayList<>();

      while (rs.next()) {
         final REL rel = new REL(sourceSab, rs, lookedUp2, isRxNorm, relReverser);

         if ((allowedCUIs != null) && (!allowedCUIs.contains(rel.cui1) ||!allowedCUIs.contains(rel.cui2))) {
            cuiSkipCounter.getAndIncrement();
            continue;
         }

         result.add(rel);
      }

      rs.close();
      return result;
   }

   //~--- get methods ---------------------------------------------------------

   public String getCvf() {
      return this.cvf;
   }

   public String getDir() {
      return this.dir;
   }

   public UUID getInverseRelHash(Function<String, Relationship> nameToRelMapper) {
      // reverse the direction of the rels, and the source/target
      final String relInverse  = nameToRelMapper.apply(this.rel)
                                          .getFSNName();
      String relaInverse = null;

      if (this.rela != null) {
         relaInverse = nameToRelMapper.apply(this.rela)
                                      .getFSNName();
      }

      return UUID.nameUUIDFromBytes(new String(relInverse + relaInverse + this.targetUUID_ + this.sourceUUID_).getBytes());
   }

   public String getRel() {
      return this.rel;
   }

   public UUID getRelHash() {
      if (this.relHash_ == null) {
         this.relHash_ = UUID.nameUUIDFromBytes(new String(this.rel + this.rela + this.sourceUUID_ + this.targetUUID_).getBytes());
      }

      return this.relHash_;
   }

   public String getRela() {
      return this.rela;
   }

   public String getRg() {
      return this.rg;
   }

   public String getRui() {
      return this.rui;
   }

   public String getSab() {
      return this.sab;
   }

   public String getSl() {
      return this.sl;
   }

   public String getSourceAUI() {
      return this.lookedUp2_ ? this.aui2
                        : this.aui1;
   }

   public String getSourceCUI() {
      return this.lookedUp2_ ? this.cui2
                        : this.cui1;
   }

   public String getSourceSAB() {
      return this.sourceSAB;
   }

   public UUID getSourceUUID() {
      return this.sourceUUID_;
   }

   //~--- set methods ---------------------------------------------------------

   public void setSourceUUID(UUID sourceUUID) {
      this.sourceUUID_ = sourceUUID;
      this.relHash_    = null;
   }

   //~--- get methods ---------------------------------------------------------

   public String getSrui() {
      return this.srui;
   }

   public String getStype1() {
      return this.stype1;
   }

   public String getStype2() {
      return this.stype2;
   }

   public String getSuppress() {
      return this.suppress;
   }

   public String getTargetAUI() {
      return this.lookedUp2_ ? this.aui1
                        : this.aui2;
   }

   public String getTargetCUI() {
      return this.lookedUp2_ ? this.cui1
                        : this.cui2;
   }

   public String getTargetCode() {
      return this.targetCODE;
   }

   public String getTargetSAB() {
      return this.targetSAB;
   }

   public UUID getTargetUUID() {
      return this.targetUUID_;
   }

   //~--- set methods ---------------------------------------------------------

   public void setTargetUUID(UUID targetUUID) {
      this.targetUUID_ = targetUUID;
      this.relHash_    = null;
   }
}

