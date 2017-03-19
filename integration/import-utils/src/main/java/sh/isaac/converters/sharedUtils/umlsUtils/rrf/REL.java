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

/**
 * The Class REL.
 */
public class REL {
   /** The stype 1. */
   private final String cui1, aui1, stype1;

   /** The rel. */
   private String rel;

   /** The cui 2. */
   private final String cui2;

   /** The aui 2. */
   private final String aui2;

   /** The stype 2. */
   private final String stype2;

   /** The rela. */
   private String rela;

   /** The rui. */
   private final String rui;

   /** The srui. */
   private final String srui;

   /** The sab. */
   private final String sab;

   /** The sl. */
   private final String sl;

   /** The rg. */
   private final String rg;

   /** The dir. */
   private final String dir;

   /** The suppress. */
   private final String suppress;

   /** The cvf. */
   private final String cvf;

   /** The target SAB. */
   private String targetSAB;

   /** The target CODE. */
   private String targetCODE;

   /** The source SAB. */
   private final String sourceSAB;

   /** The rel hash. */
   private UUID sourceUUID_, targetUUID_, relHash_;

   /** The looked up 2. */
   private final boolean lookedUp2_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new rel.
    *
    * @param sourceSab the source sab
    * @param rs the rs
    * @param lookedUp2 the looked up 2
    * @param isRxNorm the is rx norm
    * @param relReverser the rel reverser
    * @throws SQLException the SQL exception
    */
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

   /**
    * Read.
    *
    * @param sourceSab the source sab
    * @param rs the rs
    * @param lookedUp2 the looked up 2
    * @param allowedCUIs the allowed CU is
    * @param cuiSkipCounter the cui skip counter
    * @param isRxNorm the is rx norm
    * @param relReverser the rel reverser
    * @return the list
    * @throws SQLException the SQL exception
    */
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

   /**
    * Gets the cvf.
    *
    * @return the cvf
    */
   public String getCvf() {
      return this.cvf;
   }

   /**
    * Gets the dir.
    *
    * @return the dir
    */
   public String getDir() {
      return this.dir;
   }

   /**
    * Gets the inverse rel hash.
    *
    * @param nameToRelMapper the name to rel mapper
    * @return the inverse rel hash
    */
   public UUID getInverseRelHash(Function<String, Relationship> nameToRelMapper) {
      // reverse the direction of the rels, and the source/target
      final String relInverse  = nameToRelMapper.apply(this.rel)
                                                .getFSNName();
      String       relaInverse = null;

      if (this.rela != null) {
         relaInverse = nameToRelMapper.apply(this.rela)
                                      .getFSNName();
      }

      return UUID.nameUUIDFromBytes(new String(relInverse + relaInverse + this.targetUUID_ +
            this.sourceUUID_).getBytes());
   }

   /**
    * Gets the rel.
    *
    * @return the rel
    */
   public String getRel() {
      return this.rel;
   }

   /**
    * Gets the rel hash.
    *
    * @return the rel hash
    */
   public UUID getRelHash() {
      if (this.relHash_ == null) {
         this.relHash_ = UUID.nameUUIDFromBytes(new String(this.rel + this.rela + this.sourceUUID_ +
               this.targetUUID_).getBytes());
      }

      return this.relHash_;
   }

   /**
    * Gets the rela.
    *
    * @return the rela
    */
   public String getRela() {
      return this.rela;
   }

   /**
    * Gets the rg.
    *
    * @return the rg
    */
   public String getRg() {
      return this.rg;
   }

   /**
    * Gets the rui.
    *
    * @return the rui
    */
   public String getRui() {
      return this.rui;
   }

   /**
    * Gets the sab.
    *
    * @return the sab
    */
   public String getSab() {
      return this.sab;
   }

   /**
    * Gets the sl.
    *
    * @return the sl
    */
   public String getSl() {
      return this.sl;
   }

   /**
    * Gets the source AUI.
    *
    * @return the source AUI
    */
   public String getSourceAUI() {
      return this.lookedUp2_ ? this.aui2
                             : this.aui1;
   }

   /**
    * Gets the source CUI.
    *
    * @return the source CUI
    */
   public String getSourceCUI() {
      return this.lookedUp2_ ? this.cui2
                             : this.cui1;
   }

   /**
    * Gets the source SAB.
    *
    * @return the source SAB
    */
   public String getSourceSAB() {
      return this.sourceSAB;
   }

   /**
    * Gets the source UUID.
    *
    * @return the source UUID
    */
   public UUID getSourceUUID() {
      return this.sourceUUID_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the source UUID.
    *
    * @param sourceUUID the new source UUID
    */
   public void setSourceUUID(UUID sourceUUID) {
      this.sourceUUID_ = sourceUUID;
      this.relHash_    = null;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the srui.
    *
    * @return the srui
    */
   public String getSrui() {
      return this.srui;
   }

   /**
    * Gets the stype 1.
    *
    * @return the stype 1
    */
   public String getStype1() {
      return this.stype1;
   }

   /**
    * Gets the stype 2.
    *
    * @return the stype 2
    */
   public String getStype2() {
      return this.stype2;
   }

   /**
    * Gets the suppress.
    *
    * @return the suppress
    */
   public String getSuppress() {
      return this.suppress;
   }

   /**
    * Gets the target AUI.
    *
    * @return the target AUI
    */
   public String getTargetAUI() {
      return this.lookedUp2_ ? this.aui1
                             : this.aui2;
   }

   /**
    * Gets the target CUI.
    *
    * @return the target CUI
    */
   public String getTargetCUI() {
      return this.lookedUp2_ ? this.cui1
                             : this.cui2;
   }

   /**
    * Gets the target code.
    *
    * @return the target code
    */
   public String getTargetCode() {
      return this.targetCODE;
   }

   /**
    * Gets the target SAB.
    *
    * @return the target SAB
    */
   public String getTargetSAB() {
      return this.targetSAB;
   }

   /**
    * Gets the target UUID.
    *
    * @return the target UUID
    */
   public UUID getTargetUUID() {
      return this.targetUUID_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the target UUID.
    *
    * @param targetUUID the new target UUID
    */
   public void setTargetUUID(UUID targetUUID) {
      this.targetUUID_ = targetUUID;
      this.relHash_    = null;
   }
}

