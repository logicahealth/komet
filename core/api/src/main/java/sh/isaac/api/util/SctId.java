/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */


package sh.isaac.api.util;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link SctId} contains validation utilities for SNOMED ID (SCT ID). A unique long
 * Identifier applied to each SNOMED CT component ( Concept, Description, Relationship, Subset, etc.).
 * The SCTID data type is a 64-bit integer, which is subject to the following constraints:
 * - Only positive integer values are permitted.
 * - The minimum permitted value is 100,000 (6 digits)
 * - The maximum permitted value is 999,999,999,999,999,999 (18-digits).
 *
 * As a result of rules for the partition-identifier and check-digit, many integers within this range are not valid SCTIDs.
 *
 * Extension SCTID (MSD)Extension Item ID (18-11)Namespace ID(10-4)Partition ID(3-2)Check-digit(1)(LSD)
 *
 * In java, the SCTID is handled as a long.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @see <a href="http://www.snomed.org/tig?t=trg2main_sctid">IHTSDO Technical Implementation Guide - SCT ID</a>
 */
public class SctId {

	// parts of the SCTID algorithm
	private static final int[][] FnF = {
		{
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9
		}, {
			1, 5, 7, 6, 2, 8, 3, 0, 9, 4
		}, {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		}, {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		}, {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		}, {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		}, {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		}, {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		}, {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		}
	};

	private static final int[][] Dihedral = {
		{
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9
		}, {
			1, 2, 3, 4, 0, 6, 7, 8, 9, 5
		}, {
			2, 3, 4, 0, 1, 7, 8, 9, 5, 6
		}, {
			3, 4, 0, 1, 2, 8, 9, 5, 6, 7
		}, {
			4, 0, 1, 2, 3, 9, 5, 6, 7, 8
		}, {
			5, 9, 8, 7, 6, 0, 4, 3, 2, 1
		}, {
			6, 5, 9, 8, 7, 1, 0, 4, 3, 2
		}, {
			7, 6, 5, 9, 8, 2, 1, 0, 4, 3
		}, {
			8, 7, 6, 5, 9, 3, 2, 1, 0, 4
		}, {
			9, 8, 7, 6, 5, 4, 3, 2, 1, 0
		}
	};
	private static final int[]	InverseD5 = {
		0, 4, 3, 2, 1, 5, 6, 7, 8, 9
	};

	private static final String[]	InverseD5Char = {
		"0", "4", "3", "2", "1", "5", "6", "7", "8", "9"
	};

	static {
		for (int i = 2; i < 8; i++) {
			for (int j = 0; j < 10; j++) {
				FnF[i][j] = FnF[i - 1][FnF[1][j]];
			}
		}
	}

	/**
	 * The Enum TYPE listing the possible types of SCT IDs. The second and third
	 * digits from the right of the string rendering of the SCTID. The value of
	 * the partition-identifier indicates the type of component that the SCTID
	 * identifies (e.g. Concept, Description, Relationship, etc) and also
	 * indicates whether the SCTID contains a namespace identifier.
	 *
	 */
	public static enum TYPE
	{
		CONCEPT("00"), DESCRIPTION("01"), RELATIONSHIP("02"), 
		CONCEPT_LF("10"), DESCRIPTION_LF("11"), RELATIONSHIP_LF("12"),
		
		//Old RF1 stuff, not likely to be used...
		SUBSET("03"), CROSS_MAP_SET("04"), CROSS_MAP_TARGET("05"),
		SUBSET_LF("13"), CROSS_MAP_SET_LF("14"), CROSS_MAP_TARGET_LF("15");

		private final String digits;

		/**
		 * Instantiates a new SCT ID type based on the <code>digits</code>.
		 *
		 * @param digits the digits specifying the SCT ID type
		 */
		private TYPE(String digits)
		{
			this.digits = digits;
		}

		/**
		 * Gets the digits specifying the SCT ID type.
		 *
		 * @return the digits specifying the SCT ID type
		 */
		public String getDigits()
		{
			return this.digits;
		}
		
		public boolean isLongForm()
		{
			return Integer.parseInt(digits) >= 10;
		}
		
		public static TYPE parse(String input) throws IllegalArgumentException
		{
			for (TYPE t : TYPE.values())
			{
				if (t.digits.equals(input))
				{
					return t;
				}
			}
			throw new IllegalArgumentException("Invalid Partition ID");
		}
	}
	
	private long itemId;
	private Optional<String> namespace;
	private TYPE type;
	
