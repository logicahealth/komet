package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.komet.gui.manifold.Manifold;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
 * aks8m - 5/22/18
 */
public class RF2RelationshipSpec extends RF2ReaderSpecification {


    private final Manifold manifold;

    public RF2RelationshipSpec(Manifold manifold, ExportLookUpCache exportLookUpCache) {
        super(manifold, exportLookUpCache);
        this.manifold = manifold;
    }

    @Override
    public void addColumnHeaders(List<String> lines) {
        lines.add(0, "id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId" +
                "\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r");
    }

    @Override
    public String createExportString(Chronology chronology) {





        return null;
    }

    @Override
    public String getReaderUIText() {
        return "Relationships";
    }

    @Override
    public List<Chronology> createChronologyList() {


        return null;
    }

    @Override
    public String getFileName(String rootDirName) {
        return rootDirName + "/Snapshot/Terminology/sct2_Relationship_Snapshot_" + DateTimeFormatter.ofPattern("uuuuMMdd").format(LocalDateTime.now()) + ".txt";
    }
}
