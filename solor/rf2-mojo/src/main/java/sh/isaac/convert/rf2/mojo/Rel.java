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



package sh.isaac.convert.rf2.mojo;

//~--- JDK imports ------------------------------------------------------------

import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.ParseException;

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.converters.sharedUtils.sql.TableDefinition;

//~--- classes ----------------------------------------------------------------

/**
 * The Class Rel.
 */
public class Rel
         implements Comparable<Rel> {
   // private static Logger LOG = LogManager.getLogger();

   /** The sct ID. */
   Long sctID;

   /** The id. */
   UUID id;

   /** The effective time. */
   long effectiveTime;

   /** The is active. */
   boolean isActive;

   /** The module id. */
   UUID moduleId;

   /** The source id. */
   UUID sourceId;

   /** The destination id. */
   UUID destinationId;

   /** The rel group. */
   String relGroup;

   /** The type id. */
   UUID typeId;

   /** The characteristic type id. */
   UUID characteristicTypeId;

   /** The modifier id. */
   UUID modifierId;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new rel.
    *
    * @param rs the rs
    * @param td the td
    * @throws ParseException the parse exception
    * @throws SQLException the SQL exception
    */
   public Rel(ResultSet rs, TableDefinition td)
            throws ParseException, SQLException {
      if (td.getColDataType("ID")
            .isLong()) {
         this.sctID = rs.getLong("ID");
         this.id    = UuidT3Generator.fromSNOMED(this.sctID);
      } else {
         this.id = UUID.fromString(rs.getString("ID"));
      }

      this.effectiveTime = RF2Mojo.dateParse.parse(rs.getString("EFFECTIVETIME"))
            .getTime();
      this.isActive      = rs.getBoolean("ACTIVE");
      this.moduleId      = (td.getColDataType("MODULEID")
                              .isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("MODULEID"))
                                        : UUID.fromString(rs.getString("MODULEID")));
      this.sourceId      = (td.getColDataType("SOURCEID")
                              .isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("SOURCEID"))
                                        : UUID.fromString(rs.getString("SOURCEID")));
      this.destinationId = (td.getColDataType("DESTINATIONID")
                              .isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("DESTINATIONID"))
                                        : UUID.fromString(rs.getString("DESTINATIONID")));
      this.relGroup = rs.getString("relationshipGroup");
      this.typeId = (td.getColDataType("typeId")
                       .isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("typeId"))
                                 : UUID.fromString(rs.getString("typeId")));
      this.characteristicTypeId = (td.getColDataType("characteristicTypeId")
                                     .isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("characteristicTypeId"))
            : UUID.fromString(rs.getString("characteristicTypeId")));
      this.modifierId = (td.getColDataType("modifierId")
                           .isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("modifierId"))
                                     : UUID.fromString(rs.getString("modifierId")));
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   @Override
   public int compareTo(Rel o) {
      return Long.compare(this.effectiveTime, o.effectiveTime);
   }

   /**
    * To string.
    *
    * @return the string
    */

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "Rel [sctID=" + this.sctID + ", id=" + this.id + ", isActive=" + this.isActive + ", moduleId=" +
             this.moduleId + ", sourceId=" + this.sourceId + ", destinationId=" + this.destinationId + ", relGroup=" +
             this.relGroup + ", typeId=" + this.typeId + ", characteristicTypeId=" + this.characteristicTypeId + "]";
   }
}

