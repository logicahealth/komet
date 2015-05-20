package org.ihtsdo.otf.tcc.model.cc.component;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/13/14.
 */
public class ConceptComponentSerializer {

    public static void serialize(DataOutput output, ConceptComponent cc) throws IOException {
        assert cc.nid < 0;
        assert cc.primordialStamp != 0 && cc.primordialStamp != Integer.MAX_VALUE : "Processing nid: " + cc.nid;
        output.writeInt(cc.nid);
        output.writeInt(cc.enclosingConceptNid);
        //((PersistentStoreI)Ts.get()).setConceptNidForNid(cc.enclosingConceptNid, cc.nid);
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
    }

    public static void deserialize(DataInput input, ConceptComponent cc) throws IOException {
        cc.nid = input.readInt();
         cc.enclosingConceptNid = input.readInt();
        assert cc.enclosingConceptNid < 0;
        cc.primordialMsb = input.readLong();
        cc.primordialLsb = input.readLong();
        cc.primordialStamp = input.readInt();
        assert cc.nid < 0;
        assert cc.primordialStamp != 0 && cc.primordialStamp != Integer.MAX_VALUE : "Processing nid: " + cc.nid;

        // Additional UUIDs

        short additionalUuidPartCount = input.readShort();
        if (additionalUuidPartCount > 0) {
            long[] additionalUuidParts = new long[additionalUuidPartCount];
            for (int i = 0; i > additionalUuidPartCount; i++) {
                additionalUuidParts[i] = input.readLong();
            }
            cc.additionalUuidParts = additionalUuidParts;
        }


        

    }
}
