package sh.isaac.komet.batch.action;

import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.VersionChangeListener;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static sh.isaac.komet.batch.action.FindTransitiveDependenciesFactory.FIND_TRANSITIVE_DEPENDENCIES;

public class FindTransitiveDependencies extends ActionItem {

    public static final int marshalVersion = 1;
    public enum ActionKeys {
        TRANSITIVE_IDENTIFIERS,
        VIEW_MANIFOLD,
        ASSEMBLAGES_TO_PROCESS
    }

    public FindTransitiveDependencies(ByteArrayDataBuffer in) {
        // nothing do do;
    }

    public FindTransitiveDependencies() {
    }

    @Override
    protected void setupItemForGui(ObservableManifoldCoordinate manifoldForDisplay) {

    }

    @Override
    protected void setupForApply(ConcurrentHashMap<Enum, Object> cache, Transaction transaction, ManifoldCoordinateImmutable manifoldCoordinate) {
        ConcurrentSkipListSet<String> transitiveIdentifiers = new ConcurrentSkipListSet<>();
        cache.put(ActionKeys.TRANSITIVE_IDENTIFIERS, transitiveIdentifiers);
        cache.put(ActionKeys.VIEW_MANIFOLD, manifoldCoordinate);
        MutableIntSet assemblagesToProcess = IntSets.mutable.empty();
        // SOLOR concept assemblage (SOLOR)◽SOLOR concepts◽d39b3ecd-9a80-5009-a8ac-0b947f95ca7c
        assemblagesToProcess.add(Get.nidForUuids(UUID.fromString("d39b3ecd-9a80-5009-a8ac-0b947f95ca7c")));
        // SNOMED integer id (SOLOR) [0418a591-f75b-39ad-be2c-3ab849326da9, 87360947-e603-3397-804b-efd0fcc509b9]
        assemblagesToProcess.add(Get.nidForUuids(UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9")));
        // English language (SOLOR)◽English language◽06d905ea-c647-3af9-bfe5-2514e135b558
        assemblagesToProcess.add(Get.nidForUuids(UUID.fromString("06d905ea-c647-3af9-bfe5-2514e135b558")));
        // United States of America English dialect assemblage (SOLOR)◽US English◽bca0a686-3516-3daf-8fcf-fe396d13cfad
        assemblagesToProcess.add(Get.nidForUuids(UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad")));
        // RF2 inferred relationship assemblage (SOLOR)◽RF2 inferred relationships◽e3436c74-2491-50fa-b43c-13d83238648c
        assemblagesToProcess.add(Get.nidForUuids(UUID.fromString("e3436c74-2491-50fa-b43c-13d83238648c")));
        cache.put(ActionKeys.ASSEMBLAGES_TO_PROCESS, assemblagesToProcess.toImmutable());
    }
    @Override
    protected void apply(Chronology chronology, ConcurrentHashMap<Enum, Object> cache, VersionChangeListener versionChangeListener) {
        ConcurrentSkipListSet<String> transitiveIdentifiers = (ConcurrentSkipListSet<String>) cache.get(ActionKeys.TRANSITIVE_IDENTIFIERS);
        ManifoldCoordinateImmutable manifoldCoordinate = (ManifoldCoordinateImmutable) cache.get(ActionKeys.VIEW_MANIFOLD);
        ImmutableIntSet assemblagesToProcess = (ImmutableIntSet) cache.get(ActionKeys.ASSEMBLAGES_TO_PROCESS);
        SemanticChronology semanticChronology = (SemanticChronology) chronology;
        Optional<? extends Chronology> optionalChronology = Get.identifiedObjectService().getChronology(semanticChronology.getReferencedComponentNid());
        StringBuilder sb = new StringBuilder();
        optionalChronology.ifPresentOrElse(
                referencedChronology -> transitiveAdd(optionalChronology.get(), transitiveIdentifiers, manifoldCoordinate, sb, optionalChronology.get().getVersionType(),
                        assemblagesToProcess),
                 () -> LOG.error("No referenced component for: " + semanticChronology));
        LOG.info("\n\n" + sb.toString());
    }

    private void transitiveAdd(Chronology chronology, ConcurrentSkipListSet<String> transitiveIdentifiers,
                               ManifoldCoordinateImmutable manifoldCoordinate, StringBuilder sb, VersionType versionType,
                               ImmutableIntSet assemblagesToProcess) {
        if (assemblagesToProcess.contains(chronology.getAssemblageNid())) {
             for (SemanticChronology semanticChronology: Get.assemblageService().getSemanticChronologiesForComponent(chronology.getNid())) {
                 if (assemblagesToProcess.contains(semanticChronology.getAssemblageNid())) {
                    switch (semanticChronology.getVersionType()) {
                         case DESCRIPTION:
                         case RF2_RELATIONSHIP:
                             // get description and relationship identifiers
                             transitiveAdd(semanticChronology, transitiveIdentifiers, manifoldCoordinate, sb, semanticChronology.getVersionType(), assemblagesToProcess);
                             break;
                         case COMPONENT_NID:
                             // dialect info here.
                             sb.append("dialect: ").append(semanticChronology.getPrimordialUuid()).append("\n");
                             transitiveIdentifiers.add(semanticChronology.getPrimordialUuid().toString());
                             break;
                         case STRING:
                             // SCTID here
                             LatestVersion<StringVersion> stringVersion = semanticChronology.getLatestVersion(manifoldCoordinate.getViewStampFilter());
                             sb.append(versionType).append(": ").append(stringVersion.get().getString()).append("\n");
                             transitiveIdentifiers.add(stringVersion.get().getString());
                             break;
                         default:
                             // continue
                   }
                }
            }
        }
    }

    @Override
    protected void conclude(ConcurrentHashMap<Enum, Object> cache) {
        ConcurrentSkipListSet<UUID> transitiveUuids = (ConcurrentSkipListSet<UUID>) cache.get(ActionKeys.TRANSITIVE_IDENTIFIERS);
        LOG.info("Component count: " + transitiveUuids.size());
    }


    @Override
    public String getTitle() {
        return FIND_TRANSITIVE_DEPENDENCIES;
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
     }

    @Unmarshaler
    public static Object make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new FindTransitiveDependencies(in);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }
}