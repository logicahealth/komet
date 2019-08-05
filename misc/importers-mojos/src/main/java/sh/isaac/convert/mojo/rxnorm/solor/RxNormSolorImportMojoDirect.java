package sh.isaac.convert.mojo.rxnorm.solor;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 2019-08-05
 * aks8m - https://github.com/aks8m
 */
@Mojo(name = "solor-direct-rxnorm-import", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RxNormSolorImportMojoDirect extends RxNormSolorImportDirect {

    public RxNormSolorImportMojoDirect() {
    }
}
