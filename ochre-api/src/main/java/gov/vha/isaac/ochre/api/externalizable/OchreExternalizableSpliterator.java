package gov.vha.isaac.ochre.api.externalizable;

import gov.vha.isaac.ochre.api.Get;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by kec on 1/23/16.
 */
public class OchreExternalizableSpliterator implements Spliterator<OchreExternalizable> {

    List<Stream<? extends OchreExternalizable>> streams = new ArrayList<>();

    public OchreExternalizableSpliterator() {
        streams.add(Get.conceptService().getConceptChronologyStream());
        streams.add(Get.sememeService().getSememeChronologyStream());
        streams.add(Get.commitService().getStampAliasStream());
        streams.add(Get.commitService().getStampCommentStream());
    }

    @Override
    public boolean tryAdvance(Consumer<? super OchreExternalizable> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super OchreExternalizable> action) {
        for (Stream<? extends OchreExternalizable> stream: streams) {
            stream.forEach(action);
        }
    }

    @Override
    public Spliterator<OchreExternalizable> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return  Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return DISTINCT | NONNULL |  IMMUTABLE;
    }
}
