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

package sh.isaac.metadata.source;

import java.util.UUID;
import jakarta.inject.Singleton;
// ~--- non-JDK imports --------------------------------------------------------
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.IsaacCache;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.MetadataConceptConstant;
import sh.isaac.api.constants.MetadataConceptConstantGroup;
import sh.isaac.api.constants.MetadataDynamicConstant;
import sh.isaac.api.constants.ModuleProvidedConstants;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author darmbrust Unfortunately, due to the indirect use of the LookupService within this class - and the class itself being provided by a
 *         LookupService, we cannot create these constants as static - it leads to recursion in the LookupService init which breaks things.
 */
@Service
@Singleton
public class DirectImportDynamicConstants implements ModuleProvidedConstants, IsaacCache
{
    private static DirectImportDynamicConstants cache;

    private DirectImportDynamicConstants()
    {
        // Only for HK2 to construct
    }

    /**
     * Convenience getter
     *
     * @return the isaac mapping constants
     */
    public static DirectImportDynamicConstants get()
    {
        if (cache == null)
        {
            cache = LookupService.getService(DirectImportDynamicConstants.class);
        }
        return cache;
    }

    @Override
    public void reset()
    {
        cache = null;
        for (MetadataConceptConstant mcc : getConstantsToCreate())
        {
            recursiveClear(mcc);
        }
        for (MetadataConceptConstant mcc : getConstantsForInfoOnly())
        {
            recursiveClear(mcc);
        }
    }

    private void recursiveClear(MetadataConceptConstant mcc)
    {
        mcc.clearCache();
        if (mcc instanceof MetadataConceptConstantGroup)
        {
            for (MetadataConceptConstant nested : ((MetadataConceptConstantGroup) mcc).getChildren())
            {
                recursiveClear(nested);
            }
        }
    }


    public final MetadataDynamicConstant LOINC_RECORD_ASSEMBLAGE = new MetadataDynamicConstant("LOINC record assemblage - Dynamic",
            UUID.fromString("3995e48f-c77c-5b04-88d9-549dffae95f8"),
            "A Semantic used import a subset of the LOINC table",
            new DynamicColumnInfo[] {
                    new DynamicColumnInfo(0, ObservableFields.LOINC_COMPONENT.getPrimordialUuid(), DynamicDataType.STRING, null, true),
                    new DynamicColumnInfo(1, ObservableFields.LOINC_NUMBER.getPrimordialUuid(), DynamicDataType.STRING, null, true),
                    new DynamicColumnInfo(2, ObservableFields.LOINC_STATUS.getPrimordialUuid(), DynamicDataType.STRING, null, true),
                    new DynamicColumnInfo(3, ObservableFields.LOINC_LONG_COMMON_NAME.getPrimordialUuid(), DynamicDataType.STRING, null, true),
                    new DynamicColumnInfo(4, ObservableFields.LOINC_METHOD_TYPE.getPrimordialUuid(), DynamicDataType.STRING, null, true),
                    new DynamicColumnInfo(5, ObservableFields.LOINC_PROPERTY.getPrimordialUuid(), DynamicDataType.STRING, null, true),
                    new DynamicColumnInfo(6, ObservableFields.LOINC_SCALE_TYPE.getPrimordialUuid(), DynamicDataType.STRING, null, true),
                    new DynamicColumnInfo(7, ObservableFields.LOINC_SHORT_NAME.getPrimordialUuid(), DynamicDataType.STRING, null, true),
                    new DynamicColumnInfo(8, ObservableFields.LOINC_SYSTEM.getPrimordialUuid(), DynamicDataType.STRING, null, true),
                    new DynamicColumnInfo(9, ObservableFields.LOINC_TIME_ASPECT.getPrimordialUuid(), DynamicDataType.STRING, null, true),},
            null)
    {
        {
            setParent(TermAux.EXTERNAL_DATA_ASSEMBLAGE);
        }
    };

    /**
     * Gets the constants to create.
     *
     * @return the constants to create
     */
    @Override
    public MetadataConceptConstant[] getConstantsToCreate()
    {
        return new MetadataConceptConstant[] { this.LOINC_RECORD_ASSEMBLAGE};
    }
}
