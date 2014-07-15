package org.ihtsdo.otf.tcc.model.cc.refex;

import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.model.cc.component.CollectionCollector;
import org.ihtsdo.otf.tcc.model.cc.refex.type_array_of_bytearray.ArrayOfByteArrayMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_array_of_bytearray.ArrayOfByteArraySerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_boolean.BooleanMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_boolean.BooleanSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_int.IntMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_int.IntSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_long.LongMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_long.LongSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_membership.MembershipMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_membership.MembershipSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_boolean.NidBooleanMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_boolean.NidBooleanSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_float.NidFloatMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_float.NidFloatSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_int.NidIntMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_int.NidIntSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_long.NidLongMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_long.NidLongSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid.NidNidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid.NidNidSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid.NidNidNidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid.NidNidNidSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_float.NidNidNidFloatMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_float.NidNidNidFloatSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_int.NidNidNidIntMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_int.NidNidNidIntSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_long.NidNidNidLongMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_long.NidNidNidLongSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_string.NidNidNidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_string.NidNidNidStringSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_string.NidNidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_string.NidNidStringSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string.NidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string.NidStringSerializer;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by kec on 7/14/14.
 */
public class RefexGenericSerializer {
    private static RefexGenericSerializer singleton;

    public static RefexGenericSerializer get() {
        if (singleton == null) {
            singleton = new RefexGenericSerializer();
        }
        return singleton;
    }
    public void deserialize(DataInput input, CollectionCollector<RefexMember<?, ?>> collector) throws IOException {
        int collectionSize = input.readInt();
        collector.init(collectionSize);
        for (int i = 0; i < collectionSize; i++) {
            RefexType type = RefexType.readType(input);
            RefexMember m;
            switch (type) {
                case ARRAY_BYTEARRAY:
                    m = new ArrayOfByteArrayMember();
                    ArrayOfByteArraySerializer.get().deserialize(input, (ArrayOfByteArrayMember) m);
                    break;
                case BOOLEAN:
                    m = new BooleanMember();
                    BooleanSerializer.get().deserialize(input, (BooleanMember) m);
                    break;

                case CID:
                    m = new NidMember();
                    NidSerializer.get().deserialize(input, (NidMember) m);
                    break;

                case CID_BOOLEAN:
                    m = new NidBooleanMember();
                    NidBooleanSerializer.get().deserialize(input, (NidBooleanMember) m);
                    break;

                case CID_CID:
                    m = new NidNidMember();
                    NidNidSerializer.get().deserialize(input, (NidNidMember) m);
                    break;

                case CID_CID_CID:
                    m = new NidNidNidMember();
                    NidNidNidSerializer.get().deserialize(input, (NidNidNidMember) m);
                    break;

                case CID_CID_CID_FLOAT:
                    m = new NidNidNidFloatMember();
                    NidNidNidFloatSerializer.get().deserialize(input, (NidNidNidFloatMember) m);
                    break;

                case CID_CID_CID_INT:
                    m = new NidNidNidIntMember();
                    NidNidNidIntSerializer.get().deserialize(input, (NidNidNidIntMember) m);
                    break;

                case CID_CID_CID_LONG:
                    m = new NidNidNidLongMember();
                    NidNidNidLongSerializer.get().deserialize(input, (NidNidNidLongMember) m);
                    break;

                case CID_CID_CID_STRING:
                    m = new NidNidNidStringMember();
                    NidNidNidStringSerializer.get().deserialize(input, (NidNidNidStringMember) m);
                    break;

                case CID_CID_STR:
                    m = new NidNidStringMember();
                    NidNidStringSerializer.get().deserialize(input, (NidNidStringMember) m);
                    break;

                case CID_FLOAT:
                    m = new NidFloatMember();
                    NidFloatSerializer.get().deserialize(input, (NidFloatMember) m);
                    break;

                case CID_INT:
                    m = new NidIntMember();
                    NidIntSerializer.get().deserialize(input, (NidIntMember) m);
                    break;

                case CID_LONG:
                    m = new NidLongMember();
                    NidLongSerializer.get().deserialize(input, (NidLongMember) m);
                    break;

                case CID_STR:
                    m = new NidStringMember();
                    NidStringSerializer.get().deserialize(input, (NidStringMember) m);
                    break;

                case INT:
                    m = new IntMember();
                    IntSerializer.get().deserialize(input, (IntMember) m);
                    break;

                case LONG:
                    m = new LongMember();
                    LongSerializer.get().deserialize(input, (LongMember) m);
                    break;

                case MEMBER:
                    m = new MembershipMember();
                    MembershipSerializer.get().deserialize(input, (MembershipMember) m);
                    break;

                case STR:
                    m = new StringMember();
                    StringSerializer.get().deserialize(input, (StringMember) m);
                    break;

                default:
                    throw new UnsupportedOperationException("Can't handle: " + type);
            }
            collector.add(m);
        }
    }

    public void serialize(DataOutput output, Collection<RefexMember<?, ?>> members) throws IOException {
        if (members == null) {
            output.writeInt(0);
        } else {
            output.writeInt(members.size());
            for (RefexMember m : members) {
                m.getRefexType().writeType(output);

                switch (m.getRefexType()) {
                    case ARRAY_BYTEARRAY:
                        ArrayOfByteArraySerializer.get().serialize(output, (ArrayOfByteArrayMember) m);
                        break;
                    case BOOLEAN:
                        BooleanSerializer.get().serialize(output, (BooleanMember) m);
                        break;

                    case CID:
                        NidSerializer.get().serialize(output, (NidMember) m);
                        break;

                    case CID_BOOLEAN:
                        NidBooleanSerializer.get().serialize(output, (NidBooleanMember) m);
                        break;

                    case CID_CID:
                        NidNidSerializer.get().serialize(output, (NidNidMember) m);
                        break;

                    case CID_CID_CID:
                        NidNidNidSerializer.get().serialize(output, (NidNidNidMember) m);
                        break;

                    case CID_CID_CID_FLOAT:
                        NidNidNidFloatSerializer.get().serialize(output, (NidNidNidFloatMember) m);
                        break;

                    case CID_CID_CID_INT:
                        NidNidNidIntSerializer.get().serialize(output, (NidNidNidIntMember) m);
                        break;

                    case CID_CID_CID_LONG:
                        NidNidNidLongSerializer.get().serialize(output, (NidNidNidLongMember) m);
                        break;

                    case CID_CID_CID_STRING:
                        NidNidNidStringSerializer.get().serialize(output, (NidNidNidStringMember) m);
                        break;

                    case CID_CID_STR:
                        NidNidStringSerializer.get().serialize(output, (NidNidStringMember) m);
                        break;

                    case CID_FLOAT:
                        NidFloatSerializer.get().serialize(output, (NidFloatMember) m);
                        break;

                    case CID_INT:
                        NidIntSerializer.get().serialize(output, (NidIntMember) m);
                        break;

                    case CID_LONG:
                        NidLongSerializer.get().serialize(output, (NidLongMember) m);
                        break;

                    case CID_STR:
                        NidStringSerializer.get().serialize(output, (NidStringMember) m);
                        break;

                    case INT:
                        IntSerializer.get().serialize(output, (IntMember) m);
                        break;

                    case LONG:
                        LongSerializer.get().serialize(output, (LongMember) m);
                        break;

                    case MEMBER:
                        MembershipSerializer.get().serialize(output, (MembershipMember) m);
                        break;

                    case STR:
                        StringSerializer.get().serialize(output, (StringMember) m);
                        break;

                    default:
                        throw new UnsupportedOperationException("Can't handle: " + m.getRefexType());
                }

            }
        }

    }
}


