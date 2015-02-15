package org.ihtsdo.otf.tcc.api.coordinate;

import java.util.UUID;

public interface ExternalStampBI {

    Status getStatus();
    long getTime();
    UUID getAuthorUuid();
    UUID getModuleUuid();
    UUID getPathUuid();


}