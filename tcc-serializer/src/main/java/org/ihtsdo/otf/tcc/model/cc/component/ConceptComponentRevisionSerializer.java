package org.ihtsdo.otf.tcc.model.cc.component;

import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/13/14.
 */
public class ConceptComponentRevisionSerializer {
    public static void serialize(DataOutput output, Revision r) throws IOException {
        output.writeInt(r.stamp);
    }

    public static void deserialize(DataInput input, Revision r) throws IOException {
        r.stamp = input.readInt();
    }
}
