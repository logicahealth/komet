package sh.isaac.provider.datastore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.model.collections.SpineFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import static sh.isaac.model.collections.SpineFileUtil.SPINE_PREFIX;

public class SpinedArrayFileStore {
    protected static final Logger LOG = LogManager.getLogger();
    protected final Semaphore diskSemaphore;

    protected final File directory;

    protected int spineSize;

    public SpinedArrayFileStore(File directory) {
        this(directory, new Semaphore(1));
    }

    public SpinedArrayFileStore(File directory, Semaphore diskSemaphore) {
        this.directory = directory;
        this.spineSize = SpineFileUtil.readSpineCount(directory);
        this.diskSemaphore = diskSemaphore;
    }

    public final void writeSpineCount(int spineCount) {
        try {
            this.spineSize = spineCount;
            SpineFileUtil.writeSpineCount(directory, spineCount);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final int getSpineCount() {
        return SpineFileUtil.readSpineCount(directory);
    }

    public final int sizeOnDisk() {
        if (directory == null) {
            return 0;
        }
        File[] files = directory.listFiles((pathname) -> {
            return pathname.getName().startsWith(SPINE_PREFIX);
        });
        int size = 0;
        for (File spineFile : files) {
            size = (int) (size + spineFile.length());
        }
        return size;
    }
}
