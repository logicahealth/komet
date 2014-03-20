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
import java.util.Set;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;

/**
 * {@link RefexVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RefexVersion extends RefexChronicle implements RefexVersionBI
{

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#stampIsInRange(int, int)
	 */
	@Override
	public boolean stampIsInRange(int min, int max)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#toUserString(org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI)
	 */
	@Override
	public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#getAllNidsForVersion()
	 */
	@Override
	public Set<Integer> getAllNidsForVersion() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#getAuthorNid()
	 */
	@Override
	public int getAuthorNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#getModuleNid()
	 */
	@Override
	public int getModuleNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#getChronicle()
	 */
	@Override
	public ComponentChronicleBI<RefexVersion> getChronicle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#getPosition()
	 */
	@Override
	public Position getPosition() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#getStamp()
	 */
	@Override
	public int getStamp()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#getStatus()
	 */
	@Override
	public Status getStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#isBaselineGeneration()
	 */
	@Override
	public boolean isBaselineGeneration()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#versionsEqual(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate, org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate, java.lang.Boolean)
	 */
	@Override
	public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI#isActive()
	 */
	@Override
	public boolean isActive() throws IOException
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.coordinate.VersionPointBI#getTime()
	 */
	@Override
	public long getTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.coordinate.VersionPointBI#getPathNid()
	 */
	@Override
	public int getPathNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI#makeBlueprint(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate, org.ihtsdo.otf.tcc.api.blueprint.IdDirective, org.ihtsdo.otf.tcc.api.blueprint.RefexDirective)
	 */
	@Override
	public RefexCAB makeBlueprint(ViewCoordinate viewCoordinate, IdDirective idDirective, RefexDirective refexDirective) throws IOException, InvalidCAB,
			ContradictionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI#refexFieldsEqual(org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI)
	 */
	@Override
	public boolean refexFieldsEqual(RefexVersionBI another)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
