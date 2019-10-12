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

package sh.isaac.model.observable.version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.naming.InvalidNameException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.MutableDynamicVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableDynamicVersion;
import sh.isaac.model.observable.CommitAwareDynamicProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.semantic.DynamicUsageDescriptionImpl;
import sh.isaac.model.semantic.DynamicUtilityImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.DynamicImpl;

/**
 * 
 * @author darmbrust
 *
 */
public class ObservableDynamicVersionImpl extends ObservableAbstractSemanticVersionImpl implements ObservableDynamicVersion
{
	ObjectProperty<DynamicData[]> dataProperty;

	/**
	 * Instantiates a new observable description impl.
	 *
	 * @param stampedVersion the stamped version
	 * @param chronology the chronology
	 */
	public ObservableDynamicVersionImpl(DynamicVersion stampedVersion, ObservableSemanticChronology chronology)
	{
		super(stampedVersion, chronology);
	}

	public ObservableDynamicVersionImpl(ObservableDynamicVersionImpl versionToClone, ObservableSemanticChronology chronology)
	{
		super(versionToClone, chronology);
		setData(versionToClone.getData());
	}

	public ObservableDynamicVersionImpl(UUID primordialUuid, UUID referencedComponentUuid, int assemblageNid)
	{
		super(VersionType.STRING, primordialUuid, referencedComponentUuid, assemblageNid);
	}

	@Override
	public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec)
	{
		ObservableDynamicVersionImpl analog = new ObservableDynamicVersionImpl(this, getChronology());
		copyLocalFields(analog);
		analog.setModuleNid(ec.getModuleNid());
		analog.setAuthorNid(ec.getAuthorNid());
		analog.setPathNid(ec.getPathNid());
		return (V) analog;
	}

	@Override
	public <V extends Version> V makeAnalog(EditCoordinate ec)
	{
		DynamicVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
		ObservableDynamicVersionImpl newObservableVersion = new ObservableDynamicVersionImpl(newVersion, (ObservableSemanticChronology) chronology);

		((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
		return (V) newObservableVersion;
	}

	@Override
	public ObjectProperty<DynamicData[]> dataProperty()
	{
		if (this.stampedVersionProperty == null && this.dataProperty == null)
		{
			this.dataProperty = new CommitAwareDynamicProperty(this, "Dynamic Data", new DynamicData[0]);
		}
		if (this.dataProperty == null)
		{
			this.dataProperty = new CommitAwareDynamicProperty(this, "Dynamic Data", getData());
		}

		return this.dataProperty;
	}

	@Override
	public String toString()
	{
		return "ObservableStringVersionImpl{" + DynamicUtilityImpl.toString(getData()) + '}';
	}

	@Override
	protected void updateVersion()
	{
		if (this.dataProperty != null && !Arrays.deepEquals(this.dataProperty.get(), ((MutableDynamicVersion) this.stampedVersionProperty.get()).getData()))
		{
			this.dataProperty.set(((MutableDynamicVersion) this.stampedVersionProperty.get()).getData());
		}
	}

	@Override
	public List<ReadOnlyProperty<?>> getProperties()
	{
		List<ReadOnlyProperty<?>> properties = super.getProperties();

		properties.add(dataProperty());
		return properties;
	}

	@Override
	protected List<Property<?>> getEditableProperties3()
	{
		List<Property<?>> properties = new ArrayList<>();
		properties.add(dataProperty());
		return properties;
	}

	@Override
	protected void copyLocalFields(SemanticVersion analog)
	{
		if (analog instanceof ObservableDynamicVersionImpl)
		{
			ObservableDynamicVersionImpl observableAnalog = (ObservableDynamicVersionImpl) analog;
			observableAnalog.setData(this.getData());
		}
		else if (analog instanceof DynamicImpl)
		{
			DynamicImpl simpleAnalog = (DynamicImpl) analog;
			simpleAnalog.setData(this.getData());
		}
		else
		{
			throw new IllegalStateException("Can't handle class: " + analog.getClass());
		}
	}

	@Override
	public Chronology createChronologyForCommit(int stampSequence)
	{
		SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
		DynamicImpl newVersion = new DynamicImpl(sc, stampSequence);
		copyLocalFields(newVersion);
		sc.addVersion(newVersion);
		return sc;
	}

	@Override
	public List<BooleanSupplier> setData(DynamicData[] data, boolean delayValidation)
	{
		if (this.stampedVersionProperty == null)
		{
			this.dataProperty();
		}
		if (this.dataProperty != null)
		{
			if (this.stampedVersionProperty == null)
			{
				throw new RuntimeException("Not supported");
				//TODO if this is s used case, need to figure out how to handle validations...
			}
			this.dataProperty.set(data);
		}

		if (this.stampedVersionProperty != null)
		{
			return ((MutableDynamicVersion) this.stampedVersionProperty.get()).setData(data, delayValidation);
		}
		else
		{
			//We didn't have a chance to fire the validator
			throw new RuntimeException("Unsupported");
		}
	}

	@Override
	public String dataToString()
	{
		return DynamicUtilityImpl.toString(getData());
	}

	@Override
	public DynamicData[] getData()
	{
		if (this.dataProperty != null)
		{
			return this.dataProperty.get();
		}

		return ((DynamicVersion) this.stampedVersionProperty.get()).getData();
	}

	@Override
	public DynamicData getData(int columnNumber) throws IndexOutOfBoundsException
	{
		return getData()[columnNumber];
	}

	@Override
	public DynamicData getData(String columnName) throws InvalidNameException
	{
		for (final DynamicColumnInfo ci : getDynamicUsageDescription().getColumnInfo())
		{
			if (ci.getColumnName().equals(columnName))
			{
				return getData(ci.getColumnOrder());
			}
		}

		throw new InvalidNameException("Could not find a column with name '" + columnName + "'");
	}

	@Override
	public DynamicUsageDescription getDynamicUsageDescription()
	{
		return DynamicUsageDescriptionImpl.read(this.getAssemblageNid());
	}
}
