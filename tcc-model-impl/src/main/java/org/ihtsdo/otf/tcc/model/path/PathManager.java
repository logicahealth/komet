/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.otf.tcc.model.path;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.PathService;
import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.model.cc.ReferenceConcepts;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_int.NidIntMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_long.NidLongMember;

/**
 * OldPath management.
 *
 * Defines methods for obtaining and modifying paths. Paths are now
 * stored/defined in reference sets (extension by reference).
 *
 * This implementation avoids the use of the redundant OldPath termstore and
 * instead marshals to to the Extension termstore (indirectly).
 *
 */
public class PathManager implements PathService {

    private static final Logger logger = Logger.getLogger(PathManager.class.getName());
    private static Lock l = new ReentrantLock();
    private static IdentifierService identifierService;
    private static IdentifierService getIdentifierService() {
        if (identifierService == null) {
            identifierService = LookupService.getService(IdentifierService.class);
        }
        return identifierService;
    }
    
    private static ConceptService conceptService = null;
    private static ConceptService getConceptService() {
        if (conceptService == null) {
            conceptService = LookupService.getService(ConceptService.class);
        }
        return conceptService;
    }
    //~--- fields --------------------------------------------------------------
    ConcurrentHashMap<Integer, Path> pathMap;
    private ConceptChronicle pathRefsetConcept;
    private ConceptChronicle refsetPathOriginsConcept;

