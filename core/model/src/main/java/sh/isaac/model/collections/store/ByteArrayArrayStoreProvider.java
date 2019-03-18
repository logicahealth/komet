package sh.isaac.model.collections.store;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ByteArrayArrayStoreProvider {


    ByteArrayArrayStore get(int assemblageNid);
}
