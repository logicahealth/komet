package sh.isaac.provider.datastore;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.model.collections.SpineFileUtil;
import sh.isaac.model.collections.store.ByteArrayArrayStore;
import sh.isaac.model.collections.store.ByteArrayArrayStoreProvider;

import jakarta.inject.Singleton;
import java.io.File;
import java.nio.file.Path;

@Service
@Singleton
public class ByteArrayArrayFileStoreProvider implements ByteArrayArrayStoreProvider {

    @Override
    public ByteArrayArrayStore get(int assemblageNid) {
        Path folderPath = Get.configurationService().getDataStoreFolderPath();
        ByteArrayArrayFileStore byteArrayArrayFileStore = new ByteArrayArrayFileStore(
                SpineFileUtil.getSpineDirectory(new File(folderPath.toFile(), "chronologies"), assemblageNid));
        return byteArrayArrayFileStore;
    }
}
