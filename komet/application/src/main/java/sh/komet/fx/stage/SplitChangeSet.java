package sh.komet.fx.stage;

import sh.isaac.api.Get;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.ChronologyImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SplitChangeSet extends TimedTaskWithProgressTracker<Integer>  {
    final File importFile;
    final String fileRoot;
    final String fileSuffix;
    final int fileSize = 10000000;


    public SplitChangeSet(File importFile) {
        this.importFile = importFile;
        String absolutePath = importFile.getAbsolutePath();
        this.fileRoot = absolutePath.substring(0, absolutePath.lastIndexOf('.'));
        this.fileSuffix = absolutePath.substring(absolutePath.lastIndexOf('.'));
        updateTitle("Native import from " + importFile.getName());
        Get.activeTasks().add(this);
    }
    @Override
    protected Integer call() throws Exception {
        BinaryDataReaderService reader = Get.binaryDataReader(importFile.toPath());
        AtomicReference<DataWriterService> writerReference = new AtomicReference<>();

        try {
            AtomicInteger sequence = new AtomicInteger();
            AtomicInteger bytesWritten = new AtomicInteger();
            setupNextWriter(writerReference, sequence, bytesWritten);
            reader.getStream().forEach(object -> {
                writerReference.get().put(object);

                byte[] data = ((ChronologyImpl) object).getChronologyDataToWrite();
                if (bytesWritten.addAndGet(data.length) > fileSize) {
                    setupNextWriter(writerReference, sequence, bytesWritten);
                    bytesWritten.set(0);
                }

            });
            LOG.info("Split complete.");

            return null;
        } finally {
            Get.activeTasks().remove(this);
            reader.close();
            if (writerReference.get() != null) {
                writerReference.get().close();
            }
        }
    }

    private void setupNextWriter(AtomicReference<DataWriterService> writerReference, AtomicInteger sequence, AtomicInteger bytesWritten) {
        try {
            if (writerReference.get() != null) {
                writerReference.get().flush();
                writerReference.get().close();
            }
            String nextFile = fileRoot + '.' + sequence.getAndIncrement() + fileSuffix;
            writerReference.set(Get.binaryDataWriter(Path.of(nextFile)));
            LOG.info("Setup next file. Written: " + bytesWritten.get() + " new sequence: " + sequence.get());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
