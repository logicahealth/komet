package org.ihtsdo.otf.tcc.dto;

import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.TtkStamp;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesRevision;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionRevision;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierLong;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierString;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierUuid;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaChronicle;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.logicgraph.TtkLogicGraphRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_array_of_bytearray.TtkRefexArrayOfByteArrayMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_array_of_bytearray.TtkRefexArrayOfByteArrayRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_boolean.TtkRefexBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_boolean.TtkRefexBooleanRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_boolean.TtkRefexUuidBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_boolean.TtkRefexUuidBooleanRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int.TtkRefexUuidUuidUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int.TtkRefexUuidUuidUuidIntRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string.TtkRefexUuidUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string.TtkRefexUuidUuidUuidStringRevision;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicRevision;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipRevision;
import org.ihtsdo.otf.tcc.dto.taxonomy.Taxonomy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.DataOutputStream;

/**
 * Created by kec on 2/15/15.
 */
public class JaxbForDto {

    private static JAXBContext singleton;

    public static JAXBContext get() throws JAXBException {
        if (singleton == null) {
            singleton = JAXBContext.newInstance(
                    TtkConceptChronicle.class,
                    TtkStamp.class,
                    TtkRevision.class,
                    TtkComponentChronicle.class,
                    TtkConceptAttributesChronicle.class,
                    TtkConceptAttributesRevision.class,
                    TtkDescriptionChronicle.class,
                    TtkDescriptionRevision.class,
                    TtkIdentifier.class,
                    TtkIdentifierLong.class,
                    TtkIdentifierString.class,
                    TtkIdentifierUuid.class,
                    TtkMediaChronicle.class,
                    TtkMediaRevision.class,
                    TtkLogicGraphMemberChronicle.class,
                    TtkLogicGraphRevision.class,
                    TtkRefexArrayOfByteArrayMemberChronicle.class,
                    TtkRefexArrayOfByteArrayRevision.class,
                    TtkRefexBooleanMemberChronicle.class,
                    TtkRefexBooleanRevision.class,
                    TtkRefexIntMemberChronicle.class,
                    TtkRefexIntRevision.class,
                    TtkRefexLongMemberChronicle.class,
                    TtkRefexLongRevision.class,
                    TtkRefexMemberChronicle.class,
                    TtkRefexRevision.class,
                    TtkRefexStringMemberChronicle.class,
                    TtkRefexStringRevision.class,
                    TtkRefexUuidMemberChronicle.class,
                    TtkRefexUuidRevision.class,
                    TtkRefexUuidBooleanMemberChronicle.class,
                    TtkRefexUuidBooleanRevision.class,
                    TtkRefexUuidFloatMemberChronicle.class,
                    TtkRefexUuidFloatRevision.class,
                    TtkRefexUuidIntMemberChronicle.class,
                    TtkRefexUuidLongMemberChronicle.class,
                    TtkRefexUuidLongRevision.class,
                    TtkRefexUuidStringMemberChronicle.class,
                    TtkRefexUuidStringRevision.class,
                    TtkRefexUuidUuidMemberChronicle.class,
                    TtkRefexUuidUuidRevision.class,
                    TtkRefexUuidUuidStringMemberChronicle.class,
                    TtkRefexUuidUuidStringRevision.class,
                    TtkRefexUuidUuidUuidMemberChronicle.class,
                    TtkRefexUuidUuidUuidRevision.class,
                    TtkRefexUuidUuidUuidStringMemberChronicle.class,
                    TtkRefexUuidUuidUuidStringRevision.class,
                    TtkRefexUuidUuidUuidFloatMemberChronicle.class,
                    TtkRefexUuidUuidUuidFloatRevision.class,
                    TtkRefexUuidUuidUuidIntMemberChronicle.class,
                    TtkRefexUuidUuidUuidIntRevision.class,
                    TtkRefexUuidUuidUuidLongMemberChronicle.class,
                    TtkRefexUuidUuidUuidLongRevision.class,
                    TtkRefexDynamicMemberChronicle.class,
                    TtkRefexDynamicRevision.class,
                    TtkRelationshipChronicle.class,
                    TtkRelationshipRevision.class,
                    Wrapper.class
            );
        }
        return singleton;
    }

}
