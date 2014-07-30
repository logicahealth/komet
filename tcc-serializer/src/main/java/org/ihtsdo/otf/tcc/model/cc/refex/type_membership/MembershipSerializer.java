package org.ihtsdo.otf.tcc.model.cc.refex.type_membership;

import org.ihtsdo.otf.tcc.model.cc.component.RefexSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class MembershipSerializer extends RefexSerializer<MembershipMember, MembershipRevision>{
    private static MembershipSerializer singleton;
    public static MembershipSerializer get() {
        if (singleton == null) {
            singleton = new MembershipSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordialFields(DataOutput output, MembershipMember cc) throws IOException {
        // nothing to do
    }

    @Override
    public void deserializePrimordialFields(DataInput input, MembershipMember cc) throws IOException {
        // nothing to do
    }

    @Override
    protected void serializeRevision(DataOutput output, MembershipRevision membershipRevision) throws IOException {
        // nothing to do
    }

    @Override
    public MembershipRevision newRevision() {
        return new MembershipRevision();
    }

    @Override
    public MembershipMember newComponent() {
        return new MembershipMember();
    }

    @Override
    protected void deserializeRevision(DataInput input, MembershipRevision membershipRevision) throws IOException {
        // nothing to do
    }
}
