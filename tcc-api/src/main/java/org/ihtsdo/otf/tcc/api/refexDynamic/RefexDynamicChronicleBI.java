package org.ihtsdo.otf.tcc.api.refexDynamic;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescriptionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;

/**
 * 
 * {@link RefexDynamicChronicleBI}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface RefexDynamicChronicleBI<A extends RefexDynamicVersionBI<A>> extends ComponentChronicleBI<RefexDynamicVersionBI<A>> {
    /**
     * Assemblage is an assembled collection of objects. Used to identify the Refex that this item is a 
     * member of.  Used instead of RefexExtensionId because of confusion with the component the 
     * Refex Extends, or the ReferencedComponentId.
     * 
     * Note that in the new RefexDynanamicAPI - this linked concept must contain the column definitions 
     * for using this concept as a Refex container.  The linked concept must define the combination of data
     * columns being used within this Refex. The referenced concept must contains a (new style) Refex extension 
     * {@link RefexDynamicVersionBI} where the assemblage concept is {@link RefexDynamic#REFEX_DYNAMIC_DEFINITION} 
     * and the attached data is 
     * [{@link RefexDynamicIntegerBI}, {@link RefexDynamicUUIDBI}, {@link RefexDynamicStringBI}] where the int 
     * value is used to align the order with the data here, the UUID is a concept reference where the concept which 
     * should have a preferred semantic name / FSN that is suitable for describing its usage as a DynamicRefex data 
     * column and a string column which can be parsed as a member of the {@link RefexDynamicDataType} class.
     * 
     * The referenced concept (assemblage) should also contain a description of type {@link SnomedMetadataRf2#SYNONYM_RF2}
     * which itself has a refex extension of type {@link RefexDynamic#REFEX_DYNAMIC_DEFINITION_DESCRIPTION} - the value of 
     * this description should explain the the overall purpose of this Refex.
     * 
     * @return the nid that identifies the Refex that this component is a member of.
     */
    public int getAssemblageNid();

    /**
     * @return The nid of the component that is a member of the Refex specified
     *         by {@link #getAssemblageNid()}
     */
    public int getReferencedComponentNid();

    /**
     * A convenience method that reads the concept referenced in
     * {@link #getAssemblageNid()} and returns the actual column
     * information that is contained within that concept.
     */
    public RefexDynamicUsageDescriptionBI getRefexUsageDescription();

}
