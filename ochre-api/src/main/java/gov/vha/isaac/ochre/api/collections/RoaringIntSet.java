/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.collections;

import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

/**
 *
 * @author kec
 */
public class RoaringIntSet implements IntSet {

	RoaringBitmap rbmp;


	protected RoaringIntSet() {
		rbmp = new RoaringBitmap();
	}

	protected RoaringIntSet(int... members) {
		rbmp = RoaringBitmap.bitmapOf(members);
	}

	protected RoaringIntSet(IntStream memberStream) {
		rbmp = new RoaringBitmap();
		memberStream.forEach((member) -> rbmp.add(member));
	}
	
	private RoaringBitmap getRoaringSet(IntSet set) {
		if (set instanceof RoaringIntSet) {
			return ((RoaringIntSet) set).rbmp;
		}
		RoaringBitmap roaringSet = new RoaringBitmap();
		set.stream().forEach((member) -> roaringSet.add(member));
		return roaringSet;
	}

	@Override
	public void clear() {
		rbmp.clear();
	}

	@Override
	public IntSet or(IntSet otherSet) {
		
		rbmp.or(getRoaringSet(otherSet));
		return this;
	}

	@Override
	public IntSet and(IntSet otherSet) {
		rbmp.and(getRoaringSet(otherSet));
		return this;
	}

	@Override
	public IntSet andNot(IntSet otherSet) {
		rbmp.andNot(getRoaringSet(otherSet));
		return this;
	}

	@Override
	public IntSet xor(IntSet otherSet) {
		rbmp.xor(getRoaringSet(otherSet));
		return this;
	}

	/**
	 *
	 * @return the number of elements in this set.
	 */
	@Override
	public int size() {
		return rbmp.getCardinality();
	}

	/**
	 *
	 * @return true if the set is empty.
	 */
	@Override
	public boolean isEmpty() {
		return rbmp.isEmpty();
	}

	/**
	 *
	 * @param item to add to set.
	 */
	@Override
	public void add(int item) {
		rbmp.add(item);
	}

	@Override
	public void addAll(IntStream intStream) {
		intStream.forEach((anInt) -> rbmp.add(anInt));
	}

	/**
	 *
	 * @param item to remove from set.
	 */
	@Override
	public void remove(int item) {
		rbmp.remove(item);
	}

	/**
	 *
	 * @param item to test for containment in set.
	 * @return true if item is contained in set.
	 */
	@Override
	public boolean contains(int item) {
		return rbmp.contains(item);
	}

	/**
	 *
	 * @return the set members as an {@code IntStream}
	 */
	@Override
	public IntStream stream() {
		if (rbmp.isEmpty()) {
			return IntStream.empty();
		}
		Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();
		return StreamSupport.intStream(streamSupplier,
				  streamSupplier.get().characteristics(),
				  false);
	}

	@Override
	public OptionalInt findFirst() {
		return stream().findFirst();
	}

	/**
	 *
	 * @return the set members as an {@code IntStream}
	 */
	@Override
	public IntStream parallelStream() {
		if (rbmp.isEmpty()) {
			return IntStream.empty();
		}
		Supplier<? extends Spliterator.OfInt> streamSupplier = this.get();
		return StreamSupport.intStream(streamSupplier,
				  streamSupplier.get().characteristics(),
				  true);
	}

	@Override
	public int[] asArray() {
		return stream().toArray();
	}

	protected Supplier<? extends Spliterator.OfInt> get() {
		return new SpliteratorSupplier();
	}

	private class SpliteratorSupplier implements Supplier<Spliterator.OfInt> {

		@Override
		public Spliterator.OfInt get() {
			return new BitSetSpliterator();
		}

	}

	private class BitSetSpliterator implements Spliterator.OfInt {

		IntIterator intIterator = rbmp.getIntIterator();

		@Override
		public Spliterator.OfInt trySplit() {
			return null;
		}

		@Override
		public boolean tryAdvance(IntConsumer action) {
			action.accept(intIterator.next());
			return intIterator.hasNext();
		}

		@Override
		public long estimateSize() {
			return RoaringIntSet.this.size();
		}

		@Override
		public int characteristics() {
			return Spliterator.DISTINCT
					  + Spliterator.IMMUTABLE
					  + Spliterator.NONNULL
					  + Spliterator.ORDERED
					  + Spliterator.SIZED
					  + Spliterator.SORTED;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				  + " size: " + size() + " elements: " + rbmp;
	}

	private static class OfIntWrapper implements PrimitiveIterator.OfInt {

		IntIterator intIterator;

		public OfIntWrapper(IntIterator intIterator) {
			this.intIterator = intIterator;
		}
		
		@Override
		public int nextInt() {
			return intIterator.next();
		}

		@Override
		public boolean hasNext() {
			return intIterator.hasNext();
		}
		
	}
	
	
	@Override
	public PrimitiveIterator.OfInt getIntIterator() {
		return new OfIntWrapper(rbmp.getIntIterator());
	}

	@Override
	public PrimitiveIterator.OfInt getReverseIntIterator() {
		return new OfIntWrapper(rbmp.getReverseIntIterator());
	}

}
