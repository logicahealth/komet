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

import java.util.Arrays;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import java.util.function.BiConsumer;

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.logic.LogicalExpression;

//~--- classes ----------------------------------------------------------------

/**
 * A factory for creating Uuid objects.
 *
 * @author kec
 */
public class UuidFactory {
   private static final String MEMBER_SEED_STRING = "MEMBER_SEED_STRING";

   /**
    * 
    * @param namespace
    * @param assemblage
    * @param refComp
    * @param le
    * @param consumer an optional parameter that will get a callback with the string used to calculate the UUID - no impact on generation
    * @return
    */
   public static UUID getUuidForLogicGraphSemantic(UUID namespace, UUID assemblage, UUID refComp, LogicalExpression le, BiConsumer<String, UUID> consumer) {
      byte[][] leBytes = le.getData(DataTarget.EXTERNAL);
      return UuidT5Generator.get(namespace, createUuidTextSeed(assemblage.toString(), refComp.toString(), toString(leBytes)), consumer);
   }
   
   private static String toString(byte[][] b)
   {
      StringBuilder temp = new StringBuilder();
      temp.append("[");
      for (byte[] bNested : b)
      {
         temp.append(Arrays.toString(bNested));
      }
      temp.append("]");
      return temp.toString();
   }

   /**
    * 
    * @param namespace
    * @param assemblage
    * @param refComp
    * @param consumer an optional parameter that will get a callback with the string used to calculate the UUID - no impact on generation
    * @return
    */
   public static UUID getUuidForMemberSemantic(UUID namespace, UUID assemblage, UUID refComp, BiConsumer<String, UUID> consumer) {

      return UuidT5Generator.get(namespace, createUuidTextSeed(assemblage.toString(), refComp.toString(), MEMBER_SEED_STRING), consumer);
   }

   /**
    * 
    * @param namespace
    * @param assemblage
    * @param refComp
    * @param data
    * @param consumer an optional parameter that will get a callback with the string used to calculate the UUID - no impact on generation
    * @return
    */
   public static UUID getUuidForDynamic(UUID namespace, UUID assemblage, UUID refComp, DynamicData[] data, BiConsumer<String, UUID> consumer) {
      StringBuilder temp = new StringBuilder();
      temp.append(assemblage.toString()); 
      temp.append(refComp.toString());
      temp.append(data == null ? "0" : data.length + "");
      if (data != null) {
         for (DynamicData d : data)
         {
            if (d == null)
            {
               temp.append("null");
            }
            else
            {
               temp.append(d.getDynamicDataType().getDisplayName());
               if (d.getDynamicDataType() == DynamicDataType.NID)
               {
                  temp.append(Get.identifierService().getUuidPrimordialForNid(((DynamicNid)d).getDataNid()));
               }
               else
               {
                  temp.append(new String(ChecksumGenerator.calculateChecksum("SHA1", d.getData())));
               }
            }
         }
      }
      
      return UuidT5Generator.get(namespace, temp.toString(), consumer);
   }

   /**
    * 
    * @param namespace
    * @param assemblage
    * @param refComp
    * @param component
    * @param consumer an optional parameter that will get a callback with the string used to calculate the UUID - no impact on generation
    * @return
    */
   public static UUID getUuidForComponentNidSemantic(UUID namespace, UUID assemblage, UUID refComp, UUID component, BiConsumer<String, UUID> consumer) {
      return UuidT5Generator.get(namespace, createUuidTextSeed(assemblage.toString(), refComp.toString(), component.toString()), consumer);
   }

   /**
    * 
    * @param namespace
    * @param assemblage
    * @param refComp
    * @param value
    * @param consumer an optional parameter that will get a callback with the string used to calculate the UUID - no impact on generation
    * @return
    */
   public static UUID getUuidForStringSemantic(UUID namespace, UUID assemblage, UUID refComp, String value, BiConsumer<String, UUID> consumer) {
      return UuidT5Generator.get(namespace, createUuidTextSeed(assemblage.toString(), refComp.toString(), value), consumer);
   }
   
   /**
    * 
    * @param namespace
    * @param assemblage
    * @param refComp
    * @param value
    * @param consumer an optional parameter that will get a callback with the string used to calculate the UUID - no impact on generation
    * @return
    */
   public static UUID getUuidForLongSemantic(UUID namespace, UUID assemblage, UUID refComp, long value, BiConsumer<String, UUID> consumer) {
      return UuidT5Generator.get(namespace, createUuidTextSeed(assemblage.toString(), refComp.toString(), Long.toString(value)), consumer);
   }
   
   /**
    * 
    * @param namespace
    * @param assemblage
    * @param refComp
    * @param uuidForNid 
    * @param value
    * @param consumer an optional parameter that will get a callback with the string used to calculate the UUID - no impact on generation
    * @return
    */
   public static UUID getUuidForNidIntSemantic(UUID namespace, UUID assemblage, UUID refComp, UUID uuidForNid, int value, BiConsumer<String, UUID> consumer) {
      return UuidT5Generator.get(namespace, createUuidTextSeed(assemblage.toString(), refComp.toString(), uuidForNid.toString(), Integer.toString(value)), consumer);
   }
   public static UUID getUuidForNidLongSemantic(UUID namespace, UUID assemblage, UUID refComp, UUID uuidForNid, Long value, BiConsumer<String, UUID> consumer) {
      return UuidT5Generator.get(namespace, createUuidTextSeed(assemblage.toString(), refComp.toString(), uuidForNid.toString(), Long.toString(value)), consumer);
   }

   /**
    *
    * @param namespace
    * @param concept
    * @param caseSignificance
    * @param descriptionType
    * @param language
    * @param descriptionText
    * @param consumer an optional parameter that will get a callback with the string used to calculate the UUID - no impact on generation
    * @return
    */
   public static UUID getUuidForDescriptionSemantic(UUID namespace, UUID concept, UUID caseSignificance,
         UUID descriptionType, UUID language, String descriptionText, BiConsumer<String, UUID> consumer) {
      return UuidT5Generator.get(namespace, createUuidTextSeed(concept.toString(), caseSignificance.toString(),
            descriptionType.toString(), language.toString(), descriptionText), consumer);
   }

   /**
    * Create a new Type5 UUID using the provided name as the seed in the
    * configured namespace.
    * 
    * Throws a runtime exception if the namespace has not been configured.
    */
   private static String createUuidTextSeed(String... values) {
      StringBuilder uuidKey = new StringBuilder();
      for (String s : values) {
         if (s != null) {
            uuidKey.append(s);
            uuidKey.append("|");
         }
      }
      if (uuidKey.length() > 1) {
         uuidKey.setLength(uuidKey.length() - 1);
      } else {
         throw new RuntimeException("No string provided!");
      }
      return uuidKey.toString();
   }
}
