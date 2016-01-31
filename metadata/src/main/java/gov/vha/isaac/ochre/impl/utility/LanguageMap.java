package gov.vha.isaac.ochre.impl.utility;

import java.util.Optional;
import java.util.UUID;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.LanguageCode;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;

/**
 * It would be nice if these were part of the LanguageCode class itself... but there are dependency problems preventing that.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LanguageMap
{
	public static ConceptSpecification getConceptForLanguageCode(LanguageCode lc)
	{
		switch (lc)
		{
			case EN:
			case EN_AU:
			case EN_BZ:
			case EN_CA:
			case EN_GB:
			case EN_IE:
			case EN_JM:
			case EN_NZ:
			case EN_TT:
			case EN_US:
			case EN_ZA:
				return MetaData.ENGLISH_LANGUAGE;
			case ES:
			case ES_AR:
			case ES_BO:
			case ES_CL:
			case ES_CO:
			case ES_CR:
			case ES_DO:
			case ES_EC:
			case ES_ES:
			case ES_GT:
			case ES_HN:
			case ES_NI:
			case ES_MX:
			case ES_PA:
			case ES_PE:
			case ES_PY:
			case ES_SV:
			case ES_UY:
			case ES_VE:
				return MetaData.SPANISH_LANGUAGE;
			case DA:
			case DA_DK:
				return MetaData.DANISH_LANGUAGE;
			case FR:
			case FR_BE:
			case FR_CA:
			case FR_CH:
			case FR_FR:
			case FR_LU:
			case FR_MC:
				return MetaData.FRENCH_LANGUAGE;
			case LT:
			case LT_LT:
				return MetaData.LITHUANIAN_LANGUAGE;
			case NL:
				return MetaData.DUTCH_LANGUAGE;
			case PL:
				return MetaData.POLISH_LANGUAGE;
			case SV:
			case SV_FI:
			case SV_SE:
				return MetaData.SWEDISH_LANGUAGE;
			case ZH:
			case ZH_CHS:
			case ZH_CHT:
			case ZH_CN:
			case ZH_HK:
			case ZH_MO:
			case ZH_SG:
			case ZH_TW:
				return MetaData.CHINESE_LANGUAGE;
			case ZZ:
			default :
				throw new RuntimeException("Unmapped Language Code " + lc);
		}
	}
	
	public static Optional<LanguageCode> getLanguageCodeForUUID(UUID uuid)
	{
		for (LanguageCode lc : LanguageCode.values())
		{
			if (lc == LanguageCode.ZZ)
			{
				continue;
			}
			for (UUID itemUuid : getConceptForLanguageCode(lc).getUuids())
			{
				if (itemUuid.equals(uuid))
				{
					return Optional.of(lc);
				}
			}
		}
		return Optional.empty();
	}
}
