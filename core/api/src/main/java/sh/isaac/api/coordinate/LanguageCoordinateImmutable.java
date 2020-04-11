package sh.isaac.api.coordinate;

import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCoordinateService;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RunLevel(value = LookupService.SL_L2)

// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public final class LanguageCoordinateImmutable implements LanguageCoordinate, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<LanguageCoordinateImmutable, LanguageCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 1;

    final private int languageConceptNid;
    final private ImmutableIntList descriptionTypePreferenceList;
    final private ImmutableIntList dialectAssemblagePreferenceList;
    final private ImmutableIntList modulePreferenceListForLanguage;
    final private LanguageCoordinateImmutable nextPriorityLanguageCoordinate;


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
        this.dialectAssemblagePreferenceList = dialectAssemblagePreferenceList;
        this.modulePreferenceListForLanguage = modulePreferenceListForLanguage;
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

    /**
     * {@inheritDoc}
     */
    @PreDestroy
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
                        languageCoordinateImmutable -> languageCoordinateImmutable);
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
                languageCoordinateImmutable -> languageCoordinateImmutable);
    }

    public static LanguageCoordinateImmutable make(int languageConceptNid,
                                                   ImmutableIntList descriptionTypePreferenceList,
                                                   ImmutableIntList dialectAssemblagePreferenceList,
                                                   ImmutableIntList modulePreferenceListForLanguage)  {
        return SINGLETONS.computeIfAbsent(new LanguageCoordinateImmutable(languageConceptNid,
                        descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                        modulePreferenceListForLanguage),
                languageCoordinateImmutable -> languageCoordinateImmutable);
    }

    public static LanguageCoordinateImmutable make(int languageConceptNid,
                                                   ImmutableIntList descriptionTypePreferenceList,
                                                   ImmutableIntList dialectAssemblagePreferenceList,
                                                   ImmutableIntList modulePreferenceListForLanguage,
                                                   LanguageCoordinateImmutable nextPriorityLanguageCoordinate)  {
        return SINGLETONS.computeIfAbsent(new LanguageCoordinateImmutable(languageConceptNid,
                        descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                        modulePreferenceListForLanguage, nextPriorityLanguageCoordinate),
                languageCoordinateImmutable -> languageCoordinateImmutable);
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
                    languageCoordinateImmutable -> languageCoordinateImmutable);
        }
        return SINGLETONS.computeIfAbsent(new LanguageCoordinateImmutable(languageConceptNid,
                        descriptionTypePreferenceList, dialectAssemblagePreferenceList,
                        modulePreferenceListForLanguage),
                languageCoordinateImmutable -> languageCoordinateImmutable);
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
    public LatestVersion<DescriptionVersion> getDefinitionDescription(List<SemanticChronology> descriptionList, StampFilter stampFilter) {
        return Get.languageCoordinateService()
                .getSpecifiedDescription(stampFilter, descriptionList, new int[]{TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()}, this);
    }

    @Override
    public LatestVersion<DescriptionVersion> getFullyQualifiedDescription(List<SemanticChronology> descriptionList, StampFilter stampFilter) {
        return Get.languageCoordinateService()
                .getSpecifiedDescription(stampFilter, descriptionList, new int[]{TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()}, this);
    }

    @Override
    public LatestVersion<DescriptionVersion> getPreferredDescription(List<SemanticChronology> descriptionList, StampFilter stampFilter) {
        return Get.languageCoordinateService()
                .getSpecifiedDescription(stampFilter, descriptionList, new int[]{TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()}, this);
    }

    @Override
    public LanguageCoordinateImmutable toLanguageCoordinateImmutable() {
        return this;
    }


    @Override
    public LatestVersion<DescriptionVersion> getDescription(List<SemanticChronology> descriptionList, StampFilter stampFilter) {
        return Get.languageCoordinateService()
                .getSpecifiedDescription(stampFilter, descriptionList, this);
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
