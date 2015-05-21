package gov.va.oia.terminology.converters.sharedUtils.umlsUtils;

import gov.va.oia.terminology.converters.sharedUtils.EConceptUtility;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.Property;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;

public class ValuePropertyPairWithAttributes extends ValuePropertyPair
{
	protected HashMap<UUID, ArrayList<String>> stringAttributes = new HashMap<>();
	protected HashMap<UUID, ArrayList<UUID>> uuidAttributes = new HashMap<>();
	protected ArrayList<TtkConceptChronicle> refsetMembership = new ArrayList<>();
	
	public ValuePropertyPairWithAttributes(String value, Property property)
	{
		super(value, property);
	}
	
	public void addStringAttribute(UUID type, String value)
	{
		ArrayList<String> values = stringAttributes.get(type);
		if (values == null)
		{
			values = new ArrayList<>();
			stringAttributes.put(type, values);
		}
		values.add(value);
	}
	
	public ArrayList<String> getStringAttribute(UUID type)
	{
		return stringAttributes.get(type);
	}
	
	public void addUUIDAttribute(UUID type, UUID value)
	{
		ArrayList<UUID> values = uuidAttributes.get(type);
		if (values == null)
		{
			values = new ArrayList<>();
			uuidAttributes.put(type, values);
		}
		values.add(value);
	}
	
	public void addRefsetMembership(TtkConceptChronicle refsetConcept)
	{
		refsetMembership.add(refsetConcept);
	}
	
	public static void processAttributes(EConceptUtility eConceptUtility, List<? extends ValuePropertyPairWithAttributes> descriptionSource, List<TtkDescriptionChronicle> descriptions)
	{
		for (int i = 0; i < descriptionSource.size(); i++)
		{
			for (Entry<UUID, ArrayList<String>> attributes : descriptionSource.get(i).stringAttributes.entrySet())
			{
				for (String value : attributes.getValue())
				{
					eConceptUtility.addStringAnnotation(descriptions.get(i), value, attributes.getKey(), Status.ACTIVE);
				}
			}
			
			for (Entry<UUID, ArrayList<UUID>> attributes : descriptionSource.get(i).uuidAttributes.entrySet())
			{
				for (UUID value : attributes.getValue())
				{
					eConceptUtility.addUuidAnnotation(descriptions.get(i), value, attributes.getKey());
				}
			}
			
			for (TtkConceptChronicle refsetConcept : descriptionSource.get(i).refsetMembership)
			{
				eConceptUtility.addDynamicRefsetMember(refsetConcept, descriptions.get(i).getPrimordialComponentUuid(), null, Status.ACTIVE, null);
			}
		}
	}
}
