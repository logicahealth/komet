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
   private String cui1, aui1, stype1, rel, cui2, aui2, stype2, rela, rui, srui, sab, sl, rg, dir, suppress, cvf,
                  targetSAB, targetCODE, sourceSAB;
   private UUID    sourceUUID_, targetUUID_, relHash_;
   private boolean lookedUp2_;

   //~--- constructors --------------------------------------------------------

   private REL(String sourceSab,
               ResultSet rs,
               boolean lookedUp2,
               boolean isRxNorm,
               Function<String, String> relReverser)
            throws SQLException {
      sourceSAB  = sourceSab;
      lookedUp2_ = lookedUp2;
      cui1       = rs.getString(isRxNorm ? "RXCUI1"
                                         : "CUI1");
      aui1       = rs.getString(isRxNorm ? "RXAUI1"
                                         : "AUI1");
      stype1     = rs.getString("STYPE1");
      rel        = rs.getString("REL");
      cui2       = rs.getString(isRxNorm ? "RXCUI2"
                                         : "CUI2");
      aui2       = rs.getString(isRxNorm ? "RXAUI2"
                                         : "AUI2");
      stype2     = rs.getString("STYPE2");
      rela       = rs.getString("RELA");
      rui        = rs.getString("RUI");
      srui       = rs.getString("SRUI");
      sab        = rs.getString("SAB");
      sl         = rs.getString("SL");
      rg         = rs.getString("RG");
      dir        = rs.getString("DIR");
      suppress   = rs.getString("SUPPRESS");
      cvf        = (rs.getObject("CVF") == null) ? null
            : rs.getString("CVF");  // integer or string

      if ((lookedUp2 ? aui2
                     : aui1) != null) {
         // when the AUI is not null, we have a couple extra vars to read
         targetSAB  = rs.getString("targetSAB");
         targetCODE = rs.getString("targetCODE");
      }

      if (!lookedUp2_) {
         rel  = relReverser.apply(rel);
         rela = relReverser.apply(rela);
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
      ArrayList<REL> result = new ArrayList<>();

      while (rs.next()) {
         REL rel = new REL(sourceSab, rs, lookedUp2, isRxNorm, relReverser);

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
      return cvf;
   }

   public String getDir() {
      return dir;
   }

   public UUID getInverseRelHash(Function<String, Relationship> nameToRelMapper) {
      // reverse the direction of the rels, and the source/target
      String relInverse  = nameToRelMapper.apply(rel)
                                          .getFSNName();
      String relaInverse = null;

      if (rela != null) {
         relaInverse = nameToRelMapper.apply(rela)
                                      .getFSNName();
      }

      return UUID.nameUUIDFromBytes(new String(relInverse + relaInverse + targetUUID_ + sourceUUID_).getBytes());
   }

   public String getRel() {
      return rel;
   }

   public UUID getRelHash() {
      if (relHash_ == null) {
         relHash_ = UUID.nameUUIDFromBytes(new String(rel + rela + sourceUUID_ + targetUUID_).getBytes());
      }

      return relHash_;
   }

   public String getRela() {
      return rela;
   }

   public String getRg() {
      return rg;
   }

   public String getRui() {
      return rui;
   }

   public String getSab() {
      return sab;
   }

   public String getSl() {
      return sl;
   }

   public String getSourceAUI() {
      return lookedUp2_ ? aui2
                        : aui1;
   }

   public String getSourceCUI() {
      return lookedUp2_ ? cui2
                        : cui1;
   }

   public String getSourceSAB() {
      return sourceSAB;
   }

   public UUID getSourceUUID() {
      return sourceUUID_;
   }

   //~--- set methods ---------------------------------------------------------

   public void setSourceUUID(UUID sourceUUID) {
      sourceUUID_ = sourceUUID;
      relHash_    = null;
   }

   //~--- get methods ---------------------------------------------------------

   public String getSrui() {
      return srui;
   }

   public String getStype1() {
      return stype1;
   }

   public String getStype2() {
      return stype2;
   }

   public String getSuppress() {
      return suppress;
   }

   public String getTargetAUI() {
      return lookedUp2_ ? aui1
                        : aui2;
   }

   public String getTargetCUI() {
      return lookedUp2_ ? cui1
                        : cui2;
   }

   public String getTargetCode() {
      return targetCODE;
   }

   public String getTargetSAB() {
      return targetSAB;
   }

   public UUID getTargetUUID() {
      return targetUUID_;
   }

   //~--- set methods ---------------------------------------------------------

   public void setTargetUUID(UUID targetUUID) {
      targetUUID_ = targetUUID;
      relHash_    = null;
   }
}

