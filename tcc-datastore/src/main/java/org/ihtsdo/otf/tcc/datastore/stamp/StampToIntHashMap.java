package org.ihtsdo.otf.tcc.datastore.stamp;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

public class StampToIntHashMap {
   private static final int defaultCapacity = 277;

   //~--- fields --------------------------------------------------------------

   private java.util.concurrent.ConcurrentHashMap<Stamp, Integer> map;

   //~--- constructors --------------------------------------------------------

   /**
    * Constructs an empty map with default capacity and default load factors.
    */
   public StampToIntHashMap() {
      this(defaultCapacity);
   }

   /**
    * Constructs an empty map with the specified initial capacity and default
    * load factors.
    *
    * @param initialCapacity
    *            the initial capacity of the map.
    * @throws IllegalArgumentException
    *             if the initial capacity is less than zero.
    */
   public StampToIntHashMap(int initialCapacity) {
      setup(initialCapacity);
   }

   /**
    * Constructs an empty map with the specified initial capacity and the
    * specified minimum and maximum load factor.
    *
    * @param initialCapacity
    *            the initial capacity.
    * @param minLoadFactor
    *            the minimum load factor.
    * @param maxLoadFactor
    *            the maximum load factor.
    * @throws IllegalArgumentException
    *             if
    *
    *             <tt>initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0)
    *                 || (maxLoadFactor <= 0.0 || maxLoadFactor >= 1.0)
    *                 || (minLoadFactor >= maxLoadFactor)</tt>
    *             .
    */
   public StampToIntHashMap(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
      setup(initialCapacity);
   }

   //~--- methods -------------------------------------------------------------

   public boolean containsKey(Status status, long time, int authorNid, int moduleNid, int pathNid) {
      return map.containsKey(new Stamp(status, time, authorNid, moduleNid, pathNid));
   }

   public boolean put(Stamp tsp, int statusAtPositionNid) {
      return map.put(tsp, statusAtPositionNid) == null;
   }

   public boolean put(Status status, long time, int authorNid, int moduleNid, int pathNid, int statusAtPositionNid) {
      return put(new Stamp(status, time, authorNid, moduleNid, pathNid), statusAtPositionNid);
   }

   private void setup(int initialCapacity) {
      map = new ConcurrentHashMap<>(initialCapacity);
   }

   public Collection<Integer> values() {
      return map.values();
   }

   //~--- get methods ---------------------------------------------------------

   public int get(Stamp tsp) {
      return map.get(tsp);
   }

   public int get(Status status, long time, int authorNid, int moduleNid, int pathNid) {
      return map.get(new Stamp(status, time, authorNid, moduleNid, pathNid));
   }
}
