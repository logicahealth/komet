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

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex2.types.RefexDataBI;

/**
 * {@link RefexAnalog}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RefexAnalog extends RefexVersion implements RefexAnalogBI
{

	/**
	 * @see org.ihtsdo.otf.tcc.api.AnalogBI#setNid(int)
	 */
	@Override
	public void setNid(int nid) throws PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.AnalogBI#setStatus(org.ihtsdo.otf.tcc.api.coordinate.Status)
	 */
	@Override
	public void setStatus(Status nid) throws PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.AnalogBI#setAuthorNid(int)
	 */
	@Override
	public void setAuthorNid(int nid) throws PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.AnalogBI#setModuleNid(int)
	 */
	@Override
	public void setModuleNid(int nid) throws PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.AnalogBI#setPathNid(int)
	 */
	@Override
	public void setPathNid(int nid) throws PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.AnalogBI#setTime(long)
	 */
	@Override
	public void setTime(long time) throws PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setAssemblageNid(int)
	 */
	@Override
	public void setAssemblageNid(int assemblageNid) throws IOException, PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setReferencedComponentNid(int)
	 */
	@Override
	public void setReferencedComponentNid(int componentNid) throws IOException, PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setData(RefexDataBI[])
	 */
	@Override
	public void setData(RefexDataBI[] data) throws PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setData(int, RefexDataBI)
	 */
	@Override
	public void setData(int columnNumber, RefexDataBI data) throws IndexOutOfBoundsException, PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setRefexUsageDescriptorNid(int)
	 */
	@Override
	public void setRefexUsageDescriptorNid(int refexUsageDescriptorNid)
	{
		// TODO Auto-generated method stub
		
	}
}
