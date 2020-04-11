package sh.isaac.komet.batch;

import sh.isaac.api.chronicle.Version;

public interface VersionChangeListener {
    void versionChanged(Version oldValue, Version newValue);
}
