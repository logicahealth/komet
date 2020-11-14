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

//~--- JDK imports ------------------------------------------------------------

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import java.nio.ByteBuffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.BiConsumer;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;

//~--- classes ----------------------------------------------------------------

/**
 * The Class UuidT5Generator.
 *
 * @author kec
 */
public class UuidT5Generator {
   /** The Constant ENCODING. */
   public final static String ENCODING = "UTF-8";

   /** The Constant PATH_ID_FROM_FS_DESC. */
   public static final UUID PATH_ID_FROM_FS_DESC = UUID.fromString("5a2e7786-3e41-11dc-8314-0800200c9a66");

   /** The Constant REL_GROUP_NAMESPACE. */
   public static final UUID REL_GROUP_NAMESPACE = UUID.fromString("8972fef0-ad53-11df-94e2-0800200c9a66");

   /** The Constant USER_FULLNAME_NAMESPACE. */
   public static final UUID USER_FULLNAME_NAMESPACE = UUID.fromString("cad85220-1ed4-11e1-8bc2-0800200c9a66");

   /** The Constant TAXONOMY_COORDINATE_NAMESPACE. */
   public static final UUID TAXONOMY_COORDINATE_NAMESPACE = UUID.fromString("c58dcdb6-185b-11e5-b60b-1697f925ec7b");

   /** The Constant REL_ADAPTOR_NAMESPACE. */
   public static final UUID REL_ADAPTOR_NAMESPACE = UUID.fromString("9cb2bf66-1863-11e5-b60b-1697f925ec7");

   /** The Constant AUTHOR_TIME_ID. */
   public static final UUID AUTHOR_TIME_ID = UUID.fromString("c6915290-30fc-11e1-b86c-0800200c9a66");

    public static final UUID SINGLE_SEMANTIC_FOR_RC_UUID = UUID.fromString("97c14234-205f-11eb-adc1-0242ac120002");


    //~--- get methods ---------------------------------------------------------

   /**
    * Gets the.
    *
    * @param name the name
    * @return the uuid
    */
   public static UUID get(String name) {
      return get(null, name);
   }

   public static UUID loincConceptUuid(String name) {
       return get(TermAux.LOINC_CONCEPT_ASSEMBLAGE.getPrimordialUuid(),
               name);
   }
   
   public static UUID rxNormConceptUuid(String name) {
       return get(TermAux.RXNORM_CUI.getPrimordialUuid(),
               name);
   }

    /**
     * For an assemblage that has only one semantic per referenced component, this routine should be
     * used to generate reproducible UUIDs for semantics.
     * @param assemblageNid
     * @param referencedComponentNid
     * @return
     */
   public static UUID singleSemanticUuid(int assemblageNid, int referencedComponentNid) {
       return singleSemanticUuid(Get.identifierService().getUuidArrayForNid(assemblageNid), Get.identifierService().getUuidArrayForNid(referencedComponentNid));
   }
    public static UUID singleSemanticUuid(UUID[] assemblageIds, UUID[] referencedComponentIds) {
        Arrays.sort(assemblageIds);
        Arrays.sort(referencedComponentIds);
        StringBuilder builder = new StringBuilder();
        builder.append(Arrays.toString(assemblageIds));
        builder.append(Arrays.toString(referencedComponentIds));
        return get(SINGLE_SEMANTIC_FOR_RC_UUID, builder.toString());
    }
   /**
    * Gets the.
    *
    * @param namespace the namespace
    * @param name the name
    * @return the uuid
    */
   public static UUID get(UUID namespace, String name) {
      try {
         final MessageDigest sha1Algorithm = MessageDigest.getInstance("SHA-1");

         // Generate the digest.
         sha1Algorithm.reset();

         if (namespace != null) {
            sha1Algorithm.update(getRawBytes(namespace));
         }

         sha1Algorithm.update(name.getBytes(ENCODING));

         final byte[] sha1digest = sha1Algorithm.digest();

         sha1digest[6] &= 0x0f;  /* clear version */
         sha1digest[6] |= 0x50;  /* set to version 5 */
         sha1digest[8] &= 0x3f;  /* clear variant */
         sha1digest[8] |= 0x80;  /* set to IETF variant */

         long msb = 0;
         long lsb = 0;

         for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (sha1digest[i] & 0xff);
         }

         for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (sha1digest[i] & 0xff);
         }

