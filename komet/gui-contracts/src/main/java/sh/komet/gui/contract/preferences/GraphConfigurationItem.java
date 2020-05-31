package sh.komet.gui.contract.preferences;

import sh.isaac.api.util.UuidStringKey;

import java.util.UUID;

public interface GraphConfigurationItem {


    static final UuidStringKey INFERRED_PREFERRED = new UuidStringKey(UUID.fromString("51fc07d8-60ad-11ea-bc55-0242ac130003"), "Inferred, preferred");
    static final UuidStringKey INFERRED_FQN = new UuidStringKey(UUID.fromString("51fc0b48-60ad-11ea-bc55-0242ac130003"), "Inferred, FQN");
    static final UuidStringKey STATED_PREFERRED = new UuidStringKey(UUID.fromString("8edb6ca6-60d1-11ea-bc55-0242ac130003"), "Stated, preferred");
    static final UuidStringKey STATED_FQN = new UuidStringKey(UUID.fromString("6688ce4e-727f-40cf-8bf6-f285013ffb1a"), "Stated, FQN");
    static final UuidStringKey PATH_TREE = new UuidStringKey(UUID.fromString("3092e660-94b1-11ea-bb37-0242ac130002"), "Path tree");

}
