package sh.isaac.provider.datastore;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.model.collections.SpineFileUtil;
import sh.isaac.model.collections.store.IntIntArrayStore;
import sh.isaac.model.collections.store.IntIntArrayStoreProvider;

import jakarta.inject.Singleton;
import java.io.File;
import java.nio.file.Path;

@Service
@Singleton
public class IntIntArrayFileStoreProvider implements IntIntArrayStoreProvider {

    @Override
    public IntIntArrayStore get(int assemblageNid) {
        Path folderPath = Get.configurationService().getDataStoreFolderPath();
        IntIntArrayFileStore intIntArrayFileStore = new IntIntArrayFileStore(
                SpineFileUtil.getSpineDirectory(new File(folderPath.toFile(), "taxonomyMap"), assemblageNid));
        return intIntArrayFileStore;
    }
}
