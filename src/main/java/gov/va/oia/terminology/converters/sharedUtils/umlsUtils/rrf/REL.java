package gov.va.oia.terminology.converters.sharedUtils.umlsUtils.rrf;

import gov.va.oia.terminology.converters.sharedUtils.umlsUtils.Relationship;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class REL
{
	private String cui1, aui1, stype1, rel, cui2, aui2, stype2, rela, rui, srui, sab, sl, rg, dir, suppress, cvf, targetSAB, targetCODE, sourceSAB;
	
	private UUID sourceUUID_, targetUUID_, relHash_;
	
	private boolean lookedUp2_;
	
	public static List<REL> read(String sourceSab, ResultSet rs, boolean lookedUp2, Set<String> allowedCUIs, AtomicInteger cuiSkipCounter, 
			boolean isRxNorm, Function<String, String> relReverser) throws SQLException
	{
		ArrayList<REL> result = new ArrayList<>();
		while (rs.next())
		{
			REL rel = new REL(sourceSab, rs, lookedUp2, isRxNorm, relReverser);
			if (allowedCUIs != null && (!allowedCUIs.contains(rel.cui1) || !allowedCUIs.contains(rel.cui2)))
			{
				cuiSkipCounter.getAndIncrement();
				continue;
			}
			result.add(rel);
		}
		rs.close();
		return result;
	}
	
	private REL(String sourceSab, ResultSet rs, boolean lookedUp2, boolean isRxNorm, Function<String, String> relReverser) throws SQLException
	{
		sourceSAB = sourceSab;
		lookedUp2_ = lookedUp2;
		cui1 = rs.getString(isRxNorm ? "RXCUI1" : "CUI1");
		aui1 = rs.getString(isRxNorm ? "RXAUI1" : "AUI1");
		stype1 = rs.getString("STYPE1");
		rel = rs.getString("REL");
		cui2 = rs.getString(isRxNorm ? "RXCUI2" : "CUI2");
		aui2 = rs.getString(isRxNorm ? "RXAUI2" : "AUI2");
		stype2 = rs.getString("STYPE2");
		rela = rs.getString("RELA");
		rui = rs.getString("RUI");
		srui = rs.getString("SRUI");
		sab = rs.getString("SAB");
		sl = rs.getString("SL");
		rg = rs.getString("RG");
		dir = rs.getString("DIR");
		suppress = rs.getString("SUPPRESS");
		cvf = rs.getObject("CVF") == null ? null : rs.getString("CVF");  //integer or string
		
		if ((lookedUp2 ? aui2 : aui1) != null)
		{
			//when the AUI is not null, we have a couple extra vars to read
			targetSAB = rs.getString("targetSAB");
			targetCODE = rs.getString("targetCODE");
		}
		
		if (!lookedUp2_)
		{
			rel = relReverser.apply(rel);
			rela = relReverser.apply(rela);
		}
	}

	public String getSourceCUI()
	{
		return lookedUp2_ ? cui2 : cui1;
	}
	
	public String getSourceAUI()
	{
		return lookedUp2_ ? aui2 : aui1;
	}
	
	public String getTargetCUI()
	{
		return lookedUp2_ ? cui1 : cui2;
	}
	
	public String getTargetAUI()
	{
		return lookedUp2_ ? aui1 : aui2;
	}

	public String getStype1()
	{
		return stype1;
	}

	public String getRel()
	{
		return rel;
	}

	public String getStype2()
	{
		return stype2;
	}

	public String getRela()
	{
		return rela;
	}

	public String getRui()
	{
		return rui;
	}

	public String getSrui()
	{
		return srui;
	}

	public String getSab()
	{
		return sab;
	}

	public String getSl()
	{
		return sl;
	}

	public String getRg()
	{
		return rg;
	}

	public String getDir()
	{
		return dir;
	}

	public String getSuppress()
	{
		return suppress;
	}
	
	public String getTargetSAB()
	{
		return targetSAB;
	}
	
	public String getTargetCode()
	{
		return targetCODE;
	}

	public String getCvf()
	{
		return cvf;
	}
	
	public void setSourceUUID(UUID sourceUUID)
	{
		sourceUUID_ = sourceUUID;
		relHash_ = null;
	}
	
	public void setTargetUUID(UUID targetUUID)
	{
		targetUUID_ = targetUUID;
		relHash_ = null;
	}
	
	public UUID getSourceUUID()
	{
		return sourceUUID_;
	}
	
	public UUID getTargetUUID()
	{
		return targetUUID_;
	}
	
	public String getSourceSAB()
	{
		return sourceSAB;
	}
	
	public UUID getRelHash()
	{
		if (relHash_ == null)
		{
			relHash_ = UUID.nameUUIDFromBytes(new String(rel + rela + sourceUUID_+ targetUUID_).getBytes()); 
		}
		return relHash_;
	}
	
	public UUID getInverseRelHash(Function<String, Relationship> nameToRelMapper)
	{
		//reverse the direction of the rels, and the source/target
		String relInverse = nameToRelMapper.apply(rel).getFSNName();
		String relaInverse = null;
		if (rela != null)
		{
			relaInverse = nameToRelMapper.apply(rela).getFSNName();
		}
		
		return UUID.nameUUIDFromBytes(new String(relInverse + relaInverse + targetUUID_ + sourceUUID_).getBytes()); 
	}
}
