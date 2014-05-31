package org.ihtsdo.otf.tcc.model.cc.computer.version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.chronicle.TypedComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.nid.NidSet;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.model.cc.ReferenceConcepts;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.Version;
import org.ihtsdo.otf.tcc.model.version.RelativePositionComputer;
import org.ihtsdo.otf.tcc.model.version.RelativePositionComputerBI;
import org.ihtsdo.otf.tcc.model.version.RelativePositionComputerBI.RelativePosition;

public class VersionComputer<V extends Version<?, ?>> {

    protected static final Logger logger = Logger.getLogger(VersionComputer.class.getName());

    private void handlePart(HashSet<V> partsForPosition,
            RelativePositionComputerBI mapper, V part,
            Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager,
            EnumSet<Status> allowedStatus) throws RuntimeException {
        List<V> partsToCompare =
                new ArrayList<>(partsForPosition);
        for (V prevPartToTest : partsToCompare) {
            switch (mapper.fastRelativePosition(part,
                    prevPartToTest, precedencePolicy)) {
                case AFTER:
                    partsForPosition.remove(prevPartToTest);
                    partsForPosition.add(part);
                    break;
                case BEFORE:
                    break;
                case CONTRADICTION:
                    if (contradictionManager != null
                            && allowedStatus != null) {
                        partsForPosition.remove(prevPartToTest);
                        partsForPosition.addAll(
                                contradictionManager.resolveVersions(
                                part, prevPartToTest));
                    } else {
                        partsForPosition.add(part);
                        partsForPosition.add(prevPartToTest);
                    }
                    break;
                case EQUAL:
                    // Can only have one part per time/path
                    // combination.
                    if (prevPartToTest.equals(part)) {
                        // part already added from another position.
                        // No need to add again.
                        break;
                    }
                    // Duplicate values encountered.
                    errorCount++;
                    if (errorCount < 5) {
                        logger.log(
                                Level.WARNING, "{0}"
                                + " should never happen. "
                                + "Data is malformed. sap: {1} Part:\n{2} \n  Part to test: \n{3}",
                                new Object[]{RelativePosition.EQUAL,
                                    part.getStamp(),
                                    part,
                                    prevPartToTest});
                    }
                    break;
                case UNREACHABLE:
                    // Should have failed mapper.onRoute(part)
                    // above.
                    throw new RuntimeException(
                            RelativePosition.UNREACHABLE
                            + " should never happen.");
            }
        }
    }

    private class SortVersionsByTimeThenAuthor implements Comparator<V> {

