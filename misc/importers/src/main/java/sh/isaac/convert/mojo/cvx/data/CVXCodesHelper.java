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

package sh.isaac.convert.mojo.cvx.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import sh.isaac.api.Status;

/**
 * 
 * {@link CVXCodesHelper}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class CVXCodesHelper
{
	private CVXCodesHelper()
	{
	}

	public static String getShortDescription(CVXCodes.CVXInfo info)
	{
		return info.shortDescription != null ? info.shortDescription.trim() : null;
	}

	/**
	 * Gets the value of the fullVaccinename property.
	 * 
	 * @param info
	 * 
	 * @return possible object is {@link String }
	 */
	public static String getFullVaccinename(CVXCodes.CVXInfo info)
	{
		return info.fullVaccinename != null ? info.fullVaccinename.trim() : null;
	}

	/**
	 * Gets the value of the cvxCode property.
	 * 
	 * @param info
	 * @return the code
	 * 
	 */
	public static int getCVXCode(CVXCodes.CVXInfo info)
	{
		return info.cvxCode;
	}

	/**
	 * Gets the value of the notes property.
	 * 
	 * @param info
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public static String getNotes(CVXCodes.CVXInfo info)
	{
		return info.getNotes() != null ? info.getNotes().trim() : null;
	}

	/**
	 * Gets the value of the status property.
	 * 
	 * @param info
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public static String getStatus(CVXCodes.CVXInfo info)
	{
		return info.getStatus() != null ? info.getStatus().trim() : null;
	}

	/**
	 * Gets the Ochre State value of the status property.
	 * 
	 * @param info
	 * 
	 * @return possible object is {@link Status}
	 */
	public static Status getOchreState(CVXCodes.CVXInfo info)
	{
		/*
		 * "Active", "Inactive", "Never Active", "Non-US" and "Pending"
		 */
		String status = getStatus(info).toUpperCase();
		if (status.equals("ACTIVE") || status.equals("INACTIVE"))
		{
			return Status.valueOf(status);
		}
		else if (status.equals("NEVER ACTIVE"))
		{
			return Status.INACTIVE;
		}
		else if (status.equals("NON-US"))
		{
			return Status.ACTIVE;
		}
		else if (status.equals("PENDING"))
		{
			return Status.ACTIVE;
		}
		else
		{
			throw new RuntimeException("Unexpected MVX Status \"" + getStatus(info) + "\" in entry: " + info);
		}
	}

	/**
	 * Gets the value of the lastUpdated property.
	 * 
	 * @param info
	 * 
	 * @return possible object is {@link String }
	 * @throws ParseException
	 * 
	 */
	public static Date getLastUpdatedDate(CVXCodes.CVXInfo info) throws ParseException
	{
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		return df.parse(info.getLastUpdated());
	}
}
