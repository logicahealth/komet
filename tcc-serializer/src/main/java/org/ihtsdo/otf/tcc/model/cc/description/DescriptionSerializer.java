package org.ihtsdo.otf.tcc.model.cc.description;

import org.ihtsdo.otf.tcc.model.cc.component.AbstractSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class DescriptionSerializer extends AbstractSerializer<Description, DescriptionRevision> {
    private static DescriptionSerializer singleton;

    public static DescriptionSerializer get() {
        if (singleton == null) {
            singleton = new DescriptionSerializer();
        }
        return singleton;
    }

    @Override
    protected void serializePrimordial(DataOutput output, Description desc) throws IOException {
        output.writeBoolean(desc.initialCaseSignificant);
        output.writeUTF(desc.lang);
        output.writeUTF(desc.text);
        output.writeInt(desc.typeNid);

    }

    @Override
    protected void serializeRevision(DataOutput output, DescriptionRevision revision) throws IOException {
        if (revision.text.equals(revision.primordialComponent.getText())) {
            output.writeUTF("");
        } else {
            output.writeUTF(revision.text);
        }

        if (revision.lang.equals(revision.primordialComponent.getLang())) {
            output.writeUTF("");
        } else {
            output.writeUTF(revision.lang);
        }

        output.writeBoolean(revision.initialCaseSignificant);
        output.writeInt(revision.typeNid);
    }

    @Override
    public void deserializePrimordial(DataInput input, Description cc) throws IOException {
        cc.initialCaseSignificant = input.readBoolean();
        cc.lang = input.readUTF();
        cc.text = input.readUTF();
        cc.typeNid = input.readInt();

    }

    @Override
    public DescriptionRevision newRevision() {
        return new DescriptionRevision();
    }

    @Override
    public Description newComponent() {
        return new Description();
    }

    @Override
    protected void deserializeRevision(DataInput input, DescriptionRevision revision) throws IOException {
        revision.text = input.readUTF();

        if (revision.text.isEmpty()) {
            revision.text = revision.primordialComponent.getText();
        }

        revision.lang = input.readUTF();

        if (revision.lang.isEmpty()) {
            revision.lang = revision.primordialComponent.getLang();
        }

        revision.initialCaseSignificant = input.readBoolean();
        revision.typeNid                = input.readInt();

    }
}
