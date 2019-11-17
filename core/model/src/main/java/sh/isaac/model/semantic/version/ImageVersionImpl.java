package sh.isaac.model.semantic.version;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.MutableImageVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.semantic.SemanticChronologyImpl;

import java.util.Arrays;

public class ImageVersionImpl
        extends AbstractVersionImpl
        implements MutableImageVersion {
    /** The image data. */
    byte[] imageData = null;


    /**
     * Instantiates a new logic graph semantic impl.
     *
     * @param container the container
     * @param stampSequence the stamp sequence
     */
    public ImageVersionImpl(SemanticChronologyImpl container,
                                 int stampSequence) {
        super(container, stampSequence);
    }

    /**
     * Instantiates a new logic graph semantic impl.
     *
     * @param container the container
     * @param stampSequence the stamp sequence
     * @param data the data
     */
    public ImageVersionImpl(SemanticChronologyImpl container,
                                 int stampSequence,
                                 ByteArrayDataBuffer data) {
        super(container, stampSequence);

        this.imageData = data.getByteArrayField();
    }

    private ImageVersionImpl(ImageVersionImpl other, int stampSequence) {
        super(other.getChronology(), stampSequence);
        this.imageData = other.imageData.clone();
    }

    @Override
    public void writeVersionData(ByteArrayDataBuffer data) {
        super.writeVersionData(data);
        data.putByteArrayField(this.imageData);
    }

    @Override
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    @Override
    public byte[] getImageData() {
        return this.imageData;
    }

    @Override
    protected int editDistance3(AbstractVersionImpl other, int editDistance) {
        ImageVersionImpl otherImpl = (ImageVersionImpl) other;
        if (!Arrays.equals(this.imageData, otherImpl.imageData)) {
            editDistance++;
        }
        return editDistance;
    }

    @Override
    protected boolean deepEquals3(AbstractVersionImpl other) {
        if (!(other instanceof ImageVersionImpl)) {
            return false;
        }
        ImageVersionImpl otherImpl = (ImageVersionImpl) other;
        return Arrays.equals(this.imageData, otherImpl.imageData);
    }

    @Override
    public <V extends Version> V makeAnalog(EditCoordinate ec) {
        final int stampSequence = Get.stampService()
                .getStampSequence(
                        this.getStatus(),
                        Long.MAX_VALUE,
                        ec.getAuthorNid(),
                        this.getModuleNid(),
                        ec.getPathNid());
        return setupAnalog(stampSequence);
    }

    @Override
    public <V extends Version> V makeAnalog(Transaction transaction, int authorNid) {
        final int stampSequence = Get.stampService()
                .getStampSequence(transaction,
                        this.getStatus(),
                        Long.MAX_VALUE,
                        authorNid,
                        this.getModuleNid(),
                        this.getPathNid());
        return setupAnalog(stampSequence);
    }

    public <V extends Version> V setupAnalog(int stampSequence) {
        SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
        final ImageVersionImpl newVersion = new ImageVersionImpl(this, stampSequence);

        chronologyImpl.addVersion(newVersion);
        return (V) newVersion;
    }
}
