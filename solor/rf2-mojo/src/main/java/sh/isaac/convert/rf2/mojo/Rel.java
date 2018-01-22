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

import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.ParseException;

import java.util.UUID;


import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.converters.sharedUtils.sql.TableDefinition;

public class Rel implements Comparable<Rel>
{
	//private static Logger LOG = LogManager.getLogger();

	Long sctID;
	UUID id;
	long effectiveTime;
	boolean isActive;
	UUID moduleId;
	UUID sourceId;
	UUID destinationId;
	String relGroup;
	UUID typeId;
	UUID characteristicTypeId;
	UUID modifierId;
	
	public Rel(ResultSet rs, TableDefinition td) throws ParseException, SQLException
	{
		if (td.getColDataType("ID").isLong())
		{
			sctID = rs.getLong("ID");
			id = UuidT3Generator.fromSNOMED(sctID);
		}
		else
		{
			id = UUID.fromString(rs.getString("ID"));
		}
		effectiveTime  = RF2Mojo.dateParse.parse(rs.getString("EFFECTIVETIME")).getTime();
		isActive = rs.getBoolean("ACTIVE");
		moduleId = (td.getColDataType("MODULEID").isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("MODULEID")) : 
			UUID.fromString(rs.getString("MODULEID")));
		sourceId = (td.getColDataType("SOURCEID").isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("SOURCEID")) : 
			UUID.fromString(rs.getString("SOURCEID")));
		destinationId = (td.getColDataType("DESTINATIONID").isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("DESTINATIONID")) : 
			UUID.fromString(rs.getString("DESTINATIONID")));
		relGroup = rs.getString("relationshipGroup");
		typeId = (td.getColDataType("typeId").isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("typeId")) : 
			UUID.fromString(rs.getString("typeId")));
		characteristicTypeId = (td.getColDataType("characteristicTypeId").isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("characteristicTypeId")) : 
			UUID.fromString(rs.getString("characteristicTypeId")));
		modifierId = (td.getColDataType("modifierId").isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("modifierId")) : 
			UUID.fromString(rs.getString("modifierId")));
	}

	@Override
	public int compareTo(Rel o)
	{
		return Long.compare(effectiveTime, o.effectiveTime);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Rel [sctID=" + sctID + ", id=" + id + ", isActive=" + isActive + ", moduleId=" + moduleId
				+ ", sourceId=" + sourceId + ", destinationId=" + destinationId + ", relGroup=" + relGroup + ", typeId="
				+ typeId + ", characteristicTypeId=" + characteristicTypeId + "]";
	}
}
