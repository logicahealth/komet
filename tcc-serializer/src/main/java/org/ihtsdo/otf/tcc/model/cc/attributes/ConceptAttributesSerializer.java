package org.ihtsdo.otf.tcc.model.cc.attributes;

import org.ihtsdo.otf.tcc.model.cc.component.AbstractSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/13/14.
 */
public class ConceptAttributesSerializer extends AbstractSerializer<ConceptAttributes, ConceptAttributesRevision> {
    private static ConceptAttributesSerializer singleton;
    public static ConceptAttributesSerializer get() {
        if (singleton == null) {
            singleton = new ConceptAttributesSerializer();
        }
        return singleton;
    }
    @Override
    protected void serializePrimordial(DataOutput output, ConceptAttributes cc) throws IOException {
        output.writeBoolean(cc.defined);
    }

    @Override
    protected void serializeRevision(DataOutput output, ConceptAttributesRevision r) throws IOException {
        output.writeBoolean(r.defined);
    }

    @Override
    public void deserializePrimordial(DataInput input, ConceptAttributes cc) throws IOException {
        cc.defined = input.readBoolean();
    }

    @Override
    public ConceptAttributesRevision newRevision() {
        return new ConceptAttributesRevision();
    }

    @Override
    public ConceptAttributes newComponent() {
        return new ConceptAttributes();
    }

    @Override
    protected void deserializeRevision(DataInput input, ConceptAttributesRevision r) throws IOException {
        r.defined = input.readBoolean();
    }
}
