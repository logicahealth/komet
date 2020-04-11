package sh.isaac.komet.batch.action;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.VersionChangeListener;
import sh.komet.gui.util.UuidStringKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Stream;

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
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putUTF(actionTitle);
        MarshalUtil.marshal(listKey, out);
        MarshalUtil.marshal(viewKey, out);
        MarshalUtil.marshal(actionItemList, out);
    }

    @Unmarshaler
    public static CompositeAction make(ByteArrayDataBuffer in) {
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

    public void apply(int count, Stream<Chronology> itemsStream,
                      Transaction transaction,
                      StampFilter stampFilter,
                      EditCoordinate editCoordinate,
                      VersionChangeListener versionChangeListener) {
        CompositeActionTask compositeActionTask = new CompositeActionTask(count, itemsStream, transaction,
                stampFilter, editCoordinate, versionChangeListener);
        Future<?> future = Get.executor().submit(compositeActionTask);
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

    private class CompositeActionTask extends TimedTaskWithProgressTracker<Void> {

        final Stream<Chronology> itemsStream;
        final Transaction transaction;
        final StampFilter stampFilter;
        final EditCoordinate editCoordinate;
        final VersionChangeListener versionChangeListener;

        public CompositeActionTask(int size, Stream<Chronology> itemsStream,
                                   Transaction transaction,
                                   StampFilter stampFilter,
                                   EditCoordinate editCoordinate,
                                   VersionChangeListener versionChangeListener) {
            this.itemsStream = itemsStream;
            this.transaction = transaction;
            this.stampFilter = stampFilter;
            this.editCoordinate = editCoordinate;
            this.versionChangeListener = versionChangeListener;
            super.addToTotalWork(size);
            super.updateTitle("Executing: " + actionTitle);
            Get.activeTasks().add(this);
        }

        @Override
        protected Void call() throws Exception {
            try {
                ConcurrentHashMap<Enum, Object> cache = new ConcurrentHashMap<>();
                for (ActionItem actionItem: actionItemList) {
                    actionItem.setupForApply(cache, transaction,
                            stampFilter, editCoordinate);
                }

                itemsStream.parallel().forEach(chronology -> {
                    for (ActionItem actionItem: actionItemList) {
                        actionItem.apply(chronology, cache, transaction,
                                stampFilter, editCoordinate, versionChangeListener);
                        super.completedUnitOfWork();
                    }
                });
                return null;
            } finally {
                Get.activeTasks().remove(this);
            }
        }
    }
}
