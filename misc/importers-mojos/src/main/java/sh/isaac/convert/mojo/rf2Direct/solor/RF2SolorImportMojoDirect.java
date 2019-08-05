package sh.isaac.convert.mojo.rf2Direct.solor;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 2019-08-02
 * aks8m - https://github.com/aks8m
 */
@Mojo(name = "solor-direct-rf2-import", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RF2SolorImportMojoDirect extends RF2SolorImportDirect {

    public RF2SolorImportMojoDirect() {
    }
}
