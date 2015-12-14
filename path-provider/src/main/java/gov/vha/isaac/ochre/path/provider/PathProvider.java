/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.path.provider;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.PathService;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePosition;
import gov.vha.isaac.ochre.model.coordinate.StampPathImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * TODO handle versions properly in path provider.
 *
 * @author kec
 */
@Service(name = "Path Provider")
@RunLevel(value = 2)

public class PathProvider implements PathService {

    private static final Logger LOG = LogManager.getLogger();

    private static final Lock LOCK = new ReentrantLock();

    //~--- fields --------------------------------------------------------------
    ConcurrentHashMap<Integer, StampPath> pathMap;

    //~--- constructors --------------------------------------------------------
    protected PathProvider() {

    }

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean exists(int pathConceptId) {
        setupPathMap();
        if (pathConceptId < 0) {
            pathConceptId = Get.identifierService().getConceptSequence(pathConceptId);
        }
        if (pathMap.containsKey(pathConceptId)) {
            return true;
        }
        Optional<StampPath> stampPath = getFromDisk(pathConceptId);
        return stampPath.isPresent();
    }

    private void setupPathMap() {
        if (pathMap == null) {
            LOCK.lock();
            try {
                pathMap = new ConcurrentHashMap<>();
                Get.sememeService().getSememesFromAssemblage(
                        TermAux.PATH_ASSEMBLAGE.getConceptSequence()).forEach((pathSememe) -> {
                            int pathSequence = Get.identifierService().getConceptSequence(pathSememe.getReferencedComponentNid());
                            pathMap.put(pathSequence,
                                    new StampPathImpl(pathSequence));
                        });
            } finally {
                LOCK.unlock();
            }
        }
    }

    private Optional<StampPath> getFromDisk(int stampPathSequence) {
        return Get.sememeService().getSememesForComponentFromAssemblage(stampPathSequence,
                TermAux.PATH_ASSEMBLAGE.getConceptSequence()).map((sememeChronicle) -> {

                    int pathId = sememeChronicle.getReferencedComponentNid();
                    pathId = Get.identifierService().getConceptSequence(pathId);
                    assert pathId == stampPathSequence : "pathId: " + pathId + " stampPathSequence: " + stampPathSequence;
                    StampPath stampPath = new StampPathImpl(stampPathSequence);
                    pathMap.put(stampPathSequence, stampPath);
                    return stampPath;
                }).findFirst();
    }

    @Override
    public Collection<? extends StampPosition> getOrigins(int stampPathSequence) {
        setupPathMap();
        if (stampPathSequence < 0) {
            stampPathSequence = Get.identifierService().getConceptSequence(stampPathSequence);
        }
        return getPathOriginsFromDb(stampPathSequence);
    }

    private List<StampPosition> getPathOriginsFromDb(int nid) {
        return Get.sememeService().getSememesForComponentFromAssemblage(nid,
                TermAux.PATH_ORIGIN_ASSEMBLAGE.getConceptSequence())
                .map((pathOrigin) -> {
                    long time = ((LongSememe) pathOrigin.getVersionList().get(0)).getLongValue();
                    return new StampPositionImpl(time, Get.identifierService().getConceptSequence(nid));
                })
                .collect(Collectors.toList());
    }

    @Override
    public StampPath getStampPath(int stampPathSequence) {
        setupPathMap();
        if (stampPathSequence < 0) {
            stampPathSequence = Get.identifierService().getConceptSequence(stampPathSequence);
        }
        if (exists(stampPathSequence)) {
            return pathMap.get(stampPathSequence);
        }
        Optional<StampPath> stampPath = getFromDisk(stampPathSequence);
        if (stampPath.isPresent()) {
            return stampPath.get();
        }
        throw new IllegalStateException("No path for: " + stampPathSequence
                + " " + Get.conceptService().getConcept(stampPathSequence).toString());
    }

    @Override
    public Collection<? extends StampPath> getPaths() {
        return Get.sememeService().getSememesFromAssemblage(
                TermAux.PATH_ASSEMBLAGE.getConceptSequence()).map((sememeChronicle) -> {
                    int pathId = sememeChronicle.getReferencedComponentNid();
                    pathId = Get.identifierService().getConceptSequence(pathId);
                    StampPath stampPath = new StampPathImpl(pathId);
                    return stampPath;
                }).collect(Collectors.toList());
    }

    @Override
    public RelativePosition getRelativePosition(StampedVersion v1, StampedVersion v2) {
        if (v1.getPathSequence() == v2.getPathSequence()) {
            if (v1.getTime() < v2.getTime()) {
                return RelativePosition.BEFORE;
            }
            if (v1.getTime() > v2.getTime()) {
                return RelativePosition.AFTER;
            }
            return RelativePosition.EQUAL;
        }
        if (traverseOrigins(v1, getStampPath(v2.getPathSequence())) == RelativePosition.BEFORE) {
            return RelativePosition.BEFORE;
        }
        return traverseOrigins(v2, getStampPath(v1.getPathSequence()));
    }

    private RelativePosition traverseOrigins(StampedVersion v1, StampPath path) {
        for (StampPosition origin : path.getPathOrigins()) {
            if (origin.getStampPathSequence() == v1.getPathSequence()) {
                if (v1.getTime() <= origin.getTime()) {
                    return RelativePosition.BEFORE;
                }
            }
        }
        return RelativePosition.UNREACHABLE;
    }

    @Override
    public RelativePosition getRelativePosition(int stampSequence1, int stampSequence2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
