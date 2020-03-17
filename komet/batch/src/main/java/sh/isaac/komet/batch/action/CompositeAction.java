package sh.isaac.komet.batch.action;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.transaction.Transaction;
import sh.komet.gui.util.UuidStringKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CompositeAction implements Marshalable {

    public static final int marshalVersion = 1;

    private final String actionTitle;

    private final UuidStringKey listKey;

    private final UuidStringKey viewKey;

    private final List<ActionItem> actionItemList;

    public CompositeAction(String actionTitle, UuidStringKey listKey, UuidStringKey viewKey, List<ActionItem> actionItemList) {
        this.actionTitle = actionTitle;
        this.listKey = listKey;
        this.viewKey = viewKey;
        this.actionItemList = new ArrayList<>(actionItemList);
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) throws ReflectiveOperationException {
        out.putInt(marshalVersion);
        out.putUTF(actionTitle);
        MarshalUtil.marshal(listKey, out);
        MarshalUtil.marshal(viewKey, out);
        MarshalUtil.marshal(actionItemList, out);
    }

    @Unmarshaler
    public static CompositeAction make(ByteArrayDataBuffer in) throws ReflectiveOperationException {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new CompositeAction(in.getUTF(),
                        MarshalUtil.unmarshal(in),
                        MarshalUtil.unmarshal(in),
                        MarshalUtil.unmarshal(in));
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    public void apply(List<? extends Chronology> items,
                      Transaction transaction,
                      StampCoordinate stampCoordinate,
                      EditCoordinate editCoordinate) {

        ConcurrentHashMap<Enum, Object> cache = new ConcurrentHashMap<>();
        for (ActionItem actionItem: actionItemList) {
            actionItem.setupForApply(cache, transaction,
                    stampCoordinate, editCoordinate);
        }

        for (Chronology chronologyItem: items) {
            for (ActionItem actionItem: actionItemList) {
                actionItem.apply(chronologyItem, cache, transaction,
                        stampCoordinate, editCoordinate);
            }
        }
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public UuidStringKey getListKey() {
        return listKey;
    }

    public UuidStringKey getViewKey() {
        return viewKey;
    }

    public List<ActionItem> getActionItemList() {
        return Collections.unmodifiableList(actionItemList);
    }
}
