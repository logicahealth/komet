/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package sh.isaac.convert.mojo.rxnorm.rrf;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link RXNSAT}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RXNSAT
{
	public String rxcui, rxaui, stype, code, atui, satui, atn, sab, atv, suppress, cvf;

	/**
	 * Instantiates a new rxnsat.
	 *
	 * @param rs the rs
	 * @throws SQLException the SQL exception
	 */
	public RXNSAT(ResultSet rs) throws SQLException
	{
		this.rxcui = rs.getString("RXCUI");
		this.rxaui = rs.getString("RXAUI");
		this.stype = rs.getString("STYPE");
		this.code = rs.getString("CODE");
		this.atui = rs.getString("ATUI");
		this.satui = rs.getString("SATUI");
		this.atn = rs.getString("ATN");
		this.sab = rs.getString("SAB");
		this.atv = rs.getString("ATV");
		this.suppress = rs.getString("SUPPRESS");
		this.cvf = rs.getString("CVF");
	}

	@Override
	public String toString()
	{
		return "RXNSAT [rxcui=" + rxcui + ", rxaui=" + rxaui + ", stype=" + stype + ", code=" + code + ", atui=" + atui + ", satui=" + satui + ", atn=" + atn
				+ ", sab=" + sab + ", atv=" + atv + ", suppress=" + suppress + ", cvf=" + cvf + "]";
	}
}
