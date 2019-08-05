package sh.isaac.convert.mojo.loinc.solor;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 2019-08-05
 * aks8m - https://github.com/aks8m
 */
@Mojo(name = "solor-direct-loinc-rf2-collab-import", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class LoincRF2CollabSolorDirectMojo extends LoincRF2CollabSolorImportDirect {

    public LoincRF2CollabSolorDirectMojo() {
    }
}
