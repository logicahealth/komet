/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.model.cc.refex2;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexUsageDescriptionBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex2.types.RefexDataBI;

/**
 * {@link RefexChronicle}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RefexChronicle implements RefexChronicleBI
{

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getVersion(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public RefexVersionBI getVersion(ViewCoordinate c) throws ContradictionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getVersions(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public Collection<? extends RefexVersionBI> getVersions(ViewCoordinate c)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getVersions()
	 */
	@Override
	public Collection<? extends RefexVersionBI> getVersions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#isUncommitted()
	 */
	@Override
	public boolean isUncommitted()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getAllStamps()
	 */
	@Override
	public Set<Integer> getAllStamps() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getPositions()
	 */
	@Override
	public Set<Position> getPositions() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getPrimordialVersion()
	 */
	@Override
	public RefexVersionBI getPrimordialVersion()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#makeAdjudicationAnalogs(org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate, org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getEnclosingConcept()
	 */
	@Override
	public ConceptChronicleBI getEnclosingConcept()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#addAnnotation(org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI)
	 */
	@Override
	public boolean addAnnotation(org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI<?> annotation) throws IOException
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#toUserString()
	 */
	@Override
	public String toUserString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getAdditionalIds()
	 */
	@Override
	public Collection<? extends IdBI> getAdditionalIds() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getAllIds()
	 */
	@Override
	public Collection<? extends IdBI> getAllIds() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getAnnotations()
	 */
	@Override
	public Collection<? extends org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI<?>> getAnnotations() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getConceptNid()
	 */
	@Override
	public int getConceptNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getAnnotationsActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public Collection<? extends org.ihtsdo.otf.tcc.api.refex.RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getAnnotationsActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate, java.lang.Class)
	 */
	@Override
	public <T extends org.ihtsdo.otf.tcc.api.refex.RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz, Class<T> cls) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getAnnotationsActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate, int)
	 */
	@Override
	public Collection<? extends org.ihtsdo.otf.tcc.api.refex.RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz, int refexNid) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getAnnotationsActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate, int, java.lang.Class)
	 */
	@Override
	public <T extends org.ihtsdo.otf.tcc.api.refex.RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz, int refexNid, Class<T> cls)
			throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexMembersActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate, int)
	 */
	@Override
	public Collection<? extends org.ihtsdo.otf.tcc.api.refex.RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexMembersActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public Collection<? extends org.ihtsdo.otf.tcc.api.refex.RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexMembersInactive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public Collection<? extends org.ihtsdo.otf.tcc.api.refex.RefexVersionBI<?>> getRefexMembersInactive(ViewCoordinate xyz) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getNid()
	 */
	@Override
	public int getNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getPrimordialUuid()
	 */
	@Override
	public UUID getPrimordialUuid()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexMembers(int)
	 */
	@Override
	public Collection<? extends org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexes()
	 */
	@Override
	public Collection<? extends org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI<?>> getRefexes() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getUUIDs()
	 */
	@Override
	public List<UUID> getUUIDs()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#hasCurrentAnnotationMember(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate, int)
	 */
	@Override
	public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#hasCurrentRefexMember(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate, int)
	 */
	@Override
	public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getAssemblageNid()
	 */
	@Override
	public int getAssemblageNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getReferencedComponentNid()
	 */
	@Override
	public int getReferencedComponentNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getData()
	 */
	@Override
	public RefexDataBI[] getData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getData(int)
	 */
	@Override
	public RefexDataBI getData(int columnNumber) throws IndexOutOfBoundsException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getRefexUsageDescriptorNid()
	 * @see #getRefexUsageDescription()
	 */
	@Override
	public int getRefexUsageDescriptorNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getRefexUsageDescription()
	 */
	@Override
	public RefexUsageDescriptionBI getRefexUsageDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
