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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.impl.sememe;

import java.io.IOException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescriptionBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArrayBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeStringBI;
import gov.vha.isaac.ochre.model.constants.IsaacMetadataConstants;

/**
 *
 * See {@link DynamicSememeUsageDescriptionBI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeUsageDescription implements DynamicSememeUsageDescriptionBI {

    int refexUsageDescriptorSequence_;
    String sememeUsageDescription_;
    String name_;
    DynamicSememeColumnInfo[] refexColumnInfo_;
    ObjectChronologyType referencedComponentTypeRestriction_;
    SememeType referencedComponentTypeSubRestriction_;

    protected static final Logger logger = Logger.getLogger(DynamicSememeUsageDescription.class.getName());

    private static LRUDynamicSememeDescriptorCache<Integer, DynamicSememeUsageDescription> cache_
            = new LRUDynamicSememeDescriptorCache<Integer, DynamicSememeUsageDescription>(25);

    /**
     *
     * Test if dyn sememe
     *
     * @param assemblageNidOrSequence
     * @return
     */
    public static boolean isDynamicSememe(int assemblageNidOrSequence) {
        if (assemblageNidOrSequence >= 0 || Get.identifierService().getChronologyTypeForNid(assemblageNidOrSequence) == ObjectChronologyType.CONCEPT) {
            try {
                read(assemblageNidOrSequence);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static DynamicSememeUsageDescription read(int assemblageNidOrSequence) {
        //TODO (artf231860) [REFEX] maybe? implement a mechanism to allow the cache to be updated... for now
        //cache is uneditable, and may be wrong, if the user changes the definition of a dynamic sememe.  Perhaps
        //implement a callback to clear the cache when we know a change of  a certain type happened instead?

        int sequence = Get.identifierService().getConceptSequence(assemblageNidOrSequence);

        DynamicSememeUsageDescription temp = cache_.get(sequence);
        if (temp == null) {
            logger.log(Level.FINEST, "Cache miss on DynamicSememeUsageDescription Cache");
            temp = new DynamicSememeUsageDescription(sequence);
            cache_.put(sequence, temp);
        }
        return temp;
    }

    /**
     * Invent DynamicSememeUsageDescription info for other sememe types (that
     * aren't dynamic), otherwise, calls {@link #read(int)} if it is a dynamic
     * sememe
     *
     * @param sememe the sememe in question
     */
    public static DynamicSememeUsageDescription mockOrRead(SememeChronology<?> sememe) {
        DynamicSememeUsageDescription dsud = new DynamicSememeUsageDescription();
        dsud.name_ = Get.conceptDescriptionText(sememe.getAssemblageSequence());
        dsud.referencedComponentTypeRestriction_ = null;
        dsud.referencedComponentTypeSubRestriction_ = null;
        dsud.refexUsageDescriptorSequence_ = sememe.getAssemblageSequence();
        dsud.sememeUsageDescription_ = "-";

        switch (sememe.getSememeType()) {
            case COMPONENT_NID:
                dsud.refexColumnInfo_ = new DynamicSememeColumnInfo[]{new DynamicSememeColumnInfo(
                    Get.identifierService().getUuidPrimordialFromConceptSequence(sememe.getAssemblageSequence()).get(),
                    0, MetaData.NID.getPrimordialUuid(), DynamicSememeDataType.NID, null, true, null, null)};
                break;
            case LONG:
                dsud.refexColumnInfo_ = new DynamicSememeColumnInfo[]{new DynamicSememeColumnInfo(
                    Get.identifierService().getUuidPrimordialFromConceptSequence(sememe.getAssemblageSequence()).get(),
                    0, MetaData.LONG.getPrimordialUuid(), DynamicSememeDataType.LONG, null, true, null, null)};
                break;
            case DESCRIPTION:
            case STRING:
            case LOGIC_GRAPH:
                dsud.refexColumnInfo_ = new DynamicSememeColumnInfo[]{new DynamicSememeColumnInfo(
                    Get.identifierService().getUuidPrimordialFromConceptSequence(sememe.getAssemblageSequence()).get(),
                    0, MetaData.STRING.getPrimordialUuid(), DynamicSememeDataType.STRING, null, true, null, null)};
                break;
            case MEMBER:
                dsud.refexColumnInfo_ = new DynamicSememeColumnInfo[]{};
                break;
            case DYNAMIC:
                return read(sememe.getAssemblageSequence());
            case UNKNOWN:
            default:
                throw new RuntimeException("Use case not yet supported");
        }
        return dsud;
    }

    private DynamicSememeUsageDescription() {
        //For use by the mock static method
    }

    /**
     * Read the RefexUsageDescription data from the database for a given nid.
     *
     * Note that most users should call {@link #read(int)} instead, as that
     * utilizes a cache. This always reads directly from the DB.
     *
     * @param refexUsageDescriptorSequence
     */
    @SuppressWarnings("unchecked")
    public DynamicSememeUsageDescription(int refexUsageDescriptorSequence) {
        refexUsageDescriptorSequence_ = refexUsageDescriptorSequence;
        TreeMap<Integer, DynamicSememeColumnInfo> allowedColumnInfo = new TreeMap<>();
        ConceptChronology<?> assemblageConcept = Get.conceptService().getConcept(refexUsageDescriptorSequence_);

        for (SememeChronology<? extends DescriptionSememe<?>> descriptionSememe : assemblageConcept.getConceptDescriptionList()) {
            @SuppressWarnings("rawtypes")
            Optional<LatestVersion<DescriptionSememe<?>>> descriptionVersion = ((SememeChronology) descriptionSememe)
                    .getLatestVersion(DescriptionSememe.class, StampCoordinates.getDevelopmentLatestActiveOnly());

            if (descriptionVersion.isPresent()) {
                @SuppressWarnings("rawtypes")
                DescriptionSememe ds = descriptionVersion.get().value();

                if (ds.getDescriptionTypeConceptSequence() == MetaData.DEFINITION_DESCRIPTION_TYPE.getConceptSequence()) {
                    Optional<SememeChronology<? extends SememeVersion<?>>> nestesdSememe = Get.sememeService().getSememesForComponentFromAssemblage(ds.getNid(),
                            IsaacMetadataConstants.DYNAMIC_SEMEME_DEFINITION_DESCRIPTION.getSequence()).findAny();
                    if (nestesdSememe.isPresent()) {
                        sememeUsageDescription_ = ds.getText();
                    };
                }
                if (ds.getDescriptionTypeConceptSequence() == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence()) {
                    name_ = ds.getText();
                }
                if (sememeUsageDescription_ != null && name_ != null) {
                    break;
                }

            }
        }

        if (StringUtils.isEmpty(sememeUsageDescription_)) {
            throw new RuntimeException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for "
                    + "a DynamicSememeData Refex Type.  It must contain a description of type Definition with an annotation of type "
                    + "DynamicSememe.DYNAMIC_SEMEME_DEFINITION_DESCRIPTION");
        }

        Get.sememeService().getSememesForComponent(assemblageConcept.getNid()).forEach(sememe
                -> {
            if (sememe.getSememeType() == SememeType.DYNAMIC) {
                @SuppressWarnings("rawtypes")
                Optional<LatestVersion<? extends DynamicSememe>> sememeVersion
                        = ((SememeChronology) sememe).getLatestVersion(DynamicSememe.class, StampCoordinates.getDevelopmentLatestActiveOnly());

                if (sememeVersion.isPresent()) {
                    @SuppressWarnings("rawtypes")
                    DynamicSememe ds = sememeVersion.get().value();
                    DynamicSememeDataBI[] refexDefinitionData = ds.getData();

                    if (sememe.getAssemblageSequence() == IsaacMetadataConstants.DYNAMIC_SEMEME_EXTENSION_DEFINITION.getSequence()) {
                        if (refexDefinitionData == null || refexDefinitionData.length < 3 || refexDefinitionData.length > 7) {
                            throw new RuntimeException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for "
                                    + "a DynamicSememeData Refex Type.  It must contain at least 3 columns in the DynamicSememeDataBI attachment, and no more than 7.");
                        }

                        //col 0 is the column number, 
                        //col 1 is the concept with col name 
                        //col 2 is the column data type, stored as a string.
                        //col 3 (if present) is the default column data, stored as a subtype of DynamicSememeDataBI
                        //col 4 (if present) is a boolean field noting whether the column is required (true) or optional (false or null)
                        //col 5 (if present) is the validator {@link DynamicSememeValidatorType}, stored as a string array.
                        //col 6 (if present) is the validatorData for the validator in column 5, stored as a subtype of DynamicSememeDataBI
                        try {
                            int column = (Integer) refexDefinitionData[0].getDataObject();
                            UUID descriptionUUID = (UUID) refexDefinitionData[1].getDataObject();
                            DynamicSememeDataType type = DynamicSememeDataType.valueOf((String) refexDefinitionData[2].getDataObject());
                            DynamicSememeDataBI defaultData = null;
                            if (refexDefinitionData.length > 3) {
                                defaultData = (refexDefinitionData[3] == null ? null : refexDefinitionData[3]);
                            }

                            if (defaultData != null && type.getDynamicSememeMemberClass() != refexDefinitionData[3].getDynamicSememeDataType().getDynamicSememeMemberClass()) {
                                throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for "
                                        + "a DynamicSememeData Refex Type.  The type of the column (column 3) must match the type of the defaultData (column 4)");
                            }

                            Boolean columnRequired = null;
                            if (refexDefinitionData.length > 4) {
                                columnRequired = (refexDefinitionData[4] == null ? null : (Boolean) refexDefinitionData[4].getDataObject());
                            }

                            DynamicSememeValidatorType[] validators = null;
                            DynamicSememeDataBI[] validatorsData = null;
                            if (refexDefinitionData.length > 5) {
                                if (refexDefinitionData[5] != null
                                        && ((DynamicSememeArrayBI<DynamicSememeStringBI>) refexDefinitionData[5]).getDataArray().length > 0) {
                                    DynamicSememeArrayBI<DynamicSememeStringBI> readValidators = (DynamicSememeArrayBI<DynamicSememeStringBI>) refexDefinitionData[5];
                                    validators = new DynamicSememeValidatorType[readValidators.getDataArray().length];
                                    for (int i = 0; i < validators.length; i++) {
                                        validators[i] = DynamicSememeValidatorType.valueOf((String) readValidators.getDataArray()[i].getDataObject());
                                    }
                                }
                                if (refexDefinitionData.length > 6) {
                                    if (refexDefinitionData[6] != null
                                            && ((DynamicSememeArrayBI<? extends DynamicSememeDataBI>) refexDefinitionData[6]).getDataArray().length > 0) {
                                        DynamicSememeArrayBI<? extends DynamicSememeDataBI> readValidatorsData
                                                = (DynamicSememeArrayBI<? extends DynamicSememeDataBI>) refexDefinitionData[6];
                                        validatorsData = new DynamicSememeDataBI[readValidatorsData.getDataArray().length];
                                        for (int i = 0; i < validators.length; i++) {
                                            if (readValidatorsData.getDataArray()[i] != null) {
                                                validatorsData[i] = readValidatorsData.getDataArray()[i];
                                            } else {
                                                validatorsData[i] = null;
                                            }
                                        }
                                    }
                                }
                            }

                            allowedColumnInfo.put(column, new DynamicSememeColumnInfo(assemblageConcept.getPrimordialUuid(), column, descriptionUUID, type,
                                    defaultData, columnRequired, validators, validatorsData));
                        } catch (Exception e) {
                            throw new RuntimeException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for "
                                    + "a DynamicSememeData Refex Type.  The first column must have a data type of integer, and the third column must be a string "
                                    + "that is parseable as a DynamicSememeDataType");
                        }
                    } else if (sememe.getAssemblageSequence() == IsaacMetadataConstants.DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getSequence()) {
                        if (refexDefinitionData == null || refexDefinitionData.length < 1) {
                            throw new RuntimeException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for "
                                    + "a DynamicSememeData Refex Type.  If it contains a " + IsaacMetadataConstants.DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getFSN()
                                    + " then it must contain a single column of data, of type string, parseable as a " + ObjectChronologyType.class.getName());
                        }

                        //col 0 is Referenced component restriction information - as a string. 
                        try {
                            ObjectChronologyType type = ObjectChronologyType.parse(refexDefinitionData[0].getDataObject().toString());
                            if (type == ObjectChronologyType.UNKNOWN_NID) {
                                //just ignore - it shouldn't have been saved that way anyway.
                            } else {
                                referencedComponentTypeRestriction_ = type;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for "
                                    + "a DynamicSememeData Refex Type.  The component type restriction annotation has an invalid value");
                        }

                        //col 1 is an optional Referenced component sub-restriction information - as a string.
                        if (refexDefinitionData.length > 1 && refexDefinitionData[1] != null) {
                            try {
                                SememeType type = SememeType.parse(refexDefinitionData[1].getDataObject().toString());
                                if (type == SememeType.UNKNOWN) {
                                    //just ignore - it shouldn't have been saved that way anyway.
                                } else {
                                    referencedComponentTypeSubRestriction_ = type;
                                }
                            } catch (Exception e) {
                                throw new RuntimeException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for "
                                        + "a DynamicSememeData Refex Type.  The component type restriction annotation has an invalid value");
                            }
                        } else {
                            referencedComponentTypeSubRestriction_ = null;
                        }
                    }
                }

            }
        });

        refexColumnInfo_ = new DynamicSememeColumnInfo[allowedColumnInfo.size()];

        int i = 0;
        for (int key : allowedColumnInfo.keySet()) {
            if (key != i) {
                throw new RuntimeException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for "
                        + "a DynamicSememeData Refex Type.  It must contain sequential column numbers, with no gaps, which start at 0.");
            }
            refexColumnInfo_[i++] = allowedColumnInfo.get(key);
        }
    }

    /*
	 * @see DynamicSememeUsageDescriptionBI#getDynamicSememeUsageDescriptorSequence()
     */
    @Override
    public int getDynamicSememeUsageDescriptorSequence() {
        return refexUsageDescriptorSequence_;
    }

    /*
	 * @see DynamicSememeUsageDescriptionBI#getDynamicSememeUsageDescription()
     */
    @Override
    public String getDynamicSememeUsageDescription() {
        return sememeUsageDescription_;
    }

    /*
	 * @see gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescriptionBI#getDyanmicSememeName()
     */
    @Override
    public String getDyanmicSememeName() {
        return name_;
    }


    /*
	 * @see gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescriptionBI#getColumnInfo()
     */
    @Override
    public DynamicSememeColumnInfo[] getColumnInfo() {
        if (refexColumnInfo_ == null) {
            refexColumnInfo_ = new DynamicSememeColumnInfo[]{};
        }
        return refexColumnInfo_;
    }

    /*
	 * @see gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescriptionBI#getReferencedComponentTypeRestriction()
     */
    @Override
    public ObjectChronologyType getReferencedComponentTypeRestriction() {
        return referencedComponentTypeRestriction_;
    }

    /*
	 * @see gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescriptionBI#getReferencedComponentTypeSubRestriction()
     */
    @Override
    public SememeType getReferencedComponentTypeSubRestriction() {
        return referencedComponentTypeSubRestriction_;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + refexUsageDescriptorSequence_;
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DynamicSememeUsageDescription other = (DynamicSememeUsageDescription) obj;
        if (refexUsageDescriptorSequence_ != other.refexUsageDescriptorSequence_) {
            return false;
        }
        return true;
    }
}
