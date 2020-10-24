package sh.isaac.komet.batch.action;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.VersionChangeListener;
import sh.komet.gui.control.property.wrapper.PropertySheetFileWrapper;
import sh.komet.gui.util.FxGet;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static sh.isaac.komet.batch.action.FilterRf2ZipFactory.FILTER_ZIP;

public class FilterRf2Zip extends ActionItem {
    public static final int marshalVersion = 1;

    StringProperty inputFileString = new SimpleStringProperty();
    StringProperty outputFileString = new SimpleStringProperty();

    public FilterRf2Zip() {
    }

    public FilterRf2Zip(ByteArrayDataBuffer in) {
        inputFileString.set(in.getUTF());
        outputFileString.set(in.getUTF());
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putUTF(inputFileString.get());
        out.putUTF(outputFileString.get());
    }

    @Unmarshaler
    public static FilterRf2Zip make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new FilterRf2Zip(in);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    public void setupItemForGui(ObservableManifoldCoordinate manifoldForDisplay) {

        PropertySheetFileWrapper inputFileWrapper = new PropertySheetFileWrapper("Input file", inputFileString,
                PropertySheetFileWrapper.FileOperation.READ);
        getPropertySheet().getItems().add(inputFileWrapper);

        PropertySheetFileWrapper outputFileWrapper = new PropertySheetFileWrapper("Output file", outputFileString,
                PropertySheetFileWrapper.FileOperation.OVERWRITE);
        getPropertySheet().getItems().add(outputFileWrapper);
    }

    @Override
    protected void setupForApply(ConcurrentHashMap<Enum, Object> cache, Transaction transaction, ManifoldCoordinateImmutable manifoldCoordinate) {
        // Setup snapshot...
    }

    @Override
    protected void apply(Chronology chronology, ConcurrentHashMap<Enum, Object> cache,
                         VersionChangeListener versionChangeListener) {

    }

    @Override
    protected void conclude(ConcurrentHashMap<Enum, Object> cache) {
        // Filter here...
        ConcurrentSkipListSet<String> transitiveIdentifiers =
                (ConcurrentSkipListSet<String>) cache.get(FindTransitiveDependencies.ActionKeys.TRANSITIVE_IDENTIFIERS);
        File inputZipFile = new File(inputFileString.get());
        File outputZipFile = new File(outputFileString.get());

        try (ZipFile inputZip = new ZipFile(inputZipFile, Charset.forName("UTF-8"));
             ZipOutputStream writer
                     = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputZipFile)))) {
            inputZip.stream()
                    .filter(entry -> !entry.getName().contains("__MACOSX") && !entry.getName().contains("._") && !entry.getName().contains(".DS_Store"))
                    .forEach(zipEntry -> {
                try {

                    if (zipEntry.getName().endsWith(".txt")) {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputZip.getInputStream(zipEntry), Charset.forName("UTF-8")))) {
                            String firstLine = br.readLine();
                            String[] firstLineFields = firstLine.split("\t");
                            if (firstLineFields[0].equals("id")) {

                                StringWriter stringWriter = new StringWriter();
                                stringWriter.write(firstLine);
                                stringWriter.write("\n");

                                String nextLine = br.readLine();
                                while (nextLine != null) {
                                    String[] nextLineFields = nextLine.split("\t");
                                    if (transitiveIdentifiers.contains(nextLineFields[0])) {
                                        stringWriter.write(nextLine);
                                        stringWriter.write("\n");
                                    }
                                    nextLine = br.readLine();
                                }

                                writer.putNextEntry(new ZipEntry(zipEntry.getName()));
                                writer.write(stringWriter.toString().getBytes());
                                writer.closeEntry();
                                writer.flush();
                            }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        } catch (IOException ex) {
            FxGet.dialogs().showErrorDialog(ex);
        }

    }

    @Override
    public String getTitle() {
        return FILTER_ZIP;
    }

}