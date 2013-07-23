package org.ihtsdo.otf.tcc.chronicle.cc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

public abstract class NidPair implements Comparable<NidPair>, Serializable {
   private int   hash;
   protected int nid1;
   protected int nid2;

   //~--- constructors --------------------------------------------------------

   protected NidPair(long nids) {
      this((int) nids, (int) (nids >>> 32));
   }

   protected NidPair(int nid1, int nid2) {
      super();
      assert nid1 != 0;
      assert nid2 != 0;
      this.nid1 = nid1;
      this.nid2 = nid2;
      this.hash = Hashcode.compute(new int[] { nid1, nid2 });
   }

   //~--- methods -------------------------------------------------------------

   public void addToList(List<Integer> list) {
      list.add(nid1);
      list.add(nid2);
   }

   public long asLong() {
      long returnValue = nid2;

      // clear any sign bits
      returnValue = returnValue & 0x00000000FFFFFFFFL;

      // shift to the top 32 bits
      returnValue = returnValue << 32;

      long nid1Long = nid1;

      // clear any sign bits
      nid1Long    = nid1Long & 0x00000000FFFFFFFFL;
      returnValue = returnValue | nid1Long;

      return returnValue;
   }

   @Override
   public int compareTo(NidPair o) {
      long diff = asLong() - o.asLong();

      if (diff > 0) {
         return 1;
      }

      if (diff < 0) {
         return -1;
      }

      return 0;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final NidPair other = (NidPair) obj;

      if (this.nid1 != other.nid1) {
         return false;
      }

      if (this.nid2 != other.nid2) {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode() {
      return hash;
   }

   @Override
   public String toString() {
      return "nid1: " + nid1 + " nid2:" + nid2;
   }

   //~--- get methods ---------------------------------------------------------

   public static NidPair getNidPair(long nids) {
      int nid1 = (int) nids;
      int nid2 = (int) (nids >>> 32);

      return getRefexNidMemberNidPair(nid1, nid2);
   }

   public static List<NidPairForRefex> getNidPairsForRefset(long[] nidPairArray) {
      List<NidPairForRefex> returnValues = new ArrayList<>(nidPairArray.length);

      for (long nids : nidPairArray) {
         int nid1 = (int) nids;
         int nid2 = (int) (nids >>> 32);

         if (P.s.getConceptNidForNid(nid2) != nid2) {
            returnValues.add(new NidPairForRefex(nid1, nid2));
         }
      }

      return returnValues;
   }

   public static int[] getOriginsForRels(long[] nidPairArray, NidSetBI relTypes) {
      IntArrayList returnValues = new IntArrayList(nidPairArray.length);

      for (long nids : nidPairArray) {
         int nid1 = (int) nids;
         int nid2 = (int) (nids >>> 32);

         if (relTypes.contains(nid2)) {
            returnValues.add(P.s.getConceptNidForNid(nid1));
         }
      }

      returnValues.trimToSize();

      return returnValues.elements();
   }

   public static NidPairForRefex getRefexNidMemberNidPair(int refexNid, int memberNid) {

      // the refset (nid1) is a concept, the memberNid is not.
      return new NidPairForRefex(refexNid, memberNid);
   }

   public boolean isRefexPair() {
      return !isRelPair();
   }

   public abstract boolean isRelPair();
}
