package sh.komet.fx.stage;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Version;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Long2_VersionImpl;

import java.util.UUID;

public class DependencyManagementCollector extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {
    final MutableMap<UUID, MutableSet<DependencyRecord>> dependencyMap = Maps.mutable.empty();

    final ManifoldCoordinate manifoldCoordinate;

    public DependencyManagementCollector(ManifoldCoordinate manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    protected Void call() throws Exception {

        ConceptProxy moduleDependencyProxy = new ConceptProxy("Module dependency reference set (foundation metadata concept)", UUID.fromString("19076bfe-661f-39c2-860c-8706a37073b0"));


        ImmutableIntSet moduleDependencyNids = Get.assemblageService().getSemanticNidsFromAssemblage(moduleDependencyProxy.getNid());

        // Add primordial module dependency
        ConceptProxy snomedModelComponent = new ConceptProxy("SNOMED CT model component module (core metadata concept)",
                UUID.fromString("45dc5146-b0bb-3ca9-8876-0e702fa29f42"));

        UUID dependencyUuid = UuidT5Generator.get(TermAux.DEPENDENCY_MANAGEMENT.getPrimordialUuid(),
                Get.identifierService().getUuidPrimordialStringForNid(snomedModelComponent.getNid()).toString() +
                        TermAux.PRIMORDIAL_MODULE.getPrimordialUuid().toString());
        dependencyMap.getIfAbsentPut(dependencyUuid, () -> Sets.mutable.empty()).add(new DependencyRecord(snomedModelComponent.getNid(),
                System.currentTimeMillis(),
                TermAux.PRIMORDIAL_MODULE.getNid(),
                Long.MAX_VALUE));

        ImmutableSet<UUID> dependenciesToSkip = Sets.immutable.of(UUID.fromString("ab2c93c8-23de-5287-ac81-0c648eecfa97"),
                UUID.fromString("7f8a72a9-da9c-55b9-9261-315320c25dde"),
                UUID.fromString("ade79cd8-6503-548a-a043-d0e6dae2e035"),
                UUID.fromString("1b6fb3b8-40b5-54a8-b1f9-12682e51e325"));


        moduleDependencyNids.forEach(nid -> {
            SemanticChronology chronology = Get.assemblageService().getSemanticChronology(nid);
            MutableIntSet moduleNids = IntSets.mutable.empty();
            for (Version version: chronology.getVersionList()) {
                moduleNids.add(version.getModuleNid());
            }
            if (moduleNids.size() != 1) {
                throw new IllegalStateException("More than one module nid for: " + chronology);
            }
            int dependentModuleNid = moduleNids.intIterator().next();
            int providerModuleNid = chronology.getReferencedComponentNid();
            UUID dependencyManagmentUuid = UuidT5Generator.get(TermAux.DEPENDENCY_MANAGEMENT.getPrimordialUuid(),
                    Get.identifierService().getUuidPrimordialStringForNid(dependentModuleNid).toString() +
                            Get.identifierService().getUuidPrimordialStringForNid(providerModuleNid).toString());

            /*
Redundant/transative dependencies

ab2c93c8-23de-5287-ac81-0c648eecfa97
SNOMED CT model component < LOINC - SNOMED CT Cooperation Project module

7f8a72a9-da9c-55b9-9261-315320c25dde
SNOMED CT model component < US National Library of Medicine maintained module

ade79cd8-6503-548a-a043-d0e6dae2e035
SNOMED CT model component < SNOMED CT to ICD-10 rule-based mapping module

Incorrect dependencies
1b6fb3b8-40b5-54a8-b1f9-12682e51e325
SNOMED CT to ICD-10-CM rule-based mapping module < VA Extension Module
             */

            if (!dependenciesToSkip.contains(dependencyManagmentUuid)) {
                StringBuilder builder = new StringBuilder();
                builder.append(manifoldCoordinate.getPreferredDescriptionText(chronology.getReferencedComponentNid()));
                builder.append("\n").append(dependencyManagmentUuid.toString());
                if (Get.identifierService().hasUuid(dependencyManagmentUuid)) {
                    builder.append(" exists\n");
                } else {
                    builder.append(" unused\n");
                }

                for (Version version: chronology.getVersionList()) {
                    Str1_Str2_Version moduleDependencyVersion = (Str1_Str2_Version) version;
                    builder.append(version);
                    builder.append("\n");
                    dependencyMap.getIfAbsentPut(dependencyManagmentUuid, () -> Sets.mutable.empty()).add(new DependencyRecord(dependentModuleNid,
                            DateTimeUtil.compressedParse(moduleDependencyVersion.getStr1() + "T000000Z"),
                            providerModuleNid,
                            DateTimeUtil.compressedParse(moduleDependencyVersion.getStr2() + "T000000Z")));
                }
                builder.append("\n\n");
                System.out.print(builder);
            }
        });
//            SemanticBuilder<? extends SemanticChronology> originElementBuilder =  Get.semanticBuilderService()
//                    .getComponentLongSemanticBuilder(origin.getPathForPositionNid(), origin.getTime(),
//                            conceptNid, TermAux.DEPENDENCY_MANAGEMENT.getNid());

        dependencyMap.forEach((uuid, dependencyRecords) -> {
            addSemantic(uuid, dependencyRecords);
        });
        System.out.println(dependencyMap);
        return null;
    }
    private void addSemantic(UUID uuid, MutableSet<DependencyRecord> dependencyRecords) {
        SemanticChronologyImpl chronology = null;
        for (DependencyRecord dependencyRecord: dependencyRecords) {
            if (chronology == null) {
                chronology = new SemanticChronologyImpl(
                        VersionType.Nid1_Long2,
                        uuid,
                        TermAux.DEPENDENCY_MANAGEMENT.getNid(),
                        dependencyRecord.providerModuleNid);
            }
            addVersion(chronology, dependencyRecord);
        }
        Get.assemblageService().writeAndIndexSemanticChronology(chronology);
    }

    private void addVersion(SemanticChronologyImpl refsetMemberToWrite, DependencyRecord dependencyRecord) {
        int versionStamp = Get.stampService().getStampSequence(Status.ACTIVE,
                dependencyRecord.dependentModuleVersion, TermAux.USER.getNid(),
                TermAux.PRIMORDIAL_MODULE.getNid(), TermAux.DEVELOPMENT_PATH.getNid());
        Nid1_Long2_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setNid1(dependencyRecord.dependentModuleNid);
        brittleVersion.setLong2(dependencyRecord.providerModuleVersion);
    }

    // dependent module -> dependent version, provider module, provider version.
    private class DependencyRecord {
        final int dependentModuleNid;
        final long dependentModuleVersion;
        final int providerModuleNid;
        final long providerModuleVersion;

        public DependencyRecord(int dependentModuleNid, long dependentModuleVersion, int providerModuleNid, long providerModuleVersion) {
            this.dependentModuleNid = dependentModuleNid;
            this.dependentModuleVersion = dependentModuleVersion;
            this.providerModuleNid = providerModuleNid;
            this.providerModuleVersion = providerModuleVersion;
        }

        @Override
        public String toString() {
            return "DependencyRecord{" +
                    "\n  dependentModule=" + manifoldCoordinate.getPreferredDescriptionText(dependentModuleNid) +
                    ", \n  dependentModuleVersion=" + DateTimeUtil.format(dependentModuleVersion) +
                    ", \n  providerModuleNid=" + manifoldCoordinate.getPreferredDescriptionText(providerModuleNid) +
                    ", \n  providerModuleVersion=" + DateTimeUtil.format(providerModuleVersion) +
                    "\n}";
        }
    }
}
