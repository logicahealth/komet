/*
 * Created on Oct 18, 2004
 */

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

package sh.isaac.convert.mojo.vhat.data.dto;

public class CodeSystem
{
	private String name;
	private Long vuid;
	private String description;
	private String copyright;
	private String copyrightURL;
	private String preferredDesignationType;
	private String action;
	private Version version;

	public CodeSystem(String name, Long vuid, String description, String copyright, String copyrightURL, String preferredDesignationType, String action)
	{
		this.name = name;
		this.vuid = vuid;
		this.description = description;
		this.copyright = copyright;
		this.copyrightURL = copyrightURL;
		this.action = action;
		this.preferredDesignationType = preferredDesignationType;
	}

	/**
	 * @return Returns the copyright.
	 */
	public String getCopyright()
	{
		return copyright;
	}

	/**
	 * @return the copyrightURL
	 */
	public String getCopyrightURL()
	{
		return copyrightURL;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	public String toString()
	{
		return this.getName();
	}

	public Long getVuid()
	{
		return vuid;
	}

	/**
	 * @return Returns the designationType
	 */
	public String getPreferredDesignationType()
	{
		return preferredDesignationType;
	}

	public Version getVersion()
	{
		return version;
	}

	public void setVersion(Version version)
	{
		this.version = version;
	}

	public String getAction()
	{
		return action;
	}
}
