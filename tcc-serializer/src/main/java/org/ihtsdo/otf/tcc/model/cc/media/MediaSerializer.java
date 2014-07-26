package org.ihtsdo.otf.tcc.model.cc.media;

import org.ihtsdo.otf.tcc.model.cc.component.AbstractSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 7/14/14.
 */
public class MediaSerializer extends AbstractSerializer<Media, MediaRevision> {
    private static MediaSerializer singleton;
    public static MediaSerializer get() {
        if (singleton == null) {
            singleton = new MediaSerializer();
        }
        return singleton;
    }
    @Override
    protected void serializePrimordial(DataOutput output, Media media) throws IOException {
        output.writeUTF(media.format);
        output.writeInt(media.image.length);
        output.write(media.image);
        output.writeUTF(media.textDescription);
        output.writeInt(media.typeNid);

    }

    @Override
    protected void serializeRevision(DataOutput output, MediaRevision mediaRevision) throws IOException {
        output.writeUTF(mediaRevision.textDescription);
        output.writeInt(mediaRevision.typeNid);
    }

    @Override
    public void deserializePrimordial(DataInput input, Media media) throws IOException {
        media.format = input.readUTF();

        int imageBytes = input.readInt();

        media.image = new byte[imageBytes];
        input.readFully(media.image, 0, imageBytes);
        media.textDescription = input.readUTF();
        media.typeNid = input.readInt();

    }

    @Override
    public MediaRevision newRevision() {
        return new MediaRevision();
    }

    @Override
    public Media newComponent() {
        return new Media();
    }

    @Override
    protected void deserializeRevision(DataInput input, MediaRevision mediaRevision) throws IOException {
        mediaRevision.textDescription = input.readUTF();
        mediaRevision.typeNid         = input.readInt();

    }
}
