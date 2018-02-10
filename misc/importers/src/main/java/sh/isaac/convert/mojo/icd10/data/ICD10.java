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

package sh.isaac.convert.mojo.icd10.data;

public class ICD10
{
	private String orderNumber;
	private String code;
	private String header;
	private String shortDescription;
	private String longDescription;

	public ICD10(String order, String code, String header, String shortDescription, String longDescription)
	{
		this.orderNumber = order.trim();
		this.code = code.trim();
		// 0 if the code is a "header" - not valid for HIPAA-covered transactions.
		// 1 if the code is valid for submission for HIPAA-covered transactions.
		this.header = header.trim();
		this.shortDescription = shortDescription.trim();
		this.longDescription = longDescription.trim();
	}

	public String toString()
	{
		return this.orderNumber + ":" + this.code + ":" + this.header + ":" + this.shortDescription + ":" + this.longDescription;
	}

	public String getOrderNumber()
	{
		return this.orderNumber;
	}

	public String getCode()
	{
		return this.code;
	}

	public String getHeader()
	{
		return this.header;
	}

	public boolean isHeader()
	{
		if (header.equals("0"))
		{
			return true;
		}
		return false;
	}

	public String getShortDescription()
	{
		return this.shortDescription;
	}

	public String getLongDescription()
	{
		return this.longDescription;
	}
}