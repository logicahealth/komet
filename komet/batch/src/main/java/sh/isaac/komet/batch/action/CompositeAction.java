package sh.isaac.komet.batch.action;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.VersionChangeListener;
import sh.isaac.api.util.UuidStringKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class CompositeAction implements Marshalable {

    public static final int marshalVersion = 3;

    private final String actionTitle;

    private final UuidStringKey listKey;

    private final List<ActionItem> actionItemList;

    public CompositeAction(String actionTitle, UuidStringKey listKey, List<ActionItem> actionItemList) {
        this.actionTitle = actionTitle;
        this.listKey = listKey;
        this.actionItemList = new ArrayList<>(actionItemList);
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putUTF(actionTitle);
        MarshalUtil.marshal(listKey, out);
        MarshalUtil.marshal(actionItemList, out);
    }

    @Unmarshaler
    public static CompositeAction make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new CompositeAction(in.getUTF(),
                        MarshalUtil.unmarshal(in),
                        MarshalUtil.unmarshal(in));
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    public Future<?> apply(int count, Stream<Chronology> itemsStream,
                      Transaction transaction,
                      ManifoldCoordinate manifoldCoordinate,
                      VersionChangeListener versionChangeListener) {
        CompositeActionTask compositeActionTask = new CompositeActionTask(count, itemsStream, transaction,
                manifoldCoordinate, versionChangeListener);
        return Get.executor().submit(compositeActionTask);
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public UuidStringKey getListKey() {
        return listKey;
    }

    public List<ActionItem> getActionItemList() {
        return Collections.unmodifiableList(actionItemList);
    }

    private class CompositeActionTask extends TimedTaskWithProgressTracker<Void> {

        final Stream<Chronology> itemsStream;
        final Transaction transaction;
        final ManifoldCoordinate manifoldCoordinate;
        final VersionChangeListener versionChangeListener;

        public CompositeActionTask(int size, Stream<Chronology> itemsStream,
                                   Transaction transaction,
                                   ManifoldCoordinate manifoldCoordinate,
                                   VersionChangeListener versionChangeListener) {
            this.itemsStream = itemsStream;
            this.transaction = transaction;
            this.manifoldCoordinate = manifoldCoordinate;
            this.versionChangeListener = versionChangeListener;
            super.addToTotalWork(size);
            super.updateTitle("Executing: " + actionTitle);
            Get.activeTasks().add(this);
        }

        @Override
        protected Void call() throws Exception {
            try {
                ManifoldCoordinateImmutable manifoldForAction = this.manifoldCoordinate.toManifoldCoordinateImmutable();
                ConcurrentHashMap<Enum, Object> cache = new ConcurrentHashMap<>();
                for (ActionItem actionItem: actionItemList) {
                    actionItem.setupForApply(cache, transaction,
                            manifoldForAction);
                }

                itemsStream.parallel().forEach(chronology -> {
                    for (ActionItem actionItem: actionItemList) {
                        actionItem.apply(chronology, cache,
                                versionChangeListener);
                        super.completedUnitOfWork();
                    }
                });

                for (ActionItem actionItem: actionItemList) {
                    actionItem.conclude(cache);
                }
                return null;
            } finally {
                Get.activeTasks().remove(this);
            }
        }
    }
}