    //~--- constructors --------------------------------------------------------
    private PathManager() throws IOException {
        try {
            setupPathMap();
        } catch (Exception e) {
            throw new IOException("Unable to initialise path management.", e);
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean exists(int pathConceptId) {
        if (pathConceptId >= 0) {
            pathConceptId = getIdentifierService().getConceptNid(pathConceptId);
        }
        if (pathMap.containsKey(pathConceptId)) {
            return true;
        }

        return getFromDisk(pathConceptId) != null;
    }

    @SuppressWarnings("unchecked")
    private void setupPathMap() throws IOException {
        if (pathMap == null) {
            l.lock();
            pathMap = new ConcurrentHashMap<>();
            try {
                getPathRefsetConcept();

                for (RefexMember extPart : getPathRefsetConcept().getExtensions()) {
                    if (extPart instanceof NidMember) {
                        NidMember conceptExtension = (NidMember) extPart;
                        int pathId = conceptExtension.getC1Nid();
                        pathMap.put(pathId, new Path(pathId, getPathOriginsFromDb(pathId)));
                    } else {
                        int pathId = extPart.getReferencedComponentNid();
                        pathMap.put(pathId, new Path(pathId, getPathOriginsFromDb(pathId)));
                    }
                }
            } catch (Exception e) {
                throw new IOException("Unable to retrieve all paths.", e);
            } finally {
                l.unlock();
            }
        }
    }


    @SuppressWarnings("unchecked")
    private Path getFromDisk(int cNid) {
        try {
            for (RefexMember extPart : getPathRefsetConcept().getExtensions()) {
                int pathId;
                if (extPart instanceof NidMember) {
                    NidMember conceptExtension = (NidMember) extPart;
                    pathId = conceptExtension.getC1Nid();
                } else {
                    pathId = extPart.getReferencedComponentNid();
                }
                if (pathId == cNid) {
                    pathMap.put(pathId, new Path(pathId, getPathOriginsFromDb(pathId)));
                    return pathMap.get(cNid);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve all paths.", e);
        }

        return null;
    }

    @Override
    public Collection<? extends StampPosition> getOrigins(int stampPathSequence) {
        if (stampPathSequence < 0) {
            stampPathSequence = getIdentifierService().getConceptSequence(stampPathSequence);
        }
        return getPathOriginsFromDb(stampPathSequence);
    }

    private List<Position> getPathOriginsFromDb(int nid) {
        return getPathOriginsWithDepth(nid, 0);
    }

    private List<Position> getPathOriginsWithDepth(int nid, int depth) {
        try {
            ArrayList<Position> result = new ArrayList<>();
            ConceptChronicle pathConcept = (ConceptChronicle) Ts.get().getConcept(nid);

            for (RefexChronicleBI<?> extPart : pathConcept.getRefexMembers(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid())) {
                if (extPart == null) {
                    logger.log(Level.SEVERE, "", new Exception("Null path origins for: "
                            + pathConcept.toLongString() + "\n\nin refset: \n\n"
                            + getRefsetPathOriginsConcept().toLongString()));
                } else {
                    if (extPart instanceof NidLongMember) {
                        NidLongMember conceptExtension = (NidLongMember) extPart;

                        if (conceptExtension.getC1Nid() == nid) {
                            logger.log(Level.SEVERE, "Self-referencing origin in path: {0}",
                                    pathConcept.getDescriptions().iterator().next());
                        } else {
                            if (pathMap.containsKey(conceptExtension.getC1Nid())) {
                                result.add(new Position(conceptExtension.getLong1(),
                                        pathMap.get(conceptExtension.getC1Nid())));
                            } else {
                                if (depth > 40) {
                                    logger.log(Level.SEVERE, "",
                                            new Exception(
                                                    "\n\n****************************************\nDepth limit exceeded. Path concept: \n"
                                                    + pathConcept.toLongString() + "\n\n extensionPart: \n\n"
                                                    + extPart.toString() + "\n\n origin refset: \n\n"
                                                    + ConceptChronicle.get(extPart.getAssemblageNid()).toLongString()
                                                    + "\n-------------------------------------------\n\n"));
                                } else {
                                    result.add(new Position(conceptExtension.getLong1(),
                                            new Path(conceptExtension.getC1Nid(),
                                                    getPathOriginsWithDepth(conceptExtension.getC1Nid(),
                                                            depth + 1))));
                                }
                            }
                        }
                    } else {
                        // TODO remove after paths convert to NidLongMembers automatically on import...

                        NidIntMember conceptExtension = (NidIntMember) extPart;

                        if (conceptExtension.getC1Nid() == nid) {
                            logger.log(Level.SEVERE, "Self-referencing origin in path[2]: {0}",
                                    pathConcept.getDescriptions().iterator().next());
                        } else {
                            if (pathMap.containsKey(conceptExtension.getC1Nid())) {
                                result.add(new Position(ThinVersionHelper.convert(conceptExtension.getInt1()),
                                        pathMap.get(conceptExtension.getC1Nid())));
                            } else {
                                if (depth > 40) {
                                    logger.log(Level.SEVERE, "",
                                            new Exception(
                                                    "\n\n****************************************\nDepth limit exceeded[2]. Path concept: \n"
                                                    + pathConcept.toLongString() + "\n\n extensionPart: \n\n"
                                                    + extPart.toString() + "\n\n origin refset: \n\n"
                                                    + ConceptChronicle.get(extPart.getAssemblageNid()).toLongString()
                                                    + "\n-------------------------------------------\n\n"));
                                } else {
                                    result.add(new Position(ThinVersionHelper.convert(conceptExtension.getInt1()),
                                            new Path(conceptExtension.getC1Nid(),
                                                    getPathOriginsWithDepth(conceptExtension.getC1Nid(),
                                                            depth + 1))));
                                }
                            }
                        }
                    }
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve path origins.", e);
        }
    }

    private ConceptChronicle getPathRefsetConcept() throws IOException {
        if (pathRefsetConcept == null) {
            pathRefsetConcept = (ConceptChronicle) Ts.get().getConcept(ReferenceConcepts.REFSET_PATHS.getNid());
        }

        return pathRefsetConcept;
    }

    private ConceptChronicle getRefsetPathOriginsConcept() throws IOException {
        if (this.refsetPathOriginsConcept == null) {
            this.refsetPathOriginsConcept = ConceptChronicle.get(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid());
        }

        return refsetPathOriginsConcept;
    }

    @Override
    public StampPath getStampPath(int stampPathSequence) {
        if (stampPathSequence >= 0) {
            stampPathSequence = getIdentifierService().getConceptNid(stampPathSequence);
        }
        if (exists(stampPathSequence)) {
            return pathMap.get(stampPathSequence);
        } else {
            Path p = getFromDisk(stampPathSequence);

            if (p != null) {
                return p;
            }
        }
        return null;
    }
}
