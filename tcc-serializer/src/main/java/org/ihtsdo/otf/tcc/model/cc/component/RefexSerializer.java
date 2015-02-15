package org.ihtsdo.otf.tcc.model.cc.component;

import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/13/14.
 */
public abstract class RefexSerializer<C extends RefexMember<R, C>, R extends RefexRevision<R, C>> extends AbstractSerializer<C, R> {


    @Override
    protected final void serializePrimordial(DataOutput output, C cc) throws IOException {
        output.writeInt(cc.assemblageNid);
        output.writeInt(cc.referencedComponentNid);
        serializePrimordialFields(output, cc);
    }

    protected abstract void serializePrimordialFields(DataOutput output, C cc) throws IOException;

    @Override
    public final void deserializePrimordial(DataInput input, C cc) throws IOException {
        cc.assemblageNid = input.readInt();
        cc.referencedComponentNid = input.readInt();
        deserializePrimordialFields(input, cc);
    }

    public abstract void deserializePrimordialFields(DataInput input, C cc) throws IOException;

}