	public SctId(String sctid) throws IllegalArgumentException
	{
		if (!isValidSctId(sctid))
		{
			throw new IllegalArgumentException("Invalid SCTID string");
		}
		type = TYPE.parse(sctid.substring(sctid.length() - 3, sctid.length() - 1));
		
		if (type.isLongForm())
		{
			namespace = Optional.of(sctid.substring(sctid.length() - 10, sctid.length() - 3));
		}
		else
		{
			namespace = Optional.empty();
		}
		
		itemId = Long.valueOf(sctid.substring(0, (sctid.length() - (namespace.isEmpty() ? 3 : 10))));
	}
	

	public long getItemId()
	{
		return itemId;
	}

	public Optional<String> getNamespace()
	{
		return namespace;
	}

	public TYPE getType()
	{
		return type;
	}



	/**
	 * see {@link #isValidSctId(String)}.
	 *
	 * @param sctid the sctid
	 * @return true, if valid SCTID
	 */
	public static boolean isValidSCTID(int sctid)
	{
		return isValidSctId(Integer.toString(sctid));
	}

	/**
	 * see {@link #isValidSctId(String)}.
	 *
	 * @param sctid the sctid
	 * @return true, if valid sct id
	 */
	public static boolean isValidSctId(long sctid)
	{
		return isValidSctId(Long.toString(sctid));
	}


	/**
	 * Generates an SCT ID based on the given {@code itemID}, {@code namespace}, and {@code type}.
	 *
	 * @param itemID the sequence to use for the item identifier
	 * @param namespace the namespace to use
	 * @param type the SCT ID type - must be a LONGFORM type - one that ends with _LF.
	 * @return a string representation of the generated SCT ID
	 */
	public static String generate(long itemID, String namespace, TYPE type)
	{
		if (!type.isLongForm())
		{
			throw new IllegalArgumentException("Not legal to generate new shortform SCTIDs.  SCTIDs with a namespace must use longform.");
		}
		if (itemID <= 0)
		{
			throw new IllegalArgumentException("itemID must be > 0");
		}
		String mergedid = Long.toString(itemID) + namespace + type.digits;
		final String generatedId =  mergedid + verhoeffCompute(mergedid);
		if (!isValidSctId(generatedId))
		{
			throw new IllegalArgumentException("Final generated SCTID was invalid - perhaps itemID was out of valid range?");
		}
		return generatedId;
	}


	
	/**
	 * Computes the check digit. The SCTID (See Component features - Identifiers) includes a check-digit, which is generated using Verhoeff's
	 * dihedral check.
	 *
	 * @param idAsString a String representation of the SCT ID
	 * @return the generated SCT ID
	 * @see <a href="http://www.snomed.org/tig?t=trg_app_check_digit">IHTSDO Technical Implementation Guide - Verhoeff</a>
	 */

	public static long verhoeffCompute(String idAsString)
	{
		int check = 0;
		for (int i = idAsString.length() - 1; i >= 0; i--)
		{
			check = Dihedral[check][FnF[((idAsString.length() - i) % 8)][Integer.valueOf(new String(new char[] { idAsString.charAt(i) }))]];

		}
		return InverseD5[check];
	}

	public static String verhoeffComputeStr(String idAsString)
	{
		int check = 0;
		for (int i = idAsString.length() - 1; i >= 0; i--)
		{
			check = Dihedral[check][FnF[((idAsString.length() - i) % 8)][Integer.valueOf(new String(new char[] { idAsString.charAt(i) }))]];

		}
		return InverseD5Char[check];
	}
 
	/**
	 * Verifies the check digit of an SCT identifier.
	 *
	 * @param idAsString a String representation of the SCT ID
	 * @return <code>true</code>, if the checksum in the string is correct for an SCTID.
	 * @see <a href="http://www.snomed.org/tig?t=trg_app_check_digit">IHTSDO Technical Implementation Guide - Verhoeff</a>
	 */
	public static boolean isValidSctId(String idAsString)
	{
		if (StringUtils.isBlank(idAsString))
		{
			return false;
		}

		try
		{
			final long l = Long.parseLong(idAsString);

			if ((l < 100000) || (l > 999999999999999999l))
			{
				return false;
			}
		}
		catch (final NumberFormatException e)
		{
			return false;
		}
		
		//validate it has a valid partition
		try
		{
			TYPE t = TYPE.parse(idAsString.substring(idAsString.length() - 4, idAsString.length() - 2));
			//Anything that is a long format must have a namespace, which brings our min length up to 11.
			if (t.isLongForm() && idAsString.length() < 11)
			{
				return false;
			}
		}
		catch (Exception e)
		{
			return false;
		}

		int check = 0;

		for (int i = idAsString.length() - 1; i >= 0; i--)
		{
			check = Dihedral[check][FnF[(idAsString.length() - i - 1) % 8][Integer.valueOf(new String(new char[] { idAsString.charAt(i) }))]];
		}

		if (check != 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
