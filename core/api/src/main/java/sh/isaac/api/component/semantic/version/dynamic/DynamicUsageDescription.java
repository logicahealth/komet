/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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



package sh.isaac.api.component.semantic.version.dynamic;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.constants.DynamicConstants;

//~--- interfaces -------------------------------------------------------------

/**
 * {@link DynamicUsageDescription}
 *
 * In the new DynanamicSememeAPI - there are strict requirements on the structure of the
 * assemblage concept.
 * <br>
 * <br>
 * The assemblage concept must define the combination of data columns being used within this Sememe.
 * To do this, the assemblage concept must itself contain 0 or more {@link DynamicSememeVersion} annotation(s) with
 * an assemblage concept that is {@link DynamicConstants#DYNAMIC_SEMEME_EXTENSION_DEFINITION} and the attached data is<br>
 * [{@link DynamicSememeInteger}, {@link DynamicSememeUUID}, {@link DynamicSememeString}, {@link DynamicSememePolymorphic},
 * {@link DynamicSememBoolean}, {@link DynamicSememeArray< DynamicSememeString >}, {@link DynamicSememeData< DynamicSememePolymorphic >}]
 *
 * <ul>
 * <li>The int value is used to align the column order with the data array here.  The column number should be 0 indexed.
 * <li>The UUID is a concept reference where the concept should have a preferred semantic name / FQN that is
 *       suitable for the name of the DynamicSememe data column, and a description suitable for use as the description of the
 *       Dynamic data column.  Note, while any concept can be used here, and there are no specific requirements for this
 *       concept - there is a convenience method for creating one of these concepts in
 *       {@link DynamicColumnInfo#createNewDynamicSememeColumnInfoConcept(String, String, org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate, org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)}
 * <li>A string column which can be parsed as a member of the {@link DynamicSememeDataType} class, which represents
 *       the type of the column.
 * <li>An (optional) polymorphic column (any supported data type, but MUST match the data type specified in column 2) which contains
 *       the default value (if any) for this column.
 * <li>An (optional) boolean column which specifies if this column is required (true) or optional (false or null) for this column.
 * <li>An (optional) array column which contains strings which can be parsed as a member of the {@link DynamicSememeValidatorType} class, which represents
 *       the validator type(s) assigned to the the column (if any).
 * <li>An (optional) array column which contains polymorphic data (any supported data type, but MUST match the requirements of the validator specified in column 6)
 *       which contains validator data (if any) for this column.
 *
 *       The validator data array must match in size to the validator type data.  They will be evaluated as pairs.
 * </ul>
 * <br>
 * Note that while 0 rows of attached data is allowed, this would not allow the attachment of any data on the sememe.
 * <br>
 * The assemblage concept must also contain a description of type {@link IsaacMetadataAuxiliaryBinding#DEFINITION_DESCRIPTION_TYPE} which
 * itself has a semantic extension of type {@link DynamicConstants#DYNAMIC_SEMEME_DEFINITION_DESCRIPTION} - the value of
 * this description should explain the the overall purpose of this Sememe.
 * <br>
 * <br>
 * The assemblage concept may also contain a single {@link DynamicSememeVersion} annotation of type
 * {@link DynamicConstants#DYNAMIC_REFERENCED_COMPONENT_RESTRICTION} with a one or two string column(s) which can be parsed as
 * a {@link ObjectChronologyType} and a {@link VersionType}- which will restrict the type of nid that can be placed
 * into the referenced component field when creating an instance of the assemblage.
 * <br>
 * <br>
 * The class {@link DynamicUsageDescription} provides an implementation for parsing the interesting bits out of an assemblage concept.
 *
 * For an implementation on creating them,
 * See {@link sh.isaac.impl.sememe.DynamicSememeUtility#createNewDynamicSememeUsageDescriptionConcept}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface DynamicUsageDescription {
   /**
    * The ordered column information which will correspond with the data returned by {@link DynamicSememeChronicle#getData()}
    * These arrays will be the same size, and in the same order.  Will not return null.
    * @return the column information
    */
   public DynamicColumnInfo[] getColumnInfo();

   /**
    * (Convenience method).
    *
    * @return returns the FQN of the assemblage concept this was read from
    */
   public String getDynamicName();

   /**
    * Gets the dynamic usage description.
    *
    * @return A user-friendly description of the overall purpose of this Dynamic use.
    */
   public String getDynamicUsageDescription();

   /**
    * Gets the dynamic usage descriptor sequence.
    *
    * @return The sequence of the concept that the rest of the attributes of this type were read from.
    */
   public int getDynamicUsageDescriptorNid();

   /**
    * Return the {@link ObjectChronologyType} of the restriction on referenced components for this (if any - may return null)
    *
    * If there is a restriction, the nid set for the component type of this must resolve to the matching type.
    *
    * @return the referenced component type restriction
    */
   public ObjectChronologyType getReferencedComponentTypeRestriction();

   /**
    * Return the {@link VersionType} of the sub restriction on referenced components for this DynamicSememe (if any - may return null)
    *
    * If there is a restriction, the nid set for the component type of this DynamicSememe must resolve to the matching type.
    *
    * This is only applicable when {@link #getReferencedComponentTypeRestriction()} returns {@link ObjectChronologyType#SEMEME}
    *
    * @return the referenced component type sub restriction
    */
   public VersionType getReferencedComponentTypeSubRestriction();
}

