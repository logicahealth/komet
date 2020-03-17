package sh.isaac.komet.batch.action;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.transaction.Transaction;
import sh.komet.gui.manifold.Manifold;

import java.util.concurrent.ConcurrentHashMap;

public class ReplaceAllInExpression extends ActionItem {

    public static final String REPLACE_ALL_IN_EXPRESSION = "Replace all in expression";

    @Override
    public void setupItemForGui(Manifold manifold) {

    }

    @Override
    protected void setupForApply(ConcurrentHashMap<Enum, Object> cache, Transaction transaction, StampCoordinate stampCoordinate, EditCoordinate editCoordinate) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void apply(Chronology chronology, ConcurrentHashMap<Enum, Object> cache, Transaction transaction, StampCoordinate stampCoordinate, EditCoordinate editCoordinate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTitle() {
        return REPLACE_ALL_IN_EXPRESSION;
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        throw new UnsupportedOperationException();
    }
    @Unmarshaler
    public static PromoteComponentAction make(ByteArrayDataBuffer in) {
        // Using a static method rather than a constructor eliminates the need for
        // a readResolve method, but allows the implementation to decide how
        // to handle special cases.
        throw new UnsupportedOperationException();
    }

}
