package gov.vha.isaac.ochre.integration.tests;

import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Created by kec on 1/25/16.
 */
public class OchreExternalizableStatsTestFilter implements Predicate<OchreExternalizable> {
    AtomicInteger concepts = new AtomicInteger(0);
    AtomicInteger sememes = new AtomicInteger(0);
    AtomicInteger stampAliases = new AtomicInteger(0);
    AtomicInteger stampComments = new AtomicInteger(0);

    @Override
    public boolean test(OchreExternalizable ochreExternalizable) {
        switch (ochreExternalizable.getOchreObjectType()) {
            case CONCEPT:
                concepts.incrementAndGet();
                break;
            case SEMEME:
                sememes.incrementAndGet();
                break;
            case STAMP_ALIAS:
                stampAliases.incrementAndGet();
                break;
            case STAMP_COMMENT:
                stampComments.incrementAndGet();
                break;
            default:
                throw new UnsupportedOperationException("Can't handle: " + ochreExternalizable);
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OchreExternalizableStatsTestFilter that = (OchreExternalizableStatsTestFilter) o;

        if (concepts.get() != that.concepts.get()) return false;
        if (sememes.get() != that.sememes.get()) return false;
        if (stampAliases.get() != that.stampAliases.get()) return false;
        return stampComments.get() == that.stampComments.get();

    }

    @Override
    public int hashCode() {
        int result = concepts.hashCode();
        result = 31 * result + sememes.hashCode();
        result = 31 * result + stampAliases.hashCode();
        result = 31 * result + stampComments.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OchreExternalizableStatsTestFilter{" +
                "concepts=" + concepts +
                ", sememes=" + sememes +
                ", stampAliases=" + stampAliases +
                ", stampComments=" + stampComments +
                '}';
    }
}
