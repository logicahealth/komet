package sh.isaac.provider.elk;

import org.semanticweb.elk.loading.AxiomLoader;
import org.semanticweb.elk.util.concurrent.computation.InterruptMonitor;

public class ElkLoaderFactory implements AxiomLoader.Factory {
    @Override
    public AxiomLoader getAxiomLoader(InterruptMonitor interrupter) {
        return null;
    }
}
