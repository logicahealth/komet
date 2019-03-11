package sh.isaac.solor.direct.clinvar.generic.writers;

import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.util.List;

/**
 * 2019-03-10
 * aks8m - https://github.com/aks8m
 */
public abstract class ExternalWriter extends TimedTaskWithProgressTracker<Void> {

    private static final List<IndexBuilderService> indexers = LookupService.get().getAllServices(IndexBuilderService.class);

    protected void index(Chronology chronicle) {
        for (IndexBuilderService indexer: indexers) {
            indexer.indexNow(chronicle);
        }
    }

    protected static String getIsoInstant(String basicIsoDate) {
        if (basicIsoDate.contains("-")) {
            return basicIsoDate;
        }
        // From basicIsoDate: '20111203'
        StringBuilder isoInstantBuilder = new StringBuilder();

        // To IsoInstant: '2011-12-03T00:00:00Z'
        isoInstantBuilder.append(basicIsoDate.substring(0, 4));
        isoInstantBuilder.append("-");
        isoInstantBuilder.append(basicIsoDate.substring(4, 6));
        isoInstantBuilder.append("-");
        isoInstantBuilder.append(basicIsoDate.substring(6, 8));
        isoInstantBuilder.append("T00:00:00Z");
        return isoInstantBuilder.toString();
    }



}
