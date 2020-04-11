package sh.isaac.solor.rf2.utility;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Nid1_Nid2_Int3_Version;
import sh.isaac.api.util.UuidT3Generator;

import java.util.HashMap;

/**
 * 2019-01-24
 * aks8m - https://github.com/aks8m
 */
public class PreExportUtility {

    private static HashMap<Integer, Integer[]> refsetDescriptorHeaders = new HashMap<>();
    private final ManifoldCoordinate manifold;

    public PreExportUtility(ManifoldCoordinate manifold) {
        this.manifold = manifold;

    }

    public HashMap<Integer, Integer[]> generateRefsetDescriptorHeaders(){
        Get.assemblageService().getSemanticNidStream(Get.concept(UuidT3Generator.fromSNOMED("900000000000456007")).getNid())
                .forEach(semanticNid -> {

                    Observable_Nid1_Nid2_Int3_Version refDesc =
                            ((LatestVersion<Observable_Nid1_Nid2_Int3_Version>)
                                    Get.observableSnapshotService(this.manifold.getStampFilter()).getObservableSemanticVersion(semanticNid))
                                    .get();

                    if(!refsetDescriptorHeaders.containsKey(refDesc.getReferencedComponentNid())){
                        Integer[] headerColumns = new Integer[12];

                        headerColumns[refDesc.getInt3()] = refDesc.getNid1();
                        refsetDescriptorHeaders.put(refDesc.getReferencedComponentNid(), headerColumns);
                    }else{
                        refsetDescriptorHeaders.get(refDesc.getReferencedComponentNid())[refDesc.getInt3()] = refDesc.getNid1();
                    }
                });
        return refsetDescriptorHeaders;
    }
}