         return new UUID(msb, lsb);
      } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
         throw new RuntimeException(ex);
      }
   }
   
    
    /**
     * Same as {@link #get(UUID, String)} but with an optional consumer, which will get a call with the fed in name and resulting UUID.
     * 
     * @param namespace
     * @param name
     * @param consumer optional callback for debug / UUID generation tracking.
     * @return
     */
    public static UUID get(UUID namespace, String name, BiConsumer<String, UUID> consumer) {
        UUID temp = get(namespace, name);
        if (consumer != null) {
            consumer.accept(name,  temp);
        }
        return temp;
    }

   /**
    * This routine adapted from org.safehaus.uuid.UUID,
    * which is licensed under Apache 2.
    *
    * @param uid the uid
    * @return the raw bytes
    */
   public static byte[] getRawBytes(UUID uid) {
      final String id       = uid.toString();
      final byte[] rawBytes = new byte[16];

      for (int i = 0, j = 0; i < 36; ++j) {
         // Need to bypass hyphens:
         switch (i) {
         case 8:
         case 13:
         case 18:
         case 23:
            ++i;
         }

         char c = id.charAt(i);

         if ((c >= '0') && (c <= '9')) {
            rawBytes[j] = (byte) ((c - '0') << 4);
         } else if ((c >= 'a') && (c <= 'f')) {
            rawBytes[j] = (byte) ((c - 'a' + 10) << 4);
         }

         c = id.charAt(++i);

         if ((c >= '0') && (c <= '9')) {
            rawBytes[j] |= (byte) (c - '0');
         } else if ((c >= 'a') && (c <= 'f')) {
            rawBytes[j] |= (byte) (c - 'a' + 10);
         }

         ++i;
      }

      return rawBytes;
   }

   /**
    * Generates a uuid from the given {@code byteArray}.
    *
    * @param byteArray the bytes to use for generating the uuid
    * @return the generated uuid
    */
   public static UUID getUuidFromRawBytes(byte[] byteArray) {
      if (byteArray.length != 16) {
         throw new NumberFormatException("UUID must be 16 bytes");
      }

      final ByteBuffer raw = ByteBuffer.wrap(byteArray);

      return new UUID(raw.getLong(raw.position()), raw.getLong(raw.position() + 8));
   }
   
   public static String makeSolorIdFromRxNormId(String rxNormId)  {
       String compressedLoincCodeWithPartition = rxNormId +  "97";
       return compressedLoincCodeWithPartition + SctId.verhoeffComputeStr(compressedLoincCodeWithPartition);
   }
   
   
   public static String makeSolorIdFromLoincId(String loincId)  {
       if (loincId.charAt(loincId.length() - 2) != '-') {
           throw new IllegalStateException("Improperly formed loinc id: " + loincId);
       }
       String compressedLoincCode = loincId.substring(0, loincId.length() - 2);
       String compressedLoincCodeWithPartition = compressedLoincCode + loincId.charAt(loincId.length() - 1) + "98";
       return compressedLoincCodeWithPartition + SctId.verhoeffComputeStr(compressedLoincCodeWithPartition);
   }
   
   public static String makeSolorIdFromUuid(UUID uuid)  {
       // 18,446,744,073,709,551,615 max unsigned long
       // Remove the partition and check digits
       // ~18,000,000,000,000,000 = ~18 quadrillion possibilities
       try {
           final MessageDigest sha1Algorithm = MessageDigest.getInstance("SHA-1");
           
           // Generate the digest.
           sha1Algorithm.reset();
           sha1Algorithm.update(getRawBytes(uuid));
           
           sha1Algorithm.update("sh.isaac.longid".getBytes(ENCODING));
           
           final byte[] sha1digest = sha1Algorithm.digest();
           
           byte[] longBytes = new byte[8];
           
           for (int i = 0; i < longBytes.length; i++) {
               longBytes[i] = sha1digest[i];
           }

           //Zero out bytes for big endian 0,1 index
           //9223372036854775807 max long value
           //562,949,953,421,310 combinations - Mask 2 byte
           //144,115,188,075,855,870 combinations - Mask 1 bytes
           longBytes[0] = (byte)0;
           longBytes[1] = (byte)0;
           
           BigInteger bigResult = new BigInteger(1, longBytes);
           String resultAsString = bigResult.toString();
           String lastDigits = resultAsString.substring(resultAsString.length() - 3);
           String resultZeroForCheckDigit = bigResult.subtract(new BigInteger(lastDigits)).add(new BigInteger("990")).toString();
           String resultNoCheckDigit = resultZeroForCheckDigit.substring(0, resultZeroForCheckDigit.length()-1);
           return resultNoCheckDigit + SctId.verhoeffComputeStr(resultNoCheckDigit);
       } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
           throw new RuntimeException(ex.getLocalizedMessage(), ex);
       }
       
   }
   
   
   public static void main(String[] args) {
//        System.out.println(makeSolorIdFromRxNormId("1234"));
//        System.out.println(makeSolorIdFromRxNormId("12345"));
//        System.out.println(makeSolorIdFromLoincId("8867-4"));
//        System.out.println(makeSolorIdFromLoincId("67129-7"));
//        for (int i = 0; i < 10; i++) {
//           System.out.println(makeSolorIdFromUuid(UUID.randomUUID()));
//        }
       UUID uuid = UuidT5Generator.get(TermAux.KOMET_MODULE.getPrimordialUuid(), "Import specification panel");

       System.out.println(uuid.toString());
   }
   
}

