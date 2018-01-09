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



package sh.isaac.rxnorm.rrf;

//~--- JDK imports ------------------------------------------------------------

import java.sql.ResultSet;
import java.sql.SQLException;

//~--- classes ----------------------------------------------------------------

/**
 * The Class RXNCONSO.
 */
public class RXNCONSO {
   /** The cvf. */
   public String rxcui, lat, rxaui, saui, scui, sab, tty, code, str, suppress, cvf;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new rxnconso.
    *
    * @param rs the rs
    * @throws SQLException the SQL exception
    */
   public RXNCONSO(ResultSet rs)
            throws SQLException {
      this.rxcui    = rs.getString("RXCUI");
      this.lat      = rs.getString("LAT");
      this.rxaui    = rs.getString("RXAUI");
      this.saui     = rs.getString("SAUI");
      this.scui     = rs.getString("SCUI");
      this.sab      = rs.getString("SAB");
      this.tty      = rs.getString("TTY");
      this.code     = rs.getString("CODE");
      this.str      = rs.getString("STR");
      this.suppress = rs.getString("SUPPRESS");
      this.cvf      = rs.getString("CVF");
   }
   

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "RXNCONSO [rxcui=" + rxcui + ", lat=" + lat + ", rxaui=" + rxaui + ", saui=" + saui + ", scui=" + scui + ", sab=" + sab + ", tty=" + tty + ", code=" + code + ", str="
            + str + ", suppress=" + suppress + ", cvf=" + cvf + "]";
   }
}

