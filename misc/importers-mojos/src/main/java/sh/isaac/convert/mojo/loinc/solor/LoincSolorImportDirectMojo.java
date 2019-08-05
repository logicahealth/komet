package sh.isaac.convert.mojo.loinc.solor;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 2019-08-05
 * aks8m - https://github.com/aks8m
 */
@Mojo(name = "solor-direct-loinc-import", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class LoincSolorImportDirectMojo extends LoincSolorImportDirect {

    public LoincSolorImportDirectMojo() {
    }
}
