package sh.isaac.api.coordinate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

//This class is not treated as a service, however, it needs the annotation, so that the reset() gets fired at appropriate times.
@Service
public final class LanguageCoordinateImmutable implements LanguageCoordinate, ImmutableCoordinate, ChronologyChangeListener, StaticIsaacCache {

    private static final ConcurrentReferenceHashMap<LanguageCoordinateImmutable, LanguageCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 1;
    public static final String UNKNOWN_COMPONENT_TYPE = "Unknown component type";

    final private int languageConceptNid;
    final private ImmutableIntList descriptionTypePreferenceList;
    final private ImmutableIntList dialectAssemblagePreferenceList;
    final private ImmutableIntList modulePreferenceListForLanguage;
    final private LanguageCoordinateImmutable nextPriorityLanguageCoordinate;

    private ConcurrentReferenceHashMap<StampFilterImmutable, Cache<Integer, String>> preferredCaches;

    private ConcurrentReferenceHashMap<StampFilterImmutable, Cache<Integer, String>> fqnCaches;

    private ConcurrentReferenceHashMap<StampFilterImmutable, Cache<Integer, String>> descriptionCaches;

    private LanguageCoordinateImmutable(ConceptSpecification languageConcept,
                                       ImmutableIntList descriptionTypePreferenceList,
                                       ImmutableIntList dialectAssemblagePreferenceList,
                                       ImmutableIntList modulePreferenceListForLanguage) {
        this(languageConcept.getNid(), descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                modulePreferenceListForLanguage, null);
    }

    private LanguageCoordinateImmutable(int languageConceptNid,
                                       ImmutableIntList descriptionTypePreferenceList,
                                       ImmutableIntList dialectAssemblagePreferenceList,
                                       ImmutableIntList modulePreferenceListForLanguage) {
        this(languageConceptNid, descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                modulePreferenceListForLanguage, null);
    }

    private LanguageCoordinateImmutable(int languageConceptNid,
                                       ImmutableIntList descriptionTypePreferenceList,
                                       ImmutableIntList dialectAssemblagePreferenceList,
                                       ImmutableIntList modulePreferenceListForLanguage,
                                       LanguageCoordinateImmutable nextPriorityLanguageCoordinate) {
        this.languageConceptNid = languageConceptNid;
        this.descriptionTypePreferenceList = descriptionTypePreferenceList;
        this.dialectAssemblagePreferenceList = dialectAssemblagePreferenceList == null ? IntLists.immutable.empty() : dialectAssemblagePreferenceList;
        this.modulePreferenceListForLanguage = modulePreferenceListForLanguage == null ? IntLists.immutable.empty() : modulePreferenceListForLanguage;
        this.nextPriorityLanguageCoordinate = nextPriorityLanguageCoordinate;
    }
    
