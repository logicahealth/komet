package gov.va.oia.terminology.converters.sharedUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;

public class ComponentReference
{
	private IntSupplier sequenceProvider_;
	private Supplier<UUID> uuidProvider_;
	private Supplier<Long> timeProvider_ = () -> null;
	private IntSupplier nidProvider_ = () -> Get.identifierService().getNidForUuids(uuidProvider_.get());
	private Supplier<String> typeLabelSupplier_ = () -> "";
	
	private ComponentReference(Supplier<UUID> uuidProvider, IntSupplier sequenceProvider)
	{
		uuidProvider_ = uuidProvider;
		sequenceProvider_ = sequenceProvider;
	}
	
	private ComponentReference(Supplier<UUID> uuidProvider, IntSupplier sequenceProvider, Supplier<String> typeLabelSupplier)
	{
		this(uuidProvider, sequenceProvider);
		typeLabelSupplier_ = typeLabelSupplier;
	}
	
	public int getSequence()
	{
		return sequenceProvider_.getAsInt();
	}
	
	public UUID getPrimordialUuid()
	{
		return uuidProvider_.get();
	}
	
	public Long getTime()
	{
		return timeProvider_.get();
	}
	public int getNid()
	{
		return nidProvider_.getAsInt();
	}
	
	public String getTypeString()
	{
		return typeLabelSupplier_.get();
	}
	
	public static ComponentReference fromConcept(UUID uuid)
	{
		return new ComponentReference(() -> uuid, () -> Get.identifierService().getConceptSequenceForUuids(uuid), () -> "Concept");
	}
	
	public static ComponentReference fromConcept(UUID uuid, int seq)
	{
		return new ComponentReference(() -> uuid, () -> seq, () -> "Concept");
	}
	
	public static ComponentReference fromConcept(ConceptChronology<? extends ConceptVersion<?>> concept)
	{
		ComponentReference cr = new ComponentReference(() -> concept.getPrimordialUuid(), () -> concept.getConceptSequence(), () -> "Concept"); 
		cr.nidProvider_ = () -> concept.getNid();
		cr.timeProvider_ = () -> 
		{
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Optional<LatestVersion<StampedVersion>> latest = ((ObjectChronology)concept).getLatestVersion(StampedVersion.class, EConceptUtility.readBackStamp_);
			return latest.get().value().getTime();
		};
		return cr;
	}
	
	@SuppressWarnings("rawtypes")
	public static ComponentReference fromChronology(ObjectChronology<?> object)
	{
		ComponentReference cr;
		if (object instanceof SememeChronology)
		{
			cr = new ComponentReference(() -> object.getPrimordialUuid(), () -> Get.identifierService().getSememeSequence(object.getNid()));
		}
		else if (object instanceof ConceptChronology)
		{
			cr = new ComponentReference(() -> object.getPrimordialUuid(), () -> Get.identifierService().getConceptSequence(object.getNid()), () -> "Concept");
		}
		else
		{
			cr = new ComponentReference(() -> object.getPrimordialUuid(), () -> {throw new RuntimeException("unsupported");});
		}
		cr.nidProvider_ = () -> object.getNid();
		cr.timeProvider_ = () -> 
		{
			@SuppressWarnings({ "unchecked" })
			Optional<LatestVersion<StampedVersion>> latest = ((ObjectChronology)object).getLatestVersion(StampedVersion.class, EConceptUtility.readBackStamp_);
			return latest.get().value().getTime();
		};
		return cr;
	}
}
