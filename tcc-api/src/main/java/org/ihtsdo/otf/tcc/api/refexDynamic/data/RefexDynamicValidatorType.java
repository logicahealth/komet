/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.refexDynamic.data;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.media.MediaChronicleBI;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicArrayBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicDoubleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicFloatBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicLongBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;


/**
 * {@link RefexDynamicValidatorType}
 * 
 * The acceptable validatorDefinitionData object type(s) for the following fields:
 * {@link RefexDynamicValidatorType#LESS_THAN}
 * {@link RefexDynamicValidatorType#GREATER_THAN}
 * {@link RefexDynamicValidatorType#LESS_THAN_OR_EQUAL}
 * {@link RefexDynamicValidatorType#GREATER_THAN_OR_EQUAL}
 * 
 * are one of ( {@link RefexDynamicIntegerBI}, {@link RefexDynamicLongBI}, {@link RefexDynamicFloatBI}, {@link RefexDynamicDoubleBI})
 * 
 * {@link RefexDynamicValidatorType#INTERVAL} - Should be a {@link RefexDynamicStringBI} with valid interval notation - such as "[4,6)"
 * 
 * {@link RefexDynamicValidatorType#REGEXP} - Should be a {@link RefexDynamicStringBI} with valid regular expression, per 
 * http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
 * 
 * And for the following two:
 * {@link RefexDynamicValidatorType#IS_CHILD_OF}
 * {@link RefexDynamicValidatorType#IS_KIND_OF}
 * The validatorDefinitionData should be either an {@link RefexDynamicNidBI} or a {@link RefexDynamicUUIDBI}.
 * 
 * For {@link RefexDynamicValidatorType#EXTERNAL} the validatorDefinitionData should be a {@link RefexDynamicArrayBI<RefexDynamicStringBI>} 
 * which contains (in the first position of the array) the name of an HK2 named service which implements {@link ExternalValidatorBI} 
 * the name that you provide should be the value of the '@Name' annotation within the class which implements the ExternalValidatorBI class.  
 * This code will request that implementation (by name) and pass the validation call to it.
 * 
 * Optionally, the validatorDefinitionData more that one {@link RefexDynamicStringBI} in the array - only the first position of the array 
 * will be considered as the '@Name' to be used for the HK2 lookup.  All following data is ignored, and may be used by the external validator 
 * implementation to store other data.  For example, if the validatorDefinitionData {@link RefexDynamicArrayBI<RefexDynamicStringBI>}
 * contains an array of strings such as new String[]{"mySuperRefexValidator", "somespecialmappingdata", "some other mapping data"} 
 * then the following HK2 call will be made to locate the validator implementation (and validate):
 * <pre>
 *   ExternalValidatorBI validator = Hk2Looker.get().getService(ExternalValidatorBI.class, "mySuperRefexValidator");
 *   return validator.validate(userData, validatorDefinitionData, viewCoordinate);
 * </pre>
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("deprecation")
public enum RefexDynamicValidatorType
{
	LESS_THAN("<"), GREATER_THAN(">"), LESS_THAN_OR_EQUAL("<="), GREATER_THAN_OR_EQUAL(">="),  //Standard math stuff 
	INTERVAL("Interval"), //math interval notation - such as [5,10)
	REGEXP("Regular Expression"),  //http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
	EXTERNAL("External"), //see class docs above - implemented by an ExternalValidatorBI
	IS_CHILD_OF("Is Child Of"), //OTF is child of - which only includes immediate (not recursive) children on the 'Is A' relationship. 
	IS_KIND_OF("Is Kind Of"), //OTF kind of - which is child of - but recursive, and self (heart disease is a kind-of heart disease);
	COMPONENT_TYPE("Component Type Restriction"),  //specify which type of nid can be put into a UUID or nid column
	UNKNOWN("Unknown");  //Not a real validator, only exists to allow GUI convenience, or potentially store other validator data that we don't support in OTF
	//but we may need to store / retreive
	
	private String displayName_;
	private static Logger logger = Logger.getLogger(RefexDynamicValidatorType.class.getName());
	
	private RefexDynamicValidatorType(String displayName)
	{
		displayName_ = displayName;
	}
	
	public String getDisplayName()
	{
		return displayName_;
	}
	
	public boolean validatorSupportsType(RefexDynamicDataType type)
	{
		//These are supported by all types - external specifies itself, what it supports, and we always include UNKNOWN.
		if (this == UNKNOWN || this == EXTERNAL)
		{
			return true;
		}
		
		switch (type)
		{
			case BOOLEAN: case POLYMORPHIC: 
			{
				//technically, regexp would work here... but makes no sense.
				return false;
			}	
			case DOUBLE: case FLOAT: case INTEGER: case LONG:
			{
				if (this == GREATER_THAN || this == GREATER_THAN_OR_EQUAL || 
						this == LESS_THAN || this == LESS_THAN_OR_EQUAL || 
						this == INTERVAL || this == REGEXP)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			case NID: case UUID:
			{
				if (this == IS_CHILD_OF || this == IS_KIND_OF || this == REGEXP || this == COMPONENT_TYPE)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			case STRING: case BYTEARRAY:
			{
				if (this == REGEXP)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			default:
			{
				logger.warning("Unexpected case!");
				return false;
			}
		}
	}
	
	//TODO Dan notes - once again, implementation has no business being in the API... but because the entire Blueprint stack was implemented in the wrong place...
	//I have to implement this here so it can be used in blueprint.
	
	/**
	 * These are all defined from the perspective of the userData - so for passesValidator to return true -
	 * userData must be LESS_THAN validatorDefinitionData, for example.
	 * 
	 * @param userData
	 * @param validatorDefinitionData
	 * @param viewCoordinate - The View Coordinate - not needed for some types of validations. Null allowed when unneeded (for math based tests, for example)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean passesValidator(RefexDynamicDataBI userData, RefexDynamicDataBI validatorDefinitionData, ViewCoordinate viewCoordinate)
	{
		if (validatorDefinitionData == null)
		{
			throw new RuntimeException("The validator definition data is required");
		}
		if (this == RefexDynamicValidatorType.EXTERNAL)
		{
			ExternalValidatorBI validator = null;
			RefexDynamicStringBI[] valNameInfo = null;
			RefexDynamicArrayBI<RefexDynamicStringBI> stringValidatorDefData = null;
			String valName = null;
			if (validatorDefinitionData != null)
			{
				stringValidatorDefData = (RefexDynamicArrayBI<RefexDynamicStringBI>)validatorDefinitionData;
				valNameInfo = stringValidatorDefData.getDataArray();
			}
			if (valNameInfo != null && valNameInfo.length > 0)
			{
				valName = valNameInfo[0].getDataString();
				logger.fine("Looking for an ExternalValidatorBI with the name of '" + valName + "'");
				validator = Hk2Looker.get().getService(ExternalValidatorBI.class, valName);
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
		else if (this == RefexDynamicValidatorType.REGEXP)
		{
			try
			{
				if (userData == null)
				{
					return false;
				}
				return Pattern.matches(((RefexDynamicStringBI)validatorDefinitionData).getDataString(), userData.getDataObject().toString());
			}
			catch (Exception e)
			{
				throw new RuntimeException("The specified validator data object was not a valid regular expression: " + e.getMessage());
			}
		}
		else if (this == RefexDynamicValidatorType.IS_CHILD_OF || this == RefexDynamicValidatorType.IS_KIND_OF)
		{
			try
			{
				int childNid;
				int parentNid;

				if (userData instanceof RefexDynamicUUIDBI)
				{
					childNid = Ts.get().getNidForUuids(((RefexDynamicUUIDBI) userData).getDataUUID());
				}
				else if (userData instanceof RefexDynamicNidBI)
				{
					childNid = ((RefexDynamicNidBI) userData).getDataNid();
				}
				else
				{
					throw new RuntimeException("Userdata is invalid for a IS_CHILD_OF or IS_KIND_OF comparison");
				}

				if (validatorDefinitionData instanceof RefexDynamicUUIDBI)
				{
					parentNid = Ts.get().getNidForUuids(((RefexDynamicUUIDBI) validatorDefinitionData).getDataUUID());
				}
				else if (validatorDefinitionData instanceof RefexDynamicNidBI)
				{
					parentNid = ((RefexDynamicNidBI) validatorDefinitionData).getDataNid();
				}
				else
				{
					throw new RuntimeException("Validator DefinitionData is invalid for a IS_CHILD_OF or IS_KIND_OF comparison");
				}

				return (this == RefexDynamicValidatorType.IS_CHILD_OF ? Ts.get().isChildOf(childNid, parentNid, viewCoordinate) : Ts.get().isKindOf(childNid, parentNid, viewCoordinate));
			}
			catch (Exception e)
			{
				logger.log(Level.WARNING, "Failure executing validator", e);
				throw new RuntimeException("Failure executing validator", e);
			}
		}
		else if (this == RefexDynamicValidatorType.COMPONENT_TYPE)
		{
			try
			{
				int nid;

				if (userData instanceof RefexDynamicUUIDBI)
				{
					RefexDynamicUUIDBI uuid = (RefexDynamicUUIDBI) userData;
					if (!Ts.get().hasUuid(uuid.getDataUUID()))
					{
						throw new RuntimeException("The specified UUID can not be found in the database, so the validator cannot execute");
					}
					else
					{
						nid = Ts.get().getNidForUuids(uuid.getDataUUID());
					}
				}
				else if (userData instanceof RefexDynamicNidBI)
				{
					nid = ((RefexDynamicNidBI) userData).getDataNid();
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
						if (!(cc instanceof RefexDynamicChronicleBI<?>))
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
			if (this == RefexDynamicValidatorType.INTERVAL)
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

				switch (this)
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
	 * A convenience wrapper of {@link #passesValidator(RefexDynamicDataBI, RefexDynamicDataBI, ViewCoordinate)} that just returns a string - never
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
	public String passesValidatorStringReturn(RefexDynamicDataBI userData, RefexDynamicDataBI validatorDefinitionData, ViewCoordinate vc)
	{
		try
		{
			if (passesValidator(userData, validatorDefinitionData, vc))
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

	private static Number readNumber(RefexDynamicDataBI value)
	{
		if (value instanceof RefexDynamicDoubleBI)
		{
			return Double.valueOf(((RefexDynamicDoubleBI) value).getDataDouble());
		}
		else if (value instanceof RefexDynamicFloatBI)
		{
			return Float.valueOf(((RefexDynamicFloatBI) value).getDataFloat());
		}
		else if (value instanceof RefexDynamicIntegerBI)
		{
			return Integer.valueOf(((RefexDynamicIntegerBI) value).getDataInteger());
		}
		else if (value instanceof RefexDynamicLongBI)
		{
			return Long.valueOf(((RefexDynamicLongBI) value).getDataLong());
		}
		else
		{
			throw new RuntimeException("The value passed in to the validator is not a number");
		}
	}

	private static int compare(final Number x, final Number y)
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

	private static BigDecimal toBigDecimal(final Number number)
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
	
}
