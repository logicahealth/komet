package gov.vha.isaac.ochre.model.sememe.version.dynamicSememe;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArrayBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDoubleBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloatBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeIntegerBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLongBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNidBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeStringBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUIDBI;
import gov.vha.isaac.ochre.model.concept.ConceptChronicleImpl;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class DynamicSememeUtility
{
	/**
	 * These are all defined from the perspective of the userData - so for passesValidator to return true -
	 * userData must be LESS_THAN validatorDefinitionData, for example.
	 * 
	 * @param userData
	 * @param validatorDefinitionData
	 * @param viewCoordinate - The View Coordinate - not needed for some types of validations. Null allowed when unneeded (for math based tests, for example)
	 * @return
	 */
	public static boolean passesValidator(DynamicSememeValidatorType dsvt, DynamicSememeDataBI userData, DynamicSememeDataBI validatorDefinitionData, ViewCoordinate viewCoordinate)
	{
		if (validatorDefinitionData == null)
		{
			throw new RuntimeException("The validator definition data is required");
		}
		if (dsvt == DynamicSememeValidatorType.EXTERNAL)
		{
			ExternalValidatorBI validator = null;
			DynamicSememeStringBI[] valNameInfo = null;
			DynamicSememeArrayBI<DynamicSememeStringBI> stringValidatorDefData = null;
			String valName = null;
			if (validatorDefinitionData != null)
			{
				stringValidatorDefData = (DynamicSememeArrayBI<DynamicSememeStringBI>)validatorDefinitionData;
				valNameInfo = stringValidatorDefData.getDataArray();
			}
			if (valNameInfo != null && valNameInfo.length > 0)
			{
				valName = valNameInfo[0].getDataString();
				logger.fine("Looking for an ExternalValidatorBI with the name of '" + valName + "'");
				validator = LookupService.get().getService(ExternalValidatorBI.class, valName);
			}
			else
			{
				logger.severe("An external validator type was specified, but no ExternalValidatorBI 'name' was provided.  API misuse!");
			}
			if (validator == null)
			{
				throw new RuntimeException("Could not locate an implementation of ExternalValidatorBI with the requested name of '" + valName + "'");
			}
			return validator.validate(userData, stringValidatorDefData, viewCoordinate);
		}
		else if (dsvt == DynamicSememeValidatorType.REGEXP)
		{
			try
			{
				if (userData == null)
				{
					return false;
				}
				return Pattern.matches(((DynamicSememeStringBI)validatorDefinitionData).getDataString(), userData.getDataObject().toString());
			}
			catch (Exception e)
			{
				throw new RuntimeException("The specified validator data object was not a valid regular expression: " + e.getMessage());
			}
		}
		else if (dsvt == DynamicSememeValidatorType.IS_CHILD_OF || dsvt == DynamicSememeValidatorType.IS_KIND_OF)
		{
			try
			{
				int childNid;
				int parentNid;

				if (userData instanceof DynamicSememeUUIDBI)
				{
					childNid = Ts.get().getNidForUuids(((DynamicSememeUUIDBI) userData).getDataUUID());
				}
				else if (userData instanceof DynamicSememeNidBI)
				{
					childNid = ((DynamicSememeNidBI) userData).getDataNid();
				}
				else
				{
					throw new RuntimeException("Userdata is invalid for a IS_CHILD_OF or IS_KIND_OF comparison");
				}

				if (validatorDefinitionData instanceof DynamicSememeUUIDBI)
				{
					parentNid = Ts.get().getNidForUuids(((DynamicSememeUUIDBI) validatorDefinitionData).getDataUUID());
				}
				else if (validatorDefinitionData instanceof DynamicSememeNidBI)
				{
					parentNid = ((DynamicSememeNidBI) validatorDefinitionData).getDataNid();
				}
				else
				{
					throw new RuntimeException("Validator DefinitionData is invalid for a IS_CHILD_OF or IS_KIND_OF comparison");
				}

				return (dsvt == DynamicSememeValidatorType.IS_CHILD_OF ? Ts.get().isChildOf(childNid, parentNid, viewCoordinate) : Ts.get().isKindOf(childNid, parentNid, viewCoordinate));
			}
			catch (Exception e)
			{
				logger.log(Level.WARNING, "Failure executing validator", e);
				throw new RuntimeException("Failure executing validator", e);
			}
		}
		else if (dsvt == DynamicSememeValidatorType.COMPONENT_TYPE)
		{
			try
			{
				int nid;

				if (userData instanceof DynamicSememeUUIDBI)
				{
					DynamicSememeUUIDBI uuid = (DynamicSememeUUIDBI) userData;
					if (!Ts.get().hasUuid(uuid.getDataUUID()))
					{
						throw new RuntimeException("The specified UUID can not be found in the database, so the validator cannot execute");
					}
					else
					{
						nid = Ts.get().getNidForUuids(uuid.getDataUUID());
					}
				}
				else if (userData instanceof DynamicSememeNidBI)
				{
					nid = ((DynamicSememeNidBI) userData).getDataNid();
				}
				else
				{
					throw new RuntimeException("Userdata is invalid for a COMPONENT_TYPE comparison");
				}
				
				ComponentChronicleBI<?> cc = Ts.get().getComponent(nid);
				
				String valData = validatorDefinitionData.getDataObject().toString().trim();
				
				ComponentType ct = ComponentType.parse(valData);
				
				switch (ct)
				{
					//In the strange land of Workbench, concept attributes have the same NID as concepts....
					case CONCEPT: case CONCEPT_ATTRIBUTES:
					{
						if (!(cc instanceof ConceptChronicleBI) && !(cc instanceof ConceptAttributeChronicleBI))
						{
							throw new RuntimeException("The specified component must be of type " + ct.toString() + ", not " + cc.getClass().getSimpleName());
						}
						return true;
					}
					case DESCRIPTION:
					{
						if (!(cc instanceof DescriptionChronicleBI))
						{
							throw new RuntimeException("The specified component must be of type " + ct.toString() + ", not " + cc.getClass().getSimpleName());
						}
						return true;
					}
					case MEDIA:
					{
						if (!(cc instanceof MediaChronicleBI))
						{
							throw new RuntimeException("The specified component must be of type " + ct.toString() + ", not " + cc.getClass().getSimpleName());
						}
						return true;
					}
					case RELATIONSHIP:
					{
						if (!(cc instanceof RelationshipChronicleBI))
						{
							throw new RuntimeException("The specified component must be of type " + ct.toString() + ", not " + cc.getClass().getSimpleName());
						}
						return true;
					}
					case SEMEME:
					{
						if (!(cc instanceof RefexChronicleBI<?>))
						{
							throw new RuntimeException("The specified component must be of type " + ct.toString() + ", not " + cc.getClass().getSimpleName());
						}
						return true;
					}
					case SEMEME_DYNAMIC:
					{
						if (!(cc instanceof DynamicSememeChronicleBI<?>))
						{
							throw new RuntimeException("The specified component must be of type " + ct.toString() + ", not " + cc.getClass().getSimpleName());
						}
						return true;
					}
					case UNKNOWN:
					{
						throw new RuntimeException("Couldn't determine validator type from validator data '" + valData + "'");
					}

					default:
						throw new RuntimeException("Unexpected error");
				}
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				throw new RuntimeException("The entry doesn't appear to be a valid NID");
			}
			catch (RuntimeException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				logger.log(Level.WARNING, "Failure executing validator", e);
				throw new RuntimeException("Failure executing validator", e);
			}
		}
		else
		{
			Number userDataNumber = readNumber(userData);
			Number validatorDefinitionDataNumber;
			if (dsvt == DynamicSememeValidatorType.INTERVAL)
			{
				boolean leftInclusive;
				boolean rightInclusive;

				String s = validatorDefinitionData.getDataObject().toString().trim();

				if (s.charAt(0) == '[')
				{
					leftInclusive = true;
				}
				else if (s.charAt(0) == '(')
				{
					leftInclusive = false;
				}
				else
				{
					throw new RuntimeException("Invalid INTERVAL definition in the validator definition data - char 0 should be [ or (");
				}
				if (s.charAt(s.length() - 1) == ']')
				{
					rightInclusive = true;
				}
				else if (s.charAt(s.length() - 1) == ')')
				{
					rightInclusive = false;
				}
				else
				{
					throw new RuntimeException("Invalid INTERVAL definition in the validator definition data - last char should be ] or )");
				}

				String numeric = s.substring(1, s.length() - 1);
				numeric = numeric.replaceAll("\\s", "");

				int pos = numeric.indexOf(',');
				Number left = null;
				Number right = null;
				if (pos == 0)
				{
					//left is null (- infinity)
					right = parseUnknown(numeric.substring(1, numeric.length()));
				}
				else if (pos > 0)
				{
					left = parseUnknown(numeric.substring(0, pos));
					if (numeric.length() > (pos + 1))
					{
						right = parseUnknown(numeric.substring(pos + 1));
					}
				}
				else
				{
					throw new RuntimeException("Invalid INTERVAL definition in the validator definition data");
				}
				
				//make sure interval is properly specified
				if (left != null && right != null)
				{
					if (compare(left, right) > 0)
					{
						throw new RuntimeException("Invalid INTERVAL definition the left value should be <= the right value");
					}
				}

				if (left != null)
				{
					int compareLeft = compare(userDataNumber, left);
					if ((!leftInclusive && compareLeft == 0) || compareLeft < 0)
					{
						return false;
					}
				}
				if (right != null)
				{
					int compareRight = compare(userDataNumber, right);
					if ((!rightInclusive && compareRight == 0) || compareRight > 0)
					{
						return false;
					}
				}
				return true;
			}
			else
			{
				validatorDefinitionDataNumber = readNumber(validatorDefinitionData);
				int compareResult = compare(userDataNumber, validatorDefinitionDataNumber);

				switch (dsvt)
				{
					case LESS_THAN:
						return compareResult < 0;
					case GREATER_THAN:
						return compareResult > 0;
					case GREATER_THAN_OR_EQUAL:
						return compareResult >= 0;
					case LESS_THAN_OR_EQUAL:
						return compareResult <= 0;
					default:
						throw new RuntimeException("oops");
				}
			}
		}
	}
	
	/**
	 * A convenience wrapper of {@link #passesValidator(DynamicSememeDataBI, DynamicSememeDataBI, ViewCoordinate)} that just returns a string - never
	 * throws an error
	 * 
	 * These are all defined from the perspective of the userData - so for passesValidator to return true -
	 * userData must be LESS_THAN validatorDefinitionData, for example.
	 * 
	 * @param userData
	 * @param validatorDefinitionData
	 * @param vc - The View Coordinate - not needed for some types of validations. Null allowed when unneeded (for math based tests, for example)
	 * @return - empty string if valid, an error message otherwise.
	 */
	public static String passesValidatorStringReturn(DynamicSememeValidatorType dsvt, DynamicSememeDataBI userData, DynamicSememeDataBI validatorDefinitionData, ViewCoordinate vc)
	{
		try
		{
			if (passesValidator(dsvt, userData, validatorDefinitionData, vc))
			{
				return "";
			}
			else
			{
				return "The value does not pass the validator";
			}
		}
		catch (Exception e)
		{
			return e.getMessage();
		}
	}

	private static Number parseUnknown(String value)
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch (Exception e)
		{
			//noop
		}
		try
		{
			return Long.parseLong(value);
		}
		catch (Exception e)
		{
			//noop
		}
		try
		{
			return Float.parseFloat(value);
		}
		catch (Exception e)
		{
			//noop
		}
		try
		{
			return Double.parseDouble(value);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected data passed in to parseUnknown (" + value + ")");
		}
	}

	public static Number readNumber(DynamicSememeDataBI value)
	{
		if (value instanceof DynamicSememeDoubleBI)
		{
			return Double.valueOf(((DynamicSememeDoubleBI) value).getDataDouble());
		}
		else if (value instanceof DynamicSememeFloatBI)
		{
			return Float.valueOf(((DynamicSememeFloatBI) value).getDataFloat());
		}
		else if (value instanceof DynamicSememeIntegerBI)
		{
			return Integer.valueOf(((DynamicSememeIntegerBI) value).getDataInteger());
		}
		else if (value instanceof DynamicSememeLongBI)
		{
			return Long.valueOf(((DynamicSememeLongBI) value).getDataLong());
		}
		else
		{
			throw new RuntimeException("The value passed in to the validator is not a number");
		}
	}

	public static int compare(final Number x, final Number y)
	{
		if (isSpecial(x) || isSpecial(y))
		{
			return Double.compare(x.doubleValue(), y.doubleValue());
		}
		else
		{
			return toBigDecimal(x).compareTo(toBigDecimal(y));
		}
	}

	private static boolean isSpecial(final Number x)
	{
		boolean specialDouble = x instanceof Double && (Double.isNaN((Double) x) || Double.isInfinite((Double) x));
		boolean specialFloat = x instanceof Float && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));
		return specialDouble || specialFloat;
	}

	public static BigDecimal toBigDecimal(final Number number)
	{
		if (number instanceof Integer || number instanceof Long)
		{
			return new BigDecimal(number.longValue());
		}
		else if (number instanceof Float || number instanceof Double)
		{
			return new BigDecimal(number.doubleValue());
		}
		else
		{
			throw new RuntimeException("Unexpected data type passed in to toBigDecimal (" + number.getClass() + ")");
		}
	}
	
	/**
	 * Create a new concept using the provided columnName and columnDescription values which is suitable 
	 * for use as a column descriptor within {@link DynamicSememeUsageDescription}.
	 * 
	 * The new concept will be created under the concept {@link DynamicSememe#DYNAMIC_SEMEME_COLUMNS}
	 * 
	 * A complete usage pattern (where both the refex assemblage concept and the column name concept needs
	 * to be created) would look roughly like this:
	 * 
	 * DynamicSememeUsageDescriptionBuilder.createNewDynamicSememeUsageDescriptionConcept(
	 *     "The name of the Refex", 
	 *     "The description of the Refex",
	 *     new DynamicSememeColumnInfo[]{new DynamicSememeColumnInfo(
	 *         0,
	 *         DynamicSememeColumnInfo.createNewDynamicSememeColumnInfoConcept(
	 *             "column name",
	 *             "column description"
	 *             )
	 *         DynamicSememeDataType.STRING,
	 *         new RefexString("default value")
	 *         )}
	 *     )
	 * 
	 * //TODO (artf231856) [REFEX] figure out language details (how we know what language to put on the name/description
	 * * @param vc view coordinate -  highly recommended that you use ViewCoordinates.getMetadataViewCoordinate()
	 * @throws ContradictionException 
	 * @throws InvalidCAB 
	 * @throws IOException 
	 */
	public static ConceptChronicleImpl createNewDynamicSememeColumnInfoConcept(String columnName, String columnDescription, ViewCoordinate vc) 
			throws IOException
	{
		if (columnName == null || columnName.length() == 0 || columnDescription == null || columnDescription.length() == 0)
		{
			throw new IOException("Both the column name and column description are required");
		}

		LanguageCode lc = LanguageCode.EN_US;
		UUID isA = Snomed.IS_A.getUuids()[0];
		IdDirective idDir = IdDirective.GENERATE_HASH;
		UUID module = Snomed.CORE_MODULE.getUuids()[0];
		UUID parents[] = new UUID[] { DynamicSememe.DYNAMIC_SEMEME_COLUMNS.getUuids()[0] };

		ConceptCB cab = new ConceptCB(columnName, columnName, lc, isA, idDir, module, null, parents);
		
		DescriptionCAB dCab = new DescriptionCAB(cab.getComponentUuid(),  Snomed.DEFINITION_DESCRIPTION_TYPE.getUuids()[0], LanguageCode.EN, 
				columnDescription, false, IdDirective.GENERATE_HASH);
		dCab.getProperties().put(ComponentProperty.MODULE_ID, module);
		
		RefexCAB rCab = new RefexCAB(RefexType.CID, dCab.getComponentUuid(), 
				Snomed.US_LANGUAGE_REFEX.getUuids()[0], IdDirective.GENERATE_HASH, RefexDirective.EXCLUDE);
		rCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0]);
		rCab.getProperties().put(ComponentProperty.MODULE_ID, module);
		
		dCab.addAnnotationBlueprint(rCab);
		
		cab.addDescriptionCAB(dCab);
		
		ConceptChronicleBI newCon = Ts.get().getTerminologyBuilder(
				new EditCoordinate(TermAux.USER.getLenient().getConceptNid(), 
					TermAux.ISAAC_MODULE.getLenient().getNid(), 
					TermAux.WB_AUX_PATH.getLenient().getConceptNid()), 
					vc).construct(cab);
		Ts.get().addUncommitted(newCon);
		Ts.get().commit(/* newCon */);
		
		return newCon;
	}
}
