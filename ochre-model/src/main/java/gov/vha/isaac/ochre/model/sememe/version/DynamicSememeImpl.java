/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.model.sememe.version;

import java.beans.PropertyVetoException;

import javax.naming.InvalidNameException;

import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescriptionBI;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;

/**
 *
 * @author kec
 */
public class DynamicSememeImpl extends SememeVersionImpl<DynamicSememeImpl> 
    implements MutableDynamicSememe<DynamicSememeImpl> {

	private DynamicSememeDataBI[] data_;
	
    public DynamicSememeImpl(SememeChronologyImpl<DynamicSememeImpl> container, int stampSequence, short versionSequence, DataBuffer data) {
        super(container, stampSequence, versionSequence);
        data_ = null;  //TODO dan parse the data  - where is the parser?
    }
    
    public DynamicSememeImpl(SememeChronologyImpl<DynamicSememeImpl> container, int stampSequence, short versionSequence) {
        super(container, stampSequence, versionSequence);
        data_ = null; //T?ODO dan figure out how to make an empty one... currently downstream....
    }
    
    @Override
    protected void writeVersionData(DataBuffer data) {
        super.writeVersionData(data);
        throw new UnsupportedOperationException();
    }
    @Override
    public SememeType getSememeType() {
        return SememeType.DYNAMIC;
    };

   /**
     * @see gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe#getData()
     */
	@Override
	public DynamicSememeDataBI[] getData()
	{
		return data_;
	}

	@Override
	public DynamicSememeDataBI getData(int columnNumber) throws IndexOutOfBoundsException
	{
		return data_[columnNumber];
	}

	@Override
	public DynamicSememeDataBI getData(String columnName) throws InvalidNameException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DynamicSememeUsageDescriptionBI getDynamicSememeUsageDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(DynamicSememeDataBI[] data) throws PropertyVetoException
	{
		data_ = data;
		
	}

	@Override
	public void setData(int columnNumber, DynamicSememeDataBI data) throws IndexOutOfBoundsException, PropertyVetoException
	{
		data_[columnNumber] = data;
		
	};
}
