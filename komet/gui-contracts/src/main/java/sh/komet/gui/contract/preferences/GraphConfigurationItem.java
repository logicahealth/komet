package sh.komet.gui.contract.preferences;

import sh.isaac.api.util.UuidStringKey;

import java.util.UUID;

public interface GraphConfigurationItem {

// LOGICAL
    static final UuidStringKey PREMISE_DIGRAPH = new UuidStringKey(UUID.fromString("51fc07d8-60ad-11ea-bc55-0242ac130003"), "Premises");
    static final UuidStringKey PATH_DIGRAPH = new UuidStringKey(UUID.fromString("3092e660-94b1-11ea-bb37-0242ac130002"), "Paths");

}
