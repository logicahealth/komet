package sh.isaac.komet.batch.action.xslt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.VersionChangeListener;
import sh.isaac.komet.batch.action.ActionItem;
import sh.isaac.komet.batch.action.FilterRf2Zip;
import sh.komet.gui.control.property.wrapper.PropertySheetFileWrapper;
import sh.komet.gui.util.FxGet;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import static sh.isaac.komet.batch.action.xslt.XsltActionFactory.XSLT_TRANSFORM;

public class XsltAction extends ActionItem {
    public static final int marshalVersion = 1;
    private enum Keys {
        TRANSFORMER
    }

    StringProperty xslFileString = new SimpleStringProperty();
    StringProperty inputFileString = new SimpleStringProperty();
    StringProperty outputFileString = new SimpleStringProperty();

    public XsltAction() {
    }

    public XsltAction(ByteArrayDataBuffer in) {
        xslFileString.set(in.getUTF());
        inputFileString.set(in.getUTF());
        outputFileString.set(in.getUTF());
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putUTF(xslFileString.get());
        out.putUTF(inputFileString.get());
        out.putUTF(outputFileString.get());
    }

    @Unmarshaler
    public static XsltAction make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new XsltAction(in);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    protected void setupItemForGui(ObservableManifoldCoordinate manifold) {
        PropertySheetFileWrapper xslFileWrapper = new PropertySheetFileWrapper("XSL file", xslFileString,
                PropertySheetFileWrapper.FileOperation.READ);
        getPropertySheet().getItems().add(xslFileWrapper);

        PropertySheetFileWrapper inputFileWrapper = new PropertySheetFileWrapper("Input file", inputFileString,
                PropertySheetFileWrapper.FileOperation.READ);
        getPropertySheet().getItems().add(inputFileWrapper);

        PropertySheetFileWrapper outputFileWrapper = new PropertySheetFileWrapper("Output file", outputFileString,
                PropertySheetFileWrapper.FileOperation.OVERWRITE);
        getPropertySheet().getItems().add(outputFileWrapper);

    }

    @Override
    protected void setupForApply(ConcurrentHashMap<Enum, Object> cache, Transaction transaction, ManifoldCoordinateImmutable manifoldCoordinate) throws Exception {
        File xslFile = new File(xslFileString.get());
        Transformer transformer = Get.xsltTransformer(new StreamSource(xslFile), manifoldCoordinate);
        cache.put(Keys.TRANSFORMER, transformer);
    }

    @Override
    protected void apply(Chronology chronology, ConcurrentHashMap<Enum, Object> cache, VersionChangeListener versionChangeListener) {
        // nothing to do...
    }

    @Override
    protected void conclude(ConcurrentHashMap<Enum, Object> cache) {
        try {
            File xmlSource = new File(inputFileString.get());
            File outputTargetFile = new File(outputFileString.get());
            Result outputTarget = new StreamResult(outputTargetFile.getAbsoluteFile());
            Transformer transformer = (Transformer) cache.get(Keys.TRANSFORMER);
            transformer.transform(new StreamSource(xmlSource), outputTarget);
        } catch (TransformerException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }

    @Override
    public String getTitle() {
        return XSLT_TRANSFORM;
    }
}
