package org.ihtsdo.otf.tcc.model.cc.component;

import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_array_of_bytearray.ArrayOfByteArrayMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_array_of_bytearray.ArrayOfByteArraySerializer;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMember;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/13/14.
 */
public class ConceptComponentSerializer {

    public static void serialize(DataOutput output, ConceptComponent cc) throws IOException {
        assert cc.nid != 0;
        assert cc.primordialStamp != 0 && cc.primordialStamp != Integer.MAX_VALUE : "Processing nid: " + cc.nid;
        output.writeInt(cc.nid);
        output.writeInt(cc.enclosingConceptNid);
        output.writeLong(cc.primordialMsb);
        output.writeLong(cc.primordialLsb);
        output.writeInt(cc.primordialStamp);

        // Additional UUIDs

        if (cc.additionalUuidParts != null) {
            output.writeShort(cc.additionalUuidParts.length);
            for (long uuidPart : cc.additionalUuidParts) {
                output.writeLong(uuidPart);
            }
        } else {
            output.writeShort(0);
        }

        // static refexes
        if (cc.annotations != null) {
            output.writeInt(cc.annotations.size());
            for (Object obj : cc.annotations) {
                RefexMember rx = (RefexMember) obj;
                rx.getRefexType().writeType(output);
                switch (rx.getRefexType()) {
                    case ARRAY_BYTEARRAY:
                        ArrayOfByteArraySerializer.get().serialize(output, (ArrayOfByteArrayMember) rx);
                        break;
                    case BOOLEAN:
                    case CID:
                    case CID_BOOLEAN:
                    case CID_CID:
                    case CID_CID_CID:
                    case CID_CID_CID_FLOAT:
                    case CID_CID_CID_INT:
                    case CID_CID_CID_LONG:
                    case CID_CID_CID_STRING:
                    case CID_CID_STR:
                    case CID_FLOAT:
                    case CID_INT:
                    case CID_LONG:
                    case CID_STR:
                    case INT:
                    case LONG:
                    case MEMBER:
                    case STR:
                    default:
                        throw new RuntimeException("Can't handle type: " + rx.getRefexType());

                }

            }
        } else {
            output.writeInt(0);
        }

        // dynamic refexes

        if (cc.annotationsDynamic != null) {
            output.writeInt(cc.annotationsDynamic.size());
            for (Object obj : cc.annotationsDynamic) {
                RefexDynamicMember member = (RefexDynamicMember) obj;
                RefexDynamicSerializer.get().serialize(output, member);
            }
        } else {
            output.writeInt(0);
        }
    }

    public static void deserialize(DataInput input, ConceptComponent cc) throws IOException {
        assert cc.nid != 0;
        assert cc.primordialStamp != 0 && cc.primordialStamp != Integer.MAX_VALUE : "Processing nid: " + cc.nid;
        cc.nid = input.readInt();
        cc.enclosingConceptNid = input.readInt();
        cc.primordialMsb = input.readLong();
        cc.primordialLsb = input.readLong();
        cc.primordialStamp = input.readInt();

        // Additional UUIDs

        short additionalUuidPartCount = input.readShort();
        if (additionalUuidPartCount > 0) {
            long[] additionalUuidParts = new long[additionalUuidPartCount];
            for (int i = 0; i > additionalUuidPartCount; i++) {
                additionalUuidParts[i] = input.readLong();
            }
            cc.additionalUuidParts = additionalUuidParts;
        }

        // static refexes
        int staticRefexCount = input.readInt();
        if (staticRefexCount > 0) {
            for (int i = 0; i < staticRefexCount; i++) {
                RefexType rxType = RefexType.readType(input);
                RefexMember member;
                switch (rxType) {
                    case ARRAY_BYTEARRAY:
                        member = ArrayOfByteArraySerializer.get().deserialize(input, new ArrayOfByteArrayMember());
                        break;
                    case BOOLEAN:
                    case CID:
                    case CID_BOOLEAN:
                    case CID_CID:
                    case CID_CID_CID:
                    case CID_CID_CID_FLOAT:
                    case CID_CID_CID_INT:
                    case CID_CID_CID_LONG:
                    case CID_CID_CID_STRING:
                    case CID_CID_STR:
                    case CID_FLOAT:
                    case CID_INT:
                    case CID_LONG:
                    case CID_STR:
                    case INT:
                    case LONG:
                    case MEMBER:
                    case STR:
                    default:
                        throw new RuntimeException("Can't handle type: " + rxType);

                }
                cc.addAnnotation(member);
            }

        }

        // dynamic refexes
        int dynamicRefexCount = input.readInt();
        if (dynamicRefexCount > 0) {
            for (int i = 0; i < dynamicRefexCount; i++) {
                RefexDynamicMember member = new RefexDynamicMember();
                RefexDynamicSerializer.get().deserialize(input, member);
                cc.addDynamicAnnotation(member);
            }
        }
    }
}