    private LanguageCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.languageConceptNid = Integer.MAX_VALUE;
        this.descriptionTypePreferenceList = null;
        this.dialectAssemblagePreferenceList = null;
        this.modulePreferenceListForLanguage = null;
        this.nextPriorityLanguageCoordinate = null;
    }

    private LanguageCoordinateImmutable setupCache() {
        this.preferredCaches =
                new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                        ConcurrentReferenceHashMap.ReferenceType.WEAK);

        this.fqnCaches =
                new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                        ConcurrentReferenceHashMap.ReferenceType.WEAK);

        this.descriptionCaches =
                new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                        ConcurrentReferenceHashMap.ReferenceType.WEAK);

        Get.commitService().addChangeListener(this);
        return this;
    }

    @Override
    public void handleChange(ConceptChronology cc) {
        // nothing to do
    }

    @Override
    public void handleChange(SemanticChronology sc) {
        this.fqnCaches.clear();
        this.preferredCaches.clear();
        this.descriptionCaches.clear();
    }

    @Override
    public void handleCommit(CommitRecord commitRecord) {
        this.fqnCaches.clear();
        this.preferredCaches.clear();
        this.descriptionCaches.clear();
    }

    @Override
    public UUID getListenerUuid() {
        return getLanguageCoordinateUuid();
    }

    @Override
    public void reset() {
        SINGLETONS.clear();
    }

    public LanguageCoordinateImmutable(ByteArrayDataBuffer in) {
        this(in.getNid(), IntLists.immutable.of(in.getNidArray()), IntLists.immutable.of(in.getNidArray()),
                IntLists.immutable.of(in.getNidArray()), MarshalUtil.unmarshal(in));
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putNid(this.languageConceptNid);
        out.putNidArray(this.descriptionTypePreferenceList.toArray());
        out.putNidArray(this.dialectAssemblagePreferenceList.toArray());
        out.putNidArray(this.modulePreferenceListForLanguage.toArray());
        MarshalUtil.marshal(this.nextPriorityLanguageCoordinate, out);
    }

    @Unmarshaler
    public static LanguageCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new LanguageCoordinateImmutable(in),
                        languageCoordinateImmutable -> languageCoordinateImmutable.setupCache());
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    public static LanguageCoordinateImmutable make(ConceptSpecification languageConcept,
                                                   ImmutableIntList descriptionTypePreferenceList,
                                                   ImmutableIntList dialectAssemblagePreferenceList,
                                                   ImmutableIntList modulePreferenceListForLanguage)  {
        return SINGLETONS.computeIfAbsent(new LanguageCoordinateImmutable(languageConcept.getNid(),
                        descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                        modulePreferenceListForLanguage),
                languageCoordinateImmutable -> languageCoordinateImmutable.setupCache());
    }

    /**
     * 
     * @param languageConceptNid
     * @param descriptionTypePreferenceList
     * @param dialectAssemblagePreferenceList
     * @param modulePreferenceListForLanguage - if null, treated as empty
     * @return
     */
    public static LanguageCoordinateImmutable make(int languageConceptNid,
                                                   ImmutableIntList descriptionTypePreferenceList,
                                                   ImmutableIntList dialectAssemblagePreferenceList,
                                                   ImmutableIntList modulePreferenceListForLanguage)  {
        return SINGLETONS.computeIfAbsent(new LanguageCoordinateImmutable(languageConceptNid,
                        descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                        modulePreferenceListForLanguage),
                languageCoordinateImmutable -> languageCoordinateImmutable.setupCache());
    }

    public static LanguageCoordinateImmutable make(int languageConceptNid,
                                                   ImmutableIntList descriptionTypePreferenceList,
                                                   ImmutableIntList dialectAssemblagePreferenceList,
                                                   ImmutableIntList modulePreferenceListForLanguage,
                                                   LanguageCoordinateImmutable nextPriorityLanguageCoordinate)  {
        return SINGLETONS.computeIfAbsent(new LanguageCoordinateImmutable(languageConceptNid,
                        descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                        modulePreferenceListForLanguage, nextPriorityLanguageCoordinate),
                languageCoordinateImmutable -> languageCoordinateImmutable.setupCache());
    }

    public static LanguageCoordinateImmutable make(int languageConceptNid,
                                                   ImmutableIntList descriptionTypePreferenceList,
                                                   ImmutableIntList dialectAssemblagePreferenceList,
                                                   ImmutableIntList modulePreferenceListForLanguage,
                                                   Optional<? extends LanguageCoordinate> optionalNextPriorityLanguageCoordinate)  {
        if (optionalNextPriorityLanguageCoordinate.isPresent()) {
            return SINGLETONS.computeIfAbsent(new LanguageCoordinateImmutable(languageConceptNid,
                            descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                            modulePreferenceListForLanguage, optionalNextPriorityLanguageCoordinate.get().toLanguageCoordinateImmutable()),
                    languageCoordinateImmutable -> languageCoordinateImmutable.setupCache());
        }
        return SINGLETONS.computeIfAbsent(new LanguageCoordinateImmutable(languageConceptNid,
                        descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                        modulePreferenceListForLanguage),
                languageCoordinateImmutable -> languageCoordinateImmutable.setupCache());
    }


    @Override
    public Optional<LanguageCoordinate> getNextPriorityLanguageCoordinate() {
        return Optional.ofNullable(this.nextPriorityLanguageCoordinate);
    }

    @Override
    public int[] getDescriptionTypePreferenceList() {
        return this.descriptionTypePreferenceList.toArray();
    }

    @Override
    public ConceptSpecification[] getDescriptionTypeSpecPreferenceList() {
        return this.descriptionTypePreferenceList.collect(nid ->
                Get.conceptSpecification(nid)).toArray(new ConceptSpecification[this.descriptionTypePreferenceList.size()]);
    }

    @Override
    public int[] getDialectAssemblagePreferenceList() {
        return this.dialectAssemblagePreferenceList.toArray();
    }

    @Override
    public ConceptSpecification[] getDialectAssemblageSpecPreferenceList() {
         return this.dialectAssemblagePreferenceList.collect(nid ->
                Get.conceptSpecification(nid)).toArray(new ConceptSpecification[this.dialectAssemblagePreferenceList.size()]);
    }

    @Override
    public int[] getModulePreferenceListForLanguage() {
        return this.modulePreferenceListForLanguage.toArray();
    }

    @Override
    public ConceptSpecification[] getModuleSpecPreferenceListForLanguage() {
        return this.modulePreferenceListForLanguage.collect(nid ->
                Get.conceptSpecification(nid)).toArray(new ConceptSpecification[this.modulePreferenceListForLanguage.size()]);
    }

    @Override
    public int getLanguageConceptNid() {
        return this.languageConceptNid;
    }

    @Override
    public ConceptSpecification getLanguageConcept() {
        return Get.conceptSpecification(this.languageConceptNid);
    }

    @Override
    public LanguageCoordinateImmutable toLanguageCoordinateImmutable() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LanguageCoordinateImmutable)) return false;
        LanguageCoordinateImmutable that = (LanguageCoordinateImmutable) o;
        return getLanguageConceptNid() == that.getLanguageConceptNid() &&
                getDescriptionTypePreferenceList().equals(that.getDescriptionTypePreferenceList()) &&
                getDialectAssemblagePreferenceList().equals(that.getDialectAssemblagePreferenceList()) &&
                getModulePreferenceListForLanguage().equals(that.getModulePreferenceListForLanguage()) &&
                getNextPriorityLanguageCoordinate().equals(that.getNextPriorityLanguageCoordinate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLanguageConceptNid(), getDescriptionTypePreferenceList(), getDialectAssemblagePreferenceList(), getModulePreferenceListForLanguage(), getNextPriorityLanguageCoordinate());
    }
    @Override
    public Optional<String> getDescriptionText(int componentNid, StampFilter stampFilter) {
        Cache<Integer, String> descriptionCache = this.descriptionCaches.computeIfAbsent(stampFilter.toStampFilterImmutable(),
                stampFilterImmutable -> Caffeine.newBuilder().maximumSize(100000).build());
        String descriptionText = descriptionCache.getIfPresent(componentNid);
        if (descriptionText == null) {
            descriptionText = getDescriptionTextForCache(componentNid, stampFilter);
            if (descriptionText != null) {
                descriptionCache.put(componentNid, descriptionText);
            }
        }
        return Optional.ofNullable(descriptionText);
    }
    private String getDescriptionTextForCache(int componentNid, StampFilter stampFilter) {
        switch (Get.identifierService().getObjectTypeForComponent(componentNid)) {
            case CONCEPT: {
                LatestVersion<DescriptionVersion> latestDescription
                        = getDescription(Get.conceptService().getConceptDescriptions(componentNid), stampFilter);
                return latestDescription.isPresent() ? latestDescription.get().getText() : null;
            }
            case SEMANTIC: {
                return getSemanticString(componentNid, stampFilter);
            }
            case UNKNOWN:
            default:
                return UNKNOWN_COMPONENT_TYPE;
        }
    }
    @Override
    public Optional<String> getRegularDescriptionText(int componentNid, StampFilter stampFilter) {
        Cache<Integer, String> preferredCache = this.preferredCaches.computeIfAbsent(stampFilter.toStampFilterImmutable(),
                stampFilterImmutable -> Caffeine.newBuilder().maximumSize(100000).build());

        String preferredDescriptionText = preferredCache.getIfPresent(componentNid);
        if (preferredDescriptionText == null) {
            preferredDescriptionText = getPreferredDescriptionTextForCache(componentNid, stampFilter);
            if (preferredDescriptionText != null) {
                preferredCache.put(componentNid, preferredDescriptionText);
            }
        }
        return Optional.ofNullable(preferredDescriptionText);
    }
    private String getPreferredDescriptionTextForCache(int componentNid, StampFilter stampFilter) {
        switch (Get.identifierService().getObjectTypeForComponent(componentNid)) {
            case CONCEPT: {
                LatestVersion<DescriptionVersion> latestDescription
                        = getRegularDescription(Get.conceptService().getConceptDescriptions(componentNid), stampFilter);
                return latestDescription.isPresent() ? latestDescription.get().getText() : null;
            }
            case SEMANTIC: {
                return getSemanticString(componentNid, stampFilter);
            }
            case UNKNOWN:
            default:
                return UNKNOWN_COMPONENT_TYPE;
        }
    }
    @Override
    public Optional<String> getFullyQualifiedNameText(int componentNid, StampFilter stampFilter) {
        Cache<Integer, String> fqnCache = this.fqnCaches.computeIfAbsent(stampFilter.toStampFilterImmutable(),
                stampFilterImmutable -> Caffeine.newBuilder().maximumSize(100000).build());

        String fullyQualifiedNameText = fqnCache.getIfPresent(componentNid);
        if (fullyQualifiedNameText == null) {
            fullyQualifiedNameText = getFullyQualifiedNameTextForCache(componentNid, stampFilter);
            if (fullyQualifiedNameText != null) {
                fqnCache.put(componentNid, fullyQualifiedNameText);
            }
        }
        return Optional.ofNullable(fullyQualifiedNameText);
    }

    private String getFullyQualifiedNameTextForCache(int componentNid, StampFilter stampFilter) {
        switch (Get.identifierService().getObjectTypeForComponent(componentNid)) {
            case CONCEPT: {
                LatestVersion<DescriptionVersion> latestDescription
                        = getFullyQualifiedDescription(Get.conceptService().getConceptDescriptions(componentNid), stampFilter);
                return latestDescription.isPresent() ? latestDescription.get().getText() : null;
            }
            case SEMANTIC: {
                return getSemanticString(componentNid, stampFilter);
            }
            case UNKNOWN:
            default:
                return UNKNOWN_COMPONENT_TYPE;
        }
    }

    private String getSemanticString(int componentNid, StampFilter stampFilter) {
        SemanticChronology sc = Get.assemblageService().getSemanticChronology(componentNid);
        if (sc.getVersionType() == VersionType.DESCRIPTION) {
            LatestVersion<DescriptionVersion> latestDescription = sc.getLatestVersion(stampFilter);
            if (latestDescription.isPresent()) {
                return latestDescription.get().getText();
            }
            return "INACTIVE: " + ((DescriptionVersion) sc.getVersionList().get(0)).getText();
        }
        return Get.assemblageService().getSemanticChronology(componentNid).getVersionType().toString();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "Language Coordinate{" + Get.conceptDescriptionText(this.languageConceptNid)
                + ", dialect preference: " + Get.conceptDescriptionTextList(this.dialectAssemblagePreferenceList.toArray())
                + ", type preference: " + Get.conceptDescriptionTextList(this.descriptionTypePreferenceList.toArray())
                + ", module preference: " + Get.conceptDescriptionTextList(this.modulePreferenceListForLanguage.toArray()) + '}';
    }
}
