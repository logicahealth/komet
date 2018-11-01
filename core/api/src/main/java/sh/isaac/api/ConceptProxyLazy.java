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

package sh.isaac.api;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 * Used mostly to boot strap parts of the system that may come up with nids, prior to the other services being ready
 * {@link ConceptProxyLazy}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConceptProxyLazy implements ConceptSpecification 
{
	private int nid;
	
	public ConceptProxyLazy(int conceptId) 
	{
		this.nid = conceptId;
	}
	
	@Override
	public int getNid() throws NoSuchElementException
	{
		return nid;
	}

	@Override
	public List<UUID> getUuidList()
	{
		return Get.identifierService().getUuidsForNid(nid);
	}

	@Override
	public String getFullyQualifiedName()
	{
		return Get.defaultCoordinate().getLanguageCoordinate().getFullyQualifiedName(nid, Get.defaultCoordinate()).get();
	}

	@Override
	public Optional<String> getRegularName()
	{
		return Get.defaultCoordinate().getLanguageCoordinate().getRegularName(nid, Get.defaultCoordinate());
	}

	@Override
	public int hashCode()
	{
		return nid;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof ConceptSpecification))
		{
			return false;
		}
		ConceptSpecification other = (ConceptSpecification) obj;
		if (nid != other.getNid())
		{
			return false;
		}
		return true;
	}
}
