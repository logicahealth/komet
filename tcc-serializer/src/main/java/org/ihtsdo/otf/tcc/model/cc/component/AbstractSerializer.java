package org.ihtsdo.otf.tcc.model.cc.component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by kec on 7/13/14.
 */
public abstract class AbstractSerializer<C extends ConceptComponent<R, C>, R extends Revision<R, C>> {

    public void serialize(DataOutput output, Collection<C> collection) throws IOException {
        if (collection == null) {
            output.writeInt(0);
        } else {
            output.writeInt(collection.size());
            for (C c : collection) {
                serialize(output, c);
            }
        }
    }

    public void deserialize(DataInput input, CollectionCollector<C> collector) throws IOException {
        int collectionSize = input.readInt();
        collector.init(collectionSize);
        for (int i = 0; i < collectionSize; i++) {
            C component = newComponent();
            deserialize(input, component);
            collector.add(component);
        }
    }

    public void serialize(DataOutput output, C cc) throws IOException {
        ConceptComponentSerializer.serialize(output, cc);
        serializePrimordial(output, cc);
        if (cc.revisions != null) {
            output.writeShort(cc.revisions.size());
            for (R r : cc.revisions) {
                ConceptComponentRevisionSerializer.serialize(output, r);
                serializeRevision(output, r);
            }
        } else {
            output.writeShort(0);
        }
    }

    protected abstract void serializePrimordial(DataOutput output, C cc) throws IOException;

    protected abstract void serializeRevision(DataOutput output, R r) throws IOException;


    public C deserialize(DataInput input, C cc) throws IOException {
        ConceptComponentSerializer.deserialize(input, cc);
        deserializePrimordial(input, cc);
        short revisions = input.readShort();
        for (int i = 0; i < revisions; i++) {
            R r = newRevision();
            r.primordialComponent = cc;
            ConceptComponentRevisionSerializer.deserialize(input, r);
            deserializeRevision(input, r);
            cc.addRevisionNoRedundancyCheck(r);
        }
        return cc;
    }

    protected abstract void deserializePrimordial(DataInput input, C cc) throws IOException;

    protected abstract R newRevision();

    protected abstract C newComponent();

    protected abstract void deserializeRevision(DataInput input, R r) throws IOException;



}
