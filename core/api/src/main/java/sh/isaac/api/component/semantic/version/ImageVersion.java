package sh.isaac.api.component.semantic.version;

import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.logic.LogicalExpression;

public interface ImageVersion extends SemanticVersion {
    /**
     * Gets the image data.
     *
     * @return the image data
     */
    byte[] getImageData();

    @Override
    default VersionType getSemanticType() {
        return VersionType.IMAGE;
    }
}
