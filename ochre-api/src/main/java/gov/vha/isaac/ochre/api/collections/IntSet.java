/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.collections;

import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.roaringbitmap.IntIterator;

/**
 *
 * @author kec
 */
public interface IntSet  {

	/**
	 *
	 * @param item to add to set.
	 */
	void add(int item);

	void addAll(IntStream intStream);

	IntSet and(IntSet otherSet);

	IntSet andNot(IntSet otherSet);

	int[] asArray();

	void clear();

	/**
	 *
	 * @param item to test for containment in set.
	 * @return true if item is contained in set.
	 */
	boolean contains(int item);

	OptionalInt findFirst();

	PrimitiveIterator.OfInt getIntIterator();

	PrimitiveIterator.OfInt getReverseIntIterator();

	/**
	 *
	 * @return true if the set is empty.
	 */
	boolean isEmpty();

	IntSet or(IntSet otherSet);

	/**
	 *
	 * @return the set members as an {@code IntStream}
	 */
	IntStream parallelStream();

	/**
	 *
	 * @param item to remove from set.
	 */
	void remove(int item);

	/**
	 *
	 * @return the number of elements in this set.
	 */
	int size();

	/**
	 *
	 * @return the set members as an {@code IntStream}
	 */
	IntStream stream();

	IntSet xor(IntSet otherSet);
	
}
