package org.ihtsdo.otf.tcc.model.cc.computer.version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.OpenBitSet;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.Version;
import org.ihtsdo.otf.tcc.model.version.RelativePositionComputer;
import org.ihtsdo.otf.tcc.model.version.RelativePositionComputerBI;

/**
 * The Navigation class can take multiple positions and determine where they are in the bundle's "path space"
 *
 * TODO: Maybe the navigator class can implement the conflict resolution policy?
 *
 * @author kec
 *
 */
public abstract class Navigator {

    public <V extends Version<?, ?>> List<V> locateLatest(List<V> parts,
            ViewCoordinate vc) throws IOException {
        V latest = null;
        OpenBitSet resultsPartSet = new OpenBitSet(parts.size());
        
            RelativePositionComputerBI mapper = RelativePositionComputer.getComputer(vc.getViewPosition());
            OpenBitSet iteratorPartSet = new OpenBitSet(parts.size());
            for (int i = 0; i < parts.size(); i++) {
                V part = parts.get(i);
                if (mapper.onRoute(part)) {
                    if (latest == null) {
                        latest = part;
                        iteratorPartSet.set(i);
                    } else {
                        switch (mapper.relativePosition(latest, part)) {
                            case BEFORE:
                                // nothing to do
                                break;

                            case CONTRADICTION:
                                iteratorPartSet.set(i);
                                break;

                            case AFTER:
                                latest = part;
                                iteratorPartSet.clear(0, Integer.MAX_VALUE);
                                iteratorPartSet.set(i);
                                break;

                            default:
                                break;
                        }
                    }
                }
            }
            resultsPartSet.or(iteratorPartSet);
        
        List<V> resultsList = new ArrayList<>((int) resultsPartSet.cardinality());
        DocIdSetIterator resultsItr = resultsPartSet.iterator();
        int id = resultsItr.nextDoc();
        while (id != DocIdSetIterator.NO_MORE_DOCS) {
            resultsList.add(parts.get(id));
            id = resultsItr.nextDoc();
        }
        return resultsList;
    }
}
