package sh.komet.gui.contract.preferences;

import sh.komet.gui.util.UuidStringKey;

import java.util.UUID;

public interface GraphConfigurationItem {


    static final UuidStringKey DEFINING_ALL = new UuidStringKey(UUID.fromString("51fc07d8-60ad-11ea-bc55-0242ac130003"), "Inferred, nodes of all status");
    static final UuidStringKey DEFINING_ACTIVE = new UuidStringKey(UUID.fromString("51fc0a3a-60ad-11ea-bc55-0242ac130003"), "Inferred, active nodes");
    static final UuidStringKey DEFINING_ACTIVE_FQN = new UuidStringKey(UUID.fromString("51fc0b48-60ad-11ea-bc55-0242ac130003"), "Inferred, active FQN nodes");
    static final UuidStringKey STATED_ALL = new UuidStringKey(UUID.fromString("8edb6ca6-60d1-11ea-bc55-0242ac130003"), "Stated, nodes of all status");
    static final UuidStringKey PATH_TREE = new UuidStringKey(UUID.fromString("3092e660-94b1-11ea-bb37-0242ac130002"), "Path tree");

}
