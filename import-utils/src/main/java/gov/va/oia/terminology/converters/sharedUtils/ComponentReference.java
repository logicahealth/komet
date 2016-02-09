package gov.va.oia.terminology.converters.sharedUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;

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
	
//TODO do I need this?  It is unsafe, without knowing the type.
//	public int getSequence()
//	{
//		return sequenceProvider_.getAsInt();
//	}
	
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
	
	public static ComponentReference fromSememe(UUID uuid)
	{
		return new ComponentReference(() -> uuid, () -> Get.identifierService().getSememeSequenceForUuids(uuid), () -> "Sememe");
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
	
	public static ComponentReference fromChronology(ObjectChronology<?> object)
	{
		return fromChronology(object, null);
	}
	
	@SuppressWarnings("rawtypes")
	public static ComponentReference fromChronology(ObjectChronology<?> object, Supplier<String> typeLabelSupplier)
	{
		ComponentReference cr;
		if (object instanceof SememeChronology)
		{
			cr = new ComponentReference(() -> object.getPrimordialUuid(), () -> Get.identifierService().getSememeSequence(object.getNid()));
			cr.typeLabelSupplier_ = () ->
			{
				if (((SememeChronology)object).getSememeType() == SememeType.DESCRIPTION)
				{
					return "Description";
				}
				else if (((SememeChronology)object).getSememeType() == SememeType.LOGIC_GRAPH)
				{
					return "Graph";
				}
				
				return "";
			};
		}
		else if (object instanceof ConceptChronology)
		{
			cr = new ComponentReference(() -> object.getPrimordialUuid(), () -> Get.identifierService().getConceptSequence(object.getNid()), () -> "Concept");
		}
		else
		{
			cr = new ComponentReference(() -> object.getPrimordialUuid(), () -> {throw new RuntimeException("unsupported");});
		}
		if (typeLabelSupplier != null)
		{
			cr.typeLabelSupplier_ = typeLabelSupplier;
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
