package gov.va.oia.terminology.converters.sharedUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class SCTInfoProvider
{
	public static HashMap<Long, UUID> readSCTIDtoUUIDMapInfo(File snomedLocation) throws ClassNotFoundException, IOException
	{
		throw new RuntimeException("Not yet implemented");
//		File snomedJbinFile = null;
//		if (snomedLocation.isDirectory())
//		{
//			for (File f : snomedLocation.listFiles())
//			{
//				if (f.isFile() && f.getName().toLowerCase().endsWith(".jbin"))
//				{
//					snomedJbinFile = f;
//					break;
//				}
//			}
//		}
//		else
//		{
//			snomedJbinFile = snomedLocation;
//		}
//		if (snomedJbinFile == null)
//		{
//			throw new IOException("Couldn't find sct data file in " + snomedLocation);
//		}
//		
//		UUID sctIDType = IsaacMetadataAuxiliaryBinding.SNOMED_INTEGER_ID.getPrimodialUuid();
//		// Read in the SCT data
//		HashMap<Long, UUID> sctConcepts = new HashMap<>();
//		ConsoleUtil.println("Reading " + snomedJbinFile.getName());
//		DataInputStream in = new DataInputStream(new FileInputStream(snomedJbinFile));
//
//		while (in.available() > 0)
//		{
//			if (sctConcepts.size() % 1000 == 0)
//			{
//				ConsoleUtil.showProgress();
//			}
//			TtkConceptChronicle concept = new TtkConceptChronicle(in);
//
//			if (concept.getConceptAttributes() != null && concept.getConceptAttributes().getAdditionalIdComponents() != null)
//			{
//				for (TtkIdentifier id : concept.getConceptAttributes().getAdditionalIdComponents())
//				{
//					if (sctIDType.equals(id.getAuthorityUuid()))
//					{
//						//Store these by SCTID, because there is no reliable way to generate a UUID from a SCTID.
//						sctConcepts.put(Long.parseLong(id.getDenotation().toString()), concept.getPrimordialUuid());
//						break;
//					}
//				}
//			}
//
//		}
//		in.close();
//		ConsoleUtil.println("Read UUIDs from SCT file - read " + sctConcepts.size() + " concepts");
//		return sctConcepts;
	}
}