        @Override
        public int compare(V p1, V p2) {

            if (p1.getTime() < p2.getTime()) {
                return -1;
            }
            if (p1.getTime() == p2.getTime()) {
                if (p1.getPathNid() == p2.getPathNid()) {
                    if (p1.getAuthorNid() == p2.getAuthorNid()) {
                        return p1.getStatus().ordinal() - p2.getStatus().ordinal();
                    }
                    return p1.getAuthorNid() - p2.getAuthorNid();
                } else {
                    if (p1.getPathNid() > p2.getPathNid()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
            return 1;
        }
    }
    private int errorCount = 0;

    public void addSpecifiedVersions(EnumSet<Status>  allowedStatus,
            Position viewPosition, List<V> specifiedVersions,
            List<V> versions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionMgr) {
        addSpecifiedVersions(allowedStatus, (NidSetBI) null,
                viewPosition,
                specifiedVersions, versions, precedencePolicy, contradictionMgr);
    }

    public Collection<V> getSpecifiedVersions(EnumSet<Status>  allowedStatus,
            Position viewPosition,
            List<? extends V> versions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) {
        List<V> specifiedVersions = new ArrayList<>();
        addSpecifiedVersions(allowedStatus, (NidSetBI) null,
                viewPosition,
                specifiedVersions, versions, precedencePolicy,
                contradictionManager);
        return specifiedVersions;

    }

    public void addSpecifiedRelVersions(EnumSet<Status>  allowedStatus, NidSetBI allowedTypes,
            Position viewPosition, List<V> matchingTuples,
            List<V> versions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) {
        if (viewPosition == null) {
            addSpecifiedVersionsNullPositions(allowedStatus, allowedTypes,
                    matchingTuples, versions, precedencePolicy,
                    contradictionManager, null);
        } else {
            addSpecifiedVersionsWithPositions(allowedStatus, allowedTypes,
                    viewPosition, matchingTuples, versions, precedencePolicy,
                    contradictionManager, new InferredFilter(ReferenceConcepts.SNOROCKET.getNid()));
            addSpecifiedVersionsWithPositions(allowedStatus, allowedTypes,
                    viewPosition, matchingTuples, versions, precedencePolicy,
                    contradictionManager, new StatedFilter(ReferenceConcepts.SNOROCKET.getNid()));
        }
    }

    public void addSpecifiedRelVersions(List<V> matchingVersions, List<V> versions, ViewCoordinate c) {
        if (c.getViewPosition() == null) {
            addSpecifiedVersionsNullPositions(c.getAllowedStatus(), null,
                    matchingVersions, versions, c.getPrecedence(),
                    c.getContradictionManager(), null);
        } else {
            if (c.getRelationshipAssertionType() == RelAssertionType.INFERRED) {
                addSpecifiedVersionsWithPositions(c.getAllowedStatus(), null,
                        c.getViewPosition(), matchingVersions, versions, c.getPrecedence(),
                        c.getContradictionManager(), new InferredFilter(c.getClassifierNid()));
            } else if (c.getRelationshipAssertionType() == RelAssertionType.STATED) {
                addSpecifiedVersionsWithPositions(c.getAllowedStatus(), null,
                        c.getViewPosition(), matchingVersions, versions, c.getPrecedence(),
                        c.getContradictionManager(), new StatedFilter(c.getClassifierNid()));
            } else if (c.getRelationshipAssertionType() == RelAssertionType.INFERRED_THEN_STATED) {
                List<V> possibleValues = new ArrayList<>();
                addSpecifiedVersionsWithPositions(c.getAllowedStatus(), null,
                        c.getViewPosition(), possibleValues, versions, c.getPrecedence(),
                        c.getContradictionManager(), new InferredFilter(c.getClassifierNid()));
                if (possibleValues.isEmpty()) {
                    addSpecifiedVersionsWithPositions(c.getAllowedStatus(), null,
                            c.getViewPosition(), possibleValues, versions, c.getPrecedence(),
                            c.getContradictionManager(), new StatedFilter(c.getClassifierNid()));
                }
                matchingVersions.addAll(possibleValues);
            } else {
                throw new RuntimeException("Can't handle: "
                        + c.getRelationshipAssertionType());
            }
        }
    }

    /**
     *
     * @param allowedStatus
     * <code>null</code> is a wildcard.
     * @param allowedTypes
     * <code>null</code> is a wildcard.
     * @param viewPosition
     * <code>null</code> is a wildcard.
     * @param specifiedVersions
     * @param addUncommitted
     * @param versions
     * @param core
     */
    public void addSpecifiedVersions(EnumSet<Status>  allowedStatus,
            NidSetBI allowedTypes, Position viewPosition,
            List<V> specifiedVersions, List<? extends V> versions,
            Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) {
        if (viewPosition == null) {
            addSpecifiedVersionsNullPositions(allowedStatus, allowedTypes,
                    specifiedVersions, versions, precedencePolicy,
                    contradictionManager, null);
        } else {
            addSpecifiedVersionsWithPositions(allowedStatus, allowedTypes,
                    viewPosition, specifiedVersions, versions, precedencePolicy,
                    contradictionManager, null);
        }
    }

    public void addSpecifiedVersions(EnumSet<Status> allowedStatus,
            NidSetBI allowedTypes, Position viewPosition,
            List<V> specifiedVersions, List<? extends V> versions,
            Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager,
            long cutoffTime) {
        if (viewPosition == null) {
            addSpecifiedVersionsNullPositions(allowedStatus, allowedTypes,
                    specifiedVersions, versions, precedencePolicy,
                    contradictionManager, null);
        } else if (cutoffTime != 0) {
            //add more filters here if needed
            InferredFilter[] filters = new InferredFilter[1];
            filters[0] = new CutoffFilter(cutoffTime);
            addSpecifiedVersionsWithCutoff(allowedStatus, allowedTypes,
                    viewPosition, specifiedVersions, versions, precedencePolicy,
                    contradictionManager, filters);
        } else {
            addSpecifiedVersionsWithPositions(allowedStatus, allowedTypes,
                    viewPosition, specifiedVersions, versions, precedencePolicy,
                    contradictionManager, null);
        }
    }

    static class CutoffFilter extends InferredFilter {

        Long cutoffTime;

        private CutoffFilter(long cutoffTime) {
            this.cutoffTime = cutoffTime;
        }

        @Override
        public boolean pass(Version<?, ?> part) {
            if (part.getTime() > cutoffTime) {
                return false;
            } else if (part.getTime() < cutoffTime) {
                return true;
            }
            return false;
        }
    }

    private static class StatedFilter extends InferredFilter {

        private StatedFilter(int... nids) {
            super(nids);
        }

        @Override
        public boolean pass(Version<?, ?> part) {
            return !super.pass(part);
        }
    }

    private static class InferredFilter {

        NidSetBI classifierNid = new NidSet();

        private InferredFilter(int... nids) {
            if (inferredNidSet == null) {
                try {
                    NidSetBI tempInferred = new NidSet();
                    tempInferred.add(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid());

                    inferredNidSet = tempInferred;
                } catch (ValidationException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            for (int nid : nids) {
                classifierNid.add(nid);
            }
        }

        public boolean pass(Version<?, ?> part) {
            if (classifierNid.contains(part.getAuthorNid())) {
                return true;
            }
            if (part instanceof RelationshipVersionBI) {
                RelationshipVersionBI relPart = (RelationshipVersionBI) part;
                if (inferredNidSet.contains(relPart.getCharacteristicNid())) {
                    return true;
                }
            }
            return false;
        }
    }
    private static NidSetBI inferredNidSet;

    private void addSpecifiedVersionsWithCutoff(EnumSet<Status> allowedStatus,
            NidSetBI allowedTypes,
            Position viewPosition,
            List<V> specifiedVersions,
            List<? extends V> versions,
            Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, InferredFilter[] filters) {
        HashSet<V> partsToAdd = new HashSet<>();
        
            HashSet<V> partsForPosition = new HashSet<>();
            RelativePositionComputerBI mapper = RelativePositionComputer.getComputer(viewPosition);
            nextpart:
            for (V part : versions) {
                if (part.getTime() == Long.MIN_VALUE) {
                    continue nextpart;
                }
                for (InferredFilter filter : filters) {
                    if (filter != null && !filter.pass(part)) {
                        continue nextpart;
                    }
                }
                if (allowedTypes != null) {
                    if (allowedTypes.contains(
                            ((TypedComponentVersionBI) part).getTypeNid()) == false) {
                        if (mapper.onRoute(part)) {
                            handlePart(partsForPosition, mapper, part,
                                    precedencePolicy, contradictionManager,
                                    allowedStatus);
                            partsForPosition.remove(part);
                        }
                        continue nextpart;
                    }
                }
                if (mapper.onRoute(part)) {
                    if (partsForPosition.isEmpty()) {
                        partsForPosition.add(part);
                    } else {
                        handlePart(partsForPosition, mapper, part,
                                precedencePolicy, contradictionManager,
                                allowedStatus);
                    }
                }
            }
            if (allowedStatus != null) {
                List<V> partsToCompare = new ArrayList<>(partsForPosition);
                for (V part : partsToCompare) { 
                    if (!allowedStatus.contains(part.getStatus())) {
                        partsForPosition.remove(part);
                    }
                }
            }
            if (partsForPosition.size() > 0) {
                partsToAdd.addAll(partsForPosition);
            }
        
        specifiedVersions.addAll(partsToAdd);
    }

    private void addSpecifiedVersionsWithPositions(EnumSet<Status>  allowedStatus,
            NidSetBI allowedTypes,
            Position viewPosition,
            List<V> specifiedVersions,
            List<? extends V> versions,
            Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, InferredFilter filter) {
        HashSet<V> partsToAdd = new HashSet<>();
        
            HashSet<V> partsForPosition = new HashSet<>();
            RelativePositionComputerBI mapper = RelativePositionComputer.getComputer(viewPosition);
            nextpart:
            for (V part : versions) {
                if (part.getTime() == Long.MIN_VALUE) {
                    continue nextpart;
                }
                if (filter != null && !filter.pass(part)) {
                    continue nextpart;
                }
                if (allowedTypes != null) {
                    if (allowedTypes.contains(
                            ((TypedComponentVersionBI) part).getTypeNid()) == false) {
                        if (mapper.onRoute(part)) {
                            handlePart(partsForPosition, mapper, part,
                                    precedencePolicy, contradictionManager,
                                    allowedStatus);
                            partsForPosition.remove(part);
                        }
                        continue nextpart;
                    }
                }
                if (mapper.onRoute(part)) {
                    if (partsForPosition.isEmpty()) {
                        partsForPosition.add(part);
                    } else {
                        handlePart(partsForPosition, mapper, part,
                                precedencePolicy, contradictionManager,
                                allowedStatus);
                    }
                }
            }
            if (allowedStatus != null && allowedStatus.size() > 0) {
                List<V> partsToCompare = new ArrayList<>(partsForPosition);
                for (V part : partsToCompare) {                   
                    if (!allowedStatus.contains(part.getStatus())) {
                        partsForPosition.remove(part);
                    }
                }
            }
            if (partsForPosition.size() > 0) {
                partsToAdd.addAll(partsForPosition);
            }
        
        specifiedVersions.addAll(partsToAdd);
    }

    /**
     *
     * @param allowedStatus
     * @param allowedTypes
     * @param specifiedVersions
     * @param addUncommitted
     * @param versions
     * @param core
     */
    private void addSpecifiedVersionsNullPositions(EnumSet<Status>  allowedStatus,
            NidSetBI allowedTypes,
            List<V> specifiedVersions,
            List<? extends V> versions,
            Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, InferredFilter filter) {
        if (versions == null) {
            return;
        }
        HashSet<V> versionsToAdd = new HashSet<>();
        HashSet<V> rejectedVersions = new HashSet<>();
        nextpart:
        for (V part : versions) {
            if (part.getTime() == Long.MIN_VALUE
                    || (filter != null && filter.pass(part))) {
                rejectedVersions.add(part);
                continue nextpart;
            }
            if (allowedStatus != null && allowedStatus.size() > 0
                    && allowedStatus.contains(part.getStatus()) == false) {
                rejectedVersions.add(part);
                continue nextpart;
            }
            if (allowedTypes != null
                    && allowedTypes.contains(
                    ((TypedComponentVersionBI) part).getTypeNid()) == false) {
                rejectedVersions.add(part);
                continue nextpart;
            }
            versionsToAdd.add(part);
        }
        ArrayList<V> versionToRemove = new ArrayList<>();
        for (V reject : rejectedVersions) {
            for (V possibleAdd : versionsToAdd) {
                if (reject.getPathNid() == possibleAdd.getPathNid()) {
                    if (reject.getTime() > possibleAdd.getTime()) {
                        versionToRemove.add(possibleAdd);
                    }
                }
            }
        }
        versionsToAdd.removeAll(versionToRemove);

        SortedSet<V> sortedVersionsToAdd =
                new TreeSet<>(new SortVersionsByTimeThenAuthor());
        sortedVersionsToAdd.addAll(versionsToAdd);
        specifiedVersions.addAll(sortedVersionsToAdd);
    }
}
