/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.ttk.lookup;

//~--- non-JDK imports --------------------------------------------------------

import java.util.ArrayList;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.UUID;
import org.openide.util.Lookup;

/**
 *
 * @author kec
 */
public class Looker {

   /** Field description */
   private final static InstanceContent content = new InstanceContent();

   /** Field description */
   private final static AbstractLookup lookup = new AbstractLookup(content);
   
   static {
       Looker.add(new TermstoreLatch(), UUID.randomUUID(), "Termstore Latch");
       Looker.add(new TtkEnvironment(), UUID.randomUUID(), "Environment settings");
   }

   /**
    * Method description
    *
    *
    * @param inst
    */
   public static void add(Object inst) {
      content.add(inst);
   }

   /**
    * Method description
    *
    *
    * @param inst
    * @param conv
    * @param <T>
    * @param <R>
    */
   public static <T, R> void add(T inst, InstanceContent.Convertor<T, R> conv) {
      content.add(inst, conv);
   }

   /**
    * Method description
    *
    *
    * @param pair
    */
   public static void addPair(AbstractLookup.Pair<?> pair) {
      content.addPair(pair);
   }
   public static <T> InstanceWrapper<T> add(T instance, UUID uuid, String displayName, Collection<? extends InstancePropertyBI> instanceProperties) {
      InstanceWrapper<T> wrapper = new InstanceWrapper(instance, uuid.toString(), displayName, instanceProperties);
      content.addPair(wrapper);
      return wrapper;
   }
   public static <T> InstanceWrapper<T> add(T instance, UUID uuid, String displayName) {
      InstanceWrapper<T> wrapper = new InstanceWrapper(instance, uuid.toString(), displayName, new ArrayList());
      content.addPair(wrapper);
      return wrapper;
   }

   /**
    * Method description
    *
    *
    * @param clazz
    * @param <T>
    *
    * @return
    */
   public static <T> T lookup(Class<T> clazz) {
      return lookup.lookup(clazz);
   }

   /**
    * Method description
    *
    *
    * @param template
    * @param <T>
    *
    * @return
    */
   public static <T> org.openide.util.Lookup.Result<T> lookup(org.openide.util.Lookup.Template<T> template) {
      return lookup.lookup(template);
   }

   /**
    * Method description
    *
    *
    * @param clazz
    * @param <T>
    *
    * @return
    */
   public static <T> Collection<? extends T> lookupAll(Class<T> clazz) {
      return lookup.lookupAll(clazz);
   }

   /**
    * Method description
    *
    *
    * @param template
    * @param <T>
    *
    * @return
    */
   public static <T> org.openide.util.Lookup.Item<T> lookupItem(org.openide.util.Lookup.Template<T> template) {
      return lookup.lookupItem(template);
   }

   /**
    * Method description
    *
    *
    * @param clazz
    * @param <T>
    *
    * @return
    */
   public static <T> org.openide.util.Lookup.Result<T> lookupResult(Class<T> clazz) {
      return lookup.lookupResult(clazz);
   }

   /**
    * Method description
    *
    *
    * @param inst
    */
   public static void remove(Object inst) {
      content.remove(inst);
   }

   /**
    * Method description
    *
    *
    * @param inst
    * @param conv
    * @param <T>
    * @param <R>
    */
   public static <T, R> void remove(T inst, InstanceContent.Convertor<T, R> conv) {
      content.remove(inst, conv);
   }

   /**
    * Method description
    *
    *
    * @param pair
    */
   public static void removePair(AbstractLookup.Pair<?> pair) {
      content.removePair(pair);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public static synchronized Lookup getDefault() {
      return lookup;
   }

   /**
    * Method description
    *
    *
    * @param col
    * @param conv
    * @param <T>
    * @param <R>
    */
   public static <T, R> void set(Collection<T> col, InstanceContent.Convertor<T, R> conv) {
      content.set(col, conv);
   }

   /**
    * Method description
    *
    *
    * @param c
    */
   public static void setPairs(Collection<? extends AbstractLookup.Pair> c) {
      content.setPairs(c);
   }
}
