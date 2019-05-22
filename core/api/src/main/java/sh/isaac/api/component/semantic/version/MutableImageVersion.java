package sh.isaac.api.component.semantic.version;

public interface MutableImageVersion extends ImageVersion {
    /**
     * Gets the image data.
     *
     * @return the image data
     */
    void setImageData(byte[] data);
}
